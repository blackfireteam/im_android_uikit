package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImMessageImageSendBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public class IMMessageVideoSendViewHolder extends IMMessageVideoViewHolder {

    private final ImsdkUikitUnionTypeImplImMessageImageSendBinding mBinding;

    public IMMessageVideoSendViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_message_video_send);
        mBinding = ImsdkUikitUnionTypeImplImMessageImageSendBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        //noinspection unchecked
        final DataObject<MSIMMessage> itemObject = (DataObject<MSIMMessage>) this.getItemObject(Object.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMMessage message = itemObject.object;

        mBinding.sendStatusView.setMessage(message);
        mBinding.progressView.setMessage(message);

        mBinding.avatar.setTargetUserId(message.getFromUserId());
        mBinding.avatar.setShowBorder(false);

        mBinding.readStatusView.setMessage(message);

        ViewUtil.onClick(mBinding.avatar, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            // TODO open profile ?
            MSIMUikitLog.w("require open profile");
        });
    }

}
