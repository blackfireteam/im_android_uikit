package com.masonsoft.imsdk.sample;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.masonsoft.imsdk.MSIMSdkListener;
import com.masonsoft.imsdk.common.TopActivity;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.sample.app.main.MainActivity;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleContentNoticeDialog;
import com.masonsoft.imsdk.util.TimeUtil;

import io.github.idonans.core.Singleton;
import io.github.idonans.core.thread.Threads;

/**
 * 处理登录失效状态通知
 */
public class IMTokenOfflineManager {

    private static final Singleton<IMTokenOfflineManager> INSTANCE = new Singleton<IMTokenOfflineManager>() {
        @Override
        protected IMTokenOfflineManager create() {
            return new IMTokenOfflineManager();
        }
    };

    public static IMTokenOfflineManager getInstance() {
        return INSTANCE.get();
    }

    private boolean mLastKickedOffline;
    private boolean mLastTokenExpired;

    private boolean mAttached;

    private String mConnectStateHumanString;
    private long mConnectStateHumanStringTimeMs;
    private String mSignStateHumanString;
    private long mSignStateHumanStringTimeMs;

    @NonNull
    private final MSIMSdkListener mSdkListener = new MSIMSdkListener() {
        @Override
        public void onConnecting() {
            SampleLog.v("%s onConnecting", this);
            setConnectStateHumanString("onConnecting");
        }

        @Override
        public void onConnectSuccess() {
            SampleLog.v("%s onConnectSuccess", this);
            setConnectStateHumanString("onConnectSuccess");
        }

        @Override
        public void onConnectClosed() {
            SampleLog.v("%s onConnectClosed", this);
            setConnectStateHumanString("onConnectClosed");
        }

        @Override
        public void onSigningIn() {
            SampleLog.v("%s onSigningIn", this);
            setSignStateHumanString("onSigningIn");
        }

        @Override
        public void onSignInSuccess() {
            SampleLog.v("%s onSignInSuccess", this);
            setSignStateHumanString("onSignInSuccess");
        }

        @Override
        public void onSignInFail(@NonNull GeneralResult result) {
            SampleLog.v("%s onSignInFail result:%s", this, result);
            setSignStateHumanString("onSignInFail:" + result.getCause().message);
        }

        @Override
        public void onKickedOffline() {
            SampleLog.v("%s onKickedOffline", this);
            setSignStateHumanString("onKickedOffline");
            setLastKickedOffline();
        }

        @Override
        public void onTokenExpired() {
            SampleLog.v("%s onTokenExpired", this);
            setSignStateHumanString("onTokenExpired");
            setLastTokenExpired();
        }

        @Override
        public void onSigningOut() {
            SampleLog.v("%s onSigningOut", this);
            setSignStateHumanString("onSigningOut");
        }

        @Override
        public void onSignOutSuccess() {
            SampleLog.v("%s onSignOutSuccess", this);
            setSignStateHumanString("onSignOutSuccess");
        }

        @Override
        public void onSignOutFail(@NonNull GeneralResult result) {
            SampleLog.v("%s onSignOutFail result:%s", this, result);
            setSignStateHumanString("onSignOutFail:" + result.getCause().message);
        }
    };

    private void setConnectStateHumanString(String connectStateHumanString) {
        mConnectStateHumanString = connectStateHumanString;
        mConnectStateHumanStringTimeMs = System.currentTimeMillis();
    }

    private void setSignStateHumanString(String signStateHumanString) {
        mSignStateHumanString = signStateHumanString;
        mSignStateHumanStringTimeMs = System.currentTimeMillis();
    }

    public String getConnectStateHumanString() {
        return mConnectStateHumanString + "[" + TimeUtil.msToHumanString(this.mConnectStateHumanStringTimeMs) + "]";
    }

    public String getSignStateHumanString() {
        return mSignStateHumanString + "[" + TimeUtil.msToHumanString(this.mSignStateHumanStringTimeMs) + "]";
    }

    private IMTokenOfflineManager() {
    }

    @NonNull
    public MSIMSdkListener getSdkListener() {
        return mSdkListener;
    }

    private void clearLast() {
        mLastKickedOffline = false;
        mLastTokenExpired = false;
    }

    private void setLastKickedOffline() {
        mLastKickedOffline = true;
        mLastTokenExpired = false;

        if (!mAttached) {
            return;
        }
        Threads.postUi(() -> {
            if (mAttached) {
                if (!showKickedOffline()) {
                    SampleLog.v("showKickedOffline return false, default try finish current activity task");
                    final Activity activity = TopActivity.getInstance().get();
                    if (activity != null) {
                        activity.finishAndRemoveTask();
                    }
                }
            }
        });
    }

    private void setLastTokenExpired() {
        mLastKickedOffline = false;
        mLastTokenExpired = true;

        if (!mAttached) {
            return;
        }
        Threads.postUi(() -> {
            if (mAttached) {
                if (!showTokenExpired()) {
                    SampleLog.v("showTokenExpired return false, default try finish current activity task");
                    final Activity activity = TopActivity.getInstance().get();
                    if (activity != null) {
                        activity.finishAndRemoveTask();
                    }
                }
            }
        });
    }

    @UiThread
    private boolean showKickedOffline() {
        final Activity topActivity = TopActivity.getInstance().getResumed();
        if (topActivity == null) {
            SampleLog.v(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return false;
        }

        if (topActivity.isFinishing()) {
            SampleLog.v(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_FINISHING);
            return false;
        }

        final SimpleContentNoticeDialog dialog = new SimpleContentNoticeDialog(
                topActivity,
                I18nResources.getString(R.string.imsdk_sample_tip_kicked_offline)
        );
        dialog.setOnHideListener(cancel -> MainActivity.start(topActivity, true));
        dialog.show();
        return true;
    }

    @UiThread
    private boolean showTokenExpired() {
        final Activity topActivity = TopActivity.getInstance().getResumed();
        if (topActivity == null) {
            SampleLog.v(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return false;
        }

        if (topActivity.isFinishing()) {
            SampleLog.v(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_FINISHING);
            return false;
        }

        final SimpleContentNoticeDialog dialog = new SimpleContentNoticeDialog(
                topActivity,
                I18nResources.getString(R.string.imsdk_sample_tip_token_expired)
        );
        dialog.setOnHideListener(cancel -> MainActivity.start(topActivity, true));
        dialog.show();
        return true;
    }

    public void attach() {
        mAttached = true;
    }

    public void detach() {
        mAttached = false;
    }

}
