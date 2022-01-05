package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMCallback;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMWeakCallback;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.util.TipUtil;

import io.github.idonans.lang.util.ViewUtil;

public class MSIMBaseMessageSendStatusView extends MSIMBaseMessageDynamicFrameLayout {

    public MSIMBaseMessageSendStatusView(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageSendStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageSendStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageSendStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private ImageView mSendFailView;
    private ViewGroup mSendingView;

    private long mMessageSendTimeMs = 0L;
    private int mMessageSendStatus = -1;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setWillNotDraw(false);

        {
            mSendFailView = new ImageView(context);
            mSendFailView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mSendFailView.setImageResource(R.drawable.imsdk_uikit_ic_message_send_fail);
            LayoutParams layoutParams = generateDefaultLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
            addView(mSendFailView, layoutParams);
        }

        {
            final ProgressBar progressBar = new ProgressBar(context);
            mSendingView = new FrameLayout(context);
            mSendingView.addView(progressBar);

            LayoutParams layoutParams = generateDefaultLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
            addView(mSendingView, layoutParams);
        }

        ViewUtil.setVisibilityIfChanged(mSendFailView, View.GONE);
        ViewUtil.setVisibilityIfChanged(mSendingView, View.GONE);

        ViewUtil.onClick(this, v -> {
            final MSIMBaseMessage baseMessage = mBaseMessage;
            if (baseMessage != null) {
                final int sendState = baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS);
                if (sendState == MSIMConstants.SendStatus.FAIL) {

                    if (baseMessage instanceof MSIMMessage) {
                        final MSIMMessage message = (MSIMMessage) baseMessage;
                        MSIMManager.getInstance().getMessageManager().resendMessage(
                                message.getSessionUserId(),
                                message,
                                new MSIMWeakCallback<>(mEnqueueCallback));
                    } else {
                        // TODO FIXME for MSIMChatRoomMessage?
                    }
                }
            }
        });
    }

    private final MSIMCallback<GeneralResult> mEnqueueCallback = result -> {
        if (!result.isSuccess()) {
            TipUtil.showOrDefault(result.message);
        }
    };

    @Override
    protected void onBaseMessageChanged(@Nullable MSIMBaseMessage baseMessage) {
        if (DEBUG) {
            MSIMUikitLog.v("onBaseMessageChanged %s", baseMessage);
        }
        if (baseMessage == null) {
            mMessageSendStatus = MSIMConstants.SendStatus.SUCCESS;
            mMessageSendTimeMs = 0L;
        } else {
            mMessageSendStatus = baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS);
            mMessageSendTimeMs = baseMessage.getTimeMs();
        }

        syncState();
    }

    private void syncState() {
        if (mMessageSendStatus == MSIMConstants.SendStatus.IDLE
                || mMessageSendStatus == MSIMConstants.SendStatus.SENDING) {
            // 发送中
            final long delayTimeMs = 700L;
            boolean showSending = true;
            long invalidateDelay = 0;
            if (mMessageSendTimeMs > 0) {
                final long diff = System.currentTimeMillis() - mMessageSendTimeMs;
                if (diff >= 0 && diff < delayTimeMs) {
                    showSending = false;
                    invalidateDelay = delayTimeMs - diff;
                }
            }

            ViewUtil.setVisibilityIfChanged(mSendFailView, View.GONE);
            if (showSending) {
                ViewUtil.setVisibilityIfChanged(mSendingView, View.VISIBLE);
            } else {
                ViewUtil.setVisibilityIfChanged(mSendingView, View.GONE);
                postDelayed(this::syncState, invalidateDelay);
            }
        } else if (mMessageSendStatus == MSIMConstants.SendStatus.FAIL) {
            // 发送失败
            ViewUtil.setVisibilityIfChanged(mSendFailView, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mSendingView, View.GONE);
        } else {
            // 发送成功
            ViewUtil.setVisibilityIfChanged(mSendFailView, View.GONE);
            ViewUtil.setVisibilityIfChanged(mSendingView, View.GONE);
        }
    }

}
