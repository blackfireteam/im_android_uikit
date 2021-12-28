package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConversation;
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
        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage message = itemObject.getObject();

        final MSIMConversation conversation;
        final Object extObject1 = itemObject.getExtObjectObject1(null);
        if (extObject1 instanceof MSIMConversation) {
            conversation = (MSIMConversation) extObject1;
        } else {
            conversation = null;
        }

        mBinding.sendStatusView.setBaseMessage(message);

        mBinding.avatar.setTargetUserId(message.getFromUserId());
        mBinding.avatar.setShowBorder(false);

        mBinding.readStatusView.setMessageAndConversation(message, conversation);

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

}
