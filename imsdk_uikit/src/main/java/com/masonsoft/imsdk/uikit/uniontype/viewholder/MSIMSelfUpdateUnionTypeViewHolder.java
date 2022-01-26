package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMSelfUpdateListener;
import com.masonsoft.imsdk.MSIMSelfUpdater;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeMapper;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public abstract class MSIMSelfUpdateUnionTypeViewHolder extends UnionTypeViewHolder {

    public MSIMSelfUpdateUnionTypeViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
    }

    public MSIMSelfUpdateUnionTypeViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
    }

    @SuppressWarnings("FieldCanBeLocal")
    protected MSIMSelfUpdateListener mSelfUpdateListener;

    protected void bindSelfUpdate() {
        mSelfUpdateListener = () -> Threads.postUi(this::onSelfUpdate);
        final DataObject dataObject = getItemObject(DataObject.class);
        if (dataObject == null) {
            return;
        }

        final MSIMSelfUpdater selfUpdater = dataObject.getObject(MSIMSelfUpdater.class);
        if (selfUpdater == null) {
            return;
        }

        selfUpdater.addOnSelfUpdateListener(mSelfUpdateListener);
    }

    protected void onSelfUpdate() {
        if (validateUnionType()) {
            final DataObject dataObject = getItemObject(DataObject.class);
            if (dataObject != null) {
                final MSIMSelfUpdater selfUpdater = dataObject.getObject(MSIMSelfUpdater.class);
                if (selfUpdater != null) {
                    onBindUpdate();
                }
            }
        }
    }

    @Override
    public int getBestUnionType() {
        final DataObject dataObject = getItemObject(DataObject.class);
        if (dataObject != null) {
            return getBestUnionTypeAndApplyUpdate(dataObject);
        }

        return super.getBestUnionType();
    }

    protected int getBestUnionTypeAndApplyUpdate(@NonNull final DataObject dataObject) {
        final int unionType = getBestUnionType(dataObject);

        if (unionType == UnionTypeMapper.UNION_TYPE_NULL) {
            return unionType;
        }

        Preconditions.checkNotNull(unionTypeItemObject);
        if (unionTypeItemObject.unionType != unionType) {
            unionTypeItemObject.update(unionType, dataObject);
        }
        return unionType;
    }

    protected int getBestUnionType(@NonNull final DataObject dataObject) {
        return UnionTypeMapper.UNION_TYPE_NULL;
    }

    @CallSuper
    @Override
    public void onBindUpdate() {
        bindSelfUpdate();
    }

}
