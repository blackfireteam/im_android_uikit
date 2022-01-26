package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMLocationElement;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.widget.MSIMBaseMessageImageView;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;

public abstract class IMBaseMessageLocationViewHolder extends IMBaseMessageViewHolder {

    protected final ViewGroup mContentContainer;
    protected final TextView mLocationTitle;
    protected final TextView mLocationSubTitle;
    protected final MSIMBaseMessageImageView mImage;

    public IMBaseMessageLocationViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mContentContainer = itemView.findViewById(R.id.content_container);
        mLocationTitle = itemView.findViewById(R.id.location_title);
        mLocationSubTitle = itemView.findViewById(R.id.location_sub_title);
        mImage = itemView.findViewById(R.id.image);
    }

    public IMBaseMessageLocationViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mContentContainer = itemView.findViewById(R.id.content_container);
        mLocationTitle = itemView.findViewById(R.id.location_title);
        mLocationSubTitle = itemView.findViewById(R.id.location_sub_title);
        mImage = itemView.findViewById(R.id.image);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);
        mImage.setBaseMessage(baseMessage);

        final MSIMLocationElement element = baseMessage.getLocationElement();
        Preconditions.checkNotNull(element);
        mLocationTitle.setText(element.getTitle());
        mLocationSubTitle.setText(element.getSubTitle());

        ViewUtil.onClick(mContentContainer, v -> {
            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(this);
            }
        });
        mContentContainer.setOnLongClickListener(v -> {
            final UnionTypeViewHolderListeners.OnItemLongClickListener listener = itemObject.getExtHolderItemLongClick1();
            if (listener != null) {
                listener.onItemLongClick(this);
            }
            return true;
        });
    }

}
