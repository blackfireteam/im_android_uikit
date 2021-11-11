package com.masonsoft.imsdk.sample.app.mine;

import android.Manifest;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.app.main.MainActivity;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleMineFragmentBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaPickerDialog;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaSelector;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleContentConfirmDialog;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleContentInputDialog;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleLoadingDialog;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.util.Objects;
import com.tbruyelle.rxpermissions3.RxPermissions;

import io.github.idonans.dynamic.DynamicView;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 我的
 */
public class MineFragment extends SystemInsetsFragment {

    public static MineFragment newInstance() {
        Bundle args = new Bundle();
        MineFragment fragment = new MineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final DisposableHolder mPermissionRequest = new DisposableHolder();
    private static final String[] IMAGE_PICKER_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Nullable
    private ImsdkSampleMineFragmentBinding mBinding;
    private MineFragmentPresenter mPresenter;
    private ViewImpl mView;
    @Nullable
    private SimpleLoadingDialog mSignOutLoadingDialog;

    private void showSignOutLoadingDialog() {
        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }
        if (mSignOutLoadingDialog == null) {
            mSignOutLoadingDialog = new SimpleLoadingDialog(activity);
        }
        mSignOutLoadingDialog.show();
    }

    private void hideSignOutLoadingDialog() {
        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }
        if (mSignOutLoadingDialog != null) {
            mSignOutLoadingDialog.hide();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SampleLog.v("onCreateView %s", getClass());

        mBinding = ImsdkSampleMineFragmentBinding.inflate(inflater, container, false);
        ViewUtil.onClick(mBinding.avatar, v -> requestPickAvatarPermission());
        ViewUtil.onClick(mBinding.modifyUsername, v -> startModifyUsername());
        bindCheckedChangeListener();
        ViewUtil.onClick(mBinding.actionSignOut, v -> requestSignOut());

        mBinding.actionSignOut.setEnabled(MSIMManager.getInstance().hasSession());

        return mBinding.getRoot();
    }

