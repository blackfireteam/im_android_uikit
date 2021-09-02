package com.masonsoft.imsdk.uikit.util;

import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.lang.SafetyRunnable;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.annotation.concurrent.ThreadSafe;

import io.github.idonans.core.Progress;
import io.github.idonans.core.WeakAbortSignal;
import io.github.idonans.core.thread.TaskQueue;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.FileUtil;
import io.github.idonans.core.util.IOUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 下载文件到本地
 */
@ThreadSafe
public class FileDownloadHelper {

    private static final boolean DEBUG = true;

    public interface OnFileDownloadListener {
        void onDownloadSuccess(String id, String localFilePath, String serverUrl);

        void onDownloadFail(String id, String serverUrl, Throwable e);
    }

    public static class OnSampleFileDownloadListener implements OnFileDownloadListener {
        private final boolean mRunOnUi;
        private final OnFileDownloadListener mOut;

        public OnSampleFileDownloadListener(boolean runOnUi, OnFileDownloadListener out) {
            mRunOnUi = runOnUi;
            mOut = out;
        }

        @Override
        public void onDownloadSuccess(String id, String localFilePath, String serverUrl) {
            if (mRunOnUi) {
                Threads.runOnUi(() -> {
                    if (mOut != null) {
                        mOut.onDownloadSuccess(id, localFilePath, serverUrl);
                    }
                });
            } else {
                if (mOut != null) {
                    mOut.onDownloadSuccess(id, localFilePath, serverUrl);
                }
            }
        }

        @Override
        public void onDownloadFail(String id, String serverUrl, Throwable e) {
            if (mRunOnUi) {
                Threads.runOnUi(() -> {
                    if (mOut != null) {
                        mOut.onDownloadFail(id, serverUrl, e);
                    }
                });
            } else {
                if (mOut != null) {
                    mOut.onDownloadFail(id, serverUrl, e);
                }
            }
        }
    }

    public interface OnFileDownloadProgressListener {
        void onDownloadProgress(String id, String serverUrl, @NonNull Progress progress);
    }

    public static class OnSampleFileDownloadProgressListener implements OnFileDownloadProgressListener {
        private final boolean mRunOnUi;
        private final OnFileDownloadProgressListener mOut;

        public OnSampleFileDownloadProgressListener(boolean runOnUi, OnFileDownloadProgressListener out) {
            mRunOnUi = runOnUi;
            mOut = out;
        }

        @Override
        public void onDownloadProgress(String id, String serverUrl, @NonNull Progress progress) {
            if (mRunOnUi) {
                Threads.runOnUi(() -> {
                    if (mOut != null) {
                        mOut.onDownloadProgress(id, serverUrl, progress);
                    }
                });
            } else {
                if (mOut != null) {
                    mOut.onDownloadProgress(id, serverUrl, progress);
                }
            }
        }
    }

    @Nullable
    private OnFileDownloadListener mOnFileDownloadListener;
    @Nullable
    private OnFileDownloadProgressListener mOnFileDownloadProgressListener;

    private final TaskQueue mTaskQueue = new TaskQueue(2);

    public FileDownloadHelper() {
    }

    public void setOnFileDownloadListener(@Nullable OnFileDownloadListener listener) {
        mOnFileDownloadListener = listener;
    }

    public void setOnFileDownloadProgressListener(@Nullable OnFileDownloadProgressListener listener) {
        mOnFileDownloadProgressListener = listener;
    }

    public void enqueueFileDownload(String id, String serverUrl) {
        mTaskQueue.enqueue(new SafetyRunnable(new DownloadTask(id, serverUrl, this)));
    }

    public void blockingFileUpload(String id, String localFilePath) {
        new DownloadTask(id, localFilePath, this).run();
    }

    public static class DownloadTask extends WeakAbortSignal implements Runnable {

        private final String mId;
        private final String mServerUrl;

        public DownloadTask(String id, String serverUrl, @Nullable FileDownloadHelper object) {
            super(object);
            if (serverUrl != null) {
                serverUrl = serverUrl.trim();
            }
            this.mId = id;
            this.mServerUrl = serverUrl;
        }

        @Nullable
        private FileDownloadHelper getFileUploadHelper() {
            if (isAbort()) {
                return null;
            }
            return (FileDownloadHelper) getObject();
        }

