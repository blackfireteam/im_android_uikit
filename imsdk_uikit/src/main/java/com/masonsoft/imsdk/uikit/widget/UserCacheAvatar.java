package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UserCacheAvatar extends ImageLayout {

    private static final int AVATAR_SIZE_SMALL = 0;
    private static final int AVATAR_SIZE_MIDDLE = 1;
    private static final int AVATAR_SIZE_LARGE = 2;

    @IntDef({AVATAR_SIZE_SMALL, AVATAR_SIZE_MIDDLE, AVATAR_SIZE_LARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AvatarSize {
    }

    @AvatarSize
    private int mAvatarSize = AVATAR_SIZE_SMALL;
    private UserCacheChangedViewHelper mUserCacheChangedViewHelper;

    @Nullable
    private MSIMUserInfo mCacheUserInfo;

    public UserCacheAvatar(Context context) {
        this(context, null);
    }

    public UserCacheAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserCacheAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.UserCacheAvatar);
    }

    public UserCacheAvatar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UserCacheAvatar, defStyleAttr,
                defStyleRes);
        mAvatarSize = a.getInt(R.styleable.UserCacheAvatar_avatarSize, mAvatarSize);
        a.recycle();

        mUserCacheChangedViewHelper = new UserCacheChangedViewHelper() {
            @Override
            protected void onUserCacheChanged(@Nullable MSIMUserInfo userInfo) {
                mCacheUserInfo = userInfo;
                if (mCacheUserInfo == null) {
                    UserCacheAvatar.this.loadAvatar(null);
                } else {
                    UserCacheAvatar.this.loadAvatar(mCacheUserInfo.getAvatar());
                }
                UserCacheAvatar.this.invalidate();
            }
        };
    }

    public void setTargetUserId(long targetUserId) {
        mUserCacheChangedViewHelper.setTargetUserId(targetUserId);
    }

    public long getTargetUserId() {
        return mUserCacheChangedViewHelper.getTargetUserId();
    }

    private void loadAvatar(String url) {
        setImageUrl(null, url);
    }

}
