package com.masonsoft.imsdk.uikit.common.locationpicker;

import androidx.annotation.NonNull;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.geocoder.RegeocodeAddress;

public class LocationInfo {

    public double lat;
    public double lng;
    public String title;
    public String subTitle;
    public int zoom;

    @NonNull
    public static LocationInfo valueOf(LatLng location, int zoom, RegeocodeAddress address) {
        final LocationInfo target = new LocationInfo();
        target.lat = location.latitude;
        target.lng = location.longitude;
        target.zoom = zoom;
        target.title = address.getDistrict() + address.getTownship();
        target.subTitle = address.getFormatAddress();
        return target;
    }

}
