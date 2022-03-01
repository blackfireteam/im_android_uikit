package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.util.Objects;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.github.idonans.appcontext.AppContext;

public class MSIMUserInfoAvatar extends ImageLayout {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private static final int AVATAR_SIZE_SMALL = 0;
    private static final int AVATAR_SIZE_MIDDLE = 1;
    private static final int AVATAR_SIZE_LARGE = 2;

    @IntDef({AVATAR_SIZE_SMALL, AVATAR_SIZE_MIDDLE, AVATAR_SIZE_LARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AvatarSize {
    }

    @AvatarSize
    private int mAvatarSize = AVATAR_SIZE_SMALL;

    private MSIMUserInfoLoader mUserInfoLoader;

    public MSIMUserInfoAvatar(Context context) {
        this(context, null);
    }

    public MSIMUserInfoAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserInfoAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.UserCacheAvatar);
    }

    public MSIMUserInfoAvatar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MSIMUserInfoAvatar, defStyleAttr,
                defStyleRes);
        mAvatarSize = a.getInt(R.styleable.MSIMUserInfoAvatar_avatarSize, mAvatarSize);
        a.recycle();

        mUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userId, userInfo);

                MSIMUserInfoAvatar.this.onUserInfoLoad(userId, userInfo);
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

        if (userInfo == null) {
            loadAvatar(null);
        } else {
            loadAvatar(userInfo.getAvatar());
        }
    }

    private void loadAvatar(String url) {
        setImageUrl(null, url);
    }

}
