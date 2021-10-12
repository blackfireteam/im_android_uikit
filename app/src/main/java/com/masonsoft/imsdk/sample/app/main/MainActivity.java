package com.masonsoft.imsdk.sample.app.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.push.PushPayload;
import com.masonsoft.imsdk.sample.IMTokenOfflineManager;
import com.masonsoft.imsdk.sample.app.signin.SignInActivity;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;

import io.github.idonans.systeminsets.SystemUiHelper;

public class MainActivity extends FragmentDelegateActivity {

    private static final String KEY_TYPE = "extra:type_20210727";
    private static final int TYPE_REDIRECT_TO_SIGN_IN = 1;
    private static final int TYPE_WITH_PUSH_PAYLOAD = 2;

    private static final String KEY_PUSH_PAYLOAD = "extra:pushPayload_20210813";

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean redirectToSignIn) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(KEY_TYPE, TYPE_REDIRECT_TO_SIGN_IN);
        starter.putExtra(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, redirectToSignIn);
        context.startActivity(starter);
    }

    public static void startToTabConversation(Context context, @Nullable PushPayload pushPayload) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(KEY_TYPE, TYPE_WITH_PUSH_PAYLOAD);
        starter.putExtra(KEY_PUSH_PAYLOAD, pushPayload);
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

        final ObjectWrapper pushPayloadRef = new ObjectWrapper(null);

        final int type = getIntent().getIntExtra(KEY_TYPE, 0);
        if (type == TYPE_REDIRECT_TO_SIGN_IN) {
            final boolean redirectToSignIn = getIntent().getBooleanExtra(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, false);
            if (redirectToSignIn) {
                SignInActivity.start(this);
                finish();
                return;
            }
        } else if (type == TYPE_WITH_PUSH_PAYLOAD) {
            pushPayloadRef.setObject(getIntent().getParcelableExtra(KEY_PUSH_PAYLOAD));
        }

        IMTokenOfflineManager.getInstance().attach();
        setFragmentDelegate(FRAGMENT_TAG_MAIN, () -> MainFragment.newInstance((PushPayload) pushPayloadRef.getObject()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IMTokenOfflineManager.getInstance().detach();
    }

}