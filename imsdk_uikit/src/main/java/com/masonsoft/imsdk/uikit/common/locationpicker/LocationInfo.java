package com.masonsoft.imsdk.uikit.common.locationpicker;

import androidx.annotation.NonNull;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.core.util.Preconditions;

public class LocationInfo {

    public double lat;
    public double lng;
    public String title;
    public String subTitle;
    public String _poiId;
    public int distance;

    @NonNull
    public static LocationInfo valueOf(LatLng location, RegeocodeAddress address) {
        final LocationInfo target = new LocationInfo();
        target.lat = location.latitude;
        target.lng = location.longitude;
        target.title = I18nResources.getString(R.string.imsdk_uikit_hint_location_pick_position);
        target.subTitle = address.getFormatAddress();
        return target;
    }

    @NonNull
    public static LocationInfo valueOf(PoiItem poiItem) {
        final LocationInfo target = new LocationInfo();
        target.lat = poiItem.getLatLonPoint().getLatitude();
        target.lng = poiItem.getLatLonPoint().getLongitude();
        target.title = poiItem.getTitle();
        target.subTitle = poiItem.getCityName() + poiItem.getAdName() + poiItem.getSnippet();
        target.distance = poiItem.getDistance();
        target._poiId = poiItem.getPoiId();
        Preconditions.checkNotNull(target._poiId);
        return target;
    }

}