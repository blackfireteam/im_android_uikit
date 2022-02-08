package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMFlashImageElement;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public abstract class IMBaseMessageFlashImageViewHolder extends IMBaseMessageViewHolder {

    protected final View mFlashImageContainer;
    protected final View mFlashImageFlag;

    public IMBaseMessageFlashImageViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mFlashImageContainer = itemView.findViewById(R.id.flash_image_container);
        mFlashImageFlag = itemView.findViewById(R.id.flash_image_flag);
    }

    public IMBaseMessageFlashImageViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mFlashImageContainer = itemView.findViewById(R.id.flash_image_container);
        mFlashImageFlag = itemView.findViewById(R.id.flash_image_flag);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);

        boolean active = false;
        if (baseMessage instanceof MSIMMessage) {
            final MSIMMessage message = (MSIMMessage) baseMessage;
            final MSIMFlashImageElement element = message.getFlashImageElement();
            if (element != null) {
                active = !element.isReadForSessionUser();
            }
        }

        mFlashImageFlag.setActivated(active);

        mFlashImageContainer.setOnLongClickListener(v -> {
            final UnionTypeViewHolderListeners.OnItemLongClickListener listener = itemObject.getExtHolderItemLongClick1();
            if (listener != null) {
                listener.onItemLongClick(this);
            }
            return true;
        });
        ViewUtil.onClick(mFlashImageContainer, v -> {
            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(this);
            }
        });
    }

}
