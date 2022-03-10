package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.MSIMRtcMessageManager;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.entity.RtcMessagePayload;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public abstract class IMBaseMessageRtcViewHolder extends IMBaseMessageViewHolder {

    private final View mRtcClickContainer;
    private final TextView mMessageText;
    private final View mRtcTypeAudio;
    private final View mRtcTypeVideo;

    public IMBaseMessageRtcViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mRtcClickContainer = itemView.findViewById(R.id.rtc_click_container);
        mMessageText = itemView.findViewById(R.id.message_text);
        mRtcTypeAudio = itemView.findViewById(R.id.message_rtc_type_audio);
        mRtcTypeVideo = itemView.findViewById(R.id.message_rtc_type_video);
    }

    public IMBaseMessageRtcViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mRtcClickContainer = itemView.findViewById(R.id.rtc_click_container);
        mMessageText = itemView.findViewById(R.id.message_text);
        mRtcTypeAudio = itemView.findViewById(R.id.message_rtc_type_audio);
        mRtcTypeVideo = itemView.findViewById(R.id.message_rtc_type_video);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        if (mRtcClickContainer != null) {
            ViewUtil.onClick(mRtcClickContainer, v -> IMBaseMessageRtcViewHolder.this.onItemClick());
        }

        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);
        final RtcMessagePayload rtcMessagePayload = RtcMessagePayload.fromDataObjectWithCache(itemObject);
        Preconditions.checkNotNull(baseMessage);
        Preconditions.checkNotNull(rtcMessagePayload);
        final boolean received = baseMessage.isReceived();

        if (rtcMessagePayload.isAudioType()) {
            // 语音电话
            if (mRtcTypeAudio != null) {
                ViewUtil.setVisibilityIfChanged(mRtcTypeAudio, View.VISIBLE);
            }
            if (mRtcTypeVideo != null) {
                ViewUtil.setVisibilityIfChanged(mRtcTypeVideo, View.GONE);
            }
        } else if (rtcMessagePayload.isVideoType()) {
            // 视频电话
            if (mRtcTypeAudio != null) {
                ViewUtil.setVisibilityIfChanged(mRtcTypeAudio, View.GONE);
            }
            if (mRtcTypeVideo != null) {
                ViewUtil.setVisibilityIfChanged(mRtcTypeVideo, View.VISIBLE);
            }
        } else {
            // unexpected
            if (mRtcTypeAudio != null) {
                ViewUtil.setVisibilityIfChanged(mRtcTypeAudio, View.GONE);
            }
            if (mRtcTypeVideo != null) {
                ViewUtil.setVisibilityIfChanged(mRtcTypeVideo, View.GONE);
            }
        }

        final StringBuilder messageBuilder = new StringBuilder();
        final int event = rtcMessagePayload.event.getOrDefault(RtcMessagePayload.Event.UNKNOWN);
        final long duration = rtcMessagePayload.duration.getOrDefault(0L) * 1000;
        switch (event) {
            case RtcMessagePayload.Event.CANCEL:
                if (received) {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_cancel_received));
                } else {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_cancel_send));
                }
                break;
            case RtcMessagePayload.Event.REJECT:
                if (received) {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_reject_received));
                } else {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_reject_send));
                }
                break;
            case RtcMessagePayload.Event.LINEBUSY:
                if (received) {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_linebusy_received));
                } else {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_linebusy_send));
                }
                break;
            case RtcMessagePayload.Event.TIMEOUT:
                if (received) {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_timeout_received));
                } else {
                    messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_timeout_send));
                }
                break;
            case RtcMessagePayload.Event.END:
                messageBuilder.append(I18nResources.getString(R.string.imsdk_uikit_rtc_message_info_duration_format, formatDuration(duration)));
                break;
        }

        mMessageText.setText(messageBuilder);
    }

    private void onItemClick() {
        final DataObject itemObject = getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);
        if (baseMessage == null) {
            return;
        }
        final RtcMessagePayload rtcMessagePayload = RtcMessagePayload.fromDataObjectWithCache(itemObject);
        if (rtcMessagePayload == null) {
            return;
        }

        if (!(baseMessage instanceof MSIMMessage)) {
            return;
        }
        final MSIMMessage message = (MSIMMessage) baseMessage;
        final long targetUserId = message.getTargetUserId();
        if (targetUserId <= 0) {
            return;
        }
        if (rtcMessagePayload.isAudioType()) {
            // 给对方拨打语音电话
            MSIMRtcMessageManager.getInstance().startRtcMessage(targetUserId, null, false);
        } else if (rtcMessagePayload.isVideoType()) {
            // 给对方拨打视频电话
            MSIMRtcMessageManager.getInstance().startRtcMessage(targetUserId, null, true);
        } else {
            MSIMUikitLog.e("unexpected RtcMessagePayload type:" + rtcMessagePayload.type);
        }
    }

    private String formatDuration(long durationMs) {
        final long durationS = (long) Math.ceil(durationMs / 1000f);
        final long min = durationS / 60;
        final long s = durationS % 60;

        final StringBuilder builder = new StringBuilder();
        if (min < 10) {
            builder.append("0");
        }
        builder.append(min);
        builder.append(":");
        if (s < 10) {
            builder.append("0");
        }
        if (s < 1) {
            builder.append(1);
        } else {
            builder.append(s);
        }

        return builder.toString();
    }

}