    private void clearCheckedChangeListener() {
        if (mBinding == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        mBinding.modifyGenderSwitch.setOnCheckedChangeListener(null);
    }

    private void bindCheckedChangeListener() {
        if (mBinding == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        mBinding.modifyGenderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onGenderChanged(isChecked));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clearPresenter();
        mView = new ViewImpl();
        mPresenter = new MineFragmentPresenter(mView);
        mPresenter.requestSyncSessionUserInfo();
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

        mPresenter.uploadAvatar(uri);
    }

    private void startModifyUsername() {
        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
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
        final String unsafeCacheUsername = mBinding.username.getText().toString();
        final SimpleContentInputDialog dialog = new SimpleContentInputDialog(activity, unsafeCacheUsername);
        dialog.setOnBtnRightClickListener(input -> {
            if (mPresenter == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
                return;
            }

            final String nickname = input.trim();
            if (TextUtils.isEmpty(nickname)) {
                TipUtil.show(R.string.imsdk_sample_profile_modify_nickname_error_empty);
                return;
            }

            mPresenter.submitNickname(nickname);
        });
        dialog.show();
    }

    private void onGenderChanged(boolean isChecked) {
        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        if (mPresenter == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
            return;
        }
        mPresenter.trySubmitGenderChanged(isChecked);
    }

    private void requestSignOut() {
        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        final SimpleContentConfirmDialog dialog = new SimpleContentConfirmDialog(
                activity,
                I18nResources.getString(R.string.imsdk_sample_sign_out_confirm_text));
        dialog.setOnBtnRightClickListener(this::onSignOutConfirm);
        dialog.show();
    }

    private void onSignOutConfirm() {
        final Activity activity = getActivity();
        if (activity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        if (mPresenter == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
            return;
        }
        showSignOutLoadingDialog();
        mPresenter.requestSignOut();
    }

    class ViewImpl implements DynamicView {

        public void showSessionUserInfo(@Nullable MSIMUserInfo userInfo) {
            SampleLog.v(Objects.defaultObjectTag(this) + " showSessionUserInfo userInfo:%s", userInfo);
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            long gender = MSIMUikitConstants.Gender.MALE;
            if (userInfo != null) {
                gender = userInfo.getGender(gender);
            }

            clearCheckedChangeListener();
            mBinding.modifyGenderSwitch.setChecked(gender == MSIMUikitConstants.Gender.FEMALE);
            bindCheckedChangeListener();

            mBinding.actionSignOut.setEnabled(MSIMManager.getInstance().hasSession());
        }

        public void onAvatarUploadFail(Throwable e) {
            SampleLog.v(e, Objects.defaultObjectTag(this) + " onAvatarUploadFail");
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            mBinding.avatarProgressView.setProgress(0f);
            TipUtil.show(R.string.imsdk_sample_profile_modify_avatar_fail);
        }

        public void onAvatarUploadProgress(@IntRange(from = 0, to = 100) int percent) {
            SampleLog.v(Objects.defaultObjectTag(this) + " onAvatarUploadProgress percent:%s", percent);
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            mBinding.avatarProgressView.setProgress(percent / 100f);
        }

        public void onAvatarUploadSuccess(String avatarUrl) {
            SampleLog.v(Objects.defaultObjectTag(this) + " onAvatarUploadSuccess avatarUrl:%s", avatarUrl);

            final Activity activity = getActivity();
            if (activity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
                return;
            }

            if (mPresenter == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
                return;
            }
            mPresenter.submitAvatar(avatarUrl);
        }

        public void onAvatarModifyFail(Throwable e) {
            SampleLog.v(e, Objects.defaultObjectTag(this) + " onAvatarModifyFail");
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            TipUtil.show(R.string.imsdk_sample_profile_modify_avatar_fail);
        }

        public void onAvatarModifySuccess() {
            SampleLog.v(Objects.defaultObjectTag(this) + " onAvatarModifySuccess");
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            TipUtil.show(R.string.imsdk_sample_profile_modify_avatar_success);
        }

        public void onNicknameModifyFail(Throwable e) {
            SampleLog.v(e, Objects.defaultObjectTag(this) + " onNicknameModifyFail");
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            TipUtil.show(R.string.imsdk_sample_profile_modify_nickname_fail);
        }

        public void onNicknameModifySuccess() {
            SampleLog.v(Objects.defaultObjectTag(this) + " onNicknameModifySuccess");
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            TipUtil.show(R.string.imsdk_sample_profile_modify_nickname_success);
        }

        public void onGenderModifySuccess() {
            SampleLog.v(Objects.defaultObjectTag(this) + " onGenderModifySuccess");
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            TipUtil.show(R.string.imsdk_sample_tip_action_general_success);
        }

        public void onGenderModifyFail(Throwable e) {
            SampleLog.v(e, Objects.defaultObjectTag(this) + " onGenderModifyFail");
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            if (mPresenter == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
                return;
            }
            TipUtil.show(R.string.imsdk_sample_tip_action_general_fail);
            mPresenter.requestSyncSessionUserInfo();
        }

        public void onSignOutSuccess() {
            SampleLog.v(Objects.defaultObjectTag(this) + " onSignOutSuccess");

            final Activity activity = getActivity();
            if (activity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            MainActivity.start(activity, true);
        }

        public void onSignOutFail(int code, String message) {
            SampleLog.v(Objects.defaultObjectTag(this) + " onSignOutFail code:%s, message:%s", code, message);

            final Activity activity = getActivity();
            if (activity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            TipUtil.showOrDefault(message);

            MainActivity.start(activity, true);
        }

        public void onSignOutFail(Throwable e) {
            SampleLog.v(e, Objects.defaultObjectTag(this) + " onSignOutFail");

            final Activity activity = getActivity();
            if (activity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }
            if (mBinding == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            TipUtil.show(R.string.imsdk_sample_tip_action_general_fail);

            MainActivity.start(activity, true);
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
