package com.masonsoft.imsdk.uikit.common.locationpicker;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.amap.api.maps2d.AMap;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonLocationPickerDialogBinding;
import com.masonsoft.imsdk.uikit.widget.systeminsets.SoftKeyboardListenerLayout;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.util.SystemUtil;
import io.github.idonans.lang.util.ViewUtil;

public class LocationPickerDialog implements ViewBackLayer.OnBackPressedListener, ViewBackLayer.OnHideListener, LifecycleObserver {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private final AppCompatActivity mActivity;
    private final LayoutInflater mInflater;
    private ViewDialog mViewDialog;

    private final ImsdkUikitCommonLocationPickerDialogBinding mBinding;

    public LocationPickerDialog(AppCompatActivity activity, ViewGroup parentView) {
        mActivity = activity;
        mInflater = mActivity.getLayoutInflater();
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_location_picker_dialog)
                .setOnBackPressedListener(this)
                .setOnHideListener(this)
                .setParentView(parentView)
                .create();
        mBinding = ImsdkUikitCommonLocationPickerDialogBinding.bind(mViewDialog.getContentView());

        mBinding.softKeyboardListenerLayout.setOnSoftKeyboardChangedListener(new SoftKeyboardListenerLayout.OnSoftKeyboardChangedListener() {
            @Override
            public void onSoftKeyboardShown() {
                mBinding.topBottomLayout.setCollapse(false);
            }

            @Override
            public void onSoftKeyboardHidden() {
            }
        });
        ViewUtil.onClick(mBinding.topBarBack, v -> hide());
        ViewUtil.onClick(mBinding.topBarSubmit, v -> onSubmitClick());
        mBinding.topBarSubmit.setEnabled(false);
        ViewUtil.onClick(mBinding.actionCollapse, v -> mBinding.topBottomLayout.setCollapse(true));
        ViewUtil.onClick(mBinding.actionExpand, v -> mBinding.topBottomLayout.setCollapse(false));
        mBinding.softKeyboardListenerLayout.addOnDispatchTouchEventListener(new SoftKeyboardListenerLayout.FirstMoveOrUpTouchEventListener() {
            @Override
            public void onFirstMoveOrUpTouchEvent(MotionEvent event, float dx, float dy) {
                float rawX = event.getRawX();
                float rawY = event.getRawY();

                if (mBinding.softKeyboardListenerLayout.isSoftKeyboardShown()) {
                    if (isTouchOutsideEditText(rawX, rawY)) {
                        SystemUtil.hideSoftKeyboard(mBinding.editText);
                    }
                }

                if (isTouchInMapView(rawX, rawY)) {
                    // 点击了地图部分, 收起
                    mBinding.topBottomLayout.setCollapse(true);
                } else {
                    if (dy > 0) {
                        // 可能产生了向上滑动手势(非严谨，可能水平滑动的趋势更大)
                        if (isTouchInBottomListContainer(rawX, rawY)) {
                            // 在底部内容区域向上滑动，展开
                            mBinding.topBottomLayout.setCollapse(false);
                        }
                    }
                }
            }
        });

        mActivity.getLifecycle().addObserver(this);
    }

    private final Rect mTmpTouchAreaCheckRect = new Rect();

    private boolean isTouchOutsideEditText(float rawX, float rawY) {
        final View targetView = mBinding.editText;
        int[] outLocation = new int[2];
        targetView.getLocationInWindow(outLocation);
        mTmpTouchAreaCheckRect.set(
                outLocation[0],
                outLocation[1],
                outLocation[0] + targetView.getWidth(),
                outLocation[1] + targetView.getHeight()
        );
        return !mTmpTouchAreaCheckRect.contains(((int) rawX), ((int) rawY));
    }

    private boolean isTouchInMapView(float rawX, float rawY) {
        final View targetView = mBinding.mapView;
        int[] outLocation = new int[2];
        targetView.getLocationInWindow(outLocation);
        mTmpTouchAreaCheckRect.set(
                outLocation[0],
                outLocation[1],
                outLocation[0] + targetView.getWidth(),
                outLocation[1] + targetView.getHeight()
        );
        return mTmpTouchAreaCheckRect.contains(((int) rawX), ((int) rawY));
    }

    private boolean isTouchInBottomListContainer(float rawX, float rawY) {
        final View targetView = mBinding.bottomListContainer;
        int[] outLocation = new int[2];
        targetView.getLocationInWindow(outLocation);
        mTmpTouchAreaCheckRect.set(
                outLocation[0],
                outLocation[1],
                outLocation[0] + targetView.getWidth(),
                outLocation[1] + targetView.getHeight()
        );
        return mTmpTouchAreaCheckRect.contains(((int) rawX), ((int) rawY));
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(false);
    }

    @Override
    public void onHide(boolean cancel) {
        mActivity.getLifecycle().removeObserver(this);
    }

    public interface OnLocationPickListener {
        /**
         * 关闭 LocationPickerDialog 返回 true.
         *
         * @param locationInfo 当前选择的位置
         * @return 关闭 LocationPickerDialog 返回 true.
         */
        boolean onLocationPick(@NonNull LocationInfo locationInfo);
    }

    private OnLocationPickListener mOnLocationPickListener;

    public void setOnLocationPickListener(OnLocationPickListener listener) {
        mOnLocationPickListener = listener;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(LifecycleOwner owner) {
        mBinding.mapView.onCreate(null);

        final AMap map = mBinding.mapView.getMap();
        // TODO
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(LifecycleOwner owner) {
        mBinding.mapView.onResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(LifecycleOwner owner) {
        mBinding.mapView.onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(LifecycleOwner owner) {
        mBinding.mapView.onDestroy();
    }

    private void onSubmitClick() {
        MSIMUikitLog.v("onSubmitClick");
    }

}
