package com.masonsoft.imsdk.uikit.common.simpledialog;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonSimpleContentConfirmDialogBinding;

import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 内容 确认弹窗
 */
public class SimpleContentConfirmDialog {

    private final ViewDialog mViewDialog;

    @SuppressWarnings("FieldCanBeLocal")
    private final TextView mContent;
    @SuppressWarnings("FieldCanBeLocal")
    private final TextView mBtnLeft;
    @SuppressWarnings("FieldCanBeLocal")
    private final TextView mBtnRight;

    public SimpleContentConfirmDialog(Activity activity,
                                      String content) {
        this(activity, activity.findViewById(Window.ID_ANDROID_CONTENT), content);
    }

    public SimpleContentConfirmDialog(Activity activity,
                                      ViewGroup parentView,
                                      String content) {
        this(
                activity,
                parentView,
                content,
                I18nResources.getString(R.string.imsdk_uikit_button_text_cancel),
                I18nResources.getString(R.string.imsdk_uikit_button_text_submit)
        );
    }

    public SimpleContentConfirmDialog(Activity activity,
                                      ViewGroup parentView,
                                      String content,
                                      String btnLeftText,
                                      String btnRightText) {
        this(activity,
                parentView,
                content,
                btnLeftText,
                btnRightText,
                true);
    }

    public SimpleContentConfirmDialog(Activity activity,
                                      ViewGroup parentView,
                                      String content,
                                      String btnLeftText,
                                      String btnRightText,
                                      boolean dimBackground) {
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_simple_content_confirm_dialog)
                .setParentView(parentView)
                .dimBackground(dimBackground)
                .setCancelable(true)
                .create();
        final ImsdkUikitCommonSimpleContentConfirmDialogBinding binding = ImsdkUikitCommonSimpleContentConfirmDialogBinding.bind(mViewDialog.getContentView());
        mContent = binding.content;
        mBtnLeft = binding.btnLeft;
        mBtnRight = binding.btnRight;
        mContent.setText(content);
        mBtnLeft.setText(btnLeftText);
        mBtnRight.setText(btnRightText);

        ViewUtil.onClick(mBtnLeft, v -> {
            if (mOnBtnLeftClickListener != null) {
                mOnBtnLeftClickListener.onBtnLeftClick();
            }
            hide();
        });
        ViewUtil.onClick(mBtnRight, v -> {
            if (mOnBtnRightClickListener != null) {
                mOnBtnRightClickListener.onBtnRightClick();
            }
            hide();
        });
    }

    public SimpleContentConfirmDialog setCancelable(boolean cancelable) {
        mViewDialog.setCancelable(cancelable);
        return this;
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(false);
    }

    public interface OnBtnLeftClickListener {
        void onBtnLeftClick();
    }

    private OnBtnLeftClickListener mOnBtnLeftClickListener;

    public SimpleContentConfirmDialog setOnBtnLeftClickListener(OnBtnLeftClickListener listener) {
        mOnBtnLeftClickListener = listener;
        return this;
    }

    public interface OnBtnRightClickListener {
        void onBtnRightClick();
    }

    private OnBtnRightClickListener mOnBtnRightClickListener;

    public SimpleContentConfirmDialog setOnBtnRightClickListener(OnBtnRightClickListener listener) {
        mOnBtnRightClickListener = listener;
        return this;
    }

}
