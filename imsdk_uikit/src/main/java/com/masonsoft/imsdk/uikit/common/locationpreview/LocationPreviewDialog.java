package com.masonsoft.imsdk.uikit.common.locationpreview;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.ServiceSettings;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonLocationPreviewDialogBinding;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;

public class LocationPreviewDialog implements ViewBackLayer.OnBackPressedListener, ViewBackLayer.OnHideListener, LifecycleObserver {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private static final int DEFAULT_ZOOM = 16;
    private final AppCompatActivity mActivity;
    private final LayoutInflater mInflater;
    private ViewDialog mViewDialog;
    @NonNull
    private final LocationInfo mTargetLocationInfo;
    private ViewImpl mViewImpl;

    private final ImsdkUikitCommonLocationPreviewDialogBinding mBinding;

    public LocationPreviewDialog(
            AppCompatActivity activity,
            ViewGroup parentView,
            @NonNull LocationInfo targetLocationInfo) {
        ServiceSettings.updatePrivacyShow(activity, true, true);
        ServiceSettings.updatePrivacyAgree(activity, true);

        mActivity = activity;
        mInflater = mActivity.getLayoutInflater();
        mTargetLocationInfo = targetLocationInfo;
        Preconditions.checkNotNull(targetLocationInfo);
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_location_preview_dialog)
                .setOnBackPressedListener(this)
                .setOnHideListener(this)
                .setParentView(parentView)
                .create();
        mBinding = ImsdkUikitCommonLocationPreviewDialogBinding.bind(mViewDialog.getContentView());

        ViewUtil.onClick(mBinding.topBarBack, v -> hide());

        mViewImpl = new ViewImpl();

        mActivity.getLifecycle().addObserver(this);
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
        IOUtil.closeQuietly(mViewImpl);
    }

    private boolean mWasCreated;

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(LifecycleOwner owner) {
        Preconditions.checkArgument(!mWasCreated);
        mWasCreated = true;
        MSIMUikitLog.v("LocationPickerDialog onCreate");

        mBinding.mapView.onCreate(null);

        final AMap aMap = mBinding.mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.getUiSettings().setZoomGesturesEnabled(true);
        aMap.getUiSettings().setScrollGesturesEnabled(true);
        aMap.getUiSettings().setZoomPosition(DEFAULT_ZOOM);

        aMap.setMapType(AMap.MAP_TYPE_NORMAL);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.imsdk_uikit_ic_location_picker_mark));
        myLocationStyle.strokeColor(0);
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(0);
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setOnMyLocationChangeListener(mViewImpl);

        ViewUtil.onClick(mBinding.actionMoveToMyLocation, v -> {
            if (mViewImpl != null) {
                mViewImpl.moveToMyLocation();
            }
        });

        mViewImpl.followCamera(new LatLng(mTargetLocationInfo.lat, mTargetLocationInfo.lng), true);
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

    private class ViewImpl implements AMap.OnMyLocationChangeListener, Closeable {

        private boolean mClosed;
        private int mZoom = DEFAULT_ZOOM;
        private Marker mTargetMarker;
        private Location mMyLocation;

        @Override
        public void close() throws IOException {
            mClosed = true;
        }

        @Override
        public void onMyLocationChange(Location location) {
            Preconditions.checkArgument(Threads.mustUi());

            if (DEBUG) {
                MSIMUikitLog.v("LocationPickerDialog onMyLocationChange");
            }

            if (mClosed) {
                return;
            }

            mMyLocation = location;
        }

        private void moveToMyLocation() {
            Preconditions.checkArgument(Threads.mustUi());

            if (DEBUG) {
                MSIMUikitLog.v("LocationPickerDialog moveToMyLocation");
            }

            if (mClosed) {
                return;
            }

            final Location location = mMyLocation;
            if (location == null) {
                MSIMUikitLog.v("LocationPickerDialog moveToMyLocation ignore. location is null");
                return;
            }

            mZoom = DEFAULT_ZOOM;
            followCamera(new LatLng(location.getLatitude(), location.getLongitude()), true);
        }

        private void followCamera(LatLng location, boolean moveCamera) {
            Preconditions.checkArgument(Threads.mustUi());

            if (DEBUG) {
                MSIMUikitLog.v("LocationPickerDialog followCamera");
            }

            if (mClosed) {
                return;
            }

            final MapView mapView = mBinding.mapView;
            if (mapView.getWidth() <= 0) {
                MSIMUikitLog.v("ViewImpl followCamera ignore. mapView not layout.");
                return;
            }

            if (moveCamera) {
                mapView.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(location, mZoom));
            }
            if (mTargetMarker == null) {
                mTargetMarker = mapView.getMap().addMarker(new MarkerOptions());
                mTargetMarker.setPosition(new LatLng(mTargetLocationInfo.lat, mTargetLocationInfo.lng));
            }
        }

    }

}
