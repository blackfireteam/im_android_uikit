package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMVideoElement;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.widget.MSIMBaseMessageImageView;
import com.masonsoft.imsdk.uikit.widget.ResizeImageView;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public abstract class IMBaseMessageVideoViewHolder extends IMBaseMessageViewHolder {

    protected final ResizeImageView mResizeImageView;
    protected final MSIMBaseMessageImageView mImage;

    public IMBaseMessageVideoViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mResizeImageView = itemView.findViewById(R.id.resize_image_view);
        mImage = itemView.findViewById(R.id.image);
    }

    public IMBaseMessageVideoViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mResizeImageView = itemView.findViewById(R.id.resize_image_view);
        mImage = itemView.findViewById(R.id.image);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage message = (MSIMBaseMessage) itemObject.object;

        long width = 0;
        long height = 0;
        final MSIMVideoElement element = message.getVideoElement();
        if (element != null) {
            width = element.getWidth();
            height = element.getHeight();
        }
        mResizeImageView.setImageSize(width, height);
        mImage.setBaseMessage(message);

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
