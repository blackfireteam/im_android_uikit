package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.masonsoft.imsdk.uikit.R;

public class SessionUserCacheAvatar extends UserCacheAvatar {

    @SuppressWarnings("FieldCanBeLocal")
    private SessionUserIdChangedViewHelper mSessionUserIdChangedViewHelper;

    public SessionUserCacheAvatar(Context context) {
        this(context, null);
    }

    public SessionUserCacheAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionUserCacheAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.SessionUserCacheAvatar);
    }

    public SessionUserCacheAvatar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSessionUserIdChangedViewHelper = new SessionUserIdChangedViewHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                setTargetUserId(sessionUserId);
            }
        };
        setTargetUserId(mSessionUserIdChangedViewHelper.getSessionUserId());
    }

}
