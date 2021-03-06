package com.masonsoft.imsdk.uikit.common.mediapicker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.util.ObjectsCompat;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.CursorUtil;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.idonans.core.WeakAbortSignal;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.AbortUtil;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.HumanUtil;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;

public class MediaData {

    @NonNull
    public final MediaBucket allMediaInfoListBucket;

    @NonNull
    public final List<MediaBucket> allSubBuckets;

    @NonNull
    public final Map<Uri, MediaInfo> allMediaInfoListMap;

    public MediaBucket bucketSelected;

    @NonNull
    public final List<MediaInfo> mediaInfoListSelected = new ArrayList<>();

    @NonNull
    public final MediaSelector mediaSelector;

    public MediaData(@NonNull MediaBucket allMediaInfoListBucket, @NonNull List<MediaBucket> allSubBuckets, @NonNull Map<Uri, MediaInfo> allMediaInfoListMap, @NonNull MediaSelector mediaSelector) {
        this.allMediaInfoListBucket = allMediaInfoListBucket;
        this.allSubBuckets = allSubBuckets;
        this.allMediaInfoListMap = allMediaInfoListMap;
        this.mediaSelector = mediaSelector;
    }

    /**
     * 获取选择 media 的选中顺序，如果没有选中返回 -1.
     *
     * @param mediaInfo media info
     * @return 获取选择 media 的选中顺序，如果没有选中返回 -1.
     */
    public int indexOfSelected(MediaInfo mediaInfo) {
        return mediaInfoListSelected.indexOf(mediaInfo);
    }

    public static class MediaInfo {
        public Uri uri;
        public long size;
        public long durationMs;
        public int width;
        public int height;
        public String mimeType;
        public String title;
        public long addTime;
        public long lastModify;
        public int id;
        public MediaBucket mMediaBucket;

        public String bucketId;
        public String bucketDisplayName;

        private MediaInfo copyForInternalCache() {
            final MediaInfo cache = new MediaInfo();
            cache.uri = this.uri;
            cache.size = this.size;
            cache.durationMs = this.durationMs;
            cache.width = this.width;
            cache.height = this.height;
            cache.mimeType = this.mimeType;
            cache.title = this.title;
            cache.addTime = this.addTime;
            cache.lastModify = this.lastModify;
            cache.id = this.id;
            cache.bucketId = this.bucketId;
            cache.bucketDisplayName = this.bucketDisplayName;
            return cache;
        }

        public boolean isImageMimeType() {
            return this.mimeType != null && this.mimeType.startsWith("image/");
        }

        public boolean isVideoMimeType() {
            return this.mimeType != null && this.mimeType.startsWith("video/");
        }

        /**
         * 估算图片解码到内存之后的 byte 大小
         */
        public long getImageMemorySize() {
            return this.width * this.height * 4L;
        }

        public boolean isImageMemorySizeTooLarge() {
            return getImageMemorySize() > MSIMUikitConstants.SELECTOR_MAX_IMAGE_SIZE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MediaInfo mediaInfo = (MediaInfo) o;
            return Objects.equals(uri, mediaInfo.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.uri);
        }

        @NonNull
        public String toShortString() {
            //noinspection StringBufferReplaceableByString
            final StringBuilder builder = new StringBuilder();
            builder.append(com.masonsoft.imsdk.util.Objects.defaultObjectTag(this));
            builder.append(" uri:").append(this.uri);
            builder.append(" size:").append(this.size).append(" ").append(HumanUtil.getHumanSizeFromByte(this.size));
            builder.append(" width:").append(this.width);
            builder.append(" height:").append(this.height);
            builder.append(" mimeType:").append(this.mimeType);
            builder.append(" title:").append(this.title);
            builder.append(" addTime:").append(this.addTime);
            builder.append(" lastModify:").append(this.lastModify);
            builder.append(" id:").append(this.id);
            builder.append(" bucketId:").append(this.bucketId);
            builder.append(" bucketDisplayName:").append(this.bucketDisplayName);
            return builder.toString();
        }

