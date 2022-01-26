package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMSelfUpdateListener;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import javax.annotation.Nonnull;

import io.github.idonans.appcontext.AppContext;

public abstract class IMConversationDynamicTextView extends AppCompatTextView {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMConversationDynamicTextView(Context context) {
        this(context, null);
    }

    public IMConversationDynamicTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMConversationDynamicTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMConversationDynamicTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    protected MSIMConversation mConversation;
    @SuppressWarnings("FieldCanBeLocal")
    private MSIMSelfUpdateListener mSelfUpdateListener;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
    }

    public void setConversation(@Nonnull MSIMConversation conversation) {
        mConversation = conversation;
        mSelfUpdateListener = () -> onConversationChanged(mConversation);
        mConversation.addOnSelfUpdateListener(mSelfUpdateListener);
        onConversationChanged(mConversation);
    }

    protected abstract void onConversationChanged(@Nullable MSIMConversation conversation);

}
