package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.R;

public class UserCacheGenderView extends UserCacheDynamicImageView {

    public UserCacheGenderView(Context context) {
        this(context, null);
    }

    public UserCacheGenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserCacheGenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UserCacheGenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setScaleType(ScaleType.CENTER_INSIDE);
        setGender(MSIMConstants.Gender.FEMALE);
    }

    @Override
    protected void onUserCacheUpdate(@Nullable MSIMUserInfo userInfo) {
        if (userInfo == null) {
            setGender(MSIMConstants.Gender.FEMALE);
        } else {
            setGender(userInfo.getGender(MSIMConstants.Gender.MALE));
        }
    }

    private void setGender(int gender) {
        if (gender == MSIMConstants.Gender.MALE) {
            setImageResource(R.drawable.imsdk_uikit_ic_gender_male);
        } else {
            // default female
            setImageResource(R.drawable.imsdk_uikit_ic_gender_female);
        }
    }

}
