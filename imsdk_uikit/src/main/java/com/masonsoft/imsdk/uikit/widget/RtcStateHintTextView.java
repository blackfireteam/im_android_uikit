package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.uikit.MSIMRtcMessageManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.lang.util.ViewUtil;

/**
 * 显示 rtc 状态
 */
public class RtcStateHintTextView extends AppCompatTextView {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public RtcStateHintTextView(Context context) {
        this(context, null);
    }

    public RtcStateHintTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RtcStateHintTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RtcStateHintTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    public void show() {
        removeCallbacks(mHiddenRunnable);
        ViewUtil.setVisibilityIfChanged(this, View.VISIBLE);
    }

    public void hide(long delay) {
        removeCallbacks(mHiddenRunnable);
        if (delay > 0) {
            postDelayed(mHiddenRunnable, delay);
        } else {
            ViewUtil.setVisibilityIfChanged(this, View.GONE);
        }
    }

    private final Runnable mHiddenRunnable = new Runnable() {
        @Override
        public void run() {
            removeCallbacks(this);
            ViewUtil.setVisibilityIfChanged(RtcStateHintTextView.this, View.GONE);
        }
    };

    public void setRtcState(int state, int disconnectedReason, boolean video, long fromUserId, long toUserId, long sessionUserId) {
        final boolean received = fromUserId != sessionUserId;

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_WAIT_ACCEPT) {
            if (received) {
                // 等待己方接听
                //noinspection IfStatementWithIdenticalBranches
                if (video) {
                    setText(R.string.imsdk_uikit_rtc_state_hint_text_received_video);
                    show();
                } else {
                    setText(R.string.imsdk_uikit_rtc_state_hint_text_received_audio);
                    show();
                }
            } else {
                // 等待对方接听
                setText(R.string.imsdk_uikit_rtc_state_hint_text_waiting_for_target_accept);
                show();
            }
            return;
        }

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTING) {
            setText(R.string.imsdk_uikit_rtc_state_hint_text_connecting);
            show();
            return;
        }

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_CONNECTED) {
            setText(R.string.imsdk_uikit_rtc_state_hint_text_connected);
            show();
            hide(1000L);
            return;
        }

        if (state == MSIMRtcMessageManager.RtcEngineWrapper.STATE_DISCONNECTED) {
            if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_ERROR_MYSELF) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_error_myself);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_ERROR_TARGET) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_error_target);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_UNKNOWN_MYSELF) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_unknown_myself);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_UNKNOWN_TARGET) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_unknown_target);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_CANCEL_MYSELF) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_cancel_myself);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_CANCEL_TARGET) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_cancel_target);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_REJECT_MYSELF) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_reject_myself);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_REJECT_TARGET) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_reject_target);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_TIMEOUT_MYSELF) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_timeout_myself);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_TIMEOUT_TARGET) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_timeout_target);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_HANGUP_MYSELF) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_hangup_myself);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_HANGUP_TARGET) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_hangup_target);
                show();
            } else if (disconnectedReason == MSIMRtcMessageManager.RtcEngineWrapper.DISCONNECTED_REASON_LINEBUSY_TARGET) {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_linebusy_target);
                show();
            } else {
                setText(R.string.imsdk_uikit_rtc_state_hint_text_empty);
                show();
            }
            return;
        }

        setText(R.string.imsdk_uikit_rtc_state_hint_text_empty);
        show();
    }

}
