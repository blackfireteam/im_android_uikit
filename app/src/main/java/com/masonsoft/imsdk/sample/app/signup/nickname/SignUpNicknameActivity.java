package com.masonsoft.imsdk.sample.app.signup.nickname;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.sample.app.signup.SignUpArgument;
import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;

import io.github.idonans.systeminsets.SystemUiHelper;

/**
 * 注册-设置昵称
 */
public class SignUpNicknameActivity extends FragmentDelegateActivity {

    public static void start(Context context, @Nullable SignUpArgument signUpArgument) {
        Intent starter = new Intent(context, SignUpNicknameActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (signUpArgument != null) {
            signUpArgument.writeTo(starter);
        }
        context.startActivity(starter);
    }

    private static final String FRAGMENT_TAG_SIGN_UP_NICKNAME = "fragment_sign_up_nickname_20210421";

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

        setFragmentDelegate(FRAGMENT_TAG_SIGN_UP_NICKNAME,
                () -> SignUpNicknameFragment.newInstance(SignUpArgument.valueOf(getIntent())));
    }

}
