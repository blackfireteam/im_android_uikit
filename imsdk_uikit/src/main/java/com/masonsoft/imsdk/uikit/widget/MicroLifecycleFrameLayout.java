package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.view.ViewCompat;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.appcontext.AppContext;

public abstract class MicroLifecycleFrameLayout extends FrameLayout {

    protected static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MicroLifecycleFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public MicroLifecycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MicroLifecycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MicroLifecycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    protected LayoutInflater mInflater;

    protected boolean mCreated;
    protected boolean mStarted;
    protected boolean mResumed;

    /**
     * 手动模式。手动模式下在生命周期内不会自动触发 onResume，但是可以设置 allowResumedOnce 为 true 使得
     * 至多自动触发一次 onResume.
     */
    protected boolean mManual;

    /**
     * 记录包含此 micro lifecycle 的父级是否处于 resumed 状态。只有当 perform resumed 为 true 是，才能够
     * 通过 {@linkplain #toggleWithManual} 在 onResume 和 onPause 之间手动切换。
     */
    protected boolean mPerformResumed;

    /**
     * 默认是否自动触发 onResume。例如在播放器中通常用来处理是否总是自动播放的情形。
     */
    protected boolean mDefaultManual;

    /**
     * 是否在生命周期内的第一次 onResume 总是可以自动触发。例如在播放器中通常用来处理第一次自动播放的情形。
     */
    protected boolean mAllowResumedOnce;

    public interface OnResumedChangedListener {
        void onResumedChanged(boolean resumed);
    }

    private OnResumedChangedListener mOnResumedChangedListener;

    public void setOnResumedChangedListener(OnResumedChangedListener listener) {
        mOnResumedChangedListener = listener;
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
        mInflater = LayoutInflater.from(context);
    }

    protected void setContentView(@LayoutRes int layout) {
        mInflater.inflate(layout, this, true);
    }

    public void setDefaultManual(boolean defaultManual) {
        mDefaultManual = defaultManual;
    }

    public void setAllowResumedOnce(boolean allowResumedOnce) {
        mAllowResumedOnce = allowResumedOnce;
    }

    protected void toggleWithManual() {
        if (mPerformResumed) {
            if (isResumed()) {
                setManual();
                performPause();
                mPerformResumed = true;
            } else {
                clearManual();
                performResume();
            }
        } else {
            MSIMUikitLog.e("ignore toggleWithManual. perform resumed is false.");
        }
    }

    /**
     * 设置为手动模式。
     */
    @UiThread
    public void setManual() {
        mManual = true;
    }

    /**
     * 清除手动模式
     */
    @UiThread
    public void clearManual() {
        mManual = false;
    }

    /**
     * 是否处于手动模式
     */
    public boolean isManual() {
        return mManual;
    }

    @UiThread
    public void performCreate() {
        if (mCreated) {
            MSIMUikitLog.e("ignore, already created");
            return;
        }

        mManual = mDefaultManual;

        mCreated = true;
        onCreate();
    }

    @UiThread
    public void performStart() {
        if (mStarted) {
            MSIMUikitLog.e("ignore, already started");
            return;
        }
        mStarted = true;
        onStart();
    }

    @UiThread
    public void performResume() {
        mPerformResumed = true;

        if (mResumed) {
            MSIMUikitLog.v("ignore, already resume");
            return;
        }

        if (mManual && !mAllowResumedOnce) {
            MSIMUikitLog.v("can not resume, manual:%s, allow resumed once: %s", mManual, mAllowResumedOnce);
            return;
        }

        mAllowResumedOnce = false;

        mResumed = true;
        onResume();
        if (mOnResumedChangedListener != null) {
            mOnResumedChangedListener.onResumedChanged(true);
        }
    }

    public boolean isResumed() {
        return mResumed;
    }

    @UiThread
    public void performPause() {
        performPause(mManual || mDefaultManual);
    }

    @UiThread
    protected void performPause(boolean manual) {
        mPerformResumed = false;
        mAllowResumedOnce = false;
        mManual = manual;

        if (!mResumed) {
            MSIMUikitLog.v("ignore. not resume");
            return;
        }

        mResumed = false;
        onPause();
        if (mOnResumedChangedListener != null) {
            mOnResumedChangedListener.onResumedChanged(false);
        }
    }

    @UiThread
    public void performStop() {
        if (!mStarted) {
            MSIMUikitLog.e("ignore, not started.");
            return;
        }
        mStarted = false;
        onStop();
    }

    @UiThread
    public void performDestroy() {
        if (!mCreated) {
            MSIMUikitLog.e("ignore, not created.");
            return;
        }
        mCreated = false;
        onDestroy();
    }

    @CallSuper
    @UiThread
    protected void onCreate() {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onCreate");
        }
    }

    @CallSuper
    @UiThread
    protected void onStart() {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onStart");
        }
    }

    @CallSuper
    @UiThread
    protected void onResume() {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onResume");
        }
    }

    @CallSuper
    @UiThread
    protected void onPause() {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onPause");
        }
    }

    @CallSuper
    @UiThread
    protected void onStop() {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onStop");
        }
    }

    @CallSuper
    @UiThread
    protected void onDestroy() {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onDestroy");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ViewCompat.requestApplyInsets(this);
    }

}
