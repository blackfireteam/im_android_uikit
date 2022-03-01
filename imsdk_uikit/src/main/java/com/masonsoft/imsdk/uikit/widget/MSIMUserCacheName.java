package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;

public class MSIMUserCacheName extends MSIMUserInfoTextView {

    public MSIMUserCacheName(Context context) {
        this(context, null);
    }

    public MSIMUserCacheName(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserCacheName(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMUserCacheName(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
        super.onUserInfoLoad(userId, userInfo);

        if (userInfo == null) {
            setText(null);
        } else {
            setText(userInfo.getNickname());
        }
    }

}
