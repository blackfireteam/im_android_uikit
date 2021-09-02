package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.lang.util.ViewUtil;

public class UserCacheVerifiedFlagView extends UserCacheDynamicImageView {

    public UserCacheVerifiedFlagView(Context context) {
        this(context, null);
    }

    public UserCacheVerifiedFlagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserCacheVerifiedFlagView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UserCacheVerifiedFlagView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setImageResource(R.drawable.imsdk_sample_ic_profile_verified);
        setScaleType(ScaleType.CENTER_INSIDE);
    }

    @Override
    protected void onUserCacheUpdate(@Nullable MSIMUserInfo userInfo) {
        final boolean verified = userInfo != null && userInfo.isVerified();
        ViewUtil.setVisibilityIfChanged(this, verified ? View.VISIBLE : View.GONE);
    }

}
