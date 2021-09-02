package com.masonsoft.imsdk.uikit.message.packet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.core.ProtoByteMessage;
import com.masonsoft.imsdk.core.ProtoByteMessageProvider;
import com.masonsoft.imsdk.core.message.SessionProtoByteMessageWrapper;
import com.masonsoft.imsdk.core.message.packet.NotNullTimeoutMessagePacket;
import com.masonsoft.imsdk.core.proto.ProtoMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.entity.AgoraTokenInfo;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.thread.Threads;

public class GetAgoraTokenMessagePacket extends NotNullTimeoutMessagePacket {

    @Nullable
    private AgoraTokenInfo mAgoraTokenInfo;

    public GetAgoraTokenMessagePacket(ProtoByteMessageProvider protoByteMessageProvider, long sign) {
        super(protoByteMessageProvider, sign);
    }

    @Nullable
    public AgoraTokenInfo getAgoraTokenInfo() {
        return mAgoraTokenInfo;
    }

    @Override
    protected boolean doNotNullProcess(@NonNull SessionProtoByteMessageWrapper target) {
        Threads.mustNotUi();

        final Object protoMessageObject = target.getProtoByteMessageWrapper().getProtoMessageObject();
        if (protoMessageObject == null) {
            return false;
        }

        // 接收 Result 消息
        if (protoMessageObject instanceof ProtoMessage.Result) {
            final ProtoMessage.Result result = (ProtoMessage.Result) protoMessageObject;

            // 校验 sign 是否相等
            if (result.getSign() == getSign()) {
                synchronized (getStateLock()) {
                    final int state = getState();
                    if (state != STATE_WAIT_RESULT) {
                        MSIMUikitLog.e(Objects.defaultObjectTag(this)
                                + " unexpected. accept with same sign:%s and invalid state:%s", getSign(), stateToString(state));
                        return false;
                    }

                    if (result.getCode() != 0) {
                        setErrorCode((int) result.getCode());
                        setErrorMessage(result.getMsg());
                        MSIMUikitLog.e(Objects.defaultObjectTag(this) +
                                " unexpected. errorCode:%s, errorMessage:%s", result.getCode(), result.getMsg());
                    } else {
                        final Throwable e = new IllegalArgumentException(Objects.defaultObjectTag(this) + " unexpected. result code is 0.");
                        MSIMUikitLog.e(e);
                    }
                    moveToState(STATE_FAIL);
                }
                return true;
            }
        }

        // 接收 AgoraToken 消息
        if (protoMessageObject instanceof ProtoMessage.AgoraToken) {
            final ProtoMessage.AgoraToken agoraToken = (ProtoMessage.AgoraToken) protoMessageObject;

            // 校验 sign 是否相等
            if (agoraToken.getSign() == getSign()) {
                synchronized (getStateLock()) {
                    final int state = getState();
                    if (state != STATE_WAIT_RESULT) {
                        MSIMUikitLog.e(Objects.defaultObjectTag(this)
                                + " unexpected. accept with same sign:%s and invalid state:%s", getSign(), stateToString(state));
                        return false;
                    }

                    mAgoraTokenInfo = AgoraTokenInfo.valueOf(agoraToken, target.getSessionUserId());
                    moveToState(STATE_SUCCESS);
                }
                return true;
            }
        }

        return false;
    }

    public static GetAgoraTokenMessagePacket create(final long sign, final String roomId) {
        return new GetAgoraTokenMessagePacket(new ProtoByteMessageProvider.SimpleProtoByteMessageProvider(
                ProtoByteMessage.Type.encode(
                        ProtoMessage.GetAgoraToken.newBuilder()
                                .setSign(sign)
                                .setChannel(roomId)
                                .build())
        ), sign);
    }

}
