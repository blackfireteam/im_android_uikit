package com.masonsoft.imsdk.uikit.common.simpledialog;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;

import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.backstack.dialog.ViewDialog;

/**
 * loading
 */
public class SimpleLoadingDialog {

    private final ViewDialog mViewDialog;

    public SimpleLoadingDialog(Activity activity) {
        this(activity, activity.findViewById(Window.ID_ANDROID_CONTENT));
    }

    public SimpleLoadingDialog(Activity activity,
                               ViewGroup parentView) {
        this(activity, parentView, true);
    }

    public SimpleLoadingDialog(Activity activity,
                               ViewGroup parentView,
                               boolean dimBackground) {
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_sample_common_simple_loading_dialog)
                .setParentView(parentView)
                .dimBackground(dimBackground)
                .setCancelable(false)
                .create();
    }

    public SimpleLoadingDialog setCancelable(boolean cancelable) {
        mViewDialog.setCancelable(cancelable);
        return this;
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(false);
    }

}
