package com.masonsoft.imsdk.uikit.common.impreview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class IMImageOrVideoPreviewPresenter extends PagePresenter<UnionTypeItemObject, Object, IMImageOrVideoPreviewDialog.ViewImpl> {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    @NonNull
    private final List<MSIMBaseMessage> mMessageList;
    private final int mIndex;

    @UiThread
    public IMImageOrVideoPreviewPresenter(
            @NonNull IMImageOrVideoPreviewDialog.ViewImpl view,
            @NonNull List<MSIMBaseMessage> messageList,
            int index) {
        super(view);
        mMessageList = messageList;
        mIndex = index;

        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < messageList.size());

        setPrePageRequestEnable(mIndex > 0);
        setNextPageRequestEnable(mIndex < mMessageList.size() - 1);
    }

    void showInitMessage() {
        IMImageOrVideoPreviewDialog.ViewImpl view = getView();
        if (view == null) {
            return;
        }

        view.onInitRequestResult(
                new DynamicResult<UnionTypeItemObject, Object>()
                        .setItems(Lists.newArrayList(create(mMessageList.get(mIndex), true)))
        );
    }

    private final UnionTypeViewHolderListeners.OnItemClickListener mOnHolderItemClickListener = viewHolder -> {
        IMImageOrVideoPreviewDialog.ViewImpl view = (IMImageOrVideoPreviewDialog.ViewImpl) getView();
        if (view != null) {
            view.hide();
        }
    };

    @Nullable
    private UnionTypeItemObject create(MSIMBaseMessage message) {
        return create(message, false);
    }

    @Nullable
    private UnionTypeItemObject create(MSIMBaseMessage message, boolean autoPlay) {
        if (message == null) {
            return null;
        }

        return IMBaseMessageViewHolder.Helper.createPreviewDefault(
                new DataObject(message)
                        .putExtObjectBoolean1(autoPlay)
                        .putExtHolderItemClick1(mOnHolderItemClickListener)
        );
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createInitRequest() throws Exception {
        return null;
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createPrePageRequest() throws Exception {
        MSIMUikitLog.v("createPrePageRequest");
        return Single.just("")
                .map(input -> {
                    //noinspection UnnecessaryLocalVariable
                    final List<MSIMBaseMessage> messageList = mMessageList;
                    //noinspection UnnecessaryLocalVariable
                    final int index = mIndex;

                    // 上一页取 [0-index)
                    List<UnionTypeItemObject> target = new ArrayList<>();

                    for (int i = 0; i < index; i++) {
                        UnionTypeItemObject item = create(messageList.get(i));
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e("createPrePageRequest ignore null UnionTypeItemObject");
                            }
                            continue;
                        }
                        target.add(item);
                    }

                    return new DynamicResult<UnionTypeItemObject, Object>() {
                        @Override
                        public boolean isEnd() {
                            return true;
                        }

                        @Override
                        public boolean isError() {
                            return false;
                        }
                    }.setItems(target);
                });
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createNextPageRequest() throws Exception {
        MSIMUikitLog.v("createNextPageRequest");
        return Single.just("")
                .map(input -> {
                    final List<MSIMBaseMessage> messageList = mMessageList;
                    //noinspection UnnecessaryLocalVariable
                    final int index = mIndex;
                    final int size = messageList.size();

                    // 下一页取 (index-size)
                    List<UnionTypeItemObject> target = new ArrayList<>();

                    for (int i = index + 1; i < size; i++) {
                        UnionTypeItemObject item = create(messageList.get(i));
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e("createNextPageRequest ignore null UnionTypeItemObject");
                            }
                            continue;
                        }
                        target.add(item);
                    }

                    return new DynamicResult<UnionTypeItemObject, Object>() {
                        @Override
                        public boolean isEnd() {
                            return true;
                        }

                        @Override
                        public boolean isError() {
                            return false;
                        }
                    }.setItems(target);
                });
    }

}
