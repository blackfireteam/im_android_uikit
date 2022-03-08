package com.masonsoft.imsdk.uikit.common.mediapicker;

import com.masonsoft.imsdk.util.WeakObservable;

public class UnionTypeMediaDataObservable extends WeakObservable<UnionTypeMediaDataObservable.UnionTypeMediaDataObserver> {

    public void notifyBucketSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
        this.forEach((stateObserver) -> stateObserver.onBucketSelectedChanged(unionTypeMediaData));
    }

    public void notifyMediaInfoSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
        this.forEach((stateObserver) -> stateObserver.onMediaInfoSelectedChanged(unionTypeMediaData));
    }

    public interface UnionTypeMediaDataObserver {
        void onBucketSelectedChanged(UnionTypeMediaData unionTypeMediaData);

        void onMediaInfoSelectedChanged(UnionTypeMediaData unionTypeMediaData);
    }

}
