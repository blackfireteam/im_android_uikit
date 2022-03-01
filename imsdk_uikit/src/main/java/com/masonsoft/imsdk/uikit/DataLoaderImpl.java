package com.masonsoft.imsdk.uikit;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.lang.ObjectWrapper;

import java.io.IOException;

import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class DataLoaderImpl<T> implements DataLoader {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    @Override
    public void requestLoadData() {
        mRequestHolder.set(Single.just("")
                .map(input -> new ObjectWrapper(loadData()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wrapper -> {
                    //noinspection unchecked
                    onDataLoad((T) wrapper.getObject());
                }, MSIMUikitLog::e));
    }

    @CallSuper
    @Override
    public void close() throws IOException {
        mRequestHolder.clear();
    }

    @WorkerThread
    @Nullable
    protected abstract T loadData();

    @UiThread
    protected abstract void onDataLoad(@Nullable T data);

}
