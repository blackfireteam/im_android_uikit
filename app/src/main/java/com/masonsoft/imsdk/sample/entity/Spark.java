package com.masonsoft.imsdk.sample.entity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.core.proto.ProtoMessage;

import java.util.ArrayList;
import java.util.List;

public class Spark {

    public ProtoMessage.Profile profile;

    @NonNull
    public static Spark valueOf(@NonNull ProtoMessage.Profile input) {
        final Spark target = new Spark();
        target.profile = input;
        return target;
    }

    @NonNull
    public static List<Spark> valueOf(@NonNull List<ProtoMessage.Profile> input) {
        final List<Spark> target = new ArrayList<>();
        for (ProtoMessage.Profile profile : input) {
            if (profile == null) {
                continue;
            }
            target.add(valueOf(profile));
        }
        return target;
    }

}
