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

import io.github.idonans.core.FormValidator;
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
                        FormValidator.InputViewFactory.create(mBinding.editInvitationCode),
                },
                new FormValidator.SubmitView[]{
                        FormValidator.SubmitViewFactory.create(mBinding.submit),
                }
        );
        mBinding.editInvitationCode.setOnEditorActionListener((v, actionId, event) -> {
            SampleLog.v("onEditorAction actionId:%s, event:%s", actionId, event);
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onSubmit();
                return true;
            }
            return false;
        });

        final SignUpArgument args = getSignUpArgument();
        mBinding.editNickname.setText(args.nickname);
        if (args.gender == MSIMUikitConstants.Gender.MALE) {
            mBinding.editGenderMale.setChecked(true);
        } else {
            mBinding.editGenderFemale.setChecked(true);
        }

        ViewUtil.onClick(mBinding.submit, v -> onSubmit());

        mView = new ViewImpl();
        mPresenter = new SignUpStep1FragmentPresenter(mView);

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

        final String nickname = mBinding.editNickname.getText().toString().trim();
        final int minLength = 3;
        final int maxLength = 20;
        if (TextUtils.isEmpty(nickname)
                || nickname.length() < minLength
                || nickname.length() > maxLength) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_nickname_error));
            return;
        }

        final String invitationCode = mBinding.editInvitationCode.getText().toString().trim().toLowerCase();
        if (!"msipo".equals(invitationCode)) {
            ToastUtil.show(I18nResources.getString(R.string.imsdk_sample_input_error_invitation_code));
            return;
        }

        final String department = mBinding.editDepartment.getSelectedItem().toString();
        final String workplace = mBinding.editWorkplace.getSelectedItem().toString();
        final long gender;
        if (mBinding.editGenderMale.isChecked()) {
            gender = MSIMUikitConstants.Gender.MALE;
        } else {
            gender = MSIMUikitConstants.Gender.FEMALE;
        }

        final SignUpArgument signUpArgument = getSignUpArgument();
        signUpArgument.nickname = nickname;
        signUpArgument.department = department;
        signUpArgument.workplace = workplace;
        signUpArgument.gender = gender;
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
