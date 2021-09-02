package com.masonsoft.imsdk.uikit.uniontype;

import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMConversationViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageDefaultReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageDefaultSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageFirstCustomMessageReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageFirstCustomMessageSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageImageReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageImageSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessagePreviewImageViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessagePreviewVideoViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageRevokeReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageRevokeSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageTextReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageTextSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageVideoReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageVideoSendViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageVoiceReceivedViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageVoiceSendViewHolder;
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
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE = sNextUnionType++; // 聊天消息-横向全屏预览-图片
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO = sNextUnionType++; // 聊天消息-横向全屏预览-视频
    @Deprecated
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_RECEIVED = sNextUnionType++; // 聊天消息-接收的自定义消息
    @Deprecated
    public static final int UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_SEND = sNextUnionType++; // 聊天消息-发送的自定义消息

    public IMUikitUnionTypeMapper() {
        put(UNION_TYPE_IMPL_MEDIA_PICKER_GRID, MediaPickerGridViewHolder::new);
        put(UNION_TYPE_IMPL_MEDIA_PICKER_BUCKET, MediaPickerBucketViewHolder::new);
        put(UNION_TYPE_IMPL_MEDIA_PICKER_PAGER, MediaPickerPagerViewHolder::new);
        put(UNION_TYPE_IMPL_IM_CONVERSATION, IMConversationViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_RECEIVED, IMMessageDefaultReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_SEND, IMMessageDefaultSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_RECEIVED, IMMessageRevokeReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_SEND, IMMessageRevokeSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_TEXT_RECEIVED, IMMessageTextReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_TEXT_SEND, IMMessageTextSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_RECEIVED, IMMessageImageReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_SEND, IMMessageImageSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VOICE_RECEIVED, IMMessageVoiceReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VOICE_SEND, IMMessageVoiceSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_RECEIVED, IMMessageVideoReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_SEND, IMMessageVideoSendViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE, IMMessagePreviewImageViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO, IMMessagePreviewVideoViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_RECEIVED, IMMessageFirstCustomMessageReceivedViewHolder::new);
        put(UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_SEND, IMMessageFirstCustomMessageSendViewHolder::new);
    }

}
