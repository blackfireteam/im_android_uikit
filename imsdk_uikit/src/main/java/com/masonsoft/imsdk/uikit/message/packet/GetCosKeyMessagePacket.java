package com.masonsoft.imsdk.uikit.message.packet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.core.ProtoByteMessage;
import com.masonsoft.imsdk.core.ProtoByteMessageProvider;
import com.masonsoft.imsdk.core.message.SessionProtoByteMessageWrapper;
import com.masonsoft.imsdk.core.message.packet.NotNullTimeoutMessagePacket;
import com.masonsoft.imsdk.core.proto.ProtoMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.entity.CosKeyInfo;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.thread.Threads;

public class GetCosKeyMessagePacket extends NotNullTimeoutMessagePacket {

    @Nullable
    private CosKeyInfo mCosKeyInfo;

    public GetCosKeyMessagePacket(ProtoByteMessageProvider protoByteMessageProvider, long sign) {
        super(protoByteMessageProvider, sign);
    }

    @Nullable
    public CosKeyInfo getCosKeyInfo() {
        return mCosKeyInfo;
    }

    @Override
    protected boolean doNotNullProcess(@NonNull SessionProtoByteMessageWrapper target) {
        Threads.mustNotUi();

        {
            // 接收 Result 消息
            final ProtoMessage.Result result = target.getProtoByteMessageWrapper().getProtoMessageObject(ProtoMessage.Result.class);
            if (result != null) {
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
        }

        {
            // 接收 CosKey 消息
            final ProtoMessage.CosKey cosKey = target.getProtoByteMessageWrapper().getProtoMessageObject(ProtoMessage.CosKey.class);
            if (cosKey != null) {
                // 校验 sign 是否相等
                if (cosKey.getSign() == getSign()) {
                    synchronized (getStateLock()) {
                        final int state = getState();
                        if (state != STATE_WAIT_RESULT) {
                            MSIMUikitLog.e(Objects.defaultObjectTag(this)
                                    + " unexpected. accept with same sign:%s and invalid state:%s", getSign(), stateToString(state));
                            return false;
                        }

                        mCosKeyInfo = CosKeyInfo.valueOf(cosKey, target.getSessionUserId());
                        moveToState(STATE_SUCCESS);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public static GetCosKeyMessagePacket create(final long sign) {
        return new GetCosKeyMessagePacket(new ProtoByteMessageProvider.SimpleProtoByteMessageProvider(
                ProtoByteMessage.Type.encode(
                        ProtoMessage.GetCosKey.newBuilder()
                                .setSign(sign)
                                .build())
        ), sign);
    }

}
