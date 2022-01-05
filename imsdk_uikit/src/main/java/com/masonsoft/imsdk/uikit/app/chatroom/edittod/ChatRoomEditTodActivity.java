package com.masonsoft.imsdk.uikit.app.chatroom.edittod;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.app.FragmentDelegateActivity;
import com.masonsoft.imsdk.uikit.util.TipUtil;

import io.github.idonans.systeminsets.SystemUiHelper;

public class ChatRoomEditTodActivity extends FragmentDelegateActivity {

    public static void start(Context context) {
        start(context, GlobalChatRoomManager.DEFAULT_CHAT_ROOM_ID);
    }

    public static void start(Context context, long chatRoomId) {
        Intent starter = new Intent(context, ChatRoomEditTodActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, chatRoomId);
        context.startActivity(starter);
    }

    private static final String FRAGMENT_TAG_CHAT_ROOM_SETTINGS = "fragment_chat_room_edit_tod_20220105";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUiHelper.from(getWindow())
                .layoutStatusBar()
                .layoutNavigationBar()
                .layoutStable()
                .setLightStatusBar()
                .setLightNavigationBar()
                .apply();

        final long chatRoomId = getIntent().getLongExtra(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, 0L);
        if (chatRoomId <= 0) {
            TipUtil.show(MSIMUikitConstants.ErrorLog.INVALID_CHAT_ROOM_ID);
            finish();
            return;
        }
        setFragmentDelegate(FRAGMENT_TAG_CHAT_ROOM_SETTINGS, () -> ChatRoomEditTodFragment.newInstance(chatRoomId));
    }

}
