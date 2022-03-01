package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.masonsoft.imsdk.uikit.SessionUserIdChangedHelper;

public class SessionMSIMUserCacheNameText extends MSIMUserCacheName {

    @SuppressWarnings("FieldCanBeLocal")
    private SessionUserIdChangedHelper mSessionUserIdChangedHelper;

    public SessionMSIMUserCacheNameText(Context context) {
        this(context, null);
    }

    public SessionMSIMUserCacheNameText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionMSIMUserCacheNameText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SessionMSIMUserCacheNameText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSessionUserIdChangedHelper = new SessionUserIdChangedHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                SessionMSIMUserCacheNameText.this.setTargetUserId(sessionUserId);
            }
        };
        setTargetUserId(mSessionUserIdChangedHelper.getSessionUserId());
    }

}
