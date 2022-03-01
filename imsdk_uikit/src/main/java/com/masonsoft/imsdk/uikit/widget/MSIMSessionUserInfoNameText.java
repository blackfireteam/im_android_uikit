package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.masonsoft.imsdk.uikit.SessionUserIdChangedHelper;

public class MSIMSessionUserInfoNameText extends MSIMUserCacheName {

    @SuppressWarnings("FieldCanBeLocal")
    private SessionUserIdChangedHelper mSessionUserIdChangedHelper;

    public MSIMSessionUserInfoNameText(Context context) {
        this(context, null);
    }

    public MSIMSessionUserInfoNameText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMSessionUserInfoNameText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMSessionUserInfoNameText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSessionUserIdChangedHelper = new SessionUserIdChangedHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                MSIMSessionUserInfoNameText.this.setUserInfo(sessionUserId, null);
            }
        };
        MSIMSessionUserInfoNameText.this.setUserInfo(mSessionUserIdChangedHelper.getSessionUserId(), null);
    }

}