        @Override
        @NonNull
        public String toString() {
            return this.toShortString();
        }
    }

    public static class MediaBucket {
        /**
         * 是否是总的那个 bucket, 包含了所有的 media info
         */
        public boolean allMediaInfo;
        public String bucketDisplayName;
        public String bucketId;
        @Nullable
        public MediaInfo cover;

        @NonNull
        public final List<MediaInfo> mediaInfoList = new ArrayList<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MediaBucket that = (MediaBucket) o;
            return allMediaInfo == that.allMediaInfo &&
                    ObjectsCompat.equals(bucketId, that.bucketId);
        }

        @Override
        public int hashCode() {
            return ObjectsCompat.hash(allMediaInfo, bucketId);
        }
    }

    public interface MediaLoaderCallback {
        void onLoadFinish(@NonNull MediaData mediaData);
    }

    public static class MediaLoader extends WeakAbortSignal implements Runnable, Closeable {

        private static class MediaInfoInternalCache {
            private static final LruCache<String, MediaInfo> CACHE = new LruCache<>(1000);

            static void putWithCopy(MediaInfo mediaInfo) {
                if (mediaInfo == null) {
                    return;
                }

                final String key = buildKey(mediaInfo.id, mediaInfo.lastModify);
                CACHE.put(key, mediaInfo.copyForInternalCache());
            }

            static MediaInfo getWithCopy(int id, long lastModify) {
                final String key = buildKey(id, lastModify);
                final MediaInfo cache = CACHE.get(key);
                if (cache != null) {
                    return cache.copyForInternalCache();
                }
                return null;
            }

            private static String buildKey(int id, long lastModify) {
                return id + "_" + lastModify;
            }
        }

        private interface ColumnsMap {
            String[] allColumns();

            MediaInfo mapToMediaInfo(Cursor cursor);
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        private static class ContentUriColumnsMap implements ColumnsMap {

            @Override
            public String[] allColumns() {
                return new String[]{
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns._ID,            // id
                        MediaStore.MediaColumns.DATE_MODIFIED,  // 文件的最后修改时间
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.SIZE,           // 媒体的大小，long型  132492
                        MediaStore.MediaColumns.WIDTH,          // 媒体的宽度，int型  1920, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.HEIGHT,         // 媒体的高度，int型  1080, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.MIME_TYPE,      // 媒体的类型     image/jpeg
                        MediaStore.MediaColumns.TITLE,
                        MediaStore.MediaColumns.DATE_ADDED,     // 添加时间

                        MediaStore.Video.VideoColumns.DURATION, // video duration
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.BUCKET_ID,
                        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                };
            }

            @Override
            public MediaInfo mapToMediaInfo(Cursor cursor) {
                int index = -1;
                final int id = CursorUtil.getInt(cursor, ++index);
                final long lastModify = CursorUtil.getLong(cursor, ++index);
                final MediaInfo cache = MediaInfoInternalCache.getWithCopy(id, lastModify);
                if (cache != null) {
                    MSIMUikitLog.v("%s mapToMediaInfo[hit cache] %s", com.masonsoft.imsdk.util.Objects.defaultObjectTag(this), cache);
                    return cache;
                }

                MediaInfo target = new MediaInfo();
                target.id = id;
                target.lastModify = lastModify;

                target.size = CursorUtil.getLong(cursor, ++index);
                target.width = CursorUtil.getInt(cursor, ++index);
                target.height = CursorUtil.getInt(cursor, ++index);
                target.mimeType = CursorUtil.getString(cursor, ++index);
                if (target.mimeType != null) {
                    target.mimeType = target.mimeType.trim().toLowerCase();
                }
                target.title = CursorUtil.getString(cursor, ++index);
                target.addTime = CursorUtil.getLong(cursor, ++index);
                target.durationMs = CursorUtil.getLong(cursor, ++index);

                target.bucketId = CursorUtil.getString(cursor, ++index);
                target.bucketDisplayName = CursorUtil.getString(cursor, ++index);

                target.uri = MediaStore.Files.getContentUri("external", target.id);

                MSIMUikitLog.v("%s mapToMediaInfo %s", com.masonsoft.imsdk.util.Objects.defaultObjectTag(this), target);

                MediaInfoInternalCache.putWithCopy(target);

                return target;
            }
        }

        private static class DefaultColumnsMap implements ColumnsMap {
            @Override
            public String[] allColumns() {
                return new String[]{
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns._ID,            // id
                        MediaStore.MediaColumns.DATE_MODIFIED,  // 文件的最后修改时间
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.SIZE,           // 媒体的大小，long型  132492
                        MediaStore.MediaColumns.WIDTH,          // 媒体的宽度，int型  1920, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.HEIGHT,         // 媒体的高度，int型  1080, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.MIME_TYPE,      // 媒体的类型     image/jpeg
                        MediaStore.MediaColumns.TITLE,
                        MediaStore.MediaColumns.DATE_ADDED,     // 添加时间
                        MediaStore.Video.VideoColumns.DURATION, // video duration
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.DATA,           // 媒体的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                };
            }

            @Override
            public MediaInfo mapToMediaInfo(Cursor cursor) {
                int index = -1;
                final int id = CursorUtil.getInt(cursor, ++index);
                final long lastModify = CursorUtil.getLong(cursor, ++index);
                final MediaInfo cache = MediaInfoInternalCache.getWithCopy(id, lastModify);
                if (cache != null) {
                    MSIMUikitLog.v("%s mapToMediaInfo[hit cache] %s", com.masonsoft.imsdk.util.Objects.defaultObjectTag(this), cache);
                    return cache;
                }

                MediaInfo target = new MediaInfo();
                target.id = id;
                target.lastModify = lastModify;

                target.size = CursorUtil.getLong(cursor, ++index);
                target.width = CursorUtil.getInt(cursor, ++index);
                target.height = CursorUtil.getInt(cursor, ++index);
                target.mimeType = CursorUtil.getString(cursor, ++index);
                if (target.mimeType != null) {
                    target.mimeType = target.mimeType.trim().toLowerCase();
                }
                target.title = CursorUtil.getString(cursor, ++index);
                target.addTime = CursorUtil.getLong(cursor, ++index);
                target.durationMs = CursorUtil.getLong(cursor, ++index);

                {
                    final String path = CursorUtil.getString(cursor, ++index);
                    if (TextUtils.isEmpty(path)) {
                        MSIMUikitLog.v("invalid path:%s, target:%s", path, target);
                        return null;
                    }
                    final File dir = new File(path).getParentFile();
                    if (dir == null) {
                        target.bucketId = "";
                        target.bucketDisplayName = "";
                    } else {
                        target.bucketId = dir.getAbsolutePath();
                        target.bucketDisplayName = dir.getName();
                    }
                    target.uri = Uri.fromFile(new File(path));
                }

                MSIMUikitLog.v("%s mapToMediaInfo %s", com.masonsoft.imsdk.util.Objects.defaultObjectTag(this), target);

                MediaInfoInternalCache.putWithCopy(target);

                return target;
            }
        }

        private final MediaSelector mMediaSelector;
        private final ColumnsMap mColumnsMap;

        public MediaLoader(MediaLoaderCallback callback, MediaSelector mediaSelector) {
            super(callback);
            if (mediaSelector == null) {
                mediaSelector = new MediaSelector.SimpleMediaSelector();
            }
            mMediaSelector = mediaSelector;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mColumnsMap = new ContentUriColumnsMap();
            } else {
                mColumnsMap = new DefaultColumnsMap();
            }
        }

        public void start() {
            Threads.postBackground(this);
        }

        @Nullable
        private MediaLoaderCallback getCallback() {
            MediaLoaderCallback callback = (MediaLoaderCallback) getObject();
            if (isAbort()) {
                return null;
            }
            return callback;
        }

        @Override
        public void run() {
            final MediaBucket allMediaInfoBucket = new MediaBucket();
            allMediaInfoBucket.allMediaInfo = true;

            final List<MediaBucket> allBuckets = new ArrayList<>();
            allBuckets.add(allMediaInfoBucket);

            final Map<Uri, MediaInfo> allMediaInfoMap = new HashMap<>();

            Cursor cursor = null;
            try {
                AbortUtil.throwIfAbort(this);
                ContentResolver contentResolver = ContextUtil.getContext().getContentResolver();
                cursor = contentResolver.query(
                        MediaStore.Files.getContentUri("external"),
                        allColumns(),
                        MediaStore.Files.FileColumns.DATE_ADDED + " > 0 and " + MediaStore.Files.FileColumns.MIME_TYPE + " is not null and (" +
                                MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? or " + MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? )",
                        new String[]{
                                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                        },
                        MediaStore.MediaColumns.DATE_ADDED + " desc");
                Preconditions.checkNotNull(cursor);
                while (cursor.moveToNext()) {
                    AbortUtil.throwIfAbort(this);
                    MediaInfo itemMediaInfo = cursorToMediaInfo(cursor);
                    if (itemMediaInfo == null) {
                        continue;
                    }

                    if (!mMediaSelector.accept(itemMediaInfo)) {
                        continue;
                    }

                    if (allMediaInfoBucket.cover == null) {
                        allMediaInfoBucket.cover = itemMediaInfo;
                    }
                    allMediaInfoBucket.mediaInfoList.add(itemMediaInfo);
                    allMediaInfoMap.put(itemMediaInfo.uri, itemMediaInfo);

                    MediaBucket newBucket = createMediaBucket(itemMediaInfo);
                    MediaBucket oldBucket = queryOldMediaBucket(allBuckets, newBucket);
                    MediaBucket targetBucket;
                    if (oldBucket != null) {
                        // update old bucket
                        oldBucket.mediaInfoList.add(itemMediaInfo);
                        targetBucket = oldBucket;
                    } else {
                        // add new bucket;
                        allBuckets.add(newBucket);
                        targetBucket = newBucket;
                    }
                    itemMediaInfo.mMediaBucket = targetBucket;
                }

                AbortUtil.throwIfAbort(this);
                MediaLoaderCallback callback = getCallback();
                if (callback != null) {
                    callback.onLoadFinish(new MediaData(allMediaInfoBucket, allBuckets, allMediaInfoMap, mMediaSelector));
                }
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
                MediaLoaderCallback callback = getCallback();
                if (callback != null) {
                    callback.onLoadFinish(new MediaData(allMediaInfoBucket, allBuckets, allMediaInfoMap, mMediaSelector));
                }
            } finally {
                IOUtil.closeQuietly(cursor);
            }
        }

        @Nullable
        private MediaBucket queryOldMediaBucket(List<MediaBucket> allSubBuckets, MediaBucket query) {
            for (MediaBucket oldBucket : allSubBuckets) {
                if (ObjectsCompat.equals(oldBucket, query)) {
                    return oldBucket;
                }
            }
            return null;
        }

        @NonNull
        private MediaBucket createMediaBucket(MediaInfo mediaInfo) {
            MediaBucket target = new MediaBucket();
            target.cover = mediaInfo;
            target.mediaInfoList.add(mediaInfo);
            target.bucketId = mediaInfo.bucketId;
            target.bucketDisplayName = mediaInfo.bucketDisplayName;
            return target;
        }

        @NonNull
        private String[] allColumns() {
            return mColumnsMap.allColumns();
        }

        @Nullable
        private MediaInfo cursorToMediaInfo(Cursor cursor) {
            return mColumnsMap.mapToMediaInfo(cursor);
        }

        @Override
        public void close() {
            setAbort();
        }

    }

}
