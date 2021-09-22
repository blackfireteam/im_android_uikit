package com.masonsoft.imsdk.sample.app.signin;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.sample.LocalSettingsManager;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.api.ApiResponseException;
import com.masonsoft.imsdk.sample.api.DefaultApi;

import io.github.idonans.dynamic.DynamicPresenter;
import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class SignInViewPresenter<T extends SignInView> extends DynamicPresenter<T> {

    protected final DisposableHolder mRequestHolder = new DisposableHolder();

    public SignInViewPresenter(T view) {
        super(view);
    }

    /**
     * 从业务后台请求用于登录 im 的 token
     *
     * @param userId 模拟的业务用户 id
     * @see #requestTcpSignIn(String, String)
     */
    public void requestToken(long userId) {
        mRequestHolder.set(Single.just("")
                .map(input -> DefaultApi.getImToken(userId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(init -> {
                    final SignInView view = getView();
                    if (view == null) {
                        return;
                    }
                    view.onFetchTokenSuccess(init.token, init.url);
                }, e -> {
                    SampleLog.e(e);
                    final SignInView view = getView();
                    if (view == null) {
                        return;
                    }

                    if (e instanceof ApiResponseException) {
                        final int errorCode = ((ApiResponseException) e).code;
                        final String errorMessage = ((ApiResponseException) e).message;
                        view.onFetchTokenFail(userId, errorCode, errorMessage);
                        return;
                    }

                    view.onFetchTokenFail(e, userId);
                }));
    }

    /**
     * 登录 im
     *
     * @param token 用于登录 im 的 token, 由业务后台返回。
     * @param url   登录 im 的地址, 由业务后台返回。
     * @see #requestToken(long)
     */
    public void requestTcpSignIn(String token, String url) {
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final LocalSettingsManager.Settings settings = LocalSettingsManager.getInstance().getSettings();
                    settings.imToken = token;
                    settings.imServer = url;
                    LocalSettingsManager.getInstance().setSettings(settings);
                    return MSIMManager.getInstance().signInWithBlock(settings.imToken, settings.imServer);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    final SignInView view = getView();
                    if (view == null) {
                        return;
                    }
                    final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                    if (sessionUserId > 0) {
                        view.onTcpSignInSuccess();
                    } else {
                        view.onTcpSignInFail(result);
                    }
                }, e -> {
                    SampleLog.e(e);
                    final SignInView view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onTcpSignInFail(e);
                }));
    }

}
