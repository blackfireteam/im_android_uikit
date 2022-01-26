package com.masonsoft.imsdk.uikit.app.rtc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;
import com.masonsoft.imsdk.uikit.util.TipUtil;

import io.github.idonans.systeminsets.SystemUiHelper;

/**
 * 实时通话
 */
public class SingleRtcChatActivity extends FragmentDelegateActivity {

    public static void start(Context context, long fromUserId, long toUserId, boolean video, String roomId) {
        Intent starter = new Intent(context, SingleRtcChatActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(MSIMUikitConstants.ExtrasKey.KEY_FROM_UID, fromUserId);
        starter.putExtra(MSIMUikitConstants.ExtrasKey.KEY_TO_UID, toUserId);
        starter.putExtra(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, video);
        starter.putExtra(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, roomId);
        context.startActivity(starter);
    }

    private static final String FRAGMENT_TAG_SINGLE_CHAT = "fragment_single_chat_rtc_20210730";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUiHelper.from(getWindow())
                .layoutStatusBar()
                .layoutNavigationBar()
                .layoutStable()
                .setLightStatusBar(false)
                .setLightNavigationBar()
                .apply();

        final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
        final long fromUserId = getIntent().getLongExtra(MSIMUikitConstants.ExtrasKey.KEY_FROM_UID, 0L);
        final long toUserId = getIntent().getLongExtra(MSIMUikitConstants.ExtrasKey.KEY_TO_UID, 0L);
        final boolean video = getIntent().getBooleanExtra(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, false);
        final String roomId = getIntent().getStringExtra(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID);
        if (fromUserId <= 0
                || toUserId <= 0
                || fromUserId == toUserId
                || (fromUserId != sessionUserId && toUserId != sessionUserId)) {
            TipUtil.show(MSIMUikitConstants.ErrorLog.INVALID_ARGS);
            finish();
            return;
        }
        setFragmentDelegate(FRAGMENT_TAG_SINGLE_CHAT, () -> SingleRtcChatFragment.newInstance(fromUserId, toUserId, video, roomId));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
