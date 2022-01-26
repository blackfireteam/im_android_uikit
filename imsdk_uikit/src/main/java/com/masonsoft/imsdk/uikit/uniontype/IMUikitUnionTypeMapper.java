package com.masonsoft.imsdk.uikit.uniontype;

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
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageTextReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageTextSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageTipTextViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVideoReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVideoSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVoiceReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageVoiceSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMChatRoomMemberViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMConversationViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageFirstCustomBaseMessageReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageFirstCustomBaseMessageSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.LocationPickerSimpleLocationItemViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerBucketViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerGridViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerPagerViewHolder;

import io.github.idonans.dynamic.uniontype.loadingstatus.UnionTypeLoadingStatus;

public class IMUikitUnionTypeMapper extends UnionTypeLoadingStatus {

    private static int sNextUnionType = 1;
    public static final int UNION_TYPE_IMPL_MEDIA_PICKER_GRID = sNextUnionType++; // 媒体选择器 Grid 视图中的一个 item
    public static final int UNION_TYPE_IMPL_MEDIA_PICKER_BUCKET = sNextUnionType++; // 媒体选择器 bucket 视图中的一个 item
    public static final int UNION_TYPE_IMPL_MEDIA_PICKER_PAGER = sNextUnionType++; // 媒体选择器 Pager 视图中的一个 item
    public static final int UNION_TYPE_IMPL_IM_CONVERSATION = sNextUnionType++; // 会话列表中的一条会话
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_RECEIVED = sNextUnionType++; // 聊天消息-接收的消息(fallback)
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_SEND = sNextUnionType++; // 聊天消息-发送的消息(fallback)
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_RECEIVED = sNextUnionType++; // 接收到已撤回的消息
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_SEND = sNextUnionType++; // 发送的已撤回的消息
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_TEXT_RECEIVED = sNextUnionType++; // 聊天消息-接收的文字
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_TEXT_SEND = sNextUnionType++; // 聊天消息-发送的文字
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_RECEIVED = sNextUnionType++; // 聊天消息-接收的图片
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_SEND = sNextUnionType++; // 聊天消息-发送的图片
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VOICE_RECEIVED = sNextUnionType++; // 聊天消息-接收的语音
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VOICE_SEND = sNextUnionType++; // 聊天消息-发送的语音
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_RECEIVED = sNextUnionType++; // 聊天消息-接收的视频
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_SEND = sNextUnionType++; // 聊天消息-发送的视频
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_RECEIVED = sNextUnionType++; // 聊天消息-接收的位置信息
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_SEND = sNextUnionType++; // 聊天消息-发送的位置信息
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE = sNextUnionType++; // 聊天消息-横向全屏预览-图片
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO = sNextUnionType++; // 聊天消息-横向全屏预览-视频
    @Deprecated
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_RECEIVED = sNextUnionType++; // 聊天消息-接收的自定义消息
    @Deprecated
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_SEND = sNextUnionType++; // 聊天消息-发送的自定义消息
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_TIP_TEXT = sNextUnionType++; // 聊天消息中的提示性文字
    public static final int UNION_TYPE_IMPL_CHAT_ROOM_MEMBER = sNextUnionType++; // 聊天室在线用户
    public static final int UNION_TYPE_IMPL_LOCATION_PICKER_SIMPLE_LOCATION_ITEM = sNextUnionType++; // 地址选择器视图中的一个 item, LocationInfo 类型

    public IMUikitUnionTypeMapper() {
        put(UNION_TYPE_IMPL_MEDIA_PICKER_GRID, MediaPickerGridViewHolder::new);
        put(UNION_TYPE_IMPL_MEDIA_PICKER_BUCKET, MediaPickerBucketViewHolder::new);
        put(UNION_TYPE_IMPL_MEDIA_PICKER_PAGER, MediaPickerPagerViewHolder::new);
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
        put(UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE, IMBaseMessagePreviewImageViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO, IMBaseMessagePreviewVideoViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_RECEIVED, IMMessageFirstCustomBaseMessageReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_SEND, IMMessageFirstCustomBaseMessageSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_TIP_TEXT, IMBaseMessageTipTextViewHolder::new);
        put(UNION_TYPE_IMPL_CHAT_ROOM_MEMBER, IMChatRoomMemberViewHolder::new);
        put(UNION_TYPE_IMPL_LOCATION_PICKER_SIMPLE_LOCATION_ITEM, LocationPickerSimpleLocationItemViewHolder::new);
    }

}
