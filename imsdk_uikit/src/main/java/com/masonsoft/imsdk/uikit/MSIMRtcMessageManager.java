package com.masonsoft.imsdk.uikit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.common.base.Verify;
import com.masonsoft.imsdk.MSIMCallback;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageFactory;
import com.masonsoft.imsdk.MSIMSdkListener;
import com.masonsoft.imsdk.MSIMSdkListenerAdapter;
import com.masonsoft.imsdk.common.TopActivity;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.core.KeyValueStorage;
import com.masonsoft.imsdk.core.OtherMessage;
import com.masonsoft.imsdk.core.OtherMessageManager;
import com.masonsoft.imsdk.core.SignGenerator;
import com.masonsoft.imsdk.core.observable.OtherMessageObservable;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.lang.SafetyRunnable;
import com.masonsoft.imsdk.uikit.app.rtc.SingleRtcChatActivity;
import com.masonsoft.imsdk.uikit.entity.AgoraTokenInfo;
import com.masonsoft.imsdk.uikit.entity.CustomMessagePayload;
import com.masonsoft.imsdk.uikit.entity.RtcMessagePayload;
import com.masonsoft.imsdk.uikit.message.packet.GetAgoraTokenMessagePacket;
import com.masonsoft.imsdk.uikit.widget.IMReceivedRtcMessageViewHelper;
import com.masonsoft.imsdk.util.Objects;
import com.masonsoft.imsdk.util.WeakObservable;

import java.io.Closeable;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.ChannelMediaOptions;
import io.github.idonans.core.Singleton;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.PermissionUtil;
import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;

public class MSIMRtcMessageManager {

    private static final Singleton<MSIMRtcMessageManager> INSTANCE = new Singleton<MSIMRtcMessageManager>() {
        @Override
        protected MSIMRtcMessageManager create() {
            return new MSIMRtcMessageManager();
        }
    };

    public static MSIMRtcMessageManager getInstance() {
        return INSTANCE.get();
    }

    private final IMReceivedRtcMessageViewHelper mReceivedRtcMessageViewHelper;
    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMSessionUserIdChangedHelper mMSIMSessionUserIdChangedHelper;

    private static final String KEY_AGORA_APP_ID = "key:agoraAppId_20210817_t2mmmdc";
    @Nullable
    private String mAgoraAppId;
    // 记录上一次接受 CALL 的 roomId(用于忽略冗余 CALL)
    private String mLastAcceptCallRoomId;

    @Nullable
    private RtcEngineWrapper mRtcEngineWrapper;

