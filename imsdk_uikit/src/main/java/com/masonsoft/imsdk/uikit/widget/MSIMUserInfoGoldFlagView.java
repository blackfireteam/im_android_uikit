package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.lang.util.ViewUtil;

public class MSIMUserInfoGoldFlagView extends MSIMUserInfoImageView {

    public MSIMUserInfoGoldFlagView(Context context) {
        this(context, null);
    }

    public MSIMUserInfoGoldFlagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserInfoGoldFlagView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMUserInfoGoldFlagView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setImageResource(R.drawable.imsdk_uikit_ic_profile_gold);
        setScaleType(ScaleType.CENTER_INSIDE);
    }

    @Override
    protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
        super.onUserInfoLoad(userId, userInfo);

        final boolean gold = userInfo != null && userInfo.isGold();
        ViewUtil.setVisibilityIfChanged(this, gold ? View.VISIBLE : View.GONE);
    }

}
