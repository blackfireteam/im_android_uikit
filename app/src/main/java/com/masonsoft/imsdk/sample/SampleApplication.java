package com.masonsoft.imsdk.sample;

import android.app.Application;
import android.util.Log;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.sample.im.DiscoverUserManager;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.MSIMUikitManager;

import io.github.idonans.core.manager.ProcessManager;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final boolean debug = BuildConfig.DEBUG;
        if (debug) {
            // 设置 imsdk 的 log 打印级别
            IMLog.setLogLevel(Log.VERBOSE);
            // 设置 uikit 的 log 打印级别
            MSIMUikitLog.setLogLevel(Log.VERBOSE);
            // 设置 sample 的 log 打印级别
            SampleLog.setLogLevel(Log.VERBOSE);
        }

        // 仅在主进程初始化
        if (ProcessManager.getInstance().isMainProcess()) {
            DiscoverUserManager.getInstance().start();

            // 初始化 im
            MSIMManager.getInstance().initSdk(Constants.SUB_APP, IMTokenOfflineManager.getInstance().getSdkListener());
            // 初始化 uikit
            MSIMUikitManager.getInstance().start();

            LocalSettingsManager.getInstance().start();

            // 程序启动时，自动加入指定聊天室(如果有登录信息)
            GlobalChatRoomManager.getInstance().start();
        }
    }

}