        private void notifyDownloadFail(Throwable e) {
            if (DEBUG) {
                MSIMUikitLog.v(e, "notifyDownloadFail, mId:%s, mServerUrl:%s", mId, mServerUrl);
            }
            FileDownloadHelper fileUploadHelper = getFileUploadHelper();
            if (fileUploadHelper != null) {
                OnFileDownloadListener listener = fileUploadHelper.mOnFileDownloadListener;
                if (listener != null) {
                    listener.onDownloadFail(mId, mServerUrl, e);
                }
            }
        }

        private void notifyDownloadSuccess(String localFilePath) {
            if (DEBUG) {
                MSIMUikitLog.v("notifyDownloadSuccess, mId:%s, mServerUrl:%s, localFilePath:%s", mId, mServerUrl, localFilePath);
            }
            FileDownloadHelper fileDownloadHelper = getFileUploadHelper();
            if (fileDownloadHelper != null) {
                OnFileDownloadListener listener = fileDownloadHelper.mOnFileDownloadListener;
                if (listener != null) {
                    listener.onDownloadSuccess(mId, localFilePath, mServerUrl);
                }
            }
        }

        private void notifyDownloadProgress(long currentSize, long totalSize) {
            if (DEBUG) {
                MSIMUikitLog.v("notifyDownloadProgress [%s/%s], mId:%s, mServerUrl:%s",
                        currentSize, totalSize, mId, mServerUrl);
            }
            if (currentSize > totalSize && totalSize >= 0) {
                Throwable e = new IllegalArgumentException("notifyDownloadProgress invalid args currentSize:" + currentSize + ", totalSize:" + totalSize);
                MSIMUikitLog.e(e);
                currentSize = totalSize;
            }
            final Progress progress = new Progress(totalSize, currentSize);
            FileDownloadHelper fileUploadHelper = getFileUploadHelper();
            if (fileUploadHelper != null) {
                OnFileDownloadProgressListener listener = fileUploadHelper.mOnFileDownloadProgressListener;
                if (listener != null) {
                    listener.onDownloadProgress(mId, mServerUrl, progress);
                }
            }
        }

        @Override
        public void run() {
            if (DEBUG) {
                MSIMUikitLog.v("DownloadTask::run, mId:%s, mServerUrl:%s", mId, mServerUrl);
            }

            File errorFile = null;
            Response response = null;
            InputStream is = null;

            try {
                if (TextUtils.isEmpty(mServerUrl)) {
                    throw new IllegalArgumentException("server url is empty");
                }

                // 下载文件
                final String fileExtension = FileUtil.getFileExtensionFromUrl(mServerUrl);
                if (TextUtils.isEmpty(fileExtension)) {
                    throw new IllegalArgumentException("DownloadTask run fail to get fileExtension from " + mServerUrl);
                }
                // 本地生成唯一文件名
                final String unionFilename = FilenameUtil.createUnionFilename(fileExtension, null);
                final File dir = FileUtil.getAppMediaDir();
                if (dir == null) {
                    throw new IllegalArgumentException("DownloadTask run fail to get app media dir");
                }
                final File localFile = new File(dir, unionFilename);
                errorFile = localFile;

                Progress progress = new Progress() {
                    @Override
                    protected void onUpdate() {
                        super.onUpdate();
                        notifyDownloadProgress(getCurrent(), getTotal());
                    }
                };
                if (URLUtil.isNetworkUrl(mServerUrl)) {
                    OkHttpClient httpClient = OkHttpClientUtil.createDefaultOkHttpClient();
                    Request request = new Request.Builder().url(mServerUrl).get().build();
                    response = httpClient.newCall(request).execute();
                    ResponseBody responseBody = response.body();

                    progress.set(responseBody.contentLength(), 0);
                    is = responseBody.byteStream();
                } else {
                    File serverUrlAsFile = new File(mServerUrl);
                    progress.set(serverUrlAsFile.length(), 0);
                    is = new FileInputStream(serverUrlAsFile);
                }

                MSIMUikitLog.v("DownloadTask run start download, %s -> %s", mServerUrl, localFile.getAbsolutePath());
                IOUtil.copy(is, localFile, this, progress);
                errorFile = null;
                MSIMUikitLog.v("DownloadTask run start download onSuccess, %s -> %s", mServerUrl, localFile.getAbsolutePath());

                notifyDownloadSuccess(localFile.getAbsolutePath());
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
                notifyDownloadFail(e);
            } finally {
                FileUtil.deleteFileQuietly(errorFile);
                IOUtil.closeQuietly(is);
                IOUtil.closeQuietly(response);
            }
            if (DEBUG) {
                MSIMUikitLog.v("DownloadTask::run:end, mId:%s, mServerUrl:%s", mId, mServerUrl);
            }
        }
    }

}
