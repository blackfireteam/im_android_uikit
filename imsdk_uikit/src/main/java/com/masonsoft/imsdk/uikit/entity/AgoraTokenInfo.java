package com.masonsoft.imsdk.uikit.entity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.core.proto.ProtoMessage;
import com.masonsoft.imsdk.lang.StateProp;

public class AgoraTokenInfo {

    public final StateProp<Long> sessionUserId = new StateProp<>();
    public final StateProp<String> token = new StateProp<>();
    public final StateProp<String> appId = new StateProp<>();

    @NonNull
    public static AgoraTokenInfo valueOf(@NonNull ProtoMessage.AgoraToken input, long sessionUserId) {
        final AgoraTokenInfo target = new AgoraTokenInfo();
        target.sessionUserId.set(sessionUserId);

        target.token.set(input.getToken());
        target.appId.set(input.getAppId());
        return target;
    }

}
