package com.masonsoft.imsdk.uikit.common.simpledialog;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkSampleCommonSimpleContentNoticeDialogBinding;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 内容通知弹窗
 */
public class SimpleContentNoticeDialog {

    private final ViewDialog mViewDialog;

    @SuppressWarnings("FieldCanBeLocal")
    private final TextView mContent;
    @SuppressWarnings("FieldCanBeLocal")
    private final TextView mBtn;

    public SimpleContentNoticeDialog(Activity activity,
                                     String content) {
        this(activity, activity.findViewById(Window.ID_ANDROID_CONTENT), content);
    }

    public SimpleContentNoticeDialog(Activity activity,
                                     ViewGroup parentView,
                                     String content) {
        this(
                activity,
                parentView,
                content,
                I18nResources.getString(R.string.imsdk_sample_button_text_submit)
        );
    }

    public SimpleContentNoticeDialog(Activity activity,
                                     ViewGroup parentView,
                                     String content,
                                     String btnText) {
        this(activity,
                parentView,
                content,
                btnText,
                true);
    }

    public SimpleContentNoticeDialog(Activity activity,
                                     ViewGroup parentView,
                                     String content,
                                     String btnText,
                                     boolean dimBackground) {
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_sample_common_simple_content_notice_dialog)
                .setParentView(parentView)
                .dimBackground(dimBackground)
                .setOnHideListener(cancel -> {
                    if (mOnHideListener != null) {
                        mOnHideListener.onHide(cancel);
                    }
                })
                .setCancelable(true)
                .create();
        final ImsdkSampleCommonSimpleContentNoticeDialogBinding binding = ImsdkSampleCommonSimpleContentNoticeDialogBinding.bind(mViewDialog.getContentView());
        mContent = binding.content;
        mBtn = binding.btnOk;
        mContent.setText(content);
        mBtn.setText(btnText);

        ViewUtil.onClick(mBtn, v -> {
            if (mOnBtnClickListener != null) {
                mOnBtnClickListener.onBtnClick();
            }
            hide();
        });
    }

    public SimpleContentNoticeDialog setCancelable(boolean cancelable) {
        mViewDialog.setCancelable(cancelable);
        return this;
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(false);
    }

    public interface OnBtnClickListener {
        void onBtnClick();
    }

    private OnBtnClickListener mOnBtnClickListener;

    public SimpleContentNoticeDialog setOnBtnClickListener(OnBtnClickListener listener) {
        mOnBtnClickListener = listener;
        return this;
    }

    private ViewBackLayer.OnHideListener mOnHideListener;

    public void setOnHideListener(ViewBackLayer.OnHideListener onHideListener) {
        mOnHideListener = onHideListener;
    }

}
