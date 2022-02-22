package com.masonsoft.imsdk.sample.app.signup.step2;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.core.FileUploadProvider;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.api.ApiResponseException;
import com.masonsoft.imsdk.sample.api.DefaultApi;
import com.masonsoft.imsdk.sample.app.signup.SignUpArgument;
import com.masonsoft.imsdk.sample.app.signup.SignUpViewPresenter;
import com.masonsoft.imsdk.sample.util.JsonUtil;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.user.UserInfo;
import com.masonsoft.imsdk.user.UserInfoManager;

import io.github.idonans.core.Progress;
import io.github.idonans.core.thread.Threads;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SignUpStep2FragmentPresenter extends SignUpViewPresenter<SignUpStep2Fragment.ViewImpl> {

    public SignUpStep2FragmentPresenter(SignUpStep2Fragment.ViewImpl view) {
        super(view);
    }

    public void requestSignUp(final SignUpArgument args) {
        final SignUpArgument copy = args.copy();
        copy.avatar = "https://msim-test-1252460681.cos.na-siliconvalley.myqcloud.com/pers/612FA7A3-144E-4978-A75C-9D9277167292.jpeg";

        mRequestHolder.set(Single.just("")
                .map(input -> DefaultApi.reg(copy.buildRequestArgs()))
                .map(input -> {
                    final UserInfo userInfo = copy.buildUserInfo(null);
                    UserInfoManager.getInstance().updateManual(copy.userId, userInfo);

                    return input;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(obj -> {
                    final SignUpStep2Fragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }
                    view.onSignUpSuccess(args.userId);

                    // 异步更新用户的头像和 pic
                    Threads.postBackground(() -> updateUserAvatarAndPic(args.userId, args.avatar));
                }, e -> {
                    SampleLog.e(e);
                    final SignUpStep2Fragment.ViewImpl view = getView();
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

    private static void updateUserAvatarAndPic(long userId, String avatarUrl) {
        try {
            Threads.mustNotUi();

            final Progress progress = new Progress() {
                @Override
                protected void onUpdate() {
                    super.onUpdate();

                    MSIMUikitLog.v("updateUserAvatarAndPic userId:%s, avatarUrl:%s, progress percent:[%s/100]", userId, avatarUrl, getPercent());
                }
            };
            final FileUploadProvider fileUploadProvider = MSIMManager.getInstance().getFileUploadProvider();
            final String httpAvatarUrl = fileUploadProvider.uploadFile(avatarUrl, FileUploadProvider.SOURCE_OTHER, null, progress);

            // 先更新本地的 avatar 和 pic
            final UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.uid.set(userId);
            userInfoUpdate.avatar.set(httpAvatarUrl);

            {
                final UserInfo userInfoCache = UserInfoManager.getInstance().getByUserId(userId);
                if (userInfoCache != null) {
                    userInfoUpdate.custom.apply(userInfoCache.custom);
                }
            }

            // 更新 custom 中的 pic 字段
            userInfoUpdate.custom.set(JsonUtil.modifyJsonObject(
                    userInfoUpdate.custom.getOrDefault(null),
                    map -> map.put("pic", httpAvatarUrl)
            ));

            UserInfoManager.getInstance().updateManual(userId, userInfoUpdate);

            // 更新服务器
            DefaultApi.updateAvatar(userId, avatarUrl);
            DefaultApi.updateCustom(userId, userInfoUpdate.custom.get());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
