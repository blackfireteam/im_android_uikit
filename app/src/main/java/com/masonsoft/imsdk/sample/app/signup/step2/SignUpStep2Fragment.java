package com.masonsoft.imsdk.sample.app.signup.step2;

import android.Manifest;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.app.signin.SignInViewPresenter;
import com.masonsoft.imsdk.sample.app.signup.SignUpArgument;
import com.masonsoft.imsdk.sample.app.signup.SignUpFragment;
import com.masonsoft.imsdk.sample.app.signup.SignUpView;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleSignUpStep2FragmentBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaPickerDialog;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaSelector;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.util.Objects;
import com.tbruyelle.rxpermissions3.RxPermissions;

import io.github.idonans.core.FormValidator;
import io.github.idonans.core.util.ToastUtil;
import io.github.idonans.lang.DisposableHolder;
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

    private final DisposableHolder mPermissionRequest = new DisposableHolder();
    private static final String[] IMAGE_PICKER_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

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
        syncAvatarState(mVirtualAvatarPickerView);

        ViewUtil.onClick(mBinding.editAvatar, v -> requestPickAvatarPermission());
        ViewUtil.onClick(mBinding.submit, v -> onSubmit());

        mView = new ViewImpl();
        mPresenter = new SignUpStep2FragmentPresenter(mView);

        return mBinding.getRoot();
    }

    private void requestPickAvatarPermission() {
        SampleLog.v("requestPickAvatarPermission");

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        if (mBinding == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        //noinspection CastCanBeRemovedNarrowingVariableType
        final RxPermissions rxPermissions = new RxPermissions((FragmentActivity) activity);
        mPermissionRequest.set(
                rxPermissions.request(IMAGE_PICKER_PERMISSION)
                        .subscribe(granted -> {
                            if (granted) {
                                onPickAvatarPermissionGranted();
                            } else {
                                SampleLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        }));
    }

    private void onPickAvatarPermissionGranted() {
        SampleLog.v("onPickAvatarPermissionGranted");

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        if (mBinding == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        final MediaPickerDialog mediaPickerDialog = new MediaPickerDialog(activity, activity.findViewById(Window.ID_ANDROID_CONTENT));
        mediaPickerDialog.setMediaSelector(new MediaSelector.SimpleMediaSelector());
        mediaPickerDialog.setOnMediaPickListener(imageInfoList -> {
            if (imageInfoList.isEmpty()) {
                return false;
            }

            final MediaData.MediaInfo mediaInfo = imageInfoList.get(0);
            onPickAvatarResult(mediaInfo.uri);
            return true;
        });
        mediaPickerDialog.show();
    }

    private void onPickAvatarResult(final Uri uri) {
        SampleLog.v("onPickAvatarResult uri:%s", uri);

        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }
        if (isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        if (mBinding == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }
        if (mPresenter == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
            return;
        }

        mVirtualAvatarPickerView.setAvatarUri(uri);
        syncAvatarState(mVirtualAvatarPickerView);
    }

    private void syncAvatarState(VirtualAvatarPickerView view) {
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }
        if (view.getAvatarUri() == null) {
            // 等待选择头像，显示占位图
            ViewUtil.setVisibilityIfChanged(mBinding.pickAvatarHolder, View.VISIBLE);
        } else {
            // 头像已选择，隐藏占位图
            ViewUtil.setVisibilityIfChanged(mBinding.pickAvatarHolder, View.GONE);
        }
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
