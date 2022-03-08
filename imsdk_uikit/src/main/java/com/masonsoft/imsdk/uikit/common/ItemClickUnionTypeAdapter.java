package com.masonsoft.imsdk.uikit.common;

import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.uniontype.UnionTypeAdapter;

@Deprecated
public class ItemClickUnionTypeAdapter extends UnionTypeAdapter {

    private UnionTypeViewHolderListeners.OnItemClickListener mOnItemClickListener;
    private UnionTypeViewHolderListeners.OnItemLongClickListener mOnItemLongClickListener;

    public void setOnItemClickListener(UnionTypeViewHolderListeners.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public UnionTypeViewHolderListeners.OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemLongClickListener(UnionTypeViewHolderListeners.OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public UnionTypeViewHolderListeners.OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

}
