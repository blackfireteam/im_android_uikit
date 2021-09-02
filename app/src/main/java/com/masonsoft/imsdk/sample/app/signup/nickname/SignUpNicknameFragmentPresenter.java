package com.masonsoft.imsdk.sample.app.signup.nickname;

import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.api.ApiResponseException;
import com.masonsoft.imsdk.sample.api.DefaultApi;
import com.masonsoft.imsdk.sample.app.signup.SignUpViewPresenter;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SignUpNicknameFragmentPresenter extends SignUpViewPresenter<SignUpNicknameFragment.ViewImpl> {

    public SignUpNicknameFragmentPresenter(SignUpNicknameFragment.ViewImpl view) {
        super(view);
    }

    public void requestSignUp(long userId, String nickname) {
        mRequestHolder.set(Single.just("")
                .map(input -> DefaultApi.reg(userId, nickname))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(avatarUrl -> {
                    final SignUpNicknameFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }
                    view.onSignUpSuccess(userId);
                }, e -> {
                    SampleLog.e(e);
                    final SignUpNicknameFragment.ViewImpl view = getView();
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
