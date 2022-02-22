package com.masonsoft.imsdk.sample.app.signup.step2;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.app.signin.SignInViewPresenter;
import com.masonsoft.imsdk.sample.app.signup.SignUpArgument;
import com.masonsoft.imsdk.sample.app.signup.SignUpFragment;
import com.masonsoft.imsdk.sample.app.signup.SignUpView;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleSignUpStep2FragmentBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.FormValidator;
import io.github.idonans.core.util.ToastUtil;
import io.github.idonans.lang.util.ViewUtil;

public class SignUpStep2Fragment extends SignUpFragment {

    public static SignUpStep2Fragment newInstance(@Nullable SignUpArgument signUpArgument) {
        Bundle args = new Bundle();
        if (signUpArgument != null) {
            signUpArgument.writeTo(args);
        }
        SignUpStep2Fragment fragment = new SignUpStep2Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private ImsdkSampleSignUpStep2FragmentBinding mBinding;
    private VirtualAvatarPickerView mVirtualAvatarPickerView;
    private ViewImpl mView;
    private SignUpStep2FragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkSampleSignUpStep2FragmentBinding.inflate(inflater, container, false);
        mVirtualAvatarPickerView = new VirtualAvatarPickerView();
        FormValidator.bind(
                new FormValidator.InputView[]{
                        mVirtualAvatarPickerView,
                },
                new FormValidator.SubmitView[]{
                        FormValidator.SubmitViewFactory.create(mBinding.submit),
                }
        );

        ViewUtil.onClick(mBinding.submit, v -> onSubmit());

        mView = new ViewImpl();
        mPresenter = new SignUpStep2FragmentPresenter(mView);

        return mBinding.getRoot();
    }

    private static class VirtualAvatarPickerView implements FormValidator.InputView {

        private OnContentChangedListener mOnContentChangedListener;
        private Uri mAvatarUri;

        @Override
        public boolean isContentEnable() {
            return mAvatarUri != null;
        }

        public Uri getAvatarUri() {
            return this.mAvatarUri;
        }

        public void setAvatarUri(Uri avatarUri) {
            mAvatarUri = avatarUri;
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }

        @Override
        public void setOnContentChangedListener(OnContentChangedListener onContentChangedListener) {
            mOnContentChangedListener = onContentChangedListener;
        }
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

        final Uri avatarUri = mVirtualAvatarPickerView.getAvatarUri();
        if (avatarUri == null) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_require_pick_avatar));
            return;
        }

        final SignUpArgument signUpArgument = getSignUpArgument();
        signUpArgument.avatar = avatarUri.toString();
        saveSignUpArgument();

        mPresenter.requestSignUp(signUpArgument);
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

            TipUtil.show(R.string.imsdk_uikit_tip_text_error_unknown);
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
            return SignUpStep2Fragment.this.getSignUpArgument();
        }

        @Nullable
        @Override
        protected Activity getActivity() {
            return SignUpStep2Fragment.this.getActivity();
        }

        @Nullable
        @Override
        protected SignInViewPresenter<?> getPresenter() {
            return SignUpStep2Fragment.this.mPresenter;
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
