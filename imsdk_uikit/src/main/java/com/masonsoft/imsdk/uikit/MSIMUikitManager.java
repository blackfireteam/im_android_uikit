package com.masonsoft.imsdk.uikit;

import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.logging.FLogDefaultLoggingDelegate;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpNetworkFetcher;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.uikit.common.fileupload.CacheFileUploadProvider;
import com.masonsoft.imsdk.uikit.common.fileupload.TencentOSSFileUploadProvider;
import com.masonsoft.imsdk.uikit.util.OkHttpClientUtil;

import io.github.idonans.core.Singleton;
import io.github.idonans.core.manager.ProcessManager;
import io.github.idonans.core.util.ContextUtil;

public class MSIMUikitManager {

    private static final Singleton<MSIMUikitManager> INSTANCE = new Singleton<MSIMUikitManager>() {
        @Override
        protected MSIMUikitManager create() {
            return new MSIMUikitManager();
        }
    };

    public static MSIMUikitManager getInstance() {
        return INSTANCE.get();
    }

    private MSIMUikitManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WebView.setDataDirectorySuffix(ProcessManager.getInstance().getProcessTag());
        }

        // 初始化 emoji
        EmojiCompat.init(new BundledEmojiCompatConfig(ContextUtil.getContext()));

        // 设置默认文件上传组件
        MSIMManager.getInstance().setFileUploadProvider(new CacheFileUploadProvider(new TencentOSSFileUploadProvider()));

        // 初始化实时音视频通话管理
        MSIMRtcMessageManager.getInstance().start();

        initFresco();
    }

    private void initFresco() {
        if (IMLog.getLogLevel() <= Log.DEBUG) {
            FLogDefaultLoggingDelegate.getInstance().setMinimumLoggingLevel(Log.DEBUG);
        }
        Fresco.initialize(ContextUtil.getContext(), ImagePipelineConfig.newBuilder(ContextUtil.getContext())
                .setDownsampleEnabled(true)
                .setNetworkFetcher(new OkHttpNetworkFetcher(OkHttpClientUtil.createDefaultOkHttpClient()))
                .setMainDiskCacheConfig(DiskCacheConfig.newBuilder(ContextUtil.getContext())
                        .setBaseDirectoryName(ProcessManager.getInstance().getProcessTag() + "_fresco_main")
                        .build())
                .setSmallImageDiskCacheConfig(DiskCacheConfig.newBuilder(ContextUtil.getContext())
                        .setBaseDirectoryName(ProcessManager.getInstance().getProcessTag() + "_fresco_small")
                        .build())
                .build());
    }

    public void start() {
    }

}
