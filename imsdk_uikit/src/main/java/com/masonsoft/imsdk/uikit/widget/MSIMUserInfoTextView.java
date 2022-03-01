package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.appcontext.AppContext;

public abstract class MSIMUserInfoTextView extends AppCompatTextView {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMUserInfoTextView(Context context) {
        this(context, null);
    }

    public MSIMUserInfoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserInfoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMUserInfoTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private MSIMUserInfoLoader mUserInfoLoader;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);

        mUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userId, userInfo);

                MSIMUserInfoTextView.this.onUserInfoLoad(userId, userInfo);
            }
        };
    }

    public long getUserId() {
        return mUserInfoLoader.getUserId();
    }

    @Nullable
    public MSIMUserInfo getUserInfo() {
        return mUserInfoLoader.getUserInfo();
    }

    public void setUserInfo(long userId, @Nullable MSIMUserInfo userInfo) {
        mUserInfoLoader.setUserInfo(userId, userInfo);
    }

    public void setUserInfo(@NonNull MSIMUserInfo userInfo) {
        mUserInfoLoader.setUserInfo(userInfo);
    }

    public void requestLoadData() {
        mUserInfoLoader.requestLoadData();
    }

    protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onUserInfoLoad %s %s", Objects.defaultObjectTag(this), userId, userInfo);
        }
    }

}
