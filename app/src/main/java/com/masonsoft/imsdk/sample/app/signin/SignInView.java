package com.masonsoft.imsdk.sample.app.signin;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.app.main.MainActivity;
import com.masonsoft.imsdk.sample.app.signup.SignUpActivity;
import com.masonsoft.imsdk.sample.app.signup.SignUpArgument;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.dynamic.DynamicView;

public abstract class SignInView implements DynamicView {

    @Nullable
    protected abstract Activity getActivity();

    @Nullable
    protected abstract SignInViewPresenter<?> getPresenter();

    public void onRequestSignUp(final long userId) {
        SampleLog.v(Objects.defaultObjectTag(this) + " onRequestSignUp userId:%s", userId);
        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        final SignUpArgument signUpArgument = new SignUpArgument();
        signUpArgument.userId = userId;
        SignUpActivity.start(activity, signUpArgument);
    }

    public void onFetchTokenSuccess(String token, String url) {
        SampleLog.v(Objects.defaultObjectTag(this) + " onFetchTokenSuccess token:%s, url:%s", token, url);

        final SignInViewPresenter<?> presenter = getPresenter();
        if (presenter == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
            return;
        }
        presenter.requestTcpSignIn(token, url);
    }

    public void onFetchTokenFail(long userId, int code, String message) {
        SampleLog.v(Objects.defaultObjectTag(this) + " onFetchTokenFail userId:%s, code:%s, message:%s", userId, code, message);

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        TipUtil.showOrDefault(message);
    }

    public void onFetchTokenFail(Throwable e, long userId) {
        SampleLog.v(e, Objects.defaultObjectTag(this) + " onFetchTokenFail userId:%s", userId);

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        TipUtil.show(R.string.imsdk_sample_tip_text_error_unknown);
    }

    public void onTcpSignInSuccess() {
        SampleLog.v(Objects.defaultObjectTag(this) + " onTcpSignInSuccess");

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        MainActivity.start(activity);
        activity.finish();
    }

    public void onTcpSignInFail(GeneralResult result) {
        SampleLog.v(Objects.defaultObjectTag(this) + " onTcpSignInFail GeneralResult:%s", result);

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        if (result.other != null) {
            TipUtil.showOrDefault(result.other.message);
        } else {
            TipUtil.showOrDefault(result.message);
        }
    }

    public void onTcpSignInFail(Throwable e) {
        SampleLog.v(e, Objects.defaultObjectTag(this) + " onTcpSignInFail");

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        TipUtil.show(R.string.imsdk_sample_tip_text_error_unknown);
    }

}
