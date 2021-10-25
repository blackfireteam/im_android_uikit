package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMConversationPageContext;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;

public class IMMessageReadStatusView extends IMMessageDynamicFrameLayout {

    public IMMessageReadStatusView(Context context) {
        this(context, null);
    }

    public IMMessageReadStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMMessageReadStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMMessageReadStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private MSIMConversationChangedViewHelper mConversationChangedViewHelper;
    private TextView mReadTextView;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mConversationChangedViewHelper = new MSIMConversationChangedViewHelper(MSIMConversationPageContext.GLOBAL) {
            @Nullable
            @Override
            protected MSIMMessage loadCustomObject() {
                final long localMessageId = getLocalMessageId();
                if (localMessageId > 0) {
                    return MSIMManager.getInstance().getMessageManager().getMessage(getSessionUserId(), getConversationType(), getTargetUserId(), localMessageId);
                }
                return null;
            }

            @Override
            protected void onConversationChanged(@Nullable MSIMConversation conversation, @Nullable Object customObject) {
                onConversationOrMessageChanged(conversation, (MSIMMessage) customObject);
            }
        };

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
            mReadTextView.setText(R.string.imsdk_sample_tip_message_read);
        }
    }

    @Override
    public void setMessage(@NonNull MSIMMessage message) {
        super.setMessage(message);
        mConversationChangedViewHelper.setConversationByTargetUserId(
                getSessionUserId(),
                getConversationType(),
                getTargetUserId()
        );
    }

    @Override
    public void setMessage(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
        super.setMessage(sessionUserId, conversationType, targetUserId, localMessageId);
        mConversationChangedViewHelper.setConversationByTargetUserId(
                getSessionUserId(),
                getConversationType(),
                getTargetUserId()
        );
    }

    @Nullable
    @Override
    protected MSIMConversation loadCustomObject() {
        return MSIMManager.getInstance().getConversationManager().getConversationByTargetUserId(getSessionUserId(), getConversationType(), getTargetUserId());
    }

    @Override
    protected void onMessageChanged(@Nullable MSIMMessage message, @Nullable Object customObject) {
        onConversationOrMessageChanged((MSIMConversation) customObject, message);
    }

    private void onConversationOrMessageChanged(@Nullable MSIMConversation conversation, @Nullable MSIMMessage message) {
        if (DEBUG) {
            MSIMUikitLog.v("onConversationOrMessageChanged conversation:%s message:%s", conversation, message);
        }

        if (message == null || conversation == null) {
            mReadTextView.setText(null);
            return;
        }

        final int messageSendStatus = message.getSendStatus(MSIMConstants.SendStatus.SUCCESS);
        final long serverMessageId = message.getServerMessageId();
        final long lastReadServerMessageId = conversation.getLastReadServerMessageId();
        boolean read = serverMessageId > 0 && serverMessageId <= lastReadServerMessageId;

        if (messageSendStatus == MSIMConstants.SendStatus.SUCCESS) {
            if (read) {
                mReadTextView.setText(R.string.imsdk_sample_tip_message_read);
            } else {
                mReadTextView.setText(R.string.imsdk_sample_tip_message_delivered);
            }
        } else {
            mReadTextView.setText(null);
        }
    }

}
