package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMChatRoomMember;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.chat.SingleChatActivity;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplChatRoomMemberBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

/**
 * 聊天室用户
 */
public class IMChatRoomMemberViewHolder extends MSIMSelfUpdateUnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplChatRoomMemberBinding mBinding;

    public IMChatRoomMemberViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_chat_room_member);
        mBinding = ImsdkUikitUnionTypeImplChatRoomMemberBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMChatRoomMember member = itemObject.getObject(MSIMChatRoomMember.class);

        final long userId = member.getUserId();
        mBinding.avatar.setTargetUserId(userId);
        mBinding.username.setTargetUserId(userId);

        ViewUtil.onClick(itemView, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            if (userId <= 0) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.INVALID_USER_ID);
                return;
            }

            SingleChatActivity.start(innerActivity, userId);
        });

        itemView.setOnLongClickListener(v -> {
            final UnionTypeViewHolderListeners.OnItemLongClickListener listener = itemObject.getExtHolderItemLongClick1();
            if (listener != null) {
                listener.onItemLongClick(this);
            }
            return true;
        });

        final int role = member.getRole();
        if (role == MSIMConstants.ChatRoomMemberRole.ADMIN) {
            ViewUtil.setVisibilityIfChanged(mBinding.roleAdmin, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.roleTmpAdmin, View.GONE);
        } else if (role == MSIMConstants.ChatRoomMemberRole.TEMP_ADMIN) {
            ViewUtil.setVisibilityIfChanged(mBinding.roleAdmin, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.roleTmpAdmin, View.VISIBLE);
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.roleAdmin, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.roleTmpAdmin, View.GONE);
        }

        if (member.isMute()) {
            ViewUtil.setVisibilityIfChanged(mBinding.mute, View.VISIBLE);
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.mute, View.GONE);
        }
    }

}
