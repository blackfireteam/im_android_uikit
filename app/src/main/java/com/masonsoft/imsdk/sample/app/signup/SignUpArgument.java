package com.masonsoft.imsdk.sample.app.signup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SignUpArgument implements Parcelable {

    private static final String KEY_SIGN_UP_ARG = "key:sign_up_arg_20210421";

    public long userId;
    public String nickname;
    public String avatar;

    public SignUpArgument() {
    }

    public void writeTo(@Nullable Intent intent) {
        if (intent != null) {
            intent.putExtra(KEY_SIGN_UP_ARG, this);
        }
    }

    public void writeTo(@Nullable Bundle bundle) {
        if (bundle != null) {
            bundle.putParcelable(KEY_SIGN_UP_ARG, this);
        }
    }

    @NonNull
    public static SignUpArgument valueOf(@Nullable Intent intent) {
        SignUpArgument args = null;
        if (intent != null) {
            args = intent.getParcelableExtra(KEY_SIGN_UP_ARG);
        }
        if (args == null) {
            args = new SignUpArgument();
        }
        return args;
    }

    @NonNull
    public static SignUpArgument valueOf(@Nullable Bundle bundle) {
        SignUpArgument args = null;
        if (bundle != null) {
            args = bundle.getParcelable(KEY_SIGN_UP_ARG);
        }
        if (args == null) {
            args = new SignUpArgument();
        }
        return args;
    }

    public static final Creator<SignUpArgument> CREATOR = new Creator<SignUpArgument>() {
        @Override
        public SignUpArgument createFromParcel(Parcel in) {
            return new SignUpArgument(in);
        }

        @Override
        public SignUpArgument[] newArray(int size) {
            return new SignUpArgument[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected SignUpArgument(Parcel in) {
        this.userId = in.readLong();
        this.nickname = in.readString();
        this.avatar = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.userId);
        dest.writeString(this.nickname);
        dest.writeString(this.avatar);
    }
}