    private MSIMRtcMessageManager() {
        mReceivedRtcMessageViewHelper = new IMReceivedRtcMessageViewHelper() {
            @Override
            protected void onReceivedRtcMessage(@NonNull MSIMMessage message, @NonNull CustomMessagePayload customMessagePayload, @NonNull RtcMessagePayload rtcMessagePayload) {
                MSIMRtcMessageManager.this.onReceivedRtcMessage(message, customMessagePayload, rtcMessagePayload);
            }
        };

        mMSIMSessionUserIdChangedHelper = new MSIMSessionUserIdChangedHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                mReceivedRtcMessageViewHelper.setTarget(
                        sessionUserId,
                        MSIMConstants.ID_ANY
                );

                fetchAgoraAppIdAsync();
            }
        };
        mReceivedRtcMessageViewHelper.setTarget(
                mMSIMSessionUserIdChangedHelper.getSessionUserId(),
                MSIMConstants.ID_ANY
        );

        MSIMManager.getInstance().addSdkListener(mSdkListener);

        restoreAgoraAppIdAsync();
        fetchAgoraAppIdAsync();
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMSdkListener mSdkListener = new MSIMSdkListenerAdapter() {
        @Override
        public void onSignInSuccess() {
            super.onSignInSuccess();

            fetchAgoraAppIdAsync();
        }
    };

    private void setAgoraAppId(@Nullable String agoraAppId) {
        MSIMUikitLog.i("set agora app id: %s", agoraAppId);
        mAgoraAppId = agoraAppId;
        saveAgoraAppIdAsync();
    }

    private static final long TIMEOUT_MS = 20 * 1000L;
    private Object mFetchAgoraAppIdTag = new Object();

    private void fetchAgoraAppIdAsync() {
        final Object fetchAgoraAppIdTag = new Object();
        mFetchAgoraAppIdTag = fetchAgoraAppIdTag;
        Threads.postBackground(new SafetyRunnable(() -> {
            if (mFetchAgoraAppIdTag != fetchAgoraAppIdTag) {
                return;
            }

            final AgoraTokenInfo agoraTokenInfo = fetchAgoraTokenInfoQuietly("AndroidFetchAgoraAppId");

            if (mFetchAgoraAppIdTag != fetchAgoraAppIdTag) {
                return;
            }
            if (agoraTokenInfo != null) {
                setAgoraAppId(agoraTokenInfo.appId.get());
            }
        }));
    }

    @Nullable
    private AgoraTokenInfo fetchAgoraTokenInfoQuietly(final String roomId) {
        try {
            return fetchAgoraTokenInfo(roomId);
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
        }
        return null;
    }

    @Nullable
    private AgoraTokenInfo fetchAgoraTokenInfo(final String roomId) {
        final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
        if (sessionUserId <= 0) {
            IMLog.v("fast return: session user id is invalid: %s", sessionUserId);
            return null;
        }

        final SingleSubject<ObjectWrapper> subject = SingleSubject.create();
        final long originSign = SignGenerator.nextSign();
        final GetAgoraTokenMessagePacket messagePacket = GetAgoraTokenMessagePacket.create(originSign, roomId);
        final OtherMessage otherMessage = new OtherMessage(sessionUserId, messagePacket);
        final OtherMessageObservable.OtherMessageObserver otherMessageObserver = new OtherMessageObservable.OtherMessageObserver() {
            @Override
            public void onOtherMessageLoading(long sign, @NonNull OtherMessage otherMessage) {
            }

            @Override
            public void onOtherMessageSuccess(long sign, @NonNull OtherMessage otherMessage) {
                if (originSign != sign) {
                    return;
                }

                final AgoraTokenInfo agoraTokenInfo = messagePacket.getAgoraTokenInfo();
                subject.onSuccess(new ObjectWrapper(agoraTokenInfo));
            }

            @Override
            public void onOtherMessageError(long sign, @NonNull OtherMessage otherMessage, int errorCode, String errorMessage) {
                if (originSign != sign) {
                    return;
                }

                subject.onError(new IllegalArgumentException("errorCode:" + errorCode + ", errorMessage:" + errorMessage));
            }
        };
        OtherMessageObservable.DEFAULT.registerObserver(otherMessageObserver);
        OtherMessageManager.getInstance().enqueueOtherMessage(sessionUserId, originSign, otherMessage);
        final ObjectWrapper result = subject.timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).blockingGet();
        return (AgoraTokenInfo) result.getObject();
    }

    private void restoreAgoraAppIdAsync() {
        Threads.postBackground(() -> {
            final String agoraAppId = KeyValueStorage.get(KEY_AGORA_APP_ID);
            if (!TextUtils.isEmpty(agoraAppId)) {
                mAgoraAppId = agoraAppId;
            }
        });
    }

    private void saveAgoraAppIdAsync() {
        Threads.postBackground(() -> {
            KeyValueStorage.set(KEY_AGORA_APP_ID, mAgoraAppId);
        });
    }

    public void start() {
    }

    /**
     * 收到对方发来的 rtc 通话信令消息
     */
    public void onReceivedRtcMessage(@NonNull MSIMMessage message, @NonNull CustomMessagePayload customMessagePayload, @NonNull RtcMessagePayload rtcMessagePayload) {
        final long sessionUserId = message.getSessionUserId();
        final long targetUserId = message.getTargetUserId();
        final long fromUserId = message.getFromUserId();
        final long toUserId = message.getToUserId();
        MSIMUikitLog.v("onReceivedRtcMessage sessionUserId:%s, targetUserId:%s, fromUserId:%s, toUserId:%s, rtcMessagePayload:%s",
                sessionUserId, targetUserId, fromUserId, toUserId, rtcMessagePayload);
        if (fromUserId != targetUserId && toUserId != sessionUserId) {
            // 校验需要是对方发给当前用户的消息
            MSIMUikitLog.e("unexpected onReceivedRtcMessage fromUserId or toUserId");
            return;
        }
        if (!rtcMessagePayload.isVideoType() && !rtcMessagePayload.isAudioType()) {
            MSIMUikitLog.e("unexpected rtcMessagePayload type");
            return;
        }
        if (rtcMessagePayload.roomId.isUnset()) {
            MSIMUikitLog.e("unexpected rtcMessagePayload roomId required");
            return;
        }
        if (rtcMessagePayload.event.isUnset()) {
            MSIMUikitLog.e("unexpected rtcMessagePayload event required");
            return;
        }

        final int event = rtcMessagePayload.event.get();
        if (event == RtcMessagePayload.Event.CALL) {
            // 过滤需要忽略的情况
            if (mLastAcceptCallRoomId != null && mLastAcceptCallRoomId.equals(rtcMessagePayload.roomId.get())) {
                // 刚刚已经接受了该 RoomId 的 CALL（此时可能已经挂断了或者通话中或者其它状态），
                // 对方的循环 CALL 可能延迟到达，校验是否需要忽略这个 CALL。

                final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
                if (rtcEngineWrapper == null || rtcEngineWrapper.getState() >= RtcEngineWrapper.STATE_DISCONNECTED) {
                    // 当前通话已经结束，忽略这个 CALL
                    return;
                }
            }
        }

        if (abortIfBusy(sessionUserId, targetUserId, fromUserId, toUserId, rtcMessagePayload)) {
            return;
        }

        if (event == RtcMessagePayload.Event.CALL) {
            // 收到通话邀请(可能会连续收到同一个通话的多次 CALL)
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, true);
            if (rtcEngineWrapper != null) {
                // 记录下这次通话的 roomId (每一次通话的 roomId 不同), 如果再次收到这个 roomId 的 CALL 并且当前通话已经结束则忽略
                mLastAcceptCallRoomId = rtcMessagePayload.roomId.get();

                rtcEngineWrapper.setState(RtcEngineWrapper.STATE_WAIT_ACCEPT);
                showRtcView(fromUserId, toUserId, rtcMessagePayload.isVideoType(), rtcMessagePayload.roomId.get());
            }
            return;
        }

        if (event == RtcMessagePayload.Event.ACCEPT) {
            // 对方接受了邀请
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setState(RtcEngineWrapper.STATE_CONNECTING);
            }
            return;
        }

        if (event == RtcMessagePayload.Event.CANCEL) {
            // 对方取消了通话
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_CANCEL_TARGET);
            }
            return;
        }

        if (event == RtcMessagePayload.Event.REJECT) {
            // 对方拒绝了通话
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_REJECT_TARGET);
            }
            return;
        }

        if (event == RtcMessagePayload.Event.END) {
            // 对方挂断了通话
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_HANGUP_TARGET);
            }
            return;
        }

        if (event == RtcMessagePayload.Event.LINEBUSY) {
            // 对方正忙
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_LINEBUSY_TARGET);
            }
            return;
        }

        if (event == RtcMessagePayload.Event.TIMEOUT) {
            // 己方超时未响应
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_TIMEOUT_MYSELF);
            }
            return;
        }

        if (event == RtcMessagePayload.Event.ERROR) {
            // 对方系统错误
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_ERROR_TARGET);
            }
            return;
        }

        if (event == RtcMessagePayload.Event.UNKNOWN) {
            // 对方未知错误
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper != null) {
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_UNKNOWN_TARGET);
            }
            return;
        }

        MSIMUikitLog.e("unexpected event:%s %s", event, RtcMessagePayload.Event.eventToString(event));
    }

    private void showRtcView(long fromUserId, long toUserId, boolean video, String roomId) {
        Activity activity = TopActivity.getInstance().getResumed();
        if (activity == null) {
            MSIMUikitLog.v(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        if (!(activity instanceof AppCompatActivity)) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NOT_APP_COMPAT_ACTIVITY);
            return;
        }

        if (activity instanceof SingleRtcChatActivity) {
            MSIMUikitLog.i("ignore showRtcView. top activity is already SingleRtcChatActivity");
            return;
        }

        SingleRtcChatActivity.start(activity, fromUserId, toUserId, video, roomId);
    }

    /**
     * 如果当前正在通话中，则直接拒绝新的通话
     */
    private boolean abortIfBusy(long sessionUserId, long targetUserId, long fromUserId, long toUserId, @NonNull RtcMessagePayload rtcMessagePayload) {
        if (rtcMessagePayload.event.get() == RtcMessagePayload.Event.CALL) {
            // 新的通话
            final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
            if (rtcEngineWrapper == null && mRtcEngineWrapper != null && mRtcEngineWrapper.mRtcEngine != null) {
                // 当前正在进行其它通话, 回复对方正忙
                final String body = CustomIMMessageFactory.createCustomRtcMessage(
                        rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.LINEBUSY)
                );
                final MSIMMessage message = MSIMMessageFactory.createCustomSignalingMessage(body, true);
                MSIMManager.getInstance().getMessageManager().sendCustomSignaling(sessionUserId, targetUserId, message);
                return true;
            }
        }
        return false;
    }

    /**
     * 发起 rtc 通话
     *
     * @param video true: 视频通话，false: 语音通话
     */
    public void startRtcMessage(long targetUserId, @Nullable MSIMCallback<GeneralResult> callback, boolean video) {
        final long sessionUserId = mMSIMSessionUserIdChangedHelper.getSessionUserId();

        if (mRtcEngineWrapper != null && mRtcEngineWrapper.mRtcEngine != null) {
            // 当前正在通话中
            if (callback != null) {
                callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_INVALID_MESSAGE_SEND_STATUS));
            }
            return;
        }

        final RtcMessagePayload rtcMessagePayload = RtcMessagePayload.valueOf(sessionUserId, null, video);
        rtcMessagePayload.event.set(RtcMessagePayload.Event.CALL);

        final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, true);
        if (rtcEngineWrapper != null) {
            rtcEngineWrapper.bindLocalRtcEventListener(state -> {
                // 通话发起方在通话结束后补发关于此次通话的会话消息
                if (state == RtcEngineWrapper.STATE_DISCONNECTED) {
                    final int disconnectedReason = rtcEngineWrapper.getDisconnectedReason();
                    if (disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_LINEBUSY_TARGET) {
                        // 对方正忙
                        final String body = CustomIMMessageFactory.createCustomRtcMessage(
                                rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.LINEBUSY)
                        );
                        MSIMManager.getInstance().getMessageManager().sendMessage(
                                sessionUserId,
                                MSIMMessageFactory.createCustomMessage(body, true, false, true),
                                targetUserId
                        );
                    } else if (disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_TIMEOUT_TARGET) {
                        // 对方超时未接听
                        final String body = CustomIMMessageFactory.createCustomRtcMessage(
                                rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.TIMEOUT)
                        );
                        MSIMManager.getInstance().getMessageManager().sendMessage(
                                sessionUserId,
                                MSIMMessageFactory.createCustomMessage(body, true, false, true),
                                targetUserId
                        );
                    } else if (disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_CANCEL_MYSELF) {
                        // 己方取消了通话
                        final String body = CustomIMMessageFactory.createCustomRtcMessage(
                                rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.CANCEL)
                        );
                        MSIMManager.getInstance().getMessageManager().sendMessage(
                                sessionUserId,
                                MSIMMessageFactory.createCustomMessage(body, true, false, true),
                                targetUserId
                        );
                    } else if (disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_REJECT_TARGET) {
                        // 对方拒绝了通话
                        final String body = CustomIMMessageFactory.createCustomRtcMessage(
                                rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.REJECT)
                        );
                        MSIMManager.getInstance().getMessageManager().sendMessage(
                                sessionUserId,
                                MSIMMessageFactory.createCustomMessage(body, true, false, true),
                                targetUserId
                        );
                    } else if (disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_ERROR_TARGET
                            || disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_ERROR_MYSELF) {
                        // 己方或者对方发生系统错误
                        final String body = CustomIMMessageFactory.createCustomRtcMessage(
                                rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.ERROR)
                        );
                        MSIMManager.getInstance().getMessageManager().sendMessage(
                                sessionUserId,
                                MSIMMessageFactory.createCustomMessage(body, true, false, true),
                                targetUserId
                        );
                    } else if (disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_UNKNOWN_TARGET
                            || disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_UNKNOWN_MYSELF) {
                        // 己方或者对方发生未知错误
                        final String body = CustomIMMessageFactory.createCustomRtcMessage(
                                rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.UNKNOWN)
                        );
                        MSIMManager.getInstance().getMessageManager().sendMessage(
                                sessionUserId,
                                MSIMMessageFactory.createCustomMessage(body, true, false, true),
                                targetUserId
                        );
                    } else if (disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_HANGUP_TARGET
                            || disconnectedReason == RtcEngineWrapper.DISCONNECTED_REASON_HANGUP_MYSELF) {
                        // 己方或者对方正常挂断电话
                        long durationMs = rtcEngineWrapper.getStateConnectedDurationTimeMs();

                        // 通话时长(不足 1 秒时，记为 1 秒)
                        long duration = durationMs / 1000;
                        if (duration < 1) {
                            duration = 1;
                        }
                        final RtcMessagePayload copyPayload = rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.END);
                        copyPayload.duration.set(duration);
                        final String body = CustomIMMessageFactory.createCustomRtcMessage(
                                copyPayload
                        );
                        MSIMManager.getInstance().getMessageManager().sendMessage(
                                sessionUserId,
                                MSIMMessageFactory.createCustomMessage(body, true, false, true),
                                targetUserId
                        );
                    } else {
                        MSIMUikitLog.i("ignore. unexpected disconnectedReason:%s",
                                RtcEngineWrapper.disconnectedReasonToString(disconnectedReason));
                    }
                }
            });
            rtcEngineWrapper.setState(RtcEngineWrapper.STATE_WAIT_ACCEPT);
            sendRtcEventSignalingMessage(targetUserId, callback, rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.CALL));
            rtcEngineWrapper.startTimeoutCountDown(() -> timeoutRtc(sessionUserId, targetUserId, callback, rtcMessagePayload));

            showRtcView(sessionUserId, targetUserId, video, rtcMessagePayload.roomId.get());
        } else {
            if (callback != null) {
                callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
            }
        }
    }

    public void acceptRtc(long fromUserId, long toUserId, @Nullable MSIMCallback<GeneralResult> callback, @NonNull RtcMessagePayload rtcMessagePayload) {
        final long sessionUserId = mMSIMSessionUserIdChangedHelper.getSessionUserId();
        final long targetUserId = getTargetUserId(sessionUserId, fromUserId, toUserId);
        final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
        if (rtcEngineWrapper == null) {
            if (callback != null) {
                callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
            }
            return;
        }

        final int state = rtcEngineWrapper.getState();
        if (state <= RtcEngineWrapper.STATE_WAIT_ACCEPT) {
            if (rtcEngineWrapper.isReceivedCall()) {
                // 接听了对方发起的通话
                sendRtcEventSignalingMessage(targetUserId, callback, rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.ACCEPT));
                rtcEngineWrapper.setState(RtcEngineWrapper.STATE_CONNECTING);
                return;
            }
        }

        MSIMUikitLog.e("unexpected acceptRtc, fromUserId:%s, toUserId:%s, sessionUserId:%s, state:%s",
                fromUserId, toUserId, sessionUserId, RtcEngineWrapper.stateToString(state));
        if (callback != null) {
            callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
        }
    }

    public void hangupRtc(long fromUserId, long toUserId, @Nullable MSIMCallback<GeneralResult> callback, @NonNull RtcMessagePayload rtcMessagePayload) {
        final long sessionUserId = mMSIMSessionUserIdChangedHelper.getSessionUserId();
        final long targetUserId = getTargetUserId(sessionUserId, fromUserId, toUserId);
        final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
        if (rtcEngineWrapper == null) {
            if (callback != null) {
                callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
            }
            return;
        }

        final int state = rtcEngineWrapper.getState();
        if (state <= RtcEngineWrapper.STATE_WAIT_ACCEPT) {
            if (rtcEngineWrapper.isReceivedCall()) {
                // 拒绝了对方发起的通话
                sendRtcEventSignalingMessage(targetUserId, callback, rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.REJECT));
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_REJECT_MYSELF);
            } else {
                // 取消了己方发起的通话
                // sendRtcEventMessage(targetUserId, callback, rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.CANCEL));
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_CANCEL_MYSELF);
            }
            return;
        }

        if (state <= RtcEngineWrapper.STATE_CONNECTED) {
            // 通话中挂断
            if (rtcEngineWrapper.isReceivedCall()) {
                // 通话中挂断了对方发起的通话
                sendRtcEventSignalingMessage(targetUserId, callback, rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.END));
            }
            rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_HANGUP_MYSELF);
            return;
        }

        MSIMUikitLog.e("unexpected hangupRtc, fromUserId:%s, toUserId:%s, sessionUserId:%s, state:%s",
                fromUserId, toUserId, sessionUserId, RtcEngineWrapper.stateToString(state));
        if (callback != null) {
            callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
        }
    }

    private void timeoutRtc(long fromUserId, long toUserId, @Nullable MSIMCallback<GeneralResult> callback, @NonNull RtcMessagePayload rtcMessagePayload) {
        final long sessionUserId = mMSIMSessionUserIdChangedHelper.getSessionUserId();
        final long targetUserId = getTargetUserId(sessionUserId, fromUserId, toUserId);
        final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
        if (rtcEngineWrapper == null) {
            if (callback != null) {
                callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
            }
            return;
        }

        final int state = rtcEngineWrapper.getState();
        if (state == RtcEngineWrapper.STATE_WAIT_ACCEPT) {
            if (toUserId == targetUserId) {
                // 对方超时未接听
                // sendRtcEventMessage(targetUserId, callback, rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.TIMEOUT));
                rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_TIMEOUT_TARGET);
                return;
            }
        }

        MSIMUikitLog.e("unexpected timeoutRtc, fromUserId:%s, toUserId:%s, sessionUserId:%s, state:%s",
                fromUserId, toUserId, sessionUserId, RtcEngineWrapper.stateToString(state));
        if (callback != null) {
            callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
        }
    }

    private void rtcEngineError(long targetUserId, @Nullable MSIMCallback<GeneralResult> callback, @NonNull RtcMessagePayload rtcMessagePayload) {
        final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
        if (rtcEngineWrapper == null) {
            if (callback != null) {
                callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
            }
            return;
        }

        if (rtcEngineWrapper.isReceivedCall()) {
            sendRtcEventSignalingMessage(targetUserId, callback, rtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.ERROR));
        }
        rtcEngineWrapper.setStateToDisconnected(RtcEngineWrapper.DISCONNECTED_REASON_ERROR_MYSELF);
    }

    /**
     * 在 rtc 通话的过程中向对方发送 event 信令消息
     */
    private void sendRtcEventSignalingMessage(long targetUserId, @Nullable MSIMCallback<GeneralResult> callback, @NonNull RtcMessagePayload rtcMessagePayload) {
        final long sessionUserId = mMSIMSessionUserIdChangedHelper.getSessionUserId();

        final RtcEngineWrapper rtcEngineWrapper = getRtcEngineWrapper(targetUserId, rtcMessagePayload, false);
        if (rtcEngineWrapper != null) {
            final String body = CustomIMMessageFactory.createCustomRtcMessage(rtcMessagePayload);
            MSIMMessage message = MSIMMessageFactory.createCustomSignalingMessage(body, true);
            MSIMManager.getInstance().getMessageManager().sendCustomSignaling(sessionUserId, targetUserId, message, callback);
        } else {
            if (callback != null) {
                callback.onCallback(GeneralResult.valueOf(GeneralResult.ERROR_CODE_UNKNOWN));
            }
        }
    }

    private static long getTargetUserId(long sessionUserId, long fromUserId, long toUserId) {
        return sessionUserId == fromUserId ? toUserId : fromUserId;
    }

    @UiThread
    @Nullable
    public RtcEngineWrapper getRtcEngineWrapper(long targetUserId, @NonNull RtcMessagePayload rtcMessagePayload, boolean autoCreate) {
        final long sessionUserId = mMSIMSessionUserIdChangedHelper.getSessionUserId();

        if (mRtcEngineWrapper != null && mRtcEngineWrapper.mRtcEngine != null) {
            if (mRtcEngineWrapper.mSessionUserId == sessionUserId
                    && mRtcEngineWrapper.mTargetUserId == targetUserId
                    && mRtcEngineWrapper.mRtcMessagePayload != null
                    && mRtcEngineWrapper.mRtcMessagePayload.isSameRoomId(rtcMessagePayload)) {
                return mRtcEngineWrapper;
            }
        }

        if (!autoCreate) {
            return null;
        }

        closeRtcEngineWrapper();

        mRtcEngineWrapper = new RtcEngineWrapper(sessionUserId, targetUserId, rtcMessagePayload);
        if (mRtcEngineWrapper.mRtcEngine != null) {
            return mRtcEngineWrapper;
        }

        MSIMUikitLog.e("fail to create rtc engine");
        IOUtil.closeQuietly(mRtcEngineWrapper);
        mRtcEngineWrapper = null;
        return null;
    }

    private void closeRtcEngineWrapper() {
        if (mRtcEngineWrapper != null) {
            IOUtil.closeQuietly(mRtcEngineWrapper);
            mRtcEngineWrapper = null;
        }
    }

    public static class RtcEngineWrapper implements Closeable {

        private final DisposableHolder mActionHolder = new DisposableHolder();
        private final long mSessionUserId;
        private final long mTargetUserId;
        private final RtcMessagePayload mRtcMessagePayload;

        private int mUserIdJoined;

        @Nullable
        private RtcEngine mRtcEngine;

        private final ExoPlayer mPlayer;

        ///////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////

        /**
         * 空闲中
         */
        public static final int STATE_IDLE = 0;
        /**
         * 等待对方接听，或者等待己方接听
         */
        public static final int STATE_WAIT_ACCEPT = 1;
        /**
         * 对方已经同意，或者己方已经接听，但是 rtc 通道连接中(双方正在获取 token 并加入频道中)
         */
        public static final int STATE_CONNECTING = 2;
        /**
         * 通话中，连接成功，可以正常通话。
         */
        public static final int STATE_CONNECTED = 3;
        /**
         * 对方拒绝或者己方拒绝，或者通话正常结束
         */
        public static final int STATE_DISCONNECTED = 4;

        ///////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////

        /**
         * 未定义
         */
        public static final int DISCONNECTED_REASON_UNDEFINED = 0;
        /**
         * 己方系统错误导致通话结束
         */
        public static final int DISCONNECTED_REASON_ERROR_MYSELF = 1;
        /**
         * 对方系统错误导致通话结束
         */
        public static final int DISCONNECTED_REASON_ERROR_TARGET = 2;
        /**
         * 己方未知原因导致通话结束
         */
        public static final int DISCONNECTED_REASON_UNKNOWN_MYSELF = 3;
        /**
         * 对方未知原因导致通话结束
         */
        public static final int DISCONNECTED_REASON_UNKNOWN_TARGET = 4;
        /**
         * 己方取消请求导致通话结束
         */
        public static final int DISCONNECTED_REASON_CANCEL_MYSELF = 5;
        /**
         * 对方取消请求导致通话结束
         */
        public static final int DISCONNECTED_REASON_CANCEL_TARGET = 6;
        /**
         * 己方拒绝导致通话结束
         */
        public static final int DISCONNECTED_REASON_REJECT_MYSELF = 7;
        /**
         * 对方拒绝导致通话结束
         */
        public static final int DISCONNECTED_REASON_REJECT_TARGET = 8;
        /**
         * 己方超时未响应导致通话结束
         */
        public static final int DISCONNECTED_REASON_TIMEOUT_MYSELF = 9;
        /**
         * 对方超时未响应导致通话结束
         */
        public static final int DISCONNECTED_REASON_TIMEOUT_TARGET = 10;
        /**
         * 己方主动挂断导致通话结束
         */
        public static final int DISCONNECTED_REASON_HANGUP_MYSELF = 11;
        /**
         * 对方主动挂断导致通话结束
         */
        public static final int DISCONNECTED_REASON_HANGUP_TARGET = 12;
        /**
         * 对方正忙导致通话结束
         */
        public static final int DISCONNECTED_REASON_LINEBUSY_TARGET = 13;

        ///////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////

        private int mState = STATE_IDLE;
        /**
         * 当状态为 STATE_DISCONNECTED 时，该字段表示具体的原因。
         */
        private int mDisconnectedReason = DISCONNECTED_REASON_UNDEFINED;
        /**
         * 连接中开始时间
         */
        private long mStateConnectingStartTimeMs = -1L;
        /**
         * 通话开始时间
         */
        private long mStateConnectedStartTimeMs = -1L;
        /**
         * 通话结束时间
         */
        private long mStateConnectedEndTimeMs = -1L;

        private final WeakObservable<RtcEventListener> mRtcEventListeners = new WeakObservable<>();
        @SuppressWarnings("FieldCanBeLocal")
        private RtcEventListener mLocalRtcEventListener;

        public interface RtcEventListener {
            void onStateChanged(int state);
        }

        private RtcEngineWrapper(long sessionUserId, long targetUserId, @NonNull RtcMessagePayload rtcMessagePayload) {
            mSessionUserId = sessionUserId;
            mTargetUserId = targetUserId;
            mRtcMessagePayload = rtcMessagePayload.copy();

            mPlayer = new SimpleExoPlayer.Builder(ContextUtil.getContext())
                    .setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(C.USAGE_VOICE_COMMUNICATION)
                                    .setContentType(C.CONTENT_TYPE_SPEECH)
                                    .build(),
                            false)
                    .build();

            if (!PermissionUtil.isAllGranted(Manifest.permission.RECORD_AUDIO)) {
                MSIMUikitLog.e("permission required Manifest.permission.RECORD_AUDIO");
            }

            if (!PermissionUtil.isAllGranted(Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                MSIMUikitLog.e("permission required Manifest.permission.MODIFY_AUDIO_SETTINGS");
            }

            try {
                final String appId = MSIMRtcMessageManager.getInstance().mAgoraAppId;
                if (TextUtils.isEmpty(appId)) {
                    throw new IllegalStateException("agora app id is empty");
                }
                mRtcEngine = RtcEngine.create(ContextUtil.getContext(), appId, mRtcEngineEventHandler);
                if (mRtcEngine != null) {
                    mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
                    mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_DEFAULT);
                } else {
                    MSIMUikitLog.e("unexpected rtc engine is null (create rtc engine fail)");
                }
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
            }
        }

        private void bindLocalRtcEventListener(RtcEventListener listener) {
            mLocalRtcEventListener = listener;
            addRtcEventListener(mLocalRtcEventListener);
        }

        public void startVibrator() {
            setVibrator(true);
        }

        public void stopVibrator() {
            setVibrator(false);
        }

        /**
         * 当前这一次通话是否是由对方发起的
         */
        public boolean isReceivedCall() {
            return mLocalRtcEventListener == null;
        }

        /**
         * 震动
         */
        private void setVibrator(boolean play) {
            MSIMUikitLog.i("%s setVibrator play:%s", Objects.defaultObjectTag(this), play);
            final Vibrator vibrator = (Vibrator) ContextUtil.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (play) {
                    vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
                } else {
                    vibrator.cancel();
                }
            }
        }

        public void startRingtone(boolean in) {
            setRingtone(true, in);
        }

        public void stopRingtone() {
            setRingtone(false, false/* ignore */);
        }

        public void setSpeaker(boolean speaker) {
            MSIMUikitLog.i("%s setSpeaker speaker:%s", Objects.defaultObjectTag(this), speaker);
            final RtcEngine rtcEngine = getRtcEngine();
            if (rtcEngine != null) {
                rtcEngine.setDefaultAudioRoutetoSpeakerphone(speaker);
                rtcEngine.setEnableSpeakerphone(speaker);
            }

            try {
                final AudioManager audioManager = (AudioManager) ContextUtil.getContext().getSystemService(Context.AUDIO_SERVICE);
                Boolean isSpeakerOn = null;
                if (audioManager != null) {
                    audioManager.setSpeakerphoneOn(speaker);
                    isSpeakerOn = audioManager.isSpeakerphoneOn();
                }
                Boolean rtcSpeaker = null;
                if (rtcEngine != null) {
                    rtcSpeaker = rtcEngine.isSpeakerphoneEnabled();
                }
                MSIMUikitLog.i("%s setSpeaker speaker:%s ==> %s, rtcSpeaker:%s", Objects.defaultObjectTag(this), speaker, isSpeakerOn, rtcSpeaker);
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
            }
        }

        public void setMuteAudio(boolean muteAudio) {
            final RtcEngine rtcEngine = getRtcEngine();
            if (rtcEngine != null) {
                rtcEngine.muteLocalAudioStream(muteAudio);
                rtcEngine.muteAllRemoteAudioStreams(muteAudio);
            }
        }

        /**
         * 来电铃声
         */
        private void setRingtone(boolean play, boolean assetsAtcIn) {
            MSIMUikitLog.i("%s setRingtone play:%s assetsAtcIn:%s", Objects.defaultObjectTag(this), play, assetsAtcIn);
            final String assetsRtcPath;
            if (assetsAtcIn) {
                assetsRtcPath = "asset:///msimsdk/uikit/rtc_in.mp3";
            } else {
                assetsRtcPath = "asset:///msimsdk/uikit/rtc_out.mp3";
            }
            try {
                mPlayer.stop();
                mPlayer.clearMediaItems();
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
            }

            if (!play) {
                return;
            }

            try {
                mPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
                final MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(ContextUtil.getContext()))
                        .createMediaSource(MediaItem.fromUri(assetsRtcPath));
                mPlayer.setMediaSource(mediaSource);
                mPlayer.setPlayWhenReady(true);
                mPlayer.prepare();
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
            }
        }

        public RtcMessagePayload getRtcMessagePayload() {
            return mRtcMessagePayload;
        }

        public void addRtcEventListener(RtcEventListener listener) {
            mRtcEventListeners.registerObserver(listener);
        }

        public void removeRtcEventListener(RtcEventListener listener) {
            mRtcEventListeners.unregisterObserver(listener);
        }

        public int getState() {
            return mState;
        }

        public int getDisconnectedReason() {
            return mDisconnectedReason;
        }

        public static String stateToString(int state) {
            switch (state) {
                case STATE_IDLE:
                    return "STATE_IDLE";
                case STATE_WAIT_ACCEPT:
                    return "STATE_WAIT_ACCEPT";
                case STATE_CONNECTING:
                    return "STATE_CONNECTING";
                case STATE_CONNECTED:
                    return "STATE_CONNECTED";
                case STATE_DISCONNECTED:
                    return "STATE_DISCONNECTED";
                default:
                    return "unexpected " + state;
            }
        }

        public static String disconnectedReasonToString(int disconnectedReason) {
            switch (disconnectedReason) {
                case DISCONNECTED_REASON_UNDEFINED:
                    return "DISCONNECTED_REASON_UNDEFINED";
                case DISCONNECTED_REASON_ERROR_MYSELF:
                    return "DISCONNECTED_REASON_ERROR_MYSELF";
                case DISCONNECTED_REASON_ERROR_TARGET:
                    return "DISCONNECTED_REASON_ERROR_TARGET";
                case DISCONNECTED_REASON_UNKNOWN_MYSELF:
                    return "DISCONNECTED_REASON_UNKNOWN_MYSELF";
                case DISCONNECTED_REASON_UNKNOWN_TARGET:
                    return "DISCONNECTED_REASON_UNKNOWN_TARGET";
                case DISCONNECTED_REASON_CANCEL_MYSELF:
                    return "DISCONNECTED_REASON_CANCEL_MYSELF";
                case DISCONNECTED_REASON_CANCEL_TARGET:
                    return "DISCONNECTED_REASON_CANCEL_TARGET";
                case DISCONNECTED_REASON_REJECT_MYSELF:
                    return "DISCONNECTED_REASON_REJECT_MYSELF";
                case DISCONNECTED_REASON_REJECT_TARGET:
                    return "DISCONNECTED_REASON_REJECT_TARGET";
                case DISCONNECTED_REASON_TIMEOUT_MYSELF:
                    return "DISCONNECTED_REASON_TIMEOUT_MYSELF";
                case DISCONNECTED_REASON_TIMEOUT_TARGET:
                    return "DISCONNECTED_REASON_TIMEOUT_TARGET";
                case DISCONNECTED_REASON_HANGUP_MYSELF:
                    return "DISCONNECTED_REASON_HANGUP_MYSELF";
                case DISCONNECTED_REASON_HANGUP_TARGET:
                    return "DISCONNECTED_REASON_HANGUP_TARGET";
                case DISCONNECTED_REASON_LINEBUSY_TARGET:
                    return "DISCONNECTED_REASON_LINEBUSY_TARGET";
                default:
                    return "unexpected " + disconnectedReason;
            }
        }

        public void setState(int state) {
            if (mState < state) {
                mState = state;

                if (mState == STATE_CONNECTING) {
                    mStateConnectingStartTimeMs = System.currentTimeMillis();
                }

                if (mState == STATE_CONNECTED) {
                    mStateConnectedStartTimeMs = System.currentTimeMillis();
                }
                if (mState == STATE_DISCONNECTED) {
                    mStateConnectedEndTimeMs = System.currentTimeMillis();
                }

                notifyStateChanged();

                // 当前通话结束之后需要 close, 如此，新的通话才能开始。
                if (mState == STATE_DISCONNECTED) {
                    IOUtil.closeQuietly(this);
                }
            }
        }

        /**
         * 是否曾经链接中。曾经链接中返回 true，否则返回 false。
         */
        public boolean wasStateConnecting() {
            return getStateConnectingStartTimeMs() > 0;
        }

        public long getStateConnectingStartTimeMs() {
            return mStateConnectingStartTimeMs;
        }

        /**
         * 是否曾经链接成功过。曾经链接成功过返回 true，否则返回 false。
         */
        public boolean wasStateConnected() {
            return getStateConnectedStartTimeMs() > 0;
        }

        public long getStateConnectedStartTimeMs() {
            return mStateConnectedStartTimeMs;
        }

        public long getStateConnectedEndTimeMs() {
            return mStateConnectedEndTimeMs;
        }

        public long getStateConnectedDurationTimeMs() {
            if (mStateConnectedStartTimeMs > 0) {
                if (mStateConnectedEndTimeMs > 0) {
                    return mStateConnectedEndTimeMs - mStateConnectedStartTimeMs;
                }
                return System.currentTimeMillis() - mStateConnectedStartTimeMs;
            }
            return -1L;
        }

        @Nullable
        public String getFormatStateConnectedDurationTimeMs() {
            final long durationMs = getStateConnectedDurationTimeMs();
            if (durationMs <= 0) {
                return null;
            }

            // 不足 1 秒显示为 1 秒
            if (durationMs <= 1000L) {
                return "00:01";
            }

            final long durationS = durationMs / 1000L;
            final long min = durationS / 60L;
            final long seconds = durationS % 60L;
            return new DecimalFormat("00").format(min) + ":" + new DecimalFormat("00").format(seconds);
        }

        public void setStateToDisconnected(int disconnectedReason) {
            if (mState < STATE_DISCONNECTED) {
                mDisconnectedReason = disconnectedReason;
                setState(STATE_DISCONNECTED);
            }
        }

        private void notifyStateChanged() {
            mRtcEventListeners.forEach(listener -> listener.onStateChanged(mState));
        }

        @Nullable
        public RtcEngine getRtcEngine() {
            return mRtcEngine;
        }

        public int getUserIdJoined() {
            return mUserIdJoined;
        }

        /**
         * 支持重复调用
         */
        public void joinChannel() {
            if (mRtcEngine == null) {
                return;
            }

            final String channel = mRtcMessagePayload.roomId.get();
            mActionHolder.set(Single.just("")
                    .map(input -> {
                        // 获取 token
                        final AgoraTokenInfo agoraTokenInfo = MSIMRtcMessageManager.getInstance().fetchAgoraTokenInfoQuietly(channel);
                        Verify.verifyNotNull(agoraTokenInfo, "agoraTokenInfo is null");
                        //noinspection ConstantConditions
                        final String token = agoraTokenInfo.token.get();
                        Verify.verify(!TextUtils.isEmpty(token), "agora token is empty");
                        MSIMUikitLog.i("fetch agora token: %s", token);
                        return token;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(token -> {
                        if (mRtcEngine != null) {
                            MSIMUikitLog.i("%s join channel channel:%s token:%s", Objects.defaultObjectTag(RtcEngineWrapper.this), channel, token);
                            final ChannelMediaOptions options = new ChannelMediaOptions();
                            final int code = mRtcEngine.joinChannel(token, channel, null, 0, options);
                            if (code != 0) {
                                MSIMUikitLog.e("unexpected join channel return code:" + code);
                                IOUtil.closeQuietly(RtcEngineWrapper.this);
                            }
                        }
                    }, e -> {
                        MSIMUikitLog.e(e);
                        IOUtil.closeQuietly(RtcEngineWrapper.this);
                    })
            );
        }

        @SuppressWarnings("FieldCanBeLocal")
        private final IRtcEngineEventHandler mRtcEngineEventHandler = new IRtcEngineEventHandler() {

            private boolean mJoinChannelSuccess;
            private boolean mUserJoined;

            private void checkConnectState() {
                if (mJoinChannelSuccess && mUserJoined) {
                    setState(STATE_CONNECTED);
                }
            }

            @Override
            public void onWarning(int warn) {
                MSIMUikitLog.w("%s onWarning %s %s",
                        Objects.defaultObjectTag(this), warn, RtcEngine.getErrorDescription(warn));
            }

            @Override
            public void onError(int err) {
                MSIMUikitLog.e("%s onError %s %s",
                        Objects.defaultObjectTag(this), err, RtcEngine.getErrorDescription(err));
            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                MSIMUikitLog.i("%s onLeaveChannel totalDuration:%s users:%s",
                        Objects.defaultObjectTag(this), stats.totalDuration, stats.users);
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                MSIMUikitLog.i("%s onJoinChannelSuccess channel:%s uid:%s elapsed:%s",
                        Objects.defaultObjectTag(this), channel, uid, elapsed);
                mJoinChannelSuccess = true;
                checkConnectState();
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                MSIMUikitLog.i("%s onUserJoined uid:%s elapsed:%s",
                        Objects.defaultObjectTag(this), uid, elapsed);
                mUserIdJoined = uid;
                mUserJoined = true;
                checkConnectState();
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                MSIMUikitLog.i("%s onUserOffline uid:%s reason:%s",
                        Objects.defaultObjectTag(this), uid, reason);
            }

            @Override
            public void onAudioRouteChanged(int routing) {
                MSIMUikitLog.i("%s onAudioRouteChanged %s", Objects.defaultObjectTag(this), routing);
            }
        };

        @Override
        public void close() throws IOException {
            MSIMUikitLog.i("%s close", Objects.defaultObjectTag(this));
            mActionHolder.clear();

            setStateToDisconnected(DISCONNECTED_REASON_UNKNOWN_MYSELF);

            setRingtone(false, false/*ignore*/);
            setVibrator(false);

            try {
                if (mRtcEngine != null) {
                    mRtcEngine.removeHandler(mRtcEngineEventHandler);
                    mRtcEngine.leaveChannel();
                }
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
            }
            try {
                RtcEngine.destroy();
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
            }

            mRtcEngine = null;
        }

        /**
         * 接听电话超时时间
         */
        private static final long TIMEOUT_COUNT_DOWN = TimeUnit.SECONDS.toMillis(60);
        private static final long INTERVAL_COUNT_DOWN = TimeUnit.SECONDS.toMillis(2);
        private final AtomicBoolean mTimeoutCountDownStart = new AtomicBoolean(false);
        private CountDownTimer mCountDownTimer;

        private void startTimeoutCountDown(Runnable timeoutRunnable) {
            if (mTimeoutCountDownStart.compareAndSet(false, true)) {
                Threads.postUi(() -> {
                    if (mState != STATE_WAIT_ACCEPT) {
                        MSIMUikitLog.i("ignore timeout count down, state is not STATE_WAIT_ACCEPT");
                        return;
                    }

                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    mCountDownTimer = new CountDownTimer(TIMEOUT_COUNT_DOWN, INTERVAL_COUNT_DOWN) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            MSIMUikitLog.i("%s onTick %s", Objects.defaultObjectTag(this), millisUntilFinished);
                            if (mState != STATE_WAIT_ACCEPT) {
                                MSIMUikitLog.i("ignore timeout count down, state is not STATE_WAIT_ACCEPT. cancel current count down timer.");
                                cancel();
                                return;
                            }

                            // 重复发送 CALL 信令消息
                            MSIMRtcMessageManager.getInstance().sendRtcEventSignalingMessage(
                                    mTargetUserId,
                                    null,
                                    mRtcMessagePayload.copyWithEvent(RtcMessagePayload.Event.CALL)
                            );
                        }

                        @Override
                        public void onFinish() {
                            MSIMUikitLog.i("%s onFinish", Objects.defaultObjectTag(this));
                            if (mState != STATE_WAIT_ACCEPT) {
                                MSIMUikitLog.i("ignore timeout count down, state is not STATE_WAIT_ACCEPT");
                                return;
                            }

                            // 接听电话超时
                            if (timeoutRunnable != null) {
                                timeoutRunnable.run();
                            }
                        }
                    };
                    mCountDownTimer.start();
                });
            }
        }

    }

}
