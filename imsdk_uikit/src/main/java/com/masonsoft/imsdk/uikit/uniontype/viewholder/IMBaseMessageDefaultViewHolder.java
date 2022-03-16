package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import io.github.idonans.uniontype.Host;

public abstract class IMBaseMessageDefaultViewHolder extends IMBaseMessageViewHolder {

    public IMBaseMessageDefaultViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
    }

    public IMBaseMessageDefaultViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
    }

}
