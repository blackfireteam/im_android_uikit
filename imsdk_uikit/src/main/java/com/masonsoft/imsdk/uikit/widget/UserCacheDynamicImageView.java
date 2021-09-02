package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.masonsoft.imsdk.MSIMUserInfo;

public abstract class UserCacheDynamicImageView extends AppCompatImageView {

    public UserCacheDynamicImageView(Context context) {
        this(context, null);
    }

    public UserCacheDynamicImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserCacheDynamicImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UserCacheDynamicImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private UserCacheChangedViewHelper mUserCacheChangedViewHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mUserCacheChangedViewHelper = new UserCacheChangedViewHelper() {
            @Override
            protected void onUserCacheChanged(@Nullable MSIMUserInfo userInfo) {
                UserCacheDynamicImageView.this.onUserCacheUpdate(userInfo);
            }
        };
    }

    public void setTargetUserId(long targetUserId) {
        mUserCacheChangedViewHelper.setTargetUserId(targetUserId);
    }

    public long getTargetUserId() {
        return mUserCacheChangedViewHelper.getTargetUserId();
    }

    public void setExternalTargetUser(@Nullable MSIMUserInfo targetUser) {
        onUserCacheUpdate(targetUser);
    }

    protected abstract void onUserCacheUpdate(@Nullable MSIMUserInfo userInfo);

}
