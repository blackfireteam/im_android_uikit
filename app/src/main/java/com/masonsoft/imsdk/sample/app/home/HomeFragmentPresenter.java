package com.masonsoft.imsdk.sample.app.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.sample.api.DefaultApi;
import com.masonsoft.imsdk.sample.entity.Spark;
import com.masonsoft.imsdk.sample.uniontype.SampleUnionTypeMapper;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.uniontype.DeepDiff;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class HomeFragmentPresenter extends PagePresenter<UnionTypeItemObject, Object, HomeFragment.ViewImpl> {

    public HomeFragmentPresenter(HomeFragment.ViewImpl view) {
        super(view);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createInitRequest() throws Exception {
        return Single.just("")
                .map(input -> DefaultApi.getSparks())
                .map(this::create)
                .map(input -> new DynamicResult<UnionTypeItemObject, Object>().setItems(input))
                .delay(2, TimeUnit.SECONDS);
    }

    @Override
    protected void onInitRequestResult(@NonNull HomeFragment.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, Object> result) {
        super.onInitRequestResult(view, result);

        if (result.items == null || result.items.isEmpty()) {
            setLastRetryListener(() -> requestInit(true));
            setNextPageRequestEnable(false);
        } else {
            setLastRetryListener(null);
            setNextPageRequestEnable(true);
        }
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createNextPageRequest() throws Exception {
        return Single.just("")
                .map(input -> DefaultApi.getSparks())
                .map(this::create)
                .map(input -> new DynamicResult<UnionTypeItemObject, Object>().setItems(input));
    }

    @Override
    protected void onNextPageRequestResult(@NonNull HomeFragment.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, Object> result) {
        super.onNextPageRequestResult(view, result);

        if (result.items == null || result.items.isEmpty()) {
            setLastRetryListener(() -> requestNextPage(true));
            setNextPageRequestEnable(false);
        } else {
            setLastRetryListener(null);
            setNextPageRequestEnable(true);
        }
    }

    private Collection<UnionTypeItemObject> create(Collection<Spark> input) {
        List<UnionTypeItemObject> result = new ArrayList<>();
        if (input != null) {
            for (Spark item : input) {
                if (item != null) {
                    result.add(create(item));
                }
            }
        }
        return result;
    }

    private UnionTypeItemObject create(@NonNull Spark input) {
        return new UnionTypeItemObject(SampleUnionTypeMapper.UNION_TYPE_IMPL_IM_HOME_SPARK, new DeepDiffDataObject(input));
    }

    private void setLastRetryListener(LastRetryListener listener) {
        if (getView() == null) {
            return;
        }
        Threads.runOnUi(() -> mLastRetryListener = listener);
    }

    private LastRetryListener mLastRetryListener;

    private interface LastRetryListener {
        void onRetry();
    }

    public void requestLastRetry() {
        if (mLastRetryListener != null) {
            mLastRetryListener.onRetry();
        }
    }

    private static final class DeepDiffDataObject extends DataObject implements DeepDiff {

        public DeepDiffDataObject(Spark spark) {
            super(spark);
        }

        @Override
        public boolean isSameItem(@Nullable Object o) {
            if (!(o instanceof DeepDiffDataObject)) {
                return false;
            }

            final DeepDiffDataObject other = (DeepDiffDataObject) o;
            if (!(other.object instanceof Spark)) {
                return false;
            }

            if (!(this.object instanceof Spark)) {
                return false;
            }

            return this.object.equals(other.object);
        }

        @Override
        public boolean isSameContent(@Nullable Object o) {
            final Spark thisSpark = (Spark) this.object;
            //noinspection ConstantConditions
            final Spark thatSpark = (Spark) ((DeepDiffDataObject) o).object;
            return thisSpark.profile == thatSpark.profile;
        }
    }

}
