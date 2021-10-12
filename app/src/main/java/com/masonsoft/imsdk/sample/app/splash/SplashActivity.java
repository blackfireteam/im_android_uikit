package com.masonsoft.imsdk.sample.app.splash;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.push.MSIMPushManager;
import com.masonsoft.imsdk.push.PushPayload;
import com.masonsoft.imsdk.sample.app.main.MainActivity;
import com.masonsoft.imsdk.sample.app.signin.SignInActivity;
import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;

import io.github.idonans.core.WeakAbortSignal;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.systeminsets.SystemUiHelper;

public class SplashActivity extends FragmentDelegateActivity {

    private static final String FRAGMENT_TAG_SPLASH = "fragment_splash_20210322";

    private boolean mPendingRedirect;
    private boolean mStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUiHelper.from(getWindow())
                .layoutStatusBar()
                .layoutNavigationBar()
                .layoutStable()
                .setLightStatusBar()
                .setLightNavigationBar()
                .apply();

        setFragmentDelegate(FRAGMENT_TAG_SPLASH, SplashFragment::newInstance);

        Threads.postUi(new RedirectTask(this), 1500L);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStarted = true;

        if (mPendingRedirect) {
            doRedirect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStarted = false;
    }

    private void dispatchRedirect() {
        if (isFinishing()) {
            return;
        }

        if (!mStarted) {
            mPendingRedirect = true;
            return;
        }

        doRedirect();
    }

    private void doRedirect() {
        if (isFinishing()) {
            return;
        }

        if (hasValidSession()) {
            // 已经登录，跳转到主页
            final PushPayload pushPayload = MSIMPushManager.getInstance().decodePushPayload(getIntent().getExtras());
            MainActivity.startToTabConversation(this, pushPayload);
        } else {
            // 没有登录，跳转到登录页
            SignInActivity.start(this);
        }

        finish();
    }

    /**
     * 如果存在有效的登录信息，返回 true, 否则返回 false.
     */
    private boolean hasValidSession() {
        return MSIMManager.getInstance().hasSession();
    }

    private static class RedirectTask extends WeakAbortSignal implements Runnable {

        public RedirectTask(@Nullable SplashActivity splashActivity) {
            super(splashActivity);
        }

        @Override
        public void run() {
            if (isAbort()) {
                return;
            }

            SplashActivity splashActivity = (SplashActivity) getObject();
            if (splashActivity == null) {
                return;
            }

            splashActivity.dispatchRedirect();
        }

    }

}
