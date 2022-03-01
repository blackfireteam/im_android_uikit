package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.appcontext.AppContext;

public abstract class IMConversationTextView extends AppCompatTextView {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMConversationTextView(Context context) {
        this(context, null);
    }

    public IMConversationTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMConversationTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMConversationTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    protected MSIMConversation mConversation;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
    }

    public void setConversation(@Nullable MSIMConversation conversation) {
        mConversation = conversation;
        onConversationChanged(mConversation);
    }

    protected void onConversationChanged(@Nullable MSIMConversation conversation) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onConversationChanged %s", Objects.defaultObjectTag(this), conversation);
        }
    }

}
