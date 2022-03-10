package com.masonsoft.imsdk.sample.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleUnionTypeImplDiscoverUserBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.uikit.app.chat.SingleChatActivity;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

/**
 * discover user
 */
public class DiscoverUserViewHolder extends UnionTypeViewHolder {

    private final ImsdkSampleUnionTypeImplDiscoverUserBinding mBinding;

    private final MSIMUserInfoLoader mUserInfoLoader;

    public DiscoverUserViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_sample_union_type_impl_discover_user);
        mBinding = ImsdkSampleUnionTypeImplDiscoverUserBinding.bind(itemView);

        mUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userInfo);

                DiscoverUserViewHolder.this.onUserInfoLoad(userInfo);
            }
        };
    }

    @Override
    public void onBindUpdate() {
        DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        long userId = (long) itemObject.object;

        mUserInfoLoader.setUserInfo(MSIMUserInfo.mock(userId), false);

        ViewUtil.onClick(itemView, v -> onItemClick());
    }

    private void onItemClick() {
        final Activity innerActivity = host.getActivity();
        if (innerActivity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final DataObject itemObject = getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }
        final Long currentUserId = itemObject.getObject(Long.class);
        if (currentUserId == null) {
            return;
        }

        if (currentUserId <= 0) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.INVALID_USER_ID);
            return;
        }

        SingleChatActivity.start(innerActivity, currentUserId);
    }

    private void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
        final DataObject itemObject = getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }
        final Long currentUserId = itemObject.getObject(Long.class);
        if (currentUserId == null) {
            return;
        }
        if (currentUserId != userInfo.getUserId()) {
            return;
        }

        mBinding.avatar.setUserInfo(userInfo);
        mBinding.username.setUserInfo(userInfo);
    }

}
