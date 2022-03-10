package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMUserInfo;

import io.github.idonans.appcontext.AppContext;

public abstract class MSIMUserInfoTextView extends AppCompatTextView {

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

    @Nullable
    private MSIMUserInfo mUserInfo;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
    }

    @Nullable
    public MSIMUserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(@NonNull MSIMUserInfo userInfo) {
        mUserInfo = userInfo;
        this.onUserInfoUpdate(userInfo);
    }

    protected abstract void onUserInfoUpdate(@NonNull MSIMUserInfo userInfo);

}
