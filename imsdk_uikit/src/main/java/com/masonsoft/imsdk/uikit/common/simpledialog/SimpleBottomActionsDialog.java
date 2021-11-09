package com.masonsoft.imsdk.uikit.common.simpledialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonSimpleBottomActionsDialogBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonSimpleBottomActionsDialogItemBinding;

import java.util.List;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 底部弹出菜单
 */
public class SimpleBottomActionsDialog {

    private final LayoutInflater mInflater;
    private final ViewDialog mViewDialog;
    private final LinearLayout mActionsContainer;
    private final List<String> mActions;

    public SimpleBottomActionsDialog(Activity activity,
                                     List<String> actions) {
        this(activity, activity.findViewById(Window.ID_ANDROID_CONTENT), actions);
    }

    public SimpleBottomActionsDialog(Activity activity,
                                     ViewGroup parentView,
                                     List<String> actions) {
        this(activity,
                parentView,
                actions,
                true);
    }

    public SimpleBottomActionsDialog(Activity activity,
                                     ViewGroup parentView,
                                     List<String> actions,
                                     boolean dimBackground) {
        mInflater = activity.getLayoutInflater();

        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_simple_bottom_actions_dialog)
                .setParentView(parentView)
                .defaultAnimation()
                .dimBackground(dimBackground)
                .setOnHideListener(cancel -> {
                    if (mOnHideListener != null) {
                        mOnHideListener.onHide(cancel);
                    }
                })
                .setCancelable(true)
                .create();
        //noinspection ConstantConditions
        final ImsdkUikitCommonSimpleBottomActionsDialogBinding binding = ImsdkUikitCommonSimpleBottomActionsDialogBinding.bind(mViewDialog.getContentView());
        mActionsContainer = binding.actionsContainer;
        mActions = actions;

        inflateActions();
    }

    private void inflateActions() {
        mActionsContainer.removeAllViews();
        if (mActions != null) {
            for (int i = 0; i < mActions.size(); i++) {
                final int actionIndex = i;
                final String actionText = mActions.get(actionIndex);
                final ImsdkUikitCommonSimpleBottomActionsDialogItemBinding itemBinding = ImsdkUikitCommonSimpleBottomActionsDialogItemBinding.inflate(mInflater);
                ViewUtil.onClick(itemBinding.getRoot(), v -> {
                    if (mOnActionClickListener != null) {
                        mOnActionClickListener.onActionClick(actionIndex, actionText);
                    }
                    hide();
                });
                itemBinding.text.setText(actionText);
                mActionsContainer.addView(itemBinding.getRoot());
            }
        }
    }

    public SimpleBottomActionsDialog setCancelable(boolean cancelable) {
        mViewDialog.setCancelable(cancelable);
        return this;
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(false);
    }

    public interface OnActionClickListener {
        void onActionClick(int index, String actionText);
    }

    private OnActionClickListener mOnActionClickListener;

    public SimpleBottomActionsDialog setOnActionClickListener(OnActionClickListener onActionClickListener) {
        mOnActionClickListener = onActionClickListener;
        return this;
    }

    private ViewBackLayer.OnHideListener mOnHideListener;

    public void setOnHideListener(ViewBackLayer.OnHideListener onHideListener) {
        mOnHideListener = onHideListener;
    }

}
