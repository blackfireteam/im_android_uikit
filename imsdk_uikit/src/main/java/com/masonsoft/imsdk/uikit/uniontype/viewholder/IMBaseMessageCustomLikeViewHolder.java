package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import io.github.idonans.uniontype.Host;

/**
 * 自定义消息 - 喜欢
 */
public abstract class IMBaseMessageCustomLikeViewHolder extends IMBaseMessageViewHolder {

    public IMBaseMessageCustomLikeViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
    }

    public IMBaseMessageCustomLikeViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
    }

}
