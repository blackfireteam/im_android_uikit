package com.masonsoft.imsdk.sample.app.signup;

import com.masonsoft.imsdk.sample.app.signin.SignInViewPresenter;

public abstract class SignUpViewPresenter<T extends SignUpView> extends SignInViewPresenter<T> {

    public SignUpViewPresenter(T view) {
        super(view);
    }

}
