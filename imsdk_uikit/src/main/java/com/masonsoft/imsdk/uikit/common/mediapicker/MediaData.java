package com.masonsoft.imsdk.uikit.common.mediapicker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class MediaData {

    private static final boolean USE_CONTENT_URI = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

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
     * @param mediaInfo
     * @return
     */
    public int indexOfSelected(MediaInfo mediaInfo) {
        return mediaInfoListSelected.indexOf(mediaInfo);
    }

    public static class MediaInfo {
        @NonNull
        public Uri uri;
        public long size;
        public long durationMs;
        public int width;
        public int height;
        public String mimeType;
        public String title;
        public long addTime;
        public int id;
        @NonNull
        public MediaBucket mMediaBucket;

        private String bucketId;
        private String bucketDisplayName;

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
            return this.width * this.height * 4;
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
         * 是否是总的那个 bucket, 包含了所有的图片
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

        private final MediaSelector mMediaSelector;
        private final Uri mMediaUri = MediaStore.Files.getContentUri("external");

        public MediaLoader(MediaLoaderCallback callback, MediaSelector mediaSelector) {
            super(callback);
            if (mediaSelector == null) {
                mediaSelector = new MediaSelector.SimpleMediaSelector();
            }
            mMediaSelector = mediaSelector;
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
                        mMediaUri,
                        allColumns(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE + " in (?,?)",
                        new String[]{
                                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                        },
                        MediaStore.MediaColumns._ID + " asc");
                com.google.common.base.Preconditions.checkNotNull(cursor);

                for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
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
            if (USE_CONTENT_URI) {
                return new String[]{
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.SIZE,           // 媒体的大小，long型  132492
                        MediaStore.MediaColumns.WIDTH,          // 媒体的宽度，int型  1920, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.HEIGHT,         // 媒体的高度，int型  1080, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.MIME_TYPE,      // 媒体的类型     image/jpeg
                        MediaStore.MediaColumns.TITLE,
                        MediaStore.MediaColumns.DATE_ADDED,     // 添加时间
                        MediaStore.MediaColumns._ID,            // id
                        MediaStore.MediaColumns.DURATION,       // video duration
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.BUCKET_ID,
                        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                };
            } else {
                return new String[]{
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.SIZE,           // 媒体的大小，long型  132492
                        MediaStore.MediaColumns.WIDTH,          // 媒体的宽度，int型  1920, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.HEIGHT,         // 媒体的高度，int型  1080, 仅当媒体格式是图片或者视频时有效
                        MediaStore.MediaColumns.MIME_TYPE,      // 媒体的类型     image/jpeg
                        MediaStore.MediaColumns.TITLE,
                        MediaStore.MediaColumns.DATE_ADDED,     // 添加时间
                        MediaStore.MediaColumns._ID,            // id
                        MediaStore.MediaColumns.DURATION,       // video duration
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                        MediaStore.MediaColumns.DATA,           // 媒体的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                        //////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////
                };
            }
        }

        @Nullable
        private MediaInfo cursorToMediaInfo(Cursor cursor) {
            MediaInfo target = new MediaInfo();
            int index = -1;
            target.size = CursorUtil.getLong(cursor, ++index);
            target.width = CursorUtil.getInt(cursor, ++index);
            target.height = CursorUtil.getInt(cursor, ++index);
            target.mimeType = CursorUtil.getString(cursor, ++index);
            if (target.mimeType != null) {
                target.mimeType = target.mimeType.trim().toLowerCase();
            }
            target.title = CursorUtil.getString(cursor, ++index);
            target.addTime = CursorUtil.getLong(cursor, ++index);
            target.id = CursorUtil.getInt(cursor, ++index);
            target.durationMs = CursorUtil.getLong(cursor, ++index);

            if (USE_CONTENT_URI) {
                target.bucketId = CursorUtil.getString(cursor, ++index);
                target.bucketDisplayName = CursorUtil.getString(cursor, ++index);
                target.uri = mMediaUri
                        .buildUpon()
                        .appendPath(String.valueOf(target.id))
                        .build();
            } else {
                final String path = CursorUtil.getString(cursor, ++index);
                if (TextUtils.isEmpty(path)) {
                    MSIMUikitLog.v("invalid path:%s, target:%s", path, target);
                    return null;
                }
                final File dir = new File(path).getParentFile();
                if (dir == null) {
                    target.bucketId = "";
                } else {
                    target.bucketId = dir.getAbsolutePath();
                }
                target.bucketDisplayName = dir.getName();
                target.uri = Uri.fromFile(new File(path));
            }

            MSIMUikitLog.v("cursorToMediaInfo USE_CONTENT_URI:%s -> %s", USE_CONTENT_URI, target);

            if (TextUtils.isEmpty(target.mimeType)) {
                MSIMUikitLog.v("invalid mimeType:%s", target.mimeType);
                return null;
            }

            return target;
        }

        @Override
        public void close() {
            setAbort();
        }

    }

}
