package com.masonsoft.imsdk.uikit;

import com.masonsoft.imsdk.annotation.DemoOnly;
import com.masonsoft.imsdk.uikit.util.LngLatUtil;

import java.util.concurrent.TimeUnit;

import io.github.idonans.core.util.HumanUtil;

public class MSIMUikitConstants {

    public static final boolean DEBUG_WIDGET = true;

    public interface ExtrasKey {
        String KEY_BOOLEAN = "extra:boolean_20210419";
        String KEY_INTEGER = "extra:integer_20210419";
        String TARGET_USER_ID = "extra:targetUserId_20210419";
        String LONG_ANIMATE_DURATION = "extra:long_animate_duration_20210416";
        String KEY_FROM_UID = "extra:fromUid_20210730";
        String KEY_TO_UID = "extra:toUid_20210730";
        String KEY_ROOM_ID = "extra:roomId_20210817";
    }

    public interface ErrorLog {
        String FRAGMENT_MANAGER_STATE_SAVED = "fragment manager is state saved";
        String ACTIVITY_NOT_FOUND_IN_FRAGMENT = "activity not found in fragment";
        String ACTIVITY_IS_FINISHING = "activity is finishing";
        String PRESENTER_IS_NULL = "presenter is null";
        String BINDING_IS_NULL = "binding is null";
        String ACTIVITY_IS_NULL = "activity is null";
        String FRAGMENT_IS_NULL = "fragment is null";
        String EDITABLE_IS_NULL = "editable is null";
        String SOFT_KEYBOARD_HELPER_IS_NULL = "soft keyboard helper is null";
        String ACTIVITY_IS_NOT_APP_COMPAT_ACTIVITY = "activity is not AppCompatActivity";
        String PERMISSION_REQUIRED = "permission required";
        String INVALID_USER_ID = "user id is invalid";
        String INVALID_POSITION = "position is invalid";
        String INVALID_TARGET = "target is invalid";
        String INVALID_ARGS = "one or more args is invalid";
        String INVALID_CHAT_ROOM_ID = "chat room id is invalid";
        String INVALID_SESSION_USER_ID = "session user id is invalid";
        String INVALID_CHAT_ROOM_INFO = "chat room info is invalid";
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    /**
     * 图片选择中允许的最大图片尺寸(width*height*4)
     */
    public static final long SELECTOR_MAX_IMAGE_SIZE = 200 * HumanUtil.MB;
    /**
     * 图片选择中允许的最大图片文件大小
     */
    public static final long SELECTOR_MAX_IMAGE_FILE_SIZE = 15 * HumanUtil.MB;
    /**
     * 视频选择中允许的最大视频文件大小(file.length)
     */
    public static final long SELECTOR_MAX_VIDEO_SIZE = 300 * HumanUtil.MB;
    /**
     * 视频选择中允许的最长时长 ms (video.duration)
     */
    public static final long SELECTOR_MAX_VIDEO_DURATION = TimeUnit.MINUTES.toMillis(10);
    /**
     * 视频选择中允许的最短时长 ms (video.duration)
     */
    public static final long SELECTOR_MIN_VIDEO_DURATION = TimeUnit.SECONDS.toMillis(1);
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    /**
     * 录音的最大时长 ms
     */
    public static final long AUDIO_RECORD_MAX_DURATION = TimeUnit.SECONDS.toMillis(60);
    /**
     * 录音的最短时长 ms
     */
    public static final long AUDIO_RECORD_MIN_DURATION = TimeUnit.SECONDS.toMillis(1);
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    /**
     * 定制业务消息类型
     *
     * @see com.masonsoft.imsdk.MSIMConstants.MessageType#isBusinessMessage(int)
     * @see com.masonsoft.imsdk.MSIMConstants.MessageType#isSendableBusinessMessage(int)
     */
    public static class BusinessMessageType {
        /**
         * 用于 spark 上的 like 消息
         */
        @DemoOnly
        public static final int LIKE = 11;
        /**
         * 媚眼
         */
        public static final int WINK = 32;
        /**
         * 匹配
         */
        public static final int MATCH = 33;
        /**
         * 取消匹配
         */
        public static final int UNMATCH = 34;
    }

    public static class Gender {
        public static final long FEMALE = 0;
        public static final long MALE = 1;
    }

    public static final String POI_TYPE = "汽车销售|餐饮服务|购物服务|生活服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|地名地址信息|公共设施";

    public static String buildStaticAMapUrl(double wgsLat, double wgsLng, int zoom) {
        final double[] amapLngLat = LngLatUtil.wgs84ToGCJ02(wgsLng, wgsLat);
        return "https://restapi.amap.com/v3/staticmap?location=" + amapLngLat[0] + "," + amapLngLat[1]
                + "&zoom=" + zoom + "&size=550*300&markers=mid,,A:" + amapLngLat[0] + "," + amapLngLat[1]
                + "&key=d696e2e8fc6c1685be52faa64b66d318";
    }

}
