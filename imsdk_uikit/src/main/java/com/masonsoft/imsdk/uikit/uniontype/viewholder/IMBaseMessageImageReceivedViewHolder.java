package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImBaseMessageImageReceivedBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public class IMBaseMessageImageReceivedViewHolder extends IMBaseMessageImageViewHolder {

    private final ImsdkUikitUnionTypeImplImBaseMessageImageReceivedBinding mBinding;

    public IMBaseMessageImageReceivedViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_base_message_image_received);
        mBinding = ImsdkUikitUnionTypeImplImBaseMessageImageReceivedBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = (MSIMBaseMessage) itemObject.object;

        mBinding.avatar.setTargetUserId(baseMessage.getFromUserId());
        mBinding.avatar.setShowBorder(false);

        ViewUtil.onClick(mBinding.avatar, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            // TODO FIXME
            MSIMUikitLog.e("require open profile");
        });
    }

}
