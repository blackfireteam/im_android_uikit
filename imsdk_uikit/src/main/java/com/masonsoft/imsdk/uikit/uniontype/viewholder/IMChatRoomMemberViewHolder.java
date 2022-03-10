package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMChatRoomMember;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.chat.SingleChatActivity;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplChatRoomMemberBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

/**
 * 聊天室用户
 */
public class IMChatRoomMemberViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplChatRoomMemberBinding mBinding;
    private MSIMUserInfoLoader mMemberUserInfoLoader;

    public IMChatRoomMemberViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_chat_room_member);
        mBinding = ImsdkUikitUnionTypeImplChatRoomMemberBinding.bind(itemView);

        this.init();
    }

    private void init() {
        mMemberUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userInfo);

                IMChatRoomMemberViewHolder.this.onMemberUserInfoLoadInternal(userInfo);
            }
        };
    }

    private void onMemberUserInfoLoadInternal(@NonNull MSIMUserInfo userInfo) {
        final DataObject itemObject = getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }
        final MSIMChatRoomMember member = itemObject.getObject(MSIMChatRoomMember.class);
        if (member == null) {
            return;
        }
        if (member.getUserId() != userInfo.getUserId()) {
            return;
        }

        this.onMemberUserInfoLoad(userInfo);
    }

    private void onMemberUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
        mBinding.avatar.setUserInfo(userInfo);
        mBinding.username.setUserInfo(userInfo);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMChatRoomMember member = itemObject.getObject(MSIMChatRoomMember.class);

        final long userId = member.getUserId();
        mMemberUserInfoLoader.setUserInfo(MSIMUserInfo.mock(userId), false);

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
