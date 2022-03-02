package com.masonsoft.imsdk.sample.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleUnionTypeImplDiscoverUserBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.uikit.app.chat.SingleChatActivity;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

/**
 * discover user
 */
public class DiscoverUserViewHolder extends UnionTypeViewHolder {

    private final ImsdkSampleUnionTypeImplDiscoverUserBinding mBinding;

    private MSIMUserInfoLoader mUserInfoLoader;

    public DiscoverUserViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_sample_union_type_impl_discover_user);
        mBinding = ImsdkSampleUnionTypeImplDiscoverUserBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        long userId = (long) itemObject.object;

        IOUtil.closeQuietly(mUserInfoLoader);
        mUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userId, userInfo);

                DiscoverUserViewHolder.this.onUserInfoLoad(userId, userInfo);
            }
        };
        mUserInfoLoader.setUserInfo(userId, null);
        mBinding.avatar.setUserInfo(userId, null);
        mBinding.username.setUserInfo(userId, null);

        ViewUtil.onClick(itemView, v -> onItemClick());
    }

    private void onItemClick() {
        final long currentUserId = getCurrentUserId();

        final Activity innerActivity = host.getActivity();
        if (innerActivity == null) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        if (currentUserId <= 0) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.INVALID_USER_ID);
            return;
        }

        SingleChatActivity.start(innerActivity, currentUserId);
    }

    private long getCurrentUserId() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        return (long) itemObject.object;
    }

    private void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
        final long currentUserId = getCurrentUserId();
        if (currentUserId != userId) {
            return;
        }
        mBinding.avatar.setUserInfo(userId, userInfo);
        mBinding.username.setUserInfo(userId, userInfo);
    }

}
