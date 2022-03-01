package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.appcontext.AppContext;

public class MSIMBaseMessageRevokeTextViewMSIM extends MSIMUserInfoTextView {

    public MSIMBaseMessageRevokeTextViewMSIM(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageRevokeTextViewMSIM(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageRevokeTextViewMSIM(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageRevokeTextViewMSIM(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    // 标记是接收的消息还是发送的消息
    private boolean mReceived;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MSIMBaseMessageRevokeTextView, defStyleAttr,
                defStyleRes);
        mReceived = a.getBoolean(R.styleable.MSIMBaseMessageRevokeTextView_received, mReceived);
        a.recycle();

        if (isInEditMode()) {
            AppContext.setContextInEditMode(this);
            if (mReceived) {
                setText(I18nResources.getString(R.string.imsdk_uikit_recall_received_message, "xxx"));
            } else {
                setText(R.string.imsdk_uikit_recall_send_message);
            }
        }
    }

    private String buildRecallText(@Nullable MSIMUserInfo userInfo) {
        String username = null;
        if (userInfo != null) {
            username = userInfo.getNickname();
        }
        if (username == null) {
            username = "";
        }
        if (mReceived) {
            return I18nResources.getString(R.string.imsdk_uikit_recall_received_message, username);
        } else {
            return I18nResources.getString(R.string.imsdk_uikit_recall_send_message);
        }
    }

    @Override
    protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
        super.onUserInfoLoad(userId, userInfo);

        setText(buildRecallText(userInfo));
    }

}
