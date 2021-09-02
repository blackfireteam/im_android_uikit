package com.masonsoft.imsdk.sample.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.sample.databinding.ImsdkSampleWidgetMainBottomBarBinding;

import io.github.idonans.lang.util.ViewUtil;

public class MainBottomBar extends FrameLayout {

    public MainBottomBar(@NonNull Context context) {
        this(context, null);
    }

    public MainBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainBottomBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MainBottomBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private static final int TAB_HOME = 0;
    private static final int TAB_DISCOVER = 1;
    private static final int TAB_CONVERSATION = 2;
    private static final int TAB_MINE = 3;

    private int mCurrentItem = 0;
    private ImsdkSampleWidgetMainBottomBarBinding mBinding;
    private OnTabClickListener mOnTabClickListener;

    private void initFromAttributes(
            Context context,
            AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {

        mBinding = ImsdkSampleWidgetMainBottomBarBinding.inflate(
                LayoutInflater.from(context),
                this,
                true);
        syncCurrentItem();

        ViewUtil.onClick(mBinding.tabHome, v -> {
            if (mOnTabClickListener != null) {
                mOnTabClickListener.onTabClick(TAB_HOME);
            }
        });
        ViewUtil.onClick(mBinding.tabDiscover, v -> {
            if (mOnTabClickListener != null) {
                mOnTabClickListener.onTabClick(TAB_DISCOVER);
            }
        });
        ViewUtil.onClick(mBinding.tabConversation, v -> {
            if (mOnTabClickListener != null) {
                mOnTabClickListener.onTabClick(TAB_CONVERSATION);
            }
        });
        ViewUtil.onClick(mBinding.tabMine, v -> {
            if (mOnTabClickListener != null) {
                mOnTabClickListener.onTabClick(TAB_MINE);
            }
        });
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public void setCurrentItem(int currentItem) {
        if (mCurrentItem != currentItem) {
            mCurrentItem = currentItem;

            syncCurrentItem();
        }
    }

    private void syncCurrentItem() {
        mBinding.tabHome.setSelected(mCurrentItem == TAB_HOME);
        mBinding.tabDiscover.setSelected(mCurrentItem == TAB_DISCOVER);
        mBinding.tabConversation.setSelected(mCurrentItem == TAB_CONVERSATION);
        mBinding.tabMine.setSelected(mCurrentItem == TAB_MINE);
    }

    public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
        mOnTabClickListener = onTabClickListener;
    }

    public interface OnTabClickListener {
        void onTabClick(int index);
    }

}
