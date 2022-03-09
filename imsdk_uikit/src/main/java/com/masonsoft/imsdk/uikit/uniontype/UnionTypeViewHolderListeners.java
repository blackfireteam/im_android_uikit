package com.masonsoft.imsdk.uikit.uniontype;

import io.github.idonans.uniontype.UnionTypeViewHolder;

public interface UnionTypeViewHolderListeners {

    interface OnItemClickListener {
        void onItemClick(UnionTypeViewHolder viewHolder);
    }

    interface OnItemLongClickListener {
        void onItemLongClick(UnionTypeViewHolder viewHolder);
    }

    interface OnItemClickPayloadListener {
        void onItemClick(UnionTypeViewHolder viewHolder, Object payload);
    }

}
