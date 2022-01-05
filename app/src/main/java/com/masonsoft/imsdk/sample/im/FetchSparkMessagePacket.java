package com.masonsoft.imsdk.sample.im;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.core.ProtoByteMessage;
import com.masonsoft.imsdk.core.ProtoByteMessageProvider;
import com.masonsoft.imsdk.core.message.SessionProtoByteMessageWrapper;
import com.masonsoft.imsdk.core.message.packet.NotNullTimeoutMessagePacket;
import com.masonsoft.imsdk.core.proto.ProtoMessage;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.entity.Spark;
import com.masonsoft.imsdk.util.Objects;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;

public class FetchSparkMessagePacket extends NotNullTimeoutMessagePacket {

    @NonNull
    private final List<Spark> mSparkList = new ArrayList<>();

    public FetchSparkMessagePacket(ProtoByteMessageProvider protoByteMessageProvider, long sign) {
        super(protoByteMessageProvider, sign);
    }

    @NonNull
    public List<Spark> getSparkList() {
        return mSparkList;
    }

    @Override
    protected boolean doNotNullProcess(@NonNull SessionProtoByteMessageWrapper target) {
        Preconditions.checkArgument(!Threads.isUi());

        {
            // 接收 Result 消息
            final ProtoMessage.Result result = target.getProtoByteMessageWrapper().getProtoMessageObject(ProtoMessage.Result.class);
            if (result != null) {
                // 校验 sign 是否相等
                if (result.getSign() == getSign()) {
                    synchronized (getStateLock()) {
                        final int state = getState();
                        if (state != STATE_WAIT_RESULT) {
                            SampleLog.e(Objects.defaultObjectTag(this)
                                    + " unexpected. accept with same sign:%s and invalid state:%s", getSign(), stateToString(state));
                            return false;
                        }

                        if (result.getCode() != 0) {
                            setErrorCode((int) result.getCode());
                            setErrorMessage(result.getMsg());
                            SampleLog.e(Objects.defaultObjectTag(this) +
                                    " unexpected. errorCode:%s, errorMessage:%s", result.getCode(), result.getMsg());
                        } else {
                            final Throwable e = new IllegalArgumentException(Objects.defaultObjectTag(this) + " unexpected. result code is 0.");
                            SampleLog.e(e);
                        }
                        moveToState(STATE_FAIL);
                    }
                    return true;
                }
            }
        }

        {
            // 接收 Sparks 消息
            final ProtoMessage.Sparks sparks = target.getProtoByteMessageWrapper().getProtoMessageObject(ProtoMessage.Sparks.class);
            if (sparks != null) {
                // 校验 sign 是否相等
                if (sparks.getSign() == getSign()) {
                    synchronized (getStateLock()) {
                        final int state = getState();
                        if (state != STATE_WAIT_RESULT) {
                            SampleLog.e(Objects.defaultObjectTag(this)
                                    + " unexpected. accept with same sign:%s and invalid state:%s", getSign(), stateToString(state));
                            return false;
                        }

                        final List<ProtoMessage.Spark> sparkList = sparks.getSparksList();
                        if (sparkList != null) {
                            mSparkList.addAll(Spark.valueOf(sparkList));
                        }

                        moveToState(STATE_SUCCESS);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public static FetchSparkMessagePacket create(final long sign) {
        return new FetchSparkMessagePacket(
                new ProtoByteMessageProvider.SimpleProtoByteMessageProvider(
                        ProtoByteMessage.Type.encode(
                                ProtoMessage.FetchSpark.newBuilder()
                                        .setSign(sign)
                                        .build())
                )
                , sign);
    }

}
