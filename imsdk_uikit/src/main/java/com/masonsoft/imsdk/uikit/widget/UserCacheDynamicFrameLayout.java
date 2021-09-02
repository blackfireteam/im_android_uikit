package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

public abstract class UserCacheDynamicFrameLayout extends FrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private UserCacheChangedViewHelper mUserCacheChangedViewHelper;

    public UserCacheDynamicFrameLayout(Context context) {
        this(context, null);
    }

    public UserCacheDynamicFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserCacheDynamicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UserCacheDynamicFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mUserCacheChangedViewHelper = new UserCacheChangedViewHelper() {
            @Override
            protected void onUserCacheChanged(@Nullable MSIMUserInfo userInfo) {
                UserCacheDynamicFrameLayout.this.onUserCacheUpdate(userInfo);
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
