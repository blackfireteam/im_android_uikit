package com.masonsoft.imsdk.uikit.common.fileupload;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.masonsoft.imsdk.core.FileUploadProvider;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.lang.ImageInfo;
import com.masonsoft.imsdk.lang.MediaInfo;
import com.masonsoft.imsdk.util.BitmapUtil;
import com.masonsoft.imsdk.util.MediaUtil;
import com.masonsoft.imsdk.util.Objects;

import java.io.File;
import java.io.InputStream;

import io.github.idonans.core.Progress;
import io.github.idonans.core.manager.TmpFileManager;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.FileUtil;
import io.github.idonans.core.util.IOUtil;
import top.zibin.luban.Luban;

public class CacheFileUploadProvider implements FileUploadProvider {

    @NonNull
    private final FileUploadProvider mProvider;

    public CacheFileUploadProvider(@NonNull FileUploadProvider provider) {
        mProvider = provider;
    }

    private static final class CacheEntity {
        final String mimeType;
        final String accessUrl;

        public CacheEntity(String mimeType, String accessUrl) {
            this.mimeType = mimeType;
            this.accessUrl = accessUrl;
        }

        @Override
        public String toString() {
            return "CacheEntity{" +
                    "mimeType='" + mimeType + '\'' +
                    ", accessUrl='" + accessUrl + '\'' +
                    '}';
        }
    }

    private static class MemoryFullCache {

        private static final MemoryFullCache DEFAULT = new MemoryFullCache();

        private static final int MEMORY_CACHE_SIZE = 100;
        @NonNull
        private final LruCache<String, CacheEntity> mFullCaches = new LruCache<>(MEMORY_CACHE_SIZE);

        private void addFullCache(@NonNull String filePath, @NonNull CacheEntity cacheEntity) {
            mFullCaches.put(filePath, cacheEntity);
        }

        private void removeFullCache(@NonNull String filePath) {
            mFullCaches.remove(filePath);
        }

        @Nullable
        private CacheEntity getFullCache(@NonNull String filePath) {
            return mFullCaches.get(filePath);
        }
    }

    @NonNull
    @Override
    public String uploadFile(@NonNull String filePath, @Source int source, @Nullable String mimeType, @NonNull Progress progress) throws Throwable {
        final CacheEntity cache = MemoryFullCache.DEFAULT.getFullCache(filePath);
        if (cache != null) {
            IMLog.v(Objects.defaultObjectTag(this) + " uploadFile cache hit. %s -> %s",
                    filePath, cache);
            return cache.accessUrl;
        }

        final String[] outMimeType = new String[1];
        final String compressFilePath = compressFile(filePath, outMimeType);
        IMLog.v(Objects.defaultObjectTag(this) + " compress file %s -> %s, mimeType:%s",
                filePath, compressFilePath, outMimeType[0]);

        final String accessUrl = mProvider.uploadFile(compressFilePath, source, outMimeType[0], progress);
        MemoryFullCache.DEFAULT.addFullCache(filePath, new CacheEntity(outMimeType[0], accessUrl));
        return accessUrl;
    }

    /**
     * 压缩文件。如果成功压缩，返回压缩后的文件路径。否则返回原始路径。
     */
    @NonNull
    private String compressFile(@NonNull String fileUri, @NonNull String[] outMimeType) throws Throwable {
        outMimeType[0] = null;

        final Uri uri = Uri.parse(fileUri);
        final String scheme = uri.getScheme();
        File tmpFile = null;
        String filePath = null;
        if ("content".equalsIgnoreCase(scheme)) {
            InputStream is = null;
            try {
                tmpFile = TmpFileManager.getInstance().createNewTmpFileQuietly("__compress_file_copy_", null);
                if (tmpFile == null) {
                    throw new IllegalStateException("tmp file create fail");
                }
                is = ContextUtil.getContext().getContentResolver().openInputStream(uri);
                IOUtil.copy(is, tmpFile, null, null);
                filePath = tmpFile.getAbsolutePath();
            } catch (Throwable e) {
                FileUtil.deleteFileQuietly(tmpFile);
                tmpFile = null;
                throw e;
            } finally {
                IOUtil.closeQuietly(is);
            }
        } else if ("file".equalsIgnoreCase(scheme)) {
            filePath = fileUri.substring(7);
        } else {
            filePath = fileUri;
        }

        final File file = new File(filePath);
        if (!FileUtil.isFile(file)) {
            throw new IllegalStateException(filePath + " is not a exists file");
        }
        final Uri targetFileUri = Uri.fromFile(file);

        {
            // 先猜测该文件是否是图片
            final ImageInfo imageInfo = BitmapUtil.decodeImageInfo(targetFileUri);
            if (imageInfo != null) {
                // 是图片
                outMimeType[0] = imageInfo.mimeType;

                if (imageInfo.isGif()) {
                    // gif 图不压缩
                    return filePath;
                }

                // 压缩图片
                final File compressedFile = Luban.with(ContextUtil.getContext())
                        .get(filePath);
                return compressedFile.getAbsolutePath();
            }
        }

        {
            // 猜测该文件是否是视频或者音频
            final MediaInfo mediaInfo = MediaUtil.decodeMediaInfo(targetFileUri);
            if (mediaInfo != null) {
                // 是视频或者音频
                outMimeType[0] = mediaInfo.mimeType;
                // 视频或者音频不压缩
                return filePath;
            }
        }

        // 其它文件格式不压缩
        // 返回原始文件内容
        return filePath;
    }

}
