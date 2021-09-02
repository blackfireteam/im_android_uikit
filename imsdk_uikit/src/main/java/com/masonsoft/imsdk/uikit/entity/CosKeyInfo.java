package com.masonsoft.imsdk.uikit.entity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.core.proto.ProtoMessage;
import com.masonsoft.imsdk.lang.StateProp;

public class CosKeyInfo {

    public final StateProp<Long> sessionUserId = new StateProp<>();
    public final StateProp<String> token = new StateProp<>();
    public final StateProp<String> id = new StateProp<>();
    public final StateProp<String> key = new StateProp<>();
    public final StateProp<String> bucket = new StateProp<>();
    public final StateProp<String> region = new StateProp<>();
    public final StateProp<Long> startTime = new StateProp<>();
    public final StateProp<Long> expTime = new StateProp<>();
    public final StateProp<String> path = new StateProp<>();
    public final StateProp<String> pathDemo = new StateProp<>();

    @NonNull
    public static CosKeyInfo valueOf(@NonNull ProtoMessage.CosKey input, long sessionUserId) {
        final CosKeyInfo target = new CosKeyInfo();
        target.sessionUserId.set(sessionUserId);

        target.token.set(input.getToken());
        target.id.set(input.getId());
        target.key.set(input.getKey());
        target.bucket.set(input.getBucket());
        target.region.set(input.getRegion());
        target.startTime.set(input.getStartTime());
        target.expTime.set(input.getExpTime());
        target.path.set(input.getPath());
        target.pathDemo.set(input.getPathDemo());
        return target;
    }

}
