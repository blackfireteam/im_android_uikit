package com.masonsoft.imsdk.uikit.uniontype;

import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageCustomEmotion2ReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageCustomEmotion2SendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageCustomLikeReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageCustomLikeSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageDefaultReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageDefaultSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageImageReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageImageSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageLocationReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageLocationSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessagePreviewImageViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessagePreviewVideoViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageRevokeReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageRevokeSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageRtcReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageRtcSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageTextReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageTextSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageTipTextViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVideoReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVideoSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVoiceReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVoiceSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMChatRoomMemberViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMConversationViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.LocationPickerSimpleLocationItemViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerBucketViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerGridViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerPagerVideoViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerPagerViewHolder;

import io.github.idonans.dynamic.uniontype.loadingstatus.UnionTypeLoadingStatus;

public class IMUikitUnionTypeMapper extends UnionTypeLoadingStatus {

    private static int sNextUnionType = 1;
    public static final int UNION_TYPE_IMPL_MEDIA_PICKER_GRID = sNextUnionType++; // ??????????????? Grid ?????????????????? item
    public static final int UNION_TYPE_IMPL_MEDIA_PICKER_BUCKET = sNextUnionType++; // ??????????????? bucket ?????????????????? item
    public static final int UNION_TYPE_IMPL_MEDIA_PICKER_PAGER = sNextUnionType++; // ??????????????? Pager ???????????????????????? item
    public static final int UNION_TYPE_IMPL_MEDIA_PICKER_PAGER_VIDEO = sNextUnionType++; // ??????????????? Pager ???????????????????????? item
    public static final int UNION_TYPE_IMPL_IM_CONVERSATION = sNextUnionType++; // ??????????????????????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_RECEIVED = sNextUnionType++; // ????????????-???????????????(fallback)
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_SEND = sNextUnionType++; // ????????????-???????????????(fallback)
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_RECEIVED = sNextUnionType++; // ???????????????????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_SEND = sNextUnionType++; // ???????????????????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_TEXT_RECEIVED = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_TEXT_SEND = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_RECEIVED = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_SEND = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VOICE_RECEIVED = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VOICE_SEND = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_RECEIVED = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_SEND = sNextUnionType++; // ????????????-???????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_RECEIVED = sNextUnionType++; // ????????????-?????????????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_SEND = sNextUnionType++; // ????????????-?????????????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_RTC_RECEIVED = sNextUnionType++; // ????????????-????????? rtc ??????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_RTC_SEND = sNextUnionType++; // ????????????-????????? rtc ??????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE = sNextUnionType++; // ????????????-??????????????????-??????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO = sNextUnionType++; // ????????????-??????????????????-??????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_TIP_TEXT = sNextUnionType++; // ?????????????????????????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_LIKE_RECEIVED = sNextUnionType++; // ????????????-????????? like ??????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_LIKE_SEND = sNextUnionType++; // ????????????-????????? like ??????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_EMOTION_RECEIVED = sNextUnionType++; // ????????????-??????????????????????????????
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_EMOTION_SEND = sNextUnionType++; // ????????????-??????????????????????????????
    public static final int UNION_TYPE_IMPL_CHAT_ROOM_MEMBER = sNextUnionType++; // ?????????????????????
    public static final int UNION_TYPE_IMPL_LOCATION_PICKER_SIMPLE_LOCATION_ITEM = sNextUnionType++; // ????????????????????????????????? item, LocationInfo ??????

    public IMUikitUnionTypeMapper() {
        put(UNION_TYPE_IMPL_MEDIA_PICKER_GRID, MediaPickerGridViewHolder::new);
        put(UNION_TYPE_IMPL_MEDIA_PICKER_BUCKET, MediaPickerBucketViewHolder::new);
        put(UNION_TYPE_IMPL_MEDIA_PICKER_PAGER, MediaPickerPagerViewHolder::new);
        put(UNION_TYPE_IMPL_MEDIA_PICKER_PAGER_VIDEO, MediaPickerPagerVideoViewHolder::new);
        put(UNION_TYPE_IMPL_IM_CONVERSATION, IMConversationViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_RECEIVED, IMBaseMessageDefaultReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_SEND, IMBaseMessageDefaultSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_RECEIVED, IMBaseMessageRevokeReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_SEND, IMBaseMessageRevokeSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_TEXT_RECEIVED, IMBaseMessageTextReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_TEXT_SEND, IMBaseMessageTextSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_RECEIVED, IMBaseMessageImageReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_SEND, IMBaseMessageImageSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VOICE_RECEIVED, IMBaseMessageVoiceReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VOICE_SEND, IMBaseMessageVoiceSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_RECEIVED, IMBaseMessageVideoReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_SEND, IMBaseMessageVideoSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_RECEIVED, IMBaseMessageLocationReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_SEND, IMBaseMessageLocationSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_RTC_RECEIVED, IMBaseMessageRtcReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_RTC_SEND, IMBaseMessageRtcSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE, IMBaseMessagePreviewImageViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO, IMBaseMessagePreviewVideoViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_TIP_TEXT, IMBaseMessageTipTextViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_LIKE_RECEIVED, IMBaseMessageCustomLikeReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_LIKE_SEND, IMBaseMessageCustomLikeSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_EMOTION_RECEIVED, IMBaseMessageCustomEmotion2ReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_CUSTOM_EMOTION_SEND, IMBaseMessageCustomEmotion2SendViewHolder::new);
        put(UNION_TYPE_IMPL_CHAT_ROOM_MEMBER, IMChatRoomMemberViewHolder::new);
        put(UNION_TYPE_IMPL_LOCATION_PICKER_SIMPLE_LOCATION_ITEM, LocationPickerSimpleLocationItemViewHolder::new);
    }

}
