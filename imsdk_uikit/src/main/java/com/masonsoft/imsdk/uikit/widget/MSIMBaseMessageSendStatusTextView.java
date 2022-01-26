package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.util.Preconditions;

public class MSIMBaseMessageSendStatusTextView extends MSIMBaseMessageDynamicFrameLayout {

    public MSIMBaseMessageSendStatusTextView(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageSendStatusTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageSendStatusTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageSendStatusTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private Drawable mSendFailDrawable;
    private Drawable mSendingDrawable;
    private TextView mTextView;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSendFailDrawable = ContextCompat.getDrawable(context, R.drawable.imsdk_uikit_ic_conversation_send_status_small_fail);
        Preconditions.checkNotNull(mSendFailDrawable);
        mSendFailDrawable.setBounds(0, 0, mSendFailDrawable.getIntrinsicWidth(), mSendFailDrawable.getIntrinsicHeight());
        mSendingDrawable = ContextCompat.getDrawable(context, R.drawable.imsdk_uikit_ic_conversation_send_status_small_sending);
        Preconditions.checkNotNull(mSendingDrawable);
        mSendingDrawable.setBounds(0, 0, mSendingDrawable.getIntrinsicWidth(), mSendingDrawable.getIntrinsicHeight());

        mTextView = new AppCompatTextView(context);
        mTextView.setSingleLine(true);
        mTextView.setMaxLines(1);
        mTextView.setIncludeFontPadding(false);
        mTextView.setTextSize(13);
        mTextView.setTextColor(0xFF999999);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        addView(mTextView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onBaseMessageChanged(@Nullable MSIMBaseMessage baseMessage) {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onBaseMessageChanged %s", baseMessage);
        }
        if (baseMessage == null) {
            mTextView.setText(null);
        } else {
            mTextView.setText(buildStatusText(baseMessage));
        }
    }

    private CharSequence buildStatusText(@NonNull MSIMBaseMessage baseMessage) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // 消息发送状态
        final int sendState = baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS);
        switch (sendState) {
            case MSIMConstants.SendStatus.IDLE:
            case MSIMConstants.SendStatus.SENDING:
                SpannableString sendingSpan = new SpannableString("[sending]");
                sendingSpan.setSpan(new AlignImageSpan(mSendingDrawable), 0, sendingSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(sendingSpan);
                break;
            case MSIMConstants.SendStatus.FAIL:
                SpannableString failSpan = new SpannableString("[fail]");
                failSpan.setSpan(new AlignImageSpan(mSendFailDrawable), 0, failSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(failSpan);
                break;
        }

        final int messageType = baseMessage.getMessageType();
        String messageText;
        switch (messageType) {
            case MSIMConstants.MessageType.TEXT:
                //noinspection ConstantConditions
                messageText = baseMessage.getTextElement().getText();
                break;
            case MSIMConstants.MessageType.IMAGE:
                messageText = "[图片]";
                break;
            case MSIMConstants.MessageType.AUDIO:
                messageText = "[语音]";
                break;
            case MSIMConstants.MessageType.VIDEO:
                messageText = "[视频]";
                break;
            case MSIMConstants.MessageType.REVOKED:
                messageText = "[已撤回]";
                break;
            case MSIMConstants.MessageType.LOCATION:
                messageText = "[位置]";
                break;
            case MSIMConstants.MessageType.DELETED:
                messageText = "";
                break;
            default:
                messageText = "[default]type:" + messageType + ", body:" + baseMessage.getBody();
        }

        if (MSIMConstants.MessageType.isCustomMessage(messageType)) {
            messageText = "[自定义消息]";
        }

        if (messageText != null) {
            messageText = messageText.trim();
            builder.append(messageText);
        }
        return builder;
    }

}
