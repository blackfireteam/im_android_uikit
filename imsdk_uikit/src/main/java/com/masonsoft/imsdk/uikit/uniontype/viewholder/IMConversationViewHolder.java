package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.chat.SingleChatActivity;
import com.masonsoft.imsdk.uikit.common.impopup.IMChatConversationMenuDialog;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImConversationBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class IMConversationViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplImConversationBinding mBinding;

    public IMConversationViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_conversation);
        mBinding = ImsdkUikitUnionTypeImplImConversationBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMConversation conversation = itemObject.getObject(MSIMConversation.class);

        final long targetUserId = conversation.getTargetUserId();
        final MSIMUserInfo targetUserInfo = conversation.getTargetUserInfo();

        mBinding.avatar.setUserInfo(targetUserId, targetUserInfo);
        mBinding.avatar.setBorderColor(false);
        mBinding.name.setUserInfo(targetUserId, targetUserInfo);
        mBinding.userGender.setUserInfo(targetUserId, targetUserInfo);

        mBinding.unreadCountView.setConversation(conversation);

        mBinding.time.setConversation(conversation);
        mBinding.msg.setConversation(conversation);

        ViewUtil.onClick(itemView, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            SingleChatActivity.start(innerActivity, targetUserId);
        });
        itemView.setOnLongClickListener(v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return false;
            }

            final int MENU_ID_DELETE = 1;

            final List<String> menuList = new ArrayList<>();
            final List<Integer> menuIdList = new ArrayList<>();
            menuList.add(I18nResources.getString(R.string.imsdk_uikit_menu_delete));
            menuIdList.add(MENU_ID_DELETE);

            final View anchorView = itemView;
            final IMChatConversationMenuDialog menuDialog = new IMChatConversationMenuDialog(innerActivity,
                    innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                    anchorView,
                    0,
                    menuList,
                    menuIdList) {
                @Override
                protected void onShow() {
                    super.onShow();
                    anchorView.setBackgroundColor(0xfff2f4f5);
                    ViewUtil.requestParentDisallowInterceptTouchEvent(anchorView);
                }

                @Override
                protected void onHide() {
                    super.onHide();
                    anchorView.setBackground(null);
                }
            };
            menuDialog.setOnIMMenuClickListener((menuId, menuText, menuView) -> {
                if (menuId == MENU_ID_DELETE) {
                    // 删除
                    MSIMManager.getInstance().getConversationManager().deleteConversation(
                            conversation
                    );
                } else {
                    MSIMUikitLog.e("IMChatConversationMenuDialog onItemMenuClick invalid menuId:%s, menuText:%s, menuView:%s",
                            menuId, menuText, menuView);
                }
            });
            menuDialog.show();
            return true;
        });
    }

}
