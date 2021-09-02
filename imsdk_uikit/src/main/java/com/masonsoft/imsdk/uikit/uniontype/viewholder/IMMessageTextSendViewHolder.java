package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImMessageTextSendBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public class IMMessageTextSendViewHolder extends IMMessageTextViewHolder {

    private final ImsdkUikitUnionTypeImplImMessageTextSendBinding mBinding;

    public IMMessageTextSendViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_message_text_send);
        mBinding = ImsdkUikitUnionTypeImplImMessageTextSendBinding.bind(itemView);
    }

    @Override
    protected void onBindItemObject(int position, @NonNull DataObject<MSIMMessage> itemObject) {
        super.onBindItemObject(position, itemObject);
        final MSIMMessage message = itemObject.object;
        mBinding.sendStatusView.setMessage(message);

        mBinding.avatar.setTargetUserId(message.getFromUserId());
        mBinding.avatar.setShowBorder(false);

        mBinding.readStatusView.setMessage(message);

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
