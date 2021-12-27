package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetImChatRoomPreviewBinding;

import io.github.idonans.appcontext.AppContext;

/**
 * 聊天室预览视图（例如展示在会话列表顶部）
 */
public class IMChatRoomPreview extends IMChatRoomStateDynamicFrameLayout {

    public IMChatRoomPreview(Context context) {
        this(context, null);
    }

    public IMChatRoomPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMChatRoomPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMChatRoomPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private ImsdkUikitWidgetImChatRoomPreviewBinding mBinding;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
        LayoutInflater.from(context).inflate(R.layout.imsdk_uikit_widget_im_chat_room_preview, this, true);
        mBinding = ImsdkUikitWidgetImChatRoomPreviewBinding.bind(this);
        mBinding.chatRoomLogo.setImageUrl(null, "res://app/" + R.drawable.imsdk_uikit_ic_chat_room_logo);
    }

    @Override
    public void setChatRoomContext(MSIMChatRoomContext chatRoomContext) {
        super.setChatRoomContext(chatRoomContext);

        mBinding.time.setChatRoomContext(chatRoomContext);
        mBinding.name.setChatRoomContext(chatRoomContext);
        mBinding.msg.setChatRoomContext(chatRoomContext);
    }

    @Override
    protected void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject) {
    }

}
