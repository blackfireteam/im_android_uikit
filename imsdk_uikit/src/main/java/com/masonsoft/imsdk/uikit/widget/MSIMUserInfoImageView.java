package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import io.github.idonans.appcontext.AppContext;

public abstract class MSIMUserInfoImageView extends AppCompatImageView {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMUserInfoImageView(Context context) {
        this(context, null);
    }

    public MSIMUserInfoImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserInfoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMUserInfoImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private long mUserId;
    @Nullable
    private MSIMUserInfo mUserInfo;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
    }

    public long getUserId() {
        return mUserId;
    }

    @Nullable
    public MSIMUserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(long userId, @Nullable MSIMUserInfo userInfo) {
        mUserId = userId;
        mUserInfo = userInfo;
        this.onUserInfoUpdate(mUserId, mUserInfo);
    }

    protected abstract void onUserInfoUpdate(long userId, @Nullable MSIMUserInfo userInfo);

}
