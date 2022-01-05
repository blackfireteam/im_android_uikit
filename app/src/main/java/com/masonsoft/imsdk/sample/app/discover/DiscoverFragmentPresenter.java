package com.masonsoft.imsdk.sample.app.discover;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.sample.im.DiscoverUserManager;
import com.masonsoft.imsdk.sample.observable.DiscoverUserObservable;
import com.masonsoft.imsdk.sample.uniontype.SampleUnionTypeMapper;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.uniontype.DeepDiff;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class DiscoverFragmentPresenter extends PagePresenter<UnionTypeItemObject, Object, DiscoverFragment.ViewImpl> {

    public DiscoverFragmentPresenter(DiscoverFragment.ViewImpl view) {
        super(view);
        DiscoverUserObservable.DEFAULT.registerObserver(mDiscoverUserObserver);
    }

    @Nullable
    @Override
    public DiscoverFragment.ViewImpl getView() {
        return (DiscoverFragment.ViewImpl) super.getView();
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final DiscoverUserObservable.DiscoverUserObserver mDiscoverUserObserver = new DiscoverUserObservable.DiscoverUserObserver() {
        @Override
        public void onDiscoverUserOnline(long userId) {
            final DiscoverFragment.ViewImpl view = getView();
            if (view == null) {
                return;
            }
            final UnionTypeItemObject unionTypeItemObject = create(userId);
            view.replaceUser(unionTypeItemObject);
        }

        @Override
        public void onDiscoverUserOffline(long userId) {
            final DiscoverFragment.ViewImpl view = getView();
            if (view == null) {
                return;
            }
            final UnionTypeItemObject unionTypeItemObject = create(userId);
            view.removeUser(unionTypeItemObject);
        }
    };

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createInitRequest() throws Exception {
        final List<Long> userIdList = DiscoverUserManager.getInstance().getOnlineUserList();
        return Single.just(create(userIdList))
                .map(input -> new DynamicResult<UnionTypeItemObject, Object>().setItems(input));
    }

    @NonNull
    private Collection<UnionTypeItemObject> create(@Nullable Collection<Long> input) {
        List<UnionTypeItemObject> result = new ArrayList<>();
        if (input != null) {
            for (Long item : input) {
                if (item != null) {
                    result.add(create(item));
                }
            }
        }
        return result;
    }

    @NonNull
    private UnionTypeItemObject create(@NonNull Long userId) {
        return UnionTypeItemObject.valueOf(SampleUnionTypeMapper.UNION_TYPE_IMPL_IM_DISCOVER_USER, new DeepDiffDataObject(userId));
    }

    private static class DeepDiffDataObject extends DataObject implements DeepDiff {

        public DeepDiffDataObject(java.lang.Long object) {
            super(object);
        }

        @Override
        public boolean isSameItem(@Nullable Object other) {
            if (other instanceof DiscoverFragmentPresenter.DeepDiffDataObject) {
                final DiscoverFragmentPresenter.DeepDiffDataObject otherDataObject = (DiscoverFragmentPresenter.DeepDiffDataObject) other;
                return Objects.equals(this.object, otherDataObject.object);
            }
            return false;
        }

        @Override
        public boolean isSameContent(@Nullable Object other) {
            if (other instanceof DiscoverFragmentPresenter.DeepDiffDataObject) {
                final DiscoverFragmentPresenter.DeepDiffDataObject otherDataObject = (DiscoverFragmentPresenter.DeepDiffDataObject) other;
                return Objects.equals(this.object, otherDataObject.object);
            }
            return false;
        }
    }

}
