package com.masonsoft.imsdk.sample.app.signin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.sample.app.main.MainActivity;
import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;

import io.github.idonans.systeminsets.SystemUiHelper;

/**
 * 登录
 */
public class SignInActivity extends FragmentDelegateActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, SignInActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    private static final String FRAGMENT_TAG_SIGN_IN = "fragment_sign_in_20210322";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MSIMManager.getInstance().getSessionUserId() > 0) {
            // 已经登录
            MainActivity.start(this);
            finish();
            return;
        }

        SystemUiHelper.from(getWindow())
                .layoutStatusBar()
                .layoutNavigationBar()
                .layoutStable()
                .setLightStatusBar()
                .setLightNavigationBar()
                .apply();

        setFragmentDelegate(FRAGMENT_TAG_SIGN_IN, SignInFragment::newInstance);
    }

}
