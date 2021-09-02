package com.masonsoft.imsdk.sample.app.signup.nickname;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.app.signin.SignInViewPresenter;
import com.masonsoft.imsdk.sample.app.signup.SignUpArgument;
import com.masonsoft.imsdk.sample.app.signup.SignUpFragment;
import com.masonsoft.imsdk.sample.app.signup.SignUpView;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleSignUpNicknameFragmentBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.FormValidator;
import io.github.idonans.core.util.ToastUtil;
import io.github.idonans.lang.util.ViewUtil;

public class SignUpNicknameFragment extends SignUpFragment {

    public static SignUpNicknameFragment newInstance(@Nullable SignUpArgument signUpArgument) {
        Bundle args = new Bundle();
        if (signUpArgument != null) {
            signUpArgument.writeTo(args);
        }
        SignUpNicknameFragment fragment = new SignUpNicknameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private ImsdkSampleSignUpNicknameFragmentBinding mBinding;
    private ViewImpl mView;
    private SignUpNicknameFragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkSampleSignUpNicknameFragmentBinding.inflate(inflater, container, false);
        FormValidator.bind(
                new FormValidator.InputView[]{
                        FormValidator.InputViewFactory.create(mBinding.editText),
                },
                new FormValidator.SubmitView[]{
                        FormValidator.SubmitViewFactory.create(mBinding.submit),
                }
        );
        mBinding.editText.setOnEditorActionListener((v, actionId, event) -> {
            SampleLog.v("onEditorAction actionId:%s, event:%s", actionId, event);
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onSubmit();
                return true;
            }
            return false;
        });

        mBinding.editText.setText(getSignUpArgument().nickname);

        ViewUtil.onClick(mBinding.submit, v -> onSubmit());

        mView = new ViewImpl();
        mPresenter = new SignUpNicknameFragmentPresenter(mView);

        return mBinding.getRoot();
    }

    private void onSubmit() {
        SampleLog.v("onSubmit");

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        if (mBinding == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        final String nickname = mBinding.editText.getText().toString().trim();
        final int minLength = 3;
        final int maxLength = 20;
        if (TextUtils.isEmpty(nickname)
                || nickname.length() < minLength
                || nickname.length() > maxLength) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_nickname_error));
            return;
        }

        final SignUpArgument signUpArgument = getSignUpArgument();
        signUpArgument.nickname = nickname;
        saveSignUpArgument();

        mPresenter.requestSignUp(signUpArgument.userId, signUpArgument.nickname);
    }

    class ViewImpl extends SignUpView {

        public void onSignUpFail(int code, String message) {
            SampleLog.v(Objects.defaultObjectTag(this) + " onSignUpFail code:%s, message:%s", code, message);

            final Activity activity = getActivity();
            if (activity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
                return;
            }

            TipUtil.showOrDefault(message);
        }

        public void onSignUpFail(Throwable e) {
            SampleLog.v(e, Objects.defaultObjectTag(this) + " onSignUpFail");

            final Activity activity = getActivity();
            if (activity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
                return;
            }

            TipUtil.show(R.string.imsdk_sample_tip_text_error_unknown);
        }

        public void onSignUpSuccess(long userId) {
            SampleLog.v(Objects.defaultObjectTag(this) + " onSignUpSuccess userId:%s", userId);

            final Activity activity = getActivity();
            if (activity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
                return;
            }

            mPresenter.requestToken(userId);
        }

        @NonNull
        @Override
        public SignUpArgument getSignUpArgument() {
            return SignUpNicknameFragment.this.getSignUpArgument();
        }

        @Nullable
        @Override
        protected Activity getActivity() {
            return SignUpNicknameFragment.this.getActivity();
        }

        @Nullable
        @Override
        protected SignInViewPresenter<?> getPresenter() {
            return SignUpNicknameFragment.this.mPresenter;
        }
    }

    private void clearPresenter() {
        if (mPresenter != null) {
            mPresenter.setAbort();
            mPresenter = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearPresenter();
        mBinding = null;
        mView = null;
    }

}
