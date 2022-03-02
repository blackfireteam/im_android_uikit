package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;

public class MSIMUserInfoName extends MSIMUserInfoTextView {

    public MSIMUserInfoName(Context context) {
        this(context, null);
    }

    public MSIMUserInfoName(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserInfoName(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMUserInfoName(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onUserInfoUpdate(long userId, @Nullable MSIMUserInfo userInfo) {
        if (userInfo == null) {
            setText(null);
        } else {
            setText(userInfo.getNickname());
        }
    }

}
