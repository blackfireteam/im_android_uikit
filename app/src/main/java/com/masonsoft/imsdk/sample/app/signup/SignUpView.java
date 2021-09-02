package com.masonsoft.imsdk.sample.app.signup;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.app.signin.SignInActivity;
import com.masonsoft.imsdk.sample.app.signin.SignInView;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.util.Objects;

public abstract class SignUpView extends SignInView {

    @NonNull
    public abstract SignUpArgument getSignUpArgument();

    @Override
    public void onTcpSignInSuccess() {
        SampleLog.v(Objects.defaultObjectTag(this) + " onTcpSignInSuccess");

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        SignInActivity.start(activity);
        activity.finish();
    }

}
