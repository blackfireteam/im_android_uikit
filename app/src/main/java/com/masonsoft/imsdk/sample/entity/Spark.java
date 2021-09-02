package com.masonsoft.imsdk.sample.entity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.core.proto.ProtoMessage;

import java.util.ArrayList;
import java.util.List;

public class Spark {

    public long userId;
    public String nickname;
    public String avatar;
    public String pic;

    @NonNull
    public static Spark valueOf(@NonNull ProtoMessage.Spark input) {
        final Spark target = new Spark();
        target.userId = input.getUid();
        target.nickname = input.getNickName();
        target.avatar = input.getAvatar();
        target.pic = input.getPic();
        return target;
    }

    @NonNull
    public static List<Spark> valueOf(@NonNull List<ProtoMessage.Spark> input) {
        final List<Spark> target = new ArrayList<>();
        for (ProtoMessage.Spark spark : input) {
            if (spark == null) {
                continue;
            }
            target.add(valueOf(spark));
        }
        return target;
    }

}
