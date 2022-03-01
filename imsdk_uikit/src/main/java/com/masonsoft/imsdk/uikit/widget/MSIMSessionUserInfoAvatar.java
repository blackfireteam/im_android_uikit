package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.SessionUserIdChangedHelper;

public class MSIMSessionUserInfoAvatar extends MSIMUserInfoAvatar {

    @SuppressWarnings("FieldCanBeLocal")
    private SessionUserIdChangedHelper mSessionUserIdChangedHelper;

    public MSIMSessionUserInfoAvatar(Context context) {
        this(context, null);
    }

    public MSIMSessionUserInfoAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMSessionUserInfoAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.SessionUserCacheAvatar);
    }

    public MSIMSessionUserInfoAvatar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSessionUserIdChangedHelper = new SessionUserIdChangedHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                MSIMSessionUserInfoAvatar.this.setUserInfo(sessionUserId, null);
            }
        };
        MSIMSessionUserInfoAvatar.this.setUserInfo(mSessionUserIdChangedHelper.getSessionUserId(), null);
    }

}
