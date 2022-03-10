package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImBaseMessageDefaultSendBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public class IMBaseMessageDefaultSendViewHolder extends IMBaseMessageDefaultViewHolder {

    private final ImsdkUikitUnionTypeImplImBaseMessageDefaultSendBinding mBinding;

    public IMBaseMessageDefaultSendViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_base_message_default_send);
        mBinding = ImsdkUikitUnionTypeImplImBaseMessageDefaultSendBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);
        Preconditions.checkNotNull(baseMessage);

        mBinding.sendStatusView.setBaseMessage(baseMessage);

        mBinding.avatar.setShowBorder(false);

        mBinding.readStatusView.setMessageAndConversation(baseMessage, getConversationUnsafe());

        ViewUtil.onClick(mBinding.avatar, v -> {
            Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            // TODO FIXME open profile ?
            MSIMUikitLog.w("require open profile");
        });
    }

    @Override
    protected void onConversationLoad(@NonNull MSIMConversation conversation) {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);
        Preconditions.checkNotNull(baseMessage);

        mBinding.readStatusView.setMessageAndConversation(baseMessage, conversation);
    }

    @Override
    protected void onFromUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
        mBinding.avatar.setUserInfo(userInfo);
    }

}
