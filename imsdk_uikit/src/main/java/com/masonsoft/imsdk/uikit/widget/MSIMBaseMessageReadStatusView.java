package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMSelfUpdateListener;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;

public class MSIMBaseMessageReadStatusView extends FrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMBaseMessageReadStatusView(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageReadStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageReadStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageReadStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private TextView mReadTextView;

    @Nullable
    protected MSIMBaseMessage mBaseMessage;
    @Nullable
    protected MSIMConversation mConversation;
    @SuppressWarnings("FieldCanBeLocal")
    private MSIMSelfUpdateListener mSelfUpdateListener;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        {
            mReadTextView = new AppCompatTextView(context);
            mReadTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            mReadTextView.setTextColor(0xff666666);
            mReadTextView.setIncludeFontPadding(false);
            LayoutParams layoutParams = generateDefaultLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
            addView(mReadTextView, layoutParams);
        }

        if (isInEditMode()) {
            mReadTextView.setText(R.string.imsdk_uikit_tip_message_read);
        }
    }

    public void setMessageAndConversation(@Nullable MSIMBaseMessage baseMessage, @Nullable MSIMConversation conversation) {
        mBaseMessage = baseMessage;
        mConversation = conversation;
        mSelfUpdateListener = () -> onConversationOrMessageChanged(mConversation, mBaseMessage);
        if (mBaseMessage != null) {
            mBaseMessage.addOnSelfUpdateListener(mSelfUpdateListener);
        }
        if (mConversation != null) {
            mConversation.addOnSelfUpdateListener(mSelfUpdateListener);
        }
        onConversationOrMessageChanged(mConversation, mBaseMessage);
    }

    protected void onConversationOrMessageChanged(@Nullable MSIMConversation conversation, @Nullable MSIMBaseMessage baseMessage) {
        if (DEBUG) {
            MSIMUikitLog.v("onConversationOrMessageChanged conversation:%s baseMessage:%s", conversation, baseMessage);
        }

        if (baseMessage == null) {
            mReadTextView.setText(null);
            return;
        }

        final int messageSendStatus = baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS);
        final long serverMessageId = baseMessage.getServerMessageId();

        boolean read;
        if (conversation == null) {
            read = false;
        } else {
            final long lastReadServerMessageId = conversation.getLastReadServerMessageId();
            read = serverMessageId > 0 && serverMessageId <= lastReadServerMessageId;
        }

        if (messageSendStatus == MSIMConstants.SendStatus.SUCCESS) {
            if (read) {
                mReadTextView.setText(R.string.imsdk_uikit_tip_message_read);
            } else {
                mReadTextView.setText(R.string.imsdk_uikit_tip_message_delivered);
            }
        } else {
            mReadTextView.setText(null);
        }
    }

}
