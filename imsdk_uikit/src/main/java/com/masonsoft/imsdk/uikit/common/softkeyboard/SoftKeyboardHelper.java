package com.masonsoft.imsdk.uikit.common.softkeyboard;

import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.masonsoft.imsdk.uikit.widget.systeminsets.SoftKeyboardListenerLayout;

import io.github.idonans.core.util.SystemUtil;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 处理自定义键盘和系统软键盘的显示，隐藏交互
 */
public class SoftKeyboardHelper implements SoftKeyboardListenerLayout.OnSoftKeyboardChangedListener {

    private final SoftKeyboardListenerLayout mSoftKeyboardListenerLayout;
    private final EditText mEditText;
    private final View mCustomSoftKeyboard;

    private boolean mRequestShowCustomSoftKeyboard;

    public SoftKeyboardHelper(
            SoftKeyboardListenerLayout softKeyboardListenerLayout,
            EditText editText,
            View customSoftKeyboard) {
        mSoftKeyboardListenerLayout = softKeyboardListenerLayout;
        mEditText = editText;
        mCustomSoftKeyboard = customSoftKeyboard;
        mSoftKeyboardListenerLayout.setOnSoftKeyboardChangedListener(this);
        mSoftKeyboardListenerLayout.addOnDispatchTouchEventListener(new SoftKeyboardListenerLayout.FirstMoveOrUpTouchEventListener() {
            @Override
            public void onFirstMoveOrUpTouchEvent(MotionEvent event, float dx, float dy) {
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                if (isTouchOutside(rawX, rawY)) {
                    requestHideAllSoftKeyboard();
                }
            }
        });
    }

    protected boolean isTouchOutside(float rawX, float rawY) {
        return false;
    }

    public void requestShowCustomSoftKeyboard() {
        final boolean oldShowSoftInputOnFocus = mEditText.getShowSoftInputOnFocus();
        mEditText.setShowSoftInputOnFocus(false);
        mEditText.requestFocus();
        mEditText.setShowSoftInputOnFocus(oldShowSoftInputOnFocus);

        mRequestShowCustomSoftKeyboard = true;
        // 显示自定义键盘，需要等待系统键盘消失之后再显示，以免页面跳动
        if (mSoftKeyboardListenerLayout.isSoftKeyboardShown()) {
            SystemUtil.hideSoftKeyboard(mEditText);
        } else {
            // 系统键盘没有显示，直接显示自定义键盘
            ViewUtil.setVisibilityIfChanged(mCustomSoftKeyboard, View.VISIBLE);
            onSoftKeyboardLayoutShown(true, false);
        }
    }

    public void requestShowSystemSoftKeyboard() {
        final boolean oldShowSoftInputOnFocus = mEditText.getShowSoftInputOnFocus();
        mEditText.setShowSoftInputOnFocus(false);
        mEditText.requestFocus();
        mEditText.setShowSoftInputOnFocus(oldShowSoftInputOnFocus);

        mRequestShowCustomSoftKeyboard = false;
        SystemUtil.showSoftKeyboard(mEditText);
    }

    public void requestHideAllSoftKeyboard() {
        mRequestShowCustomSoftKeyboard = false;

        if (mSoftKeyboardListenerLayout.isSoftKeyboardShown()) {
            SystemUtil.hideSoftKeyboard(mEditText);
        }
        ViewUtil.setVisibilityIfChanged(mCustomSoftKeyboard, View.GONE);

        onAllSoftKeyboardLayoutHidden();
    }

    @Override
    public void onSoftKeyboardShown() {
        // 系统键盘显示之后，需要隐藏自定义键盘
        mRequestShowCustomSoftKeyboard = false;
        ViewUtil.setVisibilityIfChanged(mCustomSoftKeyboard, View.GONE);
        onSoftKeyboardLayoutShown(false, true);
    }

    @Override
    public void onSoftKeyboardHidden() {
        // 系统键盘隐藏之后，需要判断是否需要显示自定义键盘
        if (mRequestShowCustomSoftKeyboard) {
            ViewUtil.setVisibilityIfChanged(mCustomSoftKeyboard, View.VISIBLE);
            onSoftKeyboardLayoutShown(true, false);
        }
    }

    public boolean onBackPressed() {
        if (mCustomSoftKeyboard.getVisibility() == View.VISIBLE
                || mRequestShowCustomSoftKeyboard) {
            mRequestShowCustomSoftKeyboard = false;
            ViewUtil.setVisibilityIfChanged(mCustomSoftKeyboard, View.GONE);

            onAllSoftKeyboardLayoutHidden();
            return true;
        }

        return false;
    }

    /**
     * 系统键盘或者是用户自定义键盘显示了。
     *
     * @param customSoftKeyboard true 用户自定义键盘显示了
     * @param systemSoftKeyboard true 系统软键盘显示了
     */
    protected void onSoftKeyboardLayoutShown(boolean customSoftKeyboard, boolean systemSoftKeyboard) {
    }

    /**
     * 系统键盘和用户自定键盘都隐藏了
     */
    protected void onAllSoftKeyboardLayoutHidden() {
    }

}
