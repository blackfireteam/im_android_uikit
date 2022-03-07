package com.masonsoft.imsdk.sample.app.signup.step1;

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
import com.masonsoft.imsdk.sample.app.signup.step2.SignUpStep2Activity;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleSignUpStep1FragmentBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleBottomActionsDialog;

import java.util.Arrays;
import java.util.List;

import io.github.idonans.core.FormValidator;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.ToastUtil;
import io.github.idonans.lang.util.ViewUtil;

public class SignUpStep1Fragment extends SignUpFragment {

    public static SignUpStep1Fragment newInstance(@Nullable SignUpArgument signUpArgument) {
        Bundle args = new Bundle();
        if (signUpArgument != null) {
            signUpArgument.writeTo(args);
        }
        SignUpStep1Fragment fragment = new SignUpStep1Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private ImsdkSampleSignUpStep1FragmentBinding mBinding;
    private ViewImpl mView;
    private SignUpStep1FragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkSampleSignUpStep1FragmentBinding.inflate(inflater, container, false);
        FormValidator.bind(
                new FormValidator.InputView[]{
                        FormValidator.InputViewFactory.create(mBinding.editNickname),
                        FormValidator.InputViewFactory.create(mBinding.txtDepartment),
                        FormValidator.InputViewFactory.create(mBinding.txtWorkplace),
                        FormValidator.InputViewFactory.create(mBinding.txtGender),
                        FormValidator.InputViewFactory.create(mBinding.editInvitationCode),
                },
                new FormValidator.SubmitView[]{
                        FormValidator.SubmitViewFactory.create(mBinding.submit),
                }
        );
        mBinding.editInvitationCode.setOnEditorActionListener((v, actionId, event) -> {
            SampleLog.v("onEditorAction actionId:%s, event:%s", actionId, event);
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                onSubmit();
                return true;
            }
            return false;
        });

        final SignUpArgument args = getSignUpArgument();
        mBinding.editNickname.setText(args.nickname);
        mBinding.txtDepartment.setText(args.department);
        mBinding.txtWorkplace.setText(args.workplace);
        mBinding.txtGender.setText(genderToTxt(args.gender));

        ViewUtil.onClick(mBinding.modifyDepartment, v -> startModifyDepartment());
        ViewUtil.onClick(mBinding.modifyWorkplace, v -> startModifyWorkplace());
        ViewUtil.onClick(mBinding.modifyGender, v -> startModifyGender());
        ViewUtil.onClick(mBinding.submit, v -> onSubmit());

        mView = new ViewImpl();
        mPresenter = new SignUpStep1FragmentPresenter(mView);

        return mBinding.getRoot();
    }

    private void startModifyDepartment() {
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

        final List<String> departmentList = Arrays.asList(
                ContextUtil.getContext().getResources().getStringArray(R.array.imsdk_sample_department_list)
        );
        final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                activity,
                departmentList
        );
        dialog.setOnActionClickListener((index, actionText) -> {
            if (mPresenter == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
                return;
            }

            mBinding.txtDepartment.setText(actionText);
        });
        dialog.show();
    }

    private void startModifyWorkplace() {
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

        final List<String> workplaceList = Arrays.asList(
                ContextUtil.getContext().getResources().getStringArray(R.array.imsdk_sample_workplace_list)
        );
        final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                activity,
                workplaceList
        );
        dialog.setOnActionClickListener((index, actionText) -> {
            if (mPresenter == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
                return;
            }

            mBinding.txtWorkplace.setText(actionText);
        });
        dialog.show();
    }

    static long txtToGender(String txt) {
        final String txtGenderMale = I18nResources.getString(R.string.imsdk_sample_gender_male);
        if (txtGenderMale.equals(txt)) {
            return MSIMUikitConstants.Gender.MALE;
        }
        return MSIMUikitConstants.Gender.FEMALE;
    }

    static String genderToTxt(long gender) {
        if (MSIMUikitConstants.Gender.MALE == gender) {
            return I18nResources.getString(R.string.imsdk_sample_gender_male);
        }
        return I18nResources.getString(R.string.imsdk_sample_gender_female);
    }

    private void startModifyGender() {
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

        final List<String> genderList = Arrays.asList(
                ContextUtil.getContext().getResources().getStringArray(R.array.imsdk_sample_gender_list)
        );
        final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                activity,
                genderList
        );
        dialog.setOnActionClickListener((index, actionText) -> {
            if (mPresenter == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
                return;
            }

            mBinding.txtGender.setText(actionText);
        });
        dialog.show();
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

        final String nickname = mBinding.editNickname.getText().toString().trim();
        final int minLength = 3;
        final int maxLength = 20;
        if (TextUtils.isEmpty(nickname)
                || nickname.length() < minLength
                || nickname.length() > maxLength) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_nickname_error));
            return;
        }

        final String department = mBinding.txtDepartment.getText().toString().trim();
        if (TextUtils.isEmpty(department)) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_department_error));
            return;
        }

        final String workplace = mBinding.txtWorkplace.getText().toString().trim();
        if (TextUtils.isEmpty(workplace)) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_workplace_error));
            return;
        }

        final String gender = mBinding.txtGender.getText().toString().trim();
        if (TextUtils.isEmpty(gender)) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_gender_error));
            return;
        }

        final String invitationCode = mBinding.editInvitationCode.getText().toString().trim().toLowerCase();
        if (!"msipo".equals(invitationCode)) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_invitation_code));
            return;
        }

        final SignUpArgument signUpArgument = getSignUpArgument();
        signUpArgument.nickname = nickname;
        signUpArgument.department = department;
        signUpArgument.workplace = workplace;
        signUpArgument.gender = txtToGender(gender);
        saveSignUpArgument();

        SignUpStep2Activity.start(activity, signUpArgument);
    }

    class ViewImpl extends SignUpView {

        @NonNull
        @Override
        public SignUpArgument getSignUpArgument() {
            return SignUpStep1Fragment.this.getSignUpArgument();
        }

        @Nullable
        @Override
        protected Activity getActivity() {
            return SignUpStep1Fragment.this.getActivity();
        }

        @Nullable
        @Override
        protected SignInViewPresenter<?> getPresenter() {
            return SignUpStep1Fragment.this.mPresenter;
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
