package com.masonsoft.imsdk.uikit.entity;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.masonsoft.imsdk.lang.StateProp;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

public class RtcMessagePayload {

    public static final class Event {
        /**
         * 系统错误
         */
        public static final int ERROR = -1;
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 0;
        /**
         * 邀请方发起请求
         */
        public static final int CALL = 1;
        /**
         * 邀请方取消请求（只有在被邀请方还没处理的时候才能取消）
         */
        public static final int CANCEL = 2;
        /**
         * 被邀请方拒绝邀请
         */
        public static final int REJECT = 3;
        /**
         * 被邀请方超时未响应
         */
        public static final int TIMEOUT = 4;
        /**
         * 通话中断
         */
        public static final int END = 5;
        /**
         * 被邀请方正忙
         */
        public static final int LINEBUSY = 6;
        /**
         * 被邀请方接受邀请
         */
        public static final int ACCEPT = 7;

        public static String eventToString(int event) {
            switch (event) {
                case ERROR:
                    return "ERROR";
                case UNKNOWN:
                    return "UNKNOWN";
                case CALL:
                    return "CALL";
                case CANCEL:
                    return "CANCEL";
                case REJECT:
                    return "REJECT";
                case TIMEOUT:
                    return "TIMEOUT";
                case END:
                    return "END";
                case LINEBUSY:
                    return "LINEBUSY";
                case ACCEPT:
                    return "ACCEPT";
                default:
                    return "unexpected " + event;
            }
        }
    }

    public static final int TYPE_AUDIO = CustomMessagePayload.TYPE_AUDIO;
    public static final int TYPE_VIDEO = CustomMessagePayload.TYPE_VIDEO;

    /**
     * TYPE_AUDIO: 语音通信<br>
     * TYPE_VIDEO: 视频通信
     */
    public final StateProp<Integer> type = new StateProp<>();

    /**
     * @see Event
     */
    public final StateProp<Integer> event = new StateProp<>();

    /**
     * 房间号(频道)
     */
    public final StateProp<String> roomId = new StateProp<>();

    /**
     * 通话时长(秒). 注意：该字段仅用于在通话结束后，由发起方补发的自定义会话消息中展示当次通话的总时长。
     */
    public final StateProp<Long> duration = new StateProp<>();

    public boolean isVideoType() {
        return type.getOrDefault(-1) == TYPE_VIDEO;
    }

    public boolean isAudioType() {
        return type.getOrDefault(-1) == TYPE_AUDIO;
    }

    public boolean isSameRoomId(@Nullable RtcMessagePayload target) {
        if (target != null) {
            final String thisRoomId = this.roomId.getOrDefault(null);
            final String targetRoomId = target.roomId.getOrDefault(null);
            return !TextUtils.isEmpty(thisRoomId) && thisRoomId.equals(targetRoomId);
        }
        return false;
    }

    @NonNull
    public String toShortString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(Objects.defaultObjectTag(this));
        if (this.type.isUnset()) {
            builder.append(" type:unset");
        } else {
            builder.append(" type:").append(this.type.get());
        }
        if (this.event.isUnset()) {
            builder.append(" event:unset");
        } else {
            builder.append(" event:").append(this.event.get());
        }
        if (this.roomId.isUnset()) {
            builder.append(" roomId:unset");
        } else {
            builder.append(" roomId:").append(this.roomId.get());
        }
        if (this.duration.isUnset()) {
            builder.append(" duration:unset");
        } else {
            builder.append(" duration:").append(this.duration.get());
        }
        return builder.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return toShortString();
    }

    @NonNull
    public RtcMessagePayload copy() {
        final RtcMessagePayload target = new RtcMessagePayload();
        target.type.apply(this.type);
        target.event.apply(this.event);
        target.roomId.apply(this.roomId);
        target.duration.apply(this.duration);
        return target;
    }

    @NonNull
    public RtcMessagePayload copyWithEvent(int event) {
        final RtcMessagePayload target = copy();
        target.event.set(event);
        return target;
    }

    @NonNull
    public static RtcMessagePayload valueOf(long fromUserId, @Nullable String roomId, boolean video) {
        final RtcMessagePayload target = new RtcMessagePayload();

        if (TextUtils.isEmpty(roomId)) {
            roomId = "c2c_" + fromUserId + "_" + (System.currentTimeMillis() / 1000);
        }
        target.roomId.set(roomId);

        if (video) {
            target.type.set(TYPE_VIDEO);
        } else {
            target.type.set(TYPE_AUDIO);
        }
        return target;
    }

    @Nullable
    public static RtcMessagePayload fromJsonObject(@NonNull JsonObject jsonObject) {
        try {
            if (jsonObject.has("type")) {
                final int type = jsonObject.getAsJsonPrimitive("type").getAsInt();

                if (type == TYPE_AUDIO || type == TYPE_VIDEO) {
                    RtcMessagePayload target = new RtcMessagePayload();
                    target.type.set(type);

                    if (jsonObject.has("event")) {
                        target.event.set(jsonObject.getAsJsonPrimitive("event").getAsInt());
                    }
                    if (jsonObject.has("room_id")) {
                        target.roomId.set(jsonObject.getAsJsonPrimitive("room_id").getAsString());
                    }
                    if (jsonObject.has("duration")) {
                        target.duration.set(jsonObject.getAsJsonPrimitive("duration").getAsLong());
                    }
                    return target;
                }
            }
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
        }
        return null;
    }

}
