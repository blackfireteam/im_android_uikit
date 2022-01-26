package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SessionUserCacheGenderView extends UserCacheGenderView {

    @SuppressWarnings("FieldCanBeLocal")
    private SessionUserIdChangedViewHelper mSessionUserIdChangedViewHelper;

    public SessionUserCacheGenderView(Context context) {
        this(context, null);
    }

    public SessionUserCacheGenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionUserCacheGenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SessionUserCacheGenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSessionUserIdChangedViewHelper = new SessionUserIdChangedViewHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                SessionUserCacheGenderView.this.setTargetUserId(sessionUserId);
            }
        };
        setTargetUserId(mSessionUserIdChangedViewHelper.getSessionUserId());
    }

}
