package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImBaseMessageVoiceReceivedBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public class IMBaseMessageVoiceReceivedViewHolder extends IMBaseMessageVoiceViewHolder {

    private final ImsdkUikitUnionTypeImplImBaseMessageVoiceReceivedBinding mBinding;

    public IMBaseMessageVoiceReceivedViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_base_message_voice_received);
        mBinding = ImsdkUikitUnionTypeImplImBaseMessageVoiceReceivedBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);

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
