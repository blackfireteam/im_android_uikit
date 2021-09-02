package com.masonsoft.imsdk.sample.app.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.masonsoft.imsdk.sample.IMTokenOfflineManager;
import com.masonsoft.imsdk.sample.app.signin.SignInActivity;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;

import io.github.idonans.systeminsets.SystemUiHelper;

public class MainActivity extends FragmentDelegateActivity {

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean redirectToSignIn) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, redirectToSignIn);
        context.startActivity(starter);
    }

    private static final String FRAGMENT_TAG_MAIN = "fragment_main_20210322";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUiHelper.from(getWindow())
                .layoutStatusBar()
                .layoutNavigationBar()
                .layoutStable()
                .setLightStatusBar()
                .setLightNavigationBar()
                .apply();

        final boolean redirectToSignIn = getIntent().getBooleanExtra(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, false);
        if (redirectToSignIn) {
            SignInActivity.start(this);
            finish();
            return;
        }

        IMTokenOfflineManager.getInstance().attach();
        setFragmentDelegate(FRAGMENT_TAG_MAIN, MainFragment::newInstance);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IMTokenOfflineManager.getInstance().detach();
    }

}