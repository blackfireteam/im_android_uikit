package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImBaseMessageRevokeReceivedBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;

public class IMBaseMessageRevokeReceivedViewHolder extends IMBaseMessageViewHolder {

    private final ImsdkUikitUnionTypeImplImBaseMessageRevokeReceivedBinding mBinding;

    public IMBaseMessageRevokeReceivedViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_base_message_revoke_received);
        mBinding = ImsdkUikitUnionTypeImplImBaseMessageRevokeReceivedBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);
        Preconditions.checkNotNull(baseMessage);

        mBinding.messageRevokeTextView.setUserInfo(baseMessage.getFromUserId(), null);
    }

}
