package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.appcontext.AppContext;

public abstract class MSIMUserInfoFrameLayout extends FrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    @Nullable
    protected MSIMUserInfo mUserInfo;

    public MSIMUserInfoFrameLayout(Context context) {
        this(context, null);
    }

    public MSIMUserInfoFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserInfoFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMUserInfoFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
    }

    public void setUserInfo(@Nullable MSIMUserInfo userInfo) {
        mUserInfo = userInfo;
        onUserInfoChanged(mUserInfo);
    }

    protected void onUserInfoChanged(@Nullable MSIMUserInfo userInfo) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onUserInfoChanged %s", Objects.defaultObjectTag(this), userInfo);
        }
    }

}
