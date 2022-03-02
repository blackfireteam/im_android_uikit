package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.R;

public class MSIMUserInfoGenderView extends MSIMUserInfoImageView {

    public MSIMUserInfoGenderView(Context context) {
        this(context, null);
    }

    public MSIMUserInfoGenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMUserInfoGenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMUserInfoGenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setScaleType(ScaleType.CENTER_INSIDE);
        setGender(MSIMUikitConstants.Gender.FEMALE);
    }

    @Override
    protected void onUserInfoUpdate(long userId, @Nullable MSIMUserInfo userInfo) {
        if (userInfo == null) {
            setGender(MSIMUikitConstants.Gender.FEMALE);
        } else {
            setGender(userInfo.getGender(MSIMUikitConstants.Gender.MALE));
        }
    }

    private void setGender(long gender) {
        if (gender == MSIMUikitConstants.Gender.MALE) {
            setImageResource(R.drawable.imsdk_uikit_ic_gender_male);
        } else {
            // default female
            setImageResource(R.drawable.imsdk_uikit_ic_gender_female);
        }
    }

}
