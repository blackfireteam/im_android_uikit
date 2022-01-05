package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import io.github.idonans.appcontext.AppContext;

public abstract class IMChatRoomStateDynamicFrameLayout extends FrameLayout {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMChatRoomStateDynamicFrameLayout(Context context) {
        this(context, null);
    }

    public IMChatRoomStateDynamicFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMChatRoomStateDynamicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMChatRoomStateDynamicFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private MSIMChatRoomStateChangedViewHelper mChatRoomStateChangedViewHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);

        mChatRoomStateChangedViewHelper = new MSIMChatRoomStateChangedViewHelper() {
            @Nullable
            @Override
            protected Object loadCustomObject() {
                return IMChatRoomStateDynamicFrameLayout.this.loadCustomObject();
            }

            @Override
            protected void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject) {
                IMChatRoomStateDynamicFrameLayout.this.onChatRoomStateChanged(chatRoomContext, customObject);
            }
        };
    }

    public void setChatRoomContext(MSIMChatRoomContext chatRoomContext) {
        mChatRoomStateChangedViewHelper.setChatRoomContext(chatRoomContext);
    }

    @Nullable
    public MSIMChatRoomContext getChatRoomContext() {
        return mChatRoomStateChangedViewHelper.getChatRoomContext();
    }

    public long getChatRoomId() {
        return mChatRoomStateChangedViewHelper.getChatRoomId();
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    protected abstract void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject);

}
