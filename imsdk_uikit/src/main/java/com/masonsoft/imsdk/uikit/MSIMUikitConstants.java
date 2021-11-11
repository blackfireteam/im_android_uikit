package com.masonsoft.imsdk.uikit;

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

    public static class Gender {
        public static final long FEMALE = 0;
        public static final long MALE = 1;
    }

}
