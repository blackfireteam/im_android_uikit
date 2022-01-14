package com.masonsoft.imsdk.uikit.common.locationpicker;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonLocationPickerDialogBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.widget.systeminsets.SoftKeyboardListenerLayout;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.dynamic.page.UnionTypeStatusPageView;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class LocationPickerDialog implements ViewBackLayer.OnBackPressedListener, ViewBackLayer.OnHideListener, LifecycleObserver {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private static final int DEFAULT_ZOOM = 16;
    private final AppCompatActivity mActivity;
    private final LayoutInflater mInflater;
    private ViewDialog mViewDialog;
    private ViewImpl mViewImpl;
    private PresenterImpl mPresenter;

    private final ImsdkUikitCommonLocationPickerDialogBinding mBinding;

    public LocationPickerDialog(AppCompatActivity activity, ViewGroup parentView) {
        ServiceSettings.updatePrivacyShow(activity, true, true);
        ServiceSettings.updatePrivacyAgree(activity, true);

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
        mBinding.softKeyboardListenerLayout.addOnDispatchTouchEventListener(new SoftKeyboardListenerLayout.FirstMoveOrUpTouchEventListener() {
            @Override
            public void onFirstMoveOrUpTouchEvent(MotionEvent event, float dx, float dy) {
                float rawX = event.getRawX();
                float rawY = event.getRawY();

//                if (mBinding.softKeyboardListenerLayout.isSoftKeyboardShown()) {
//                    if (isTouchOutsideEditText(rawX, rawY)) {
//                        SystemUtil.hideSoftKeyboard(mBinding.editText);
//                    }
//                }

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
        mBinding.topBottomLayout.setOnCollapseChangedListener(collapse ->
                ViewUtil.setVisibilityIfChanged(mBinding.actionCollapse, collapse ? View.GONE : View.VISIBLE));
        ViewUtil.setVisibilityIfChanged(mBinding.actionCollapse,
                mBinding.topBottomLayout.isCollapsed() ? View.GONE : View.VISIBLE);

        final RecyclerView recyclerView = mBinding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setHasFixedSize(true);
        final UnionTypeAdapter adapter = new UnionTypeAdapter();
        adapter.setHost(Host.Factory.create(mActivity, recyclerView, adapter));
        adapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
        recyclerView.setAdapter(adapter);

        mViewImpl = new ViewImpl(adapter);
        mPresenter = new PresenterImpl(mViewImpl);
        mViewImpl.setPresenter(mPresenter);

        mActivity.getLifecycle().addObserver(this);
    }

    private final Rect mTmpTouchAreaCheckRect = new Rect();

//    private boolean isTouchOutsideEditText(float rawX, float rawY) {
//        final View targetView = mBinding.editText;
//        int[] outLocation = new int[2];
//        targetView.getLocationInWindow(outLocation);
//        mTmpTouchAreaCheckRect.set(
//                outLocation[0],
//                outLocation[1],
//                outLocation[0] + targetView.getWidth(),
//                outLocation[1] + targetView.getHeight()
//        );
//        return !mTmpTouchAreaCheckRect.contains(((int) rawX), ((int) rawY));
//    }

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

        final View topBarView = mBinding.topBar;
        topBarView.getLocationInWindow(outLocation);
        mTmpTouchAreaCheckRect.top = outLocation[1] + topBarView.getHeight();

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
        IOUtil.closeQuietly(mViewImpl);
    }

    public interface OnLocationPickListener {
        /**
         * 关闭 LocationPickerDialog 返回 true.
         *
         * @param locationInfo 当前选择的位置
         * @param zoom         当前地图的缩放层级
         * @return 关闭 LocationPickerDialog 返回 true.
         */
        boolean onLocationPick(@NonNull LocationInfo locationInfo, long zoom);
    }

    private OnLocationPickListener mOnLocationPickListener;

    public void setOnLocationPickListener(OnLocationPickListener listener) {
        mOnLocationPickListener = listener;
    }

    private boolean mWasCreated;

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(LifecycleOwner owner) {
        Preconditions.checkArgument(!mWasCreated);
        mWasCreated = true;
        MSIMUikitLog.v("LocationPickerDialog onCreate");

        mBinding.mapView.onCreate(null);
        mBinding.mapView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            Threads.postUi(() -> {
                if (mViewImpl != null) {
                    mViewImpl.updateMarkerCenterPointPosition(true);
                }
            });
        });

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
        aMap.setOnCameraChangeListener(mViewImpl);

        ViewUtil.onClick(mBinding.actionMoveToMyLocation, v -> {
            if (mViewImpl != null) {
                mViewImpl.moveToMyLocation();
            }
        });
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
        final LocationInfo selectedLocationInfo = mViewImpl.getSelectedLocationInfo();
        if (selectedLocationInfo == null) {
            MSIMUikitLog.e("unexpected. selectedLocationInfo is null");
            return;
        }
        long zoom = mViewImpl.mZoom;
        if (zoom <= 0) {
            zoom = DEFAULT_ZOOM;
        }

        if (mOnLocationPickListener != null) {
            if (mOnLocationPickListener.onLocationPick(selectedLocationInfo.gcj02ToWGS84Location(), zoom)) {
                hide();
            }
        }
    }

    private class ViewImpl extends UnionTypeStatusPageView<GeneralResult> implements AMap.OnMyLocationChangeListener, AMap.OnCameraChangeListener, Closeable {

        private boolean mClosed;
        private int mZoom = DEFAULT_ZOOM;
        private Marker mMarkerCenterPoint;
        private boolean mFollowMyLocation;
        private Location mMyLocation;
        private LocationInfo mSelectedLocationInfo;

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            super(adapter);
            setAlwaysHidePrePageNoMoreData(true);
            setClearContentWhenRequestInit(true);
        }

        @Override
        public void close() throws IOException {
            mClosed = true;
        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            // ignore
        }

        private void onSelectedChanged(LocationInfo locationInfo) {
            mSelectedLocationInfo = locationInfo;
            mBinding.topBarSubmit.setEnabled(locationInfo != null);
        }

        @Nullable
        public LocationInfo getSelectedLocationInfo() {
            return mSelectedLocationInfo;
        }

        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            Preconditions.checkArgument(Threads.mustUi());

            if (DEBUG) {
                MSIMUikitLog.v("LocationPickerDialog onCameraChangeFinish");
            }

            if (mClosed) {
                return;
            }

            mZoom = (int) cameraPosition.zoom;
            followCamera(cameraPosition.target, false);
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

            if (mFollowMyLocation) {
                return;
            }
            mFollowMyLocation = true;

            mZoom = DEFAULT_ZOOM;
            followCamera(new LatLng(location.getLatitude(), location.getLongitude()), true);
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
            if (moveCamera) {
                mapView.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(location, mZoom));
            }

            updateMarkerCenterPointPosition(false);

            mPresenter.startResearch(location);
        }

        private void updateMarkerCenterPointPosition(boolean force) {
            Preconditions.checkArgument(Threads.mustUi());

            if (mClosed) {
                return;
            }
            final MapView mapView = mBinding.mapView;
            if (mapView.getWidth() <= 0) {
                return;
            }
            if (mMarkerCenterPoint == null) {
                mMarkerCenterPoint = mapView.getMap().addMarker(new MarkerOptions());
                mMarkerCenterPoint.setPositionByPixels(mapView.getWidth() / 2, mapView.getHeight() / 2);
                return;
            }

            if (force) {
                mMarkerCenterPoint.setPositionByPixels(mapView.getWidth() / 2, mapView.getHeight() / 2);
            }
        }

    }

    private static class PresenterImpl extends PagePresenter<UnionTypeItemObject, GeneralResult, ViewImpl> {

        private LatLng mLocation;
        private int mPoiSearchPageNo = -1;
        private LocationInfo mFirstLocationInfo;
        private final ObjectWrapper mLocationInfoSelectedWrapper = new ObjectWrapper(null) {
            @Override
            public void setObject(@Nullable Object object) {
                super.setObject(object);

                final ViewImpl view = getView();
                if (view != null) {
                    view.onSelectedChanged((LocationInfo) object);
                }
            }
        };

        public PresenterImpl(ViewImpl view) {
            super(view);
            setPrePageRequestEnable(false);
        }

        private void startResearch(LatLng location) {
            mLocation = location;

            mFirstLocationInfo = null;
            mLocationInfoSelectedWrapper.setObject(null);
            mPoiSearchPageNo = -1;
            setNextPageRequestEnable(false);

            requestInit(true);
        }

        @Nullable
        @Override
        protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createInitRequest() throws Exception {
            if (DEBUG) {
                MSIMUikitLog.v("LocationPickerDialog createInitRequest");
            }
            return Single.just("")
                    .map(input -> {
                        final List<LocationInfo> result = new ArrayList<>();
                        final LatLng location = mLocation;

                        {
                            // 先搜索第一页 poi
                            PoiSearch.Query query = new PoiSearch.Query("", MSIMUikitConstants.POI_TYPE);
                            query.setPageSize(20);
                            query.setPageNum(1);
                            PoiSearch poiSearch = new PoiSearch(ContextUtil.getContext(), query);
                            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(location.latitude, location.longitude), 1500));
                            PoiResult poiResult = poiSearch.searchPOI();
                            final List<PoiItem> poiItems = poiResult.getPois();
                            if (poiItems != null) {
                                final LocationInfo firstLocationInfo = mFirstLocationInfo;
                                for (PoiItem poiItem : poiItems) {
                                    if (firstLocationInfo != null) {
                                        if (firstLocationInfo._poiId != null && firstLocationInfo._poiId.equals(poiItem.getPoiId())) {
                                            continue;
                                        }
                                    }
                                    result.add(LocationInfo.valueOf(poiItem));
                                }
                            }
                        }

                        // 是否需要将当前定位逆地理编码
                        final boolean requireRegeocodeQuery;
                        if (result.isEmpty()) {
                            requireRegeocodeQuery = true;
                        } else {
                            // 如果第一个 poi 的位置距离在 100 米以外，则将当前定位逆地理编码
                            requireRegeocodeQuery = result.get(0).distance > 100;

                        }

                        if (requireRegeocodeQuery) {
                            GeocodeSearch search = new GeocodeSearch(ContextUtil.getContext());
                            RegeocodeQuery query = new RegeocodeQuery(
                                    new LatLonPoint(location.latitude, location.longitude),
                                    200,
                                    GeocodeSearch.AMAP
                            );
                            RegeocodeAddress address = search.getFromLocation(query);
                            result.add(0, LocationInfo.valueOf(location, address));
                        }

                        return result;
                    })
                    .map(locationInfoList -> {
                        final List<UnionTypeItemObject> target = new ArrayList<>();
                        for (LocationInfo locationInfo : locationInfoList) {
                            target.add(createUnionTypeItemObject(locationInfo));
                        }
                        return new DynamicResult<UnionTypeItemObject, GeneralResult>()
                                .setItems(target);
                    });
        }

        @Override
        protected void onInitRequestResult(@NonNull ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            MSIMUikitLog.v("LocationPickerDialog onInitRequestResult");
            if (result.items == null || result.items.isEmpty()) {
                mFirstLocationInfo = null;
                mLocationInfoSelectedWrapper.setObject(null);
                mPoiSearchPageNo = -1;
                setNextPageRequestEnable(false);
            } else {
                final UnionTypeItemObject firstUnionTypeItemObject = ((UnionTypeItemObject) ((List<?>) result.items).get(0));
                //noinspection ConstantConditions
                final LocationInfo locationInfo = firstUnionTypeItemObject.getItemObject(DataObject.class).getObject(LocationInfo.class);
                mFirstLocationInfo = locationInfo;
                mLocationInfoSelectedWrapper.setObject(locationInfo);

                if (result.items.size() > 1) {
                    mPoiSearchPageNo = 1;
                    setNextPageRequestEnable(true);
                } else {
                    mPoiSearchPageNo = -1;
                    setNextPageRequestEnable(false);
                }
            }

            super.onInitRequestResult(view, result);
        }

        @Nullable
        @Override
        protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createNextPageRequest() throws Exception {
            if (DEBUG) {
                MSIMUikitLog.v("LocationPickerDialog createNextPageRequest");
            }

            return Single.just("")
                    .map(input -> {
                        final List<LocationInfo> result = new ArrayList<>();
                        final LatLng location = mLocation;
                        final int pageNo = mPoiSearchPageNo;
                        if (location != null && pageNo >= 1) {
                            PoiSearch.Query query = new PoiSearch.Query("", MSIMUikitConstants.POI_TYPE);
                            query.setPageSize(20);
                            query.setPageNum(pageNo + 1);
                            PoiSearch poiSearch = new PoiSearch(ContextUtil.getContext(), query);
                            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(location.latitude, location.longitude), 1500));
                            PoiResult poiResult = poiSearch.searchPOI();
                            final List<PoiItem> poiItems = poiResult.getPois();
                            if (poiItems != null) {
                                final LocationInfo firstLocationInfo = mFirstLocationInfo;
                                for (PoiItem poiItem : poiItems) {
                                    if (firstLocationInfo != null) {
                                        if (firstLocationInfo._poiId != null && firstLocationInfo._poiId.equals(poiItem.getPoiId())) {
                                            continue;
                                        }
                                    }
                                    result.add(LocationInfo.valueOf(poiItem));
                                }
                            }
                        }

                        return result;
                    })
                    .map(locationInfoList -> {
                        final List<UnionTypeItemObject> target = new ArrayList<>();
                        for (LocationInfo locationInfo : locationInfoList) {
                            target.add(createUnionTypeItemObject(locationInfo));
                        }
                        return new DynamicResult<UnionTypeItemObject, GeneralResult>()
                                .setItems(target);
                    });
        }

        @Override
        protected void onNextPageRequestResult(@NonNull ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            if (DEBUG) {
                MSIMUikitLog.v("LocationPickerDialog onNextPageRequestResult");
            }

            if (result.items != null && !result.items.isEmpty()) {
                mPoiSearchPageNo++;
            }

            super.onNextPageRequestResult(view, result);
        }

        private UnionTypeItemObject createUnionTypeItemObject(LocationInfo locationInfo) {
            final DataObject dataObject = new DataObject(locationInfo);
            dataObject.putExtObjectObject1(mLocationInfoSelectedWrapper);
            dataObject.putExtHolderItemClick1(mOnHolderItemClickListener);
            return new UnionTypeItemObject(
                    IMUikitUnionTypeMapper.UNION_TYPE_IMPL_LOCATION_PICKER_SIMPLE_LOCATION_ITEM,
                    dataObject
            );
        }

        @SuppressLint("NotifyDataSetChanged")
        private final UnionTypeViewHolderListeners.OnItemClickListener mOnHolderItemClickListener = viewHolder -> {
            final DataObject dataObject = viewHolder.getItemObject(DataObject.class);
            if (dataObject == null) {
                return;
            }
            final LocationInfo locationInfo = dataObject.getObject(LocationInfo.class);
            if (locationInfo == null) {
                return;
            }

            if (mLocationInfoSelectedWrapper.getObject() == locationInfo) {
                return;
            }

            mLocationInfoSelectedWrapper.setObject(locationInfo);
            viewHolder.host.getAdapter().notifyDataSetChanged();
        };
    }

}
