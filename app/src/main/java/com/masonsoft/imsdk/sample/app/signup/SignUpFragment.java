package com.masonsoft.imsdk.sample.app.signup;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

public abstract class SignUpFragment extends SystemInsetsFragment {

    @Nullable
    private SignUpArgument mSignUpArgument;

    @NonNull
    protected SignUpArgument getSignUpArgument() {
        if (mSignUpArgument == null) {
            mSignUpArgument = new SignUpArgument();
        }
        return mSignUpArgument;
    }

    protected void saveSignUpArgument() {
        if (isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        if (mSignUpArgument != null) {
            mSignUpArgument.writeTo(args);
        }
        setArguments(args);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSignUpArgument = SignUpArgument.valueOf(getArguments());
    }

}
