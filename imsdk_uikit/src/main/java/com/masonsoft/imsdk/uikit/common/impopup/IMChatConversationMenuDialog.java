package com.masonsoft.imsdk.uikit.common.impopup;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonImChatConversationMenuDialogBinding;

import java.util.List;

import io.github.idonans.backstack.dialog.ViewDialog;

/**
 * 长按一个会话的弹框
 */
public class IMChatConversationMenuDialog {

    private final ViewDialog mViewDialog;
    private IMChatConversationPopupView mPopupView;

    public IMChatConversationMenuDialog(Activity activity,
                                        ViewGroup parentView,
                                        View anchorView,
                                        int coverDrawableResId,
                                        List<String> menuList,
                                        List<Integer> menuIdList) {
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_im_chat_conversation_menu_dialog)
                .setParentView(parentView)
                .setOnShowListener(() -> {
                    if (mPopupView != null) {
                        mPopupView.showAnchorViewCover();
                    }
                    onShow();
                })
                .setOnHideListener(cancel -> {
                    if (mPopupView != null) {
                        mPopupView.hideAnchorViewCover();
                    }
                    onHide();
                })
                .dimBackground(false)
                .setCancelable(true)
                .create();
        final ImsdkUikitCommonImChatConversationMenuDialogBinding binding = ImsdkUikitCommonImChatConversationMenuDialogBinding.bind(mViewDialog.getContentView());
        mPopupView = binding.popupView;
        mPopupView.showForAnchorView(anchorView, coverDrawableResId, menuList, menuIdList);
        mPopupView.setOnMenuClickListener((menuId, menuText, menuView) -> {
            if (mOnIMMenuClickListener != null) {
                mOnIMMenuClickListener.onItemMenuClick(menuId, menuText, menuView);
            }
            hide();
        });
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(false);
    }

    private OnIMMenuClickListener mOnIMMenuClickListener;

    public IMChatConversationMenuDialog setOnIMMenuClickListener(OnIMMenuClickListener listener) {
        mOnIMMenuClickListener = listener;
        return this;
    }

    protected void onShow() {
    }

    protected void onHide() {
    }

}
