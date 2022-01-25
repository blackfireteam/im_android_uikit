package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMVideoElement;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.widget.IMImageView;
import com.masonsoft.imsdk.uikit.widget.ResizeImageView;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public abstract class IMMessageVideoViewHolder extends IMMessageViewHolder {

    protected final ResizeImageView mResizeImageView;
    protected final IMImageView mImage;

    public IMMessageVideoViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mResizeImageView = itemView.findViewById(R.id.resize_image_view);
        mImage = itemView.findViewById(R.id.image);
    }

    public IMMessageVideoViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mResizeImageView = itemView.findViewById(R.id.resize_image_view);
        mImage = itemView.findViewById(R.id.image);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        //noinspection unchecked
        final DataObject<MSIMMessage> itemObject = (DataObject<MSIMMessage>) this.getItemObject(Object.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMMessage message = itemObject.object;

        long width = 0;
        long height = 0;
        final MSIMVideoElement element = message.getVideoElement();
        if (element != null) {
            width = element.getWidth();
            height = element.getHeight();
        }
        mResizeImageView.setImageSize(width, height);
        mImage.setChatMessage(message);

        mResizeImageView.setOnLongClickListener(v -> {
            final UnionTypeViewHolderListeners.OnItemLongClickListener listener = itemObject.getExtHolderItemLongClick1();
            if (listener != null) {
                listener.onItemLongClick(this);
            }
            return true;
        });
        ViewUtil.onClick(mResizeImageView, v -> {
            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(this);
            }
        });
    }

}
