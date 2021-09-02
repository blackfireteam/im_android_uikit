package com.masonsoft.imsdk.sample.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.sample.databinding.ImsdkSampleWidgetMainTopBarBinding;

public class MainTopBar extends FrameLayout {

    public MainTopBar(@NonNull Context context) {
        this(context, null);
    }

    public MainTopBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTopBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MainTopBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private ImsdkSampleWidgetMainTopBarBinding mBinding;

    private void initFromAttributes(
            Context context,
            AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {

        mBinding = ImsdkSampleWidgetMainTopBarBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setTitle(CharSequence title) {
        mBinding.titleText.setText(title);
    }

}
