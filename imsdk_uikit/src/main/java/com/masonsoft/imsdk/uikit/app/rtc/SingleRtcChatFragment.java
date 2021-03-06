package com.masonsoft.imsdk.uikit.app.rtc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Verify;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMRtcMessageManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitSingleRtcChatFragmentBinding;
import com.masonsoft.imsdk.uikit.entity.RtcMessagePayload;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.util.Objects;
import com.tbruyelle.rxpermissions3.RxPermissions;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 实时通话
 */
public class SingleRtcChatFragment extends SystemInsetsFragment {

    public static SingleRtcChatFragment newInstance(long fromUserId, long toUserId, boolean video, String roomId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.KEY_FROM_UID, fromUserId);
        args.putLong(MSIMUikitConstants.ExtrasKey.KEY_TO_UID, toUserId);
        args.putBoolean(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, video);
        args.putString(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, roomId);
        SingleRtcChatFragment fragment = new SingleRtcChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean mReverseVideoLayoutBackground;
    private boolean mMuteAudio;
    private boolean mSpeaker;

    private String mRoomId;
    private long mSessionUserId;
    private long mTargetUserId;
    private long mFromUserId;
    private long mToUserId;
    // true: 视频通话; false: 语音通话
    private boolean mVideo;

    @Nullable
    private ImsdkUikitSingleRtcChatFragmentBinding mBinding;

    @Nullable
    private MSIMRtcMessageManager.RtcEngineWrapper mRtcEngineWrapper;

    private MSIMUserInfoLoader mTargetUserInfoLoader;

    private final DisposableHolder mPermissionRequest = new DisposableHolder();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        final Bundle args = getArguments();
        if (args != null) {
            mFromUserId = args.getLong(MSIMUikitConstants.ExtrasKey.KEY_FROM_UID, mFromUserId);
            mToUserId = args.getLong(MSIMUikitConstants.ExtrasKey.KEY_TO_UID, mToUserId);
            mVideo = args.getBoolean(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, mVideo);
            mRoomId = args.getString(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, mRoomId);

            if (mFromUserId != mToUserId && mFromUserId > 0 && mToUserId > 0) {
                if (mFromUserId == mSessionUserId) {
                    mTargetUserId = mToUserId;
                } else if (mToUserId == mSessionUserId) {
                    mTargetUserId = mFromUserId;
                }
            }
        }

        mTargetUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userInfo);

                if (mBinding != null) {
                    mBinding.userAvatarAudio.setUserInfo(userInfo);
                    mBinding.usernameAudio.setUserInfo(userInfo);
                    mBinding.userAvatarVideo.setUserInfo(userInfo);
                    mBinding.usernameVideo.setUserInfo(userInfo);
                }
            }
        };
        mTargetUserInfoLoader.setUserInfo(MSIMUserInfo.mock(mTargetUserId), false);

        mRtcEngineWrapper = MSIMRtcMessageManager.getInstance().getRtcEngineWrapper(
                mTargetUserId,
                RtcMessagePayload.valueOf(mFromUserId, mRoomId, mVideo),
                false
        );
        if (mRtcEngineWrapper == null) {
            MSIMUikitLog.e("unexpected %s rtcEngineWrapper is null", Objects.defaultObjectTag(this));
            requireActivity().finish();
            return;
        }
        mRtcEngineWrapper.addRtcEventListener(mRtcEventListener);

        blockBackPressed();

        Threads.postUi(this::requestPermission);
    }

    private void requestPermission() {
        final FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (activity.getSupportFragmentManager().isStateSaved()) {
            return;
        }

        final String[] requiredPermissions;
        if (mVideo) {
            requiredPermissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.CAMERA,
            };
        } else {
            requiredPermissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
            };
        }
        final RxPermissions rxPermissions = new RxPermissions(activity);
        mPermissionRequest.set(
                rxPermissions.request(requiredPermissions)
                        .subscribe(granted -> {
                            if (!granted) {
                                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                                TipUtil.show(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        })
        );
    }

    private void blockBackPressed() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // ignore
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkUikitSingleRtcChatFragmentBinding.inflate(inflater, container, false);

        Preconditions.checkNotNull(mTargetUserInfoLoader);
        final MSIMUserInfo targetUserInfo = mTargetUserInfoLoader.getUserInfo();
        Preconditions.checkNotNull(targetUserInfo);
        mBinding.userAvatarAudio.setUserInfo(targetUserInfo);
        mBinding.usernameAudio.setUserInfo(targetUserInfo);
        mBinding.userAvatarVideo.setUserInfo(targetUserInfo);
        mBinding.usernameVideo.setUserInfo(targetUserInfo);

        ViewUtil.onClick(mBinding.actionMutemic, v -> setMuteAudio(!mMuteAudio));
        ViewUtil.onClick(mBinding.actionSpeaker, v -> setSpeaker(!mSpeaker));

        final View.OnClickListener hangupListener = v -> {
            // 挂断电话
            MSIMUikitLog.i("hang up %s", Objects.defaultObjectTag(SingleRtcChatFragment.this));
            if (mRtcEngineWrapper != null) {
                MSIMRtcMessageManager.getInstance().hangupRtc(mFromUserId, mToUserId, null, mRtcEngineWrapper.getRtcMessagePayload());
            }
        };
        ViewUtil.onClick(mBinding.actionHangup, hangupListener);
        ViewUtil.onClick(mBinding.actionReceivedHangup, hangupListener);

        ViewUtil.onClick(mBinding.actionReceivedAccept, v -> {
            // 接听电话
            MSIMUikitLog.i("answer phone %s", Objects.defaultObjectTag(SingleRtcChatFragment.this));
            if (mRtcEngineWrapper != null) {
                MSIMRtcMessageManager.getInstance().acceptRtc(mFromUserId, mToUserId, null, mRtcEngineWrapper.getRtcMessagePayload());
            }
        });

        ViewUtil.onClick(mBinding.videoLayoutForeground, v -> {
            if (mRtcEngineWrapper != null) {
                final int state = mRtcEngineWrapper.getState();

                mReverseVideoLayoutBackground = !mReverseVideoLayoutBackground;
                syncPreviewPanel(state);
            }
        });

        setMuteAudio(mMuteAudio);
        syncRtcState();

        return mBinding.getRoot();
    }

    private void syncActionPanel(int state) {
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        if (mRtcEngineWrapper == null) {
            MSIMUikitLog.e("unexpected %s rtcEngineWrapper is null", Objects.defaultObjectTag(this));
            return;
        }

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_WAIT_ACCEPT) {
            if (mFromUserId == mSessionUserId) {
                // 等待对方接听
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAccept, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAcceptText, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangup, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangupText, View.GONE);

                ViewUtil.setVisibilityIfChanged(mBinding.actionMutemic, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionMutemicText, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionHangup, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionHangupText, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionSpeaker, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionSpeakerText, View.VISIBLE);
            } else {
                // 等待己方接听
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAccept, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAcceptText, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangup, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangupText, View.VISIBLE);

                ViewUtil.setVisibilityIfChanged(mBinding.actionMutemic, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionMutemicText, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionHangup, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionHangupText, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionSpeaker, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionSpeakerText, View.GONE);
            }
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTING
                || state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTED) {
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAccept, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAcceptText, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangup, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangupText, View.GONE);

            ViewUtil.setVisibilityIfChanged(mBinding.actionMutemic, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionMutemicText, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionHangup, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionHangupText, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionSpeaker, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionSpeakerText, View.VISIBLE);
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_DISCONNECTED) {
            if (mRtcEngineWrapper.wasStateConnecting() || mRtcEngineWrapper.wasStateConnected()) {
                // 曾经链接中或者链接成功
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAccept, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAcceptText, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangup, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangupText, View.GONE);

                ViewUtil.setVisibilityIfChanged(mBinding.actionMutemic, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionMutemicText, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionHangup, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionHangupText, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionSpeaker, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.actionSpeakerText, View.VISIBLE);
            } else {
                // 尚未链接中或者链接成功，直接结束通话
                if (mFromUserId == mSessionUserId) {
                    // 等待对方接听 -> 直接结束通话
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAccept, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAcceptText, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangup, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangupText, View.GONE);

                    ViewUtil.setVisibilityIfChanged(mBinding.actionMutemic, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionMutemicText, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionHangup, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionHangupText, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionSpeaker, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionSpeakerText, View.VISIBLE);
                } else {
                    // 等待己方接听 -> 直接结束通话
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAccept, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAcceptText, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangup, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangupText, View.VISIBLE);

                    ViewUtil.setVisibilityIfChanged(mBinding.actionMutemic, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionMutemicText, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionHangup, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionHangupText, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionSpeaker, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.actionSpeakerText, View.GONE);
                }
            }
        } else {
            // default hide all
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAccept, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedAcceptText, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangup, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionReceivedHangupText, View.GONE);

            ViewUtil.setVisibilityIfChanged(mBinding.actionMutemic, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionMutemicText, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionHangup, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionHangupText, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionSpeaker, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.actionSpeakerText, View.GONE);
        }
    }

    private void syncAvatarAndHintPanel(int state, int disconnectedReason) {
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        if (mRtcEngineWrapper == null) {
            MSIMUikitLog.e("unexpected %s rtcEngineWrapper is null", Objects.defaultObjectTag(this));
            return;
        }

        mBinding.rtcStateHintTextAudio.setRtcState(state, disconnectedReason, mVideo, mFromUserId, mToUserId, mSessionUserId);
        mBinding.rtcStateHintTextVideo.setRtcState(state, disconnectedReason, mVideo, mFromUserId, mToUserId, mSessionUserId);
        mBinding.rtcStateHintText.setRtcState(state, disconnectedReason, mVideo, mFromUserId, mToUserId, mSessionUserId);

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_WAIT_ACCEPT
                || state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTING) {
            if (mVideo) {
                ViewUtil.setVisibilityIfChanged(mBinding.userAvatarVideo, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.usernameVideo, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextVideo, View.VISIBLE);

                ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);
            } else {
                ViewUtil.setVisibilityIfChanged(mBinding.userAvatarVideo, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.usernameVideo, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextVideo, View.GONE);

                ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.VISIBLE);
            }

            ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintText, View.GONE);
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTED) {
            ViewUtil.setVisibilityIfChanged(mBinding.userAvatarVideo, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.usernameVideo, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextVideo, View.GONE);

            if (mVideo) {
                ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);
            } else {
                ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);
            }

            ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintText, View.VISIBLE);
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_DISCONNECTED) {
            if (mRtcEngineWrapper.wasStateConnected()) {
                // 成功通话一段时间后结束通话
                ViewUtil.setVisibilityIfChanged(mBinding.userAvatarVideo, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.usernameVideo, View.GONE);
                ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextVideo, View.GONE);
                if (mVideo) {
                    ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);
                } else {
                    ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);
                }
            } else {
                // 尚未链接成功，直接结束通话
                if (mVideo) {
                    ViewUtil.setVisibilityIfChanged(mBinding.userAvatarVideo, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.usernameVideo, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextVideo, View.GONE);

                    ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);
                } else {
                    ViewUtil.setVisibilityIfChanged(mBinding.userAvatarVideo, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.usernameVideo, View.GONE);
                    ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextVideo, View.GONE);

                    ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.VISIBLE);
                    ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);
                }
            }
            ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintText, View.VISIBLE);
        } else {
            // default
            ViewUtil.setVisibilityIfChanged(mBinding.userAvatarVideo, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.usernameVideo, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextVideo, View.GONE);

            ViewUtil.setVisibilityIfChanged(mBinding.userAvatarAudio, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.usernameAudio, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintTextAudio, View.GONE);

            ViewUtil.setVisibilityIfChanged(mBinding.rtcStateHintText, View.GONE);
        }
    }

    private FrameLayout pickVideoLayoutBackground() {
        Verify.verifyNotNull(mBinding);
        if (mReverseVideoLayoutBackground) {
            return mBinding.videoLayoutForeground;
        } else {
            return mBinding.videoLayoutBackground;
        }
    }

    private FrameLayout pickVideoLayoutForeground() {
        Verify.verifyNotNull(mBinding);
        if (mReverseVideoLayoutBackground) {
            return mBinding.videoLayoutBackground;
        } else {
            return mBinding.videoLayoutForeground;
        }
    }

    private void syncPreviewPanel(int state) {
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        if (mRtcEngineWrapper == null) {
            ViewUtil.setVisibilityIfChanged(pickVideoLayoutBackground(), View.GONE);
            ViewUtil.setVisibilityIfChanged(pickVideoLayoutForeground(), View.GONE);
            return;
        }

        if (!mVideo) {
            ViewUtil.setVisibilityIfChanged(pickVideoLayoutBackground(), View.GONE);
            ViewUtil.setVisibilityIfChanged(pickVideoLayoutForeground(), View.GONE);
            return;
        }

        final Activity context = getActivity();
        if (context == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_WAIT_ACCEPT
                || state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTING) {
            //noinspection IfStatementWithIdenticalBranches
            if (mFromUserId == mSessionUserId) {
                // 显示自己的 preview
                final RtcEngine rtcEngine = mRtcEngineWrapper.getRtcEngine();
                if (rtcEngine != null) {
                    bindTextureView(pickVideoLayoutBackground(), context, rtcEngine, 0, false);
                } else {
                    MSIMUikitLog.e("%s unexpected rtcEngine is null", Objects.defaultObjectTag(this));
                }
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutBackground(), View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutForeground(), View.GONE);
            } else {
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutBackground(), View.GONE);
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutForeground(), View.GONE);
            }
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTED) {
            final RtcEngine rtcEngine = mRtcEngineWrapper.getRtcEngine();
            if (rtcEngine != null) {
                final int userIdJoined = mRtcEngineWrapper.getUserIdJoined();
                bindTextureView(pickVideoLayoutBackground(), context, rtcEngine, userIdJoined, true);
                bindTextureView(pickVideoLayoutForeground(), context, rtcEngine, 0, false);
            } else {
                MSIMUikitLog.e("%s unexpected rtcEngine is null", Objects.defaultObjectTag(this));
            }

            ViewUtil.setVisibilityIfChanged(pickVideoLayoutBackground(), View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(pickVideoLayoutForeground(), View.VISIBLE);
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_DISCONNECTED) {
            if (mRtcEngineWrapper.wasStateConnected()) {
                // 成功通话一段时间后结束通话
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutBackground(), View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutForeground(), View.VISIBLE);
            } else {
                // 尚未链接成功，直接结束通话
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutBackground(), View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(pickVideoLayoutForeground(), View.GONE);
            }
        }
    }

    private void bindTextureView(@NonNull FrameLayout parentView,
                                 @NonNull Context context,
                                 @NonNull RtcEngine rtcEngine,
                                 int uid,
                                 boolean remote) {
        TextureViewBinderObject binderObject = TextureViewBinderObject.from(parentView);
        if (binderObject != null) {
            if (binderObject.match(rtcEngine, uid, remote)) {
                return;
            }
        }

        binderObject = new TextureViewBinderObject(rtcEngine, uid, remote);
        binderObject.bindTo(parentView, context);
    }


    private static class TextureViewBinderObject {
        private final RtcEngine mRtcEngine;
        private final int mUid;
        private final boolean mRemote;

        private TextureViewBinderObject(RtcEngine rtcEngine, int uid, boolean remote) {
            mRtcEngine = rtcEngine;
            mUid = uid;
            mRemote = remote;
        }

        private boolean match(RtcEngine rtcEngine, int uid, boolean remote) {
            return this.mRtcEngine == rtcEngine && this.mUid == uid && mRemote == remote;
        }

        private void bindTo(@NonNull FrameLayout parentView, @NonNull Context context) {
            parentView.removeAllViews();
            final TextureView textureView = RtcEngine.CreateTextureView(context);
            if (textureView != null) {
                parentView.addView(textureView);

                mRtcEngine.enableVideo();
                if (mRemote) {
                    mRtcEngine.setupRemoteVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, mUid));
                } else {
                    mRtcEngine.setupLocalVideo(new VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, mUid));
                }
                final int startPreviewCode = mRtcEngine.startPreview();
                if (startPreviewCode != 0) {
                    MSIMUikitLog.e("unexpected startPreview code:%s %s", startPreviewCode, RtcEngine.getErrorDescription(startPreviewCode));
                }
            } else {
                MSIMUikitLog.e("fail to create texture view");
            }
            parentView.setTag(R.id.imsdk_uikit_rtc_texture_view_binder_object_tag, this);
        }

        @Nullable
        private static TextureViewBinderObject from(@NonNull FrameLayout parentView) {
            return (TextureViewBinderObject) parentView.getTag(R.id.imsdk_uikit_rtc_texture_view_binder_object_tag);
        }
    }

    private int mLastState = Integer.MIN_VALUE;

    private void syncRtcState() {
        if (mRtcEngineWrapper == null) {
            return;
        }
        final int state = mRtcEngineWrapper.getState();
        if (mLastState == state) {
            // ignore duplicate state
            return;
        }
        mLastState = state;
        final int disconnectedReason = mRtcEngineWrapper.getDisconnectedReason();
        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_DISCONNECTED) {
            MSIMUikitLog.i("%s syncRtcState state:%s, reason:%s",
                    Objects.defaultObjectTag(this),
                    MSIMRtcMessageManager.RtcEngineWrapper.stateToString(state),
                    MSIMRtcMessageManager.RtcEngineWrapper.disconnectedReasonToString(disconnectedReason));
        } else {
            MSIMUikitLog.i("%s syncRtcState state:%s",
                    Objects.defaultObjectTag(this),
                    MSIMRtcMessageManager.RtcEngineWrapper.stateToString(state));
        }

        syncConnectedDuration();
        syncActionPanel(state);
        syncAvatarAndHintPanel(state, disconnectedReason);
        syncPreviewPanel(state);

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_WAIT_ACCEPT) {
            final boolean in = mFromUserId != mSessionUserId;
            if (in) {
                // 来电使用扬声器
                setSpeaker(true);

                mRtcEngineWrapper.startVibrator();
                mRtcEngineWrapper.startRingtone(true);
            } else {
                // 打视频电话使用扬声器
                setSpeaker(mVideo);

                mRtcEngineWrapper.stopVibrator();
                mRtcEngineWrapper.startRingtone(false);
            }
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTING) {
            mRtcEngineWrapper.stopVibrator();
            mRtcEngineWrapper.stopRingtone();
            mRtcEngineWrapper.joinChannel();
        } else if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_DISCONNECTED) {
            mRtcEngineWrapper.stopVibrator();
            mRtcEngineWrapper.stopRingtone();
            closeRtcEngineWrapper();

            // 延迟关闭界面
            Threads.postUi(() -> {
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }, 1000L);
        }
    }

    private void syncConnectedDuration() {
        if (mRtcEngineWrapper == null) {
            return;
        }
        final int state = mRtcEngineWrapper.getState();
        if (mBinding != null) {
            mBinding.connectedTimeDuration.setText(mRtcEngineWrapper.getFormatStateConnectedDurationTimeMs());
            if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTED) {
                mBinding.connectedTimeDuration.postDelayed(mSyncConnectedDurationAction, 300L);
            } else {
                mBinding.connectedTimeDuration.removeCallbacks(mSyncConnectedDurationAction);
            }
        }
    }

    private final Runnable mSyncConnectedDurationAction = this::syncConnectedDuration;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPermissionRequest.clear();
        IOUtil.closeQuietly(mTargetUserInfoLoader);
        mTargetUserInfoLoader = null;

        if (mRtcEngineWrapper != null) {
            mRtcEngineWrapper.stopVibrator();
            mRtcEngineWrapper.stopRingtone();
        }
        closeRtcEngineWrapper();
    }

    private void setMuteAudio(boolean muteAudio) {
        mMuteAudio = muteAudio;
        if (mRtcEngineWrapper != null) {
            mRtcEngineWrapper.setMuteAudio(mMuteAudio);
        }
        if (mBinding != null) {
            if (mBinding.actionMutemic.isSelected() != mMuteAudio) {
                mBinding.actionMutemic.setSelected(mMuteAudio);
            }
            if (mMuteAudio) {
                mBinding.actionMutemicText.setText(R.string.imsdk_uikit_rtc_microphone_off);
            } else {
                mBinding.actionMutemicText.setText(R.string.imsdk_uikit_rtc_microphone_on);
            }
        }
    }

    private void setSpeaker(boolean speaker) {
        mSpeaker = speaker;
        if (mRtcEngineWrapper != null) {
            mRtcEngineWrapper.setSpeaker(mSpeaker);
        }
        if (mBinding != null) {
            if (mBinding.actionSpeaker.isSelected() != mSpeaker) {
                mBinding.actionSpeaker.setSelected(mSpeaker);
            }
            if (mSpeaker) {
                mBinding.actionSpeakerText.setText(R.string.imsdk_uikit_rtc_speaker_on);
            } else {
                mBinding.actionSpeakerText.setText(R.string.imsdk_uikit_rtc_speaker_off);
            }
        }
    }

    private void closeRtcEngineWrapper() {
        if (mRtcEngineWrapper != null) {
            mRtcEngineWrapper.removeRtcEventListener(mRtcEventListener);
            IOUtil.closeQuietly(mRtcEngineWrapper);
            mRtcEngineWrapper = null;
        }
    }

    private final MSIMRtcMessageManager.RtcEngineWrapper.RtcEventListener mRtcEventListener = new MSIMRtcMessageManager.RtcEngineWrapper.RtcEventListener() {
        @Override
        public void onStateChanged(int state) {
            MSIMUikitLog.i("onStateChanged %s", MSIMRtcMessageManager.RtcEngineWrapper.stateToString(state));
            Threads.postUi(SingleRtcChatFragment.this::syncRtcState);
        }
    };

}
