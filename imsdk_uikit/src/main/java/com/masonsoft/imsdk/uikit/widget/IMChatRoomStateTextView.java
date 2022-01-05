package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

public abstract class IMChatRoomStateTextView extends AppCompatTextView {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private MSIMChatRoomStateChangedViewHelper mChatRoomStateChangedViewHelper;

    public IMChatRoomStateTextView(Context context) {
        this(context, null);
    }

    public IMChatRoomStateTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMChatRoomStateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMChatRoomStateTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mChatRoomStateChangedViewHelper = new MSIMChatRoomStateChangedViewHelper() {
            @Nullable
            @Override
            protected Object loadCustomObject() {
                return IMChatRoomStateTextView.this.loadCustomObject();
            }

            @Override
            protected void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject) {
                IMChatRoomStateTextView.this.onChatRoomStateChanged(chatRoomContext, customObject);
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
