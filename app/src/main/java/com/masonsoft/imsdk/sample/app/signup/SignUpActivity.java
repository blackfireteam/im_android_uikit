package com.masonsoft.imsdk.sample.app.signup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;
import com.masonsoft.imsdk.sample.app.signup.nickname.SignUpNicknameActivity;

/**
 * 注册
 */
public class SignUpActivity extends FragmentDelegateActivity {

    public static void start(Context context, @Nullable SignUpArgument signUpArgument) {
        Intent starter = new Intent(context, SignUpActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (signUpArgument != null) {
            signUpArgument.writeTo(starter);
        }
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SignUpArgument signUpArgument = SignUpArgument.valueOf(getIntent());
        SignUpNicknameActivity.start(this, signUpArgument);
        finish();
    }

}
