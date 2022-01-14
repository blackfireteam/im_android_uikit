package com.masonsoft.imsdk.uikit.common.locationpicker;

import androidx.annotation.NonNull;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.util.LngLatUtil;

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

    @NonNull
    public LocationInfo gcj02ToWGS84Location() {
        final double[] wsg = LngLatUtil.gcj02ToWGS84(this.lng, this.lat);

        final LocationInfo target = new LocationInfo();
        target.lat = wsg[1];
        target.lng = wsg[0];
        target.title = this.title;
        target.subTitle = this.subTitle;
        target._poiId = this._poiId;
        target.distance = this.distance;
        return target;
    }

    public LocationInfo wgs84ToGCJ02Location() {
        final double[] wsg = LngLatUtil.wgs84ToGCJ02(this.lng, this.lat);

        final LocationInfo target = new LocationInfo();
        target.lat = wsg[1];
        target.lng = wsg[0];
        target.title = this.title;
        target.subTitle = this.subTitle;
        target._poiId = this._poiId;
        target.distance = this.distance;
        return target;
    }

}
