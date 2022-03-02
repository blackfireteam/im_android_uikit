package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.util.FormatUtil;

public class MSIMConversationTimeView extends MSIMConversationFrameLayout {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMConversationTimeView(Context context) {
        this(context, null);
    }

    public MSIMConversationTimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMConversationTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMConversationTimeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private TextView mTimeTextView;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mTimeTextView = new AppCompatTextView(context);
        mTimeTextView.setSingleLine(true);
        mTimeTextView.setMaxLines(1);
        mTimeTextView.setIncludeFontPadding(false);
        mTimeTextView.setTextSize(12);
        mTimeTextView.setTextColor(0xFF999999);
        mTimeTextView.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params = generateDefaultLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        mTimeTextView.setLayoutParams(params);
        addView(mTimeTextView);

        if (isInEditMode()) {
            setTime("12:01");
        }
    }

    @Override
    protected void onConversationUpdate(@Nullable MSIMConversation conversation) {
        if (conversation == null) {
            setTime(null);
        } else {
            setTime(buildConversationTime(conversation));
        }
    }

    private String buildConversationTime(@NonNull MSIMConversation conversation) {
        // 用会话的更新时间
        final long timeMs = conversation.getTimeMs();
        if (timeMs > 0) {
            return FormatUtil.getHumanTimeDistance(timeMs, new FormatUtil.DefaultShortDateFormatOptions());
        }

        return null;
    }

    public void setTime(String time) {
        mTimeTextView.setText(time);
    }

}
