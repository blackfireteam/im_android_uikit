package com.masonsoft.imsdk.uikit.common.simpledialog;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonSimpleContentInputDialogBinding;

import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 简单内容输入弹窗
 */
public class SimpleContentInputDialog {

    private final ViewDialog mViewDialog;

    @SuppressWarnings("FieldCanBeLocal")
    private final EditText mEditText;
    @SuppressWarnings("FieldCanBeLocal")
    private final TextView mBtnLeft;
    @SuppressWarnings("FieldCanBeLocal")
    private final TextView mBtnRight;

    public SimpleContentInputDialog(Activity activity,
                                    String content) {
        this(activity, activity.findViewById(Window.ID_ANDROID_CONTENT), content);
    }

    public SimpleContentInputDialog(Activity activity,
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

    public SimpleContentInputDialog(Activity activity,
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

    public SimpleContentInputDialog(Activity activity,
                                    ViewGroup parentView,
                                    String content,
                                    String btnLeftText,
                                    String btnRightText,
                                    boolean dimBackground) {
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_simple_content_input_dialog)
                .setParentView(parentView)
                .dimBackground(dimBackground)
                .setCancelable(true)
                .create();
        final ImsdkUikitCommonSimpleContentInputDialogBinding binding = ImsdkUikitCommonSimpleContentInputDialogBinding.bind(mViewDialog.getContentView());
        mEditText = binding.editText;
        mBtnLeft = binding.btnLeft;
        mBtnRight = binding.btnRight;
        mEditText.setText(content);
        mBtnLeft.setText(btnLeftText);
        mBtnRight.setText(btnRightText);

        ViewUtil.onClick(mBtnLeft, v -> {
            if (mOnBtnLeftClickListener != null) {
                mOnBtnLeftClickListener.onBtnLeftClick();
            }
            hide();
        });
        ViewUtil.onClick(mBtnRight, v -> {
            final String input = mEditText.getText().toString();
            if (mOnBtnRightClickListener != null) {
                mOnBtnRightClickListener.onBtnRightClick(input);
            }
            hide();
        });
    }

    public SimpleContentInputDialog setCancelable(boolean cancelable) {
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

    public SimpleContentInputDialog setOnBtnLeftClickListener(OnBtnLeftClickListener listener) {
        mOnBtnLeftClickListener = listener;
        return this;
    }

    public interface OnBtnRightClickListener {
        void onBtnRightClick(String input);
    }

    private OnBtnRightClickListener mOnBtnRightClickListener;

    public SimpleContentInputDialog setOnBtnRightClickListener(OnBtnRightClickListener listener) {
        mOnBtnRightClickListener = listener;
        return this;
    }

}
