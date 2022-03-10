package com.masonsoft.imsdk.sample.app.mine;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.core.FileUploadProvider;
import com.masonsoft.imsdk.sample.LocalSettingsManager;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.api.DefaultApi;
import com.masonsoft.imsdk.sample.im.DiscoverUserManager;
import com.masonsoft.imsdk.sample.util.JsonUtil;
import com.masonsoft.imsdk.uikit.MSIMSessionUserIdChangedHelper;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.user.UserInfoManager;

import io.github.idonans.core.Progress;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.dynamic.DynamicPresenter;
import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MineFragmentPresenter extends DynamicPresenter<MineFragment.ViewImpl> {

    private final DisposableHolder mRequestHolder = new DisposableHolder();
    private Object mLastUploadAvatarTag;
    private MSIMUserInfoLoader mSessionUserInfoLoader;
    private MSIMSessionUserIdChangedHelper mMSIMSessionUserIdChangedHelper;

    public MineFragmentPresenter(MineFragment.ViewImpl view) {
        super(view);

        mSessionUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userInfo);
                showSessionUserInfo(userInfo);
            }
        };
        mMSIMSessionUserIdChangedHelper = new MSIMSessionUserIdChangedHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                if (mSessionUserInfoLoader != null) {
                    mSessionUserInfoLoader.setUserInfo(MSIMUserInfo.mock(sessionUserId));
                }
            }
        };
    }

    void start() {
        mSessionUserInfoLoader.setUserInfo(MSIMUserInfo.mock(mMSIMSessionUserIdChangedHelper.getSessionUserId()));
    }

    private void showSessionUserInfo(@NonNull MSIMUserInfo userInfo) {
        final MineFragment.ViewImpl view = getView();
        if (view != null) {
            view.showSessionUserInfo(userInfo);
        }
    }

    public void uploadAvatar(Uri photoUri) {
        final Object uploadAvatarTag = new Object();
        mLastUploadAvatarTag = uploadAvatarTag;
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final Progress progress = new Progress() {
                        @Override
                        protected void onUpdate() {
                            super.onUpdate();
                            // notify avatar upload progress
                            Threads.postUi(() -> {
                                if (mLastUploadAvatarTag == uploadAvatarTag) {
                                    final MineFragment.ViewImpl view = getView();
                                    if (view == null) {
                                        return;
                                    }
                                    view.onAvatarUploadProgress(getPercent());
                                }
                            });
                        }
                    };
                    final FileUploadProvider fileUploadProvider = MSIMManager.getInstance().getFileUploadProvider();
                    return fileUploadProvider.uploadFile(photoUri.toString(), FileUploadProvider.SOURCE_OTHER, null, progress);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(avatarUrl -> {
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }
                    view.onAvatarUploadSuccess(avatarUrl);
                }, e -> {
                    SampleLog.e(e);
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onAvatarUploadFail(e);
                }));
    }

    public void submitAvatar(String avatarUrl) {
        mLastUploadAvatarTag = null;
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                    Preconditions.checkArgument(sessionUserId > 0);
                    DefaultApi.updateAvatar(sessionUserId, avatarUrl);
                    UserInfoManager.getInstance().updateAvatar(sessionUserId, avatarUrl);
                    return input;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignore -> {
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onAvatarModifySuccess();
                }, e -> {
                    SampleLog.e(e);
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onAvatarModifyFail(e);
                }));
    }

    public void submitNickname(String nickname) {
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                    Preconditions.checkArgument(sessionUserId > 0);
                    DefaultApi.updateNickname(sessionUserId, nickname);
                    UserInfoManager.getInstance().updateNickname(sessionUserId, nickname);
                    return input;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignore -> {
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onNicknameModifySuccess();
                }, e -> {
                    SampleLog.e(e);
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onNicknameModifyFail(e);
                }));
    }

    public void submitDepartment(String department) {
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                    Preconditions.checkArgument(sessionUserId > 0);

                    final MSIMUserInfo userInfoCache = MSIMManager.getInstance().getUserInfoManager().getUserInfo(sessionUserId);
                    Preconditions.checkNotNull(userInfoCache);

                    final String custom = JsonUtil.modifyJsonObject(
                            userInfoCache.getCustom(),
                            map -> map.put("department", department)
                    );
                    DefaultApi.updateCustom(sessionUserId, custom);

                    MSIMManager.getInstance().getUserInfoManager().insertOrUpdateUserInfo(
                            new MSIMUserInfo.Editor(sessionUserId)
                                    .setCustom(custom)
                    );
                    return input;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignore -> {
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onDepartmentModifySuccess();
                }, e -> {
                    SampleLog.e(e);
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onDepartmentModifyFail(e);
                }));
    }

    public void submitWorkplace(String workplace) {
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                    Preconditions.checkArgument(sessionUserId > 0);

                    final MSIMUserInfo userInfoCache = MSIMManager.getInstance().getUserInfoManager().getUserInfo(sessionUserId);
                    Preconditions.checkNotNull(userInfoCache);

                    final String custom = JsonUtil.modifyJsonObject(
                            userInfoCache.getCustom(),
                            map -> map.put("workplace", workplace)
                    );
                    DefaultApi.updateCustom(sessionUserId, custom);

                    MSIMManager.getInstance().getUserInfoManager().insertOrUpdateUserInfo(
                            new MSIMUserInfo.Editor(sessionUserId)
                                    .setCustom(custom)
                    );
                    return input;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignore -> {
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onWorkplaceModifySuccess();
                }, e -> {
                    SampleLog.e(e);
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onWorkplaceModifyFail(e);
                }));
    }

    public void submitGender(long gender) {
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                    Preconditions.checkArgument(sessionUserId > 0);
                    DefaultApi.updateGender(sessionUserId, gender);
                    UserInfoManager.getInstance().updateGender(sessionUserId, gender);
                    return input;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignore -> {
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onGenderModifySuccess();
                }, e -> {
                    SampleLog.e(e);
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    view.onGenderModifyFail(e);
                }));
    }

    public void requestSignOut() {
        mRequestHolder.set(Single.just("")
                .map(input -> MSIMManager.getInstance().signOutWithBlock(200L))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    // clear settings token
                    LocalSettingsManager.getInstance().clearToken();
                    // clear discover user
                    DiscoverUserManager.getInstance().clearAllAsync();

                    if (result.isSuccess()) {
                        view.onSignOutSuccess();
                    } else {
                        if (result.other != null) {
                            view.onSignOutFail(result.other.code, result.other.message);
                        } else {
                            view.onSignOutFail(result.code, result.message);
                        }
                    }
                }, e -> {
                    SampleLog.e(e);
                    final MineFragment.ViewImpl view = getView();
                    if (view == null) {
                        return;
                    }

                    // clear settings token
                    LocalSettingsManager.getInstance().clearToken();
                    // clear discover user
                    DiscoverUserManager.getInstance().clearAllAsync();

                    view.onSignOutFail(e);
                }));
    }

}
