package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import io.github.idonans.lang.util.ViewUtil;

/**
 * 如果消息不存在，则都不可见。<br>
 * 如果消息已撤回，则第一个 child 可见，否则第二个 child 可见。
 *
 * @since 1.0
 */
public class IMMessageRevokeStateFrameLayout extends IMMessageDynamicFrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMMessageRevokeStateFrameLayout(Context context) {
        this(context, null);
    }

    public IMMessageRevokeStateFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMMessageRevokeStateFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMMessageRevokeStateFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childCount = getChildCount();
        if (childCount != 2) {
            throw new IllegalStateException("only support 2 child. current child count:" + childCount);
        }

        final View firstChild = getChildAt(0);
        final View secondChild = getChildAt(1);

        // 默认都不可见
        ViewUtil.setVisibilityIfChanged(firstChild, View.GONE);
        ViewUtil.setVisibilityIfChanged(secondChild, View.GONE);

        if (isInEditMode()) {
            ViewUtil.setVisibilityIfChanged(secondChild, View.VISIBLE);
        }

        if (mMessageChangedViewHelper != null) {
            mMessageChangedViewHelper.requestLoadData(false);
        }
    }

    @Override
    protected void onMessageChanged(@Nullable MSIMMessage message, @Nullable Object customObject) {
        int childCount = getChildCount();
        if (childCount != 2) {
            final Throwable e = new IllegalStateException("only support 2 child. current child count:" + childCount);
            MSIMUikitLog.e(e);
            return;
        }

        final View firstChild = getChildAt(0);
        final View secondChild = getChildAt(1);
        final boolean isRevoked = message != null && message.isRevoked();
        if (isRevoked) {
            ViewUtil.setVisibilityIfChanged(firstChild, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(secondChild, View.GONE);
        } else {
            ViewUtil.setVisibilityIfChanged(firstChild, View.GONE);
            ViewUtil.setVisibilityIfChanged(secondChild, View.VISIBLE);
        }
    }

}
