package com.masonsoft.imsdk.sample.app.signup.step1;

import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.api.ApiResponseException;
import com.masonsoft.imsdk.sample.api.DefaultApi;
import com.masonsoft.imsdk.sample.app.signup.SignUpArgument;
import com.masonsoft.imsdk.sample.app.signup.SignUpViewPresenter;
import com.masonsoft.imsdk.user.UserInfoManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SignUpStep1FragmentPresenter extends SignUpViewPresenter<SignUpStep1Fragment.ViewImpl> {

    public SignUpStep1FragmentPresenter(SignUpStep1Fragment.ViewImpl view) {
        super(view);
    }

    public void requestSignUp(final SignUpArgument args) {
        mRequestHolder.set(Single.just("")
                .map(input -> DefaultApi.reg(args.buildRequestArgs()))
                .map(input -> {
                    UserInfoManager.getInstance().updateAvatarAndNickname(
                            args.userId,
                            args.avatar,
                            args.nickname
                    );

                    return input;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(obj -> {
                    final SignUpStep1Fragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }
                    view.onSignUpSuccess(args.userId);
                }, e -> {
                    SampleLog.e(e);
                    final SignUpStep1Fragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    if (e instanceof ApiResponseException) {
                        view.onSignUpFail(((ApiResponseException) e).code, ((ApiResponseException) e).message);
                        return;
                    }
                    view.onSignUpFail(e);
                }));
    }

}
