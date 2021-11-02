package com.masonsoft.imsdk.sample.selector;

import android.app.Activity;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleSimpleTextListPickerDialogBinding;
import com.masonsoft.imsdk.sample.uniontype.SampleUnionTypeMapper;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

public class SimpleTextListPickerDialog {

    public interface OnTextSelectedListener {
        void onTextSelected(String text);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final Activity mActivity;
    @SuppressWarnings("FieldCanBeLocal")
    private final ImsdkSampleSimpleTextListPickerDialogBinding mBinding;
    private final ViewDialog mViewDialog;

    @Nullable
    private OnTextSelectedListener mOnTextSelectedListener;

    public SimpleTextListPickerDialog(Activity activity, List<String> textList) {
        mActivity = activity;
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_sample_simple_text_list_picker_dialog)
                .defaultAnimation()
                .setParentView(activity.findViewById(Window.ID_ANDROID_CONTENT))
                .dimBackground(true)
                .create();
        mBinding = ImsdkSampleSimpleTextListPickerDialogBinding.bind(mViewDialog.getContentView());

        /////////////////
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.addItemDecoration(new DividerItemDecoration(
                DividerItemDecoration.VERTICAL,
                DividerItemDecoration.SHOW_DIVIDER_MIDDLE,
                0xFFe1e1e1,
                DimenUtil.dp2px(1),
                DimenUtil.dp2px(1)
        ));
        final DataAdapter adapter = new DataAdapter(textList);
        adapter.setUnionTypeMapper(new SampleUnionTypeMapper());
        adapter.setHost(Host.Factory.create(mActivity, mBinding.recyclerView, adapter));
        mBinding.recyclerView.setAdapter(adapter);
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(true);
    }

    public void setOnTextSelectedListener(@Nullable OnTextSelectedListener listener) {
        mOnTextSelectedListener = listener;
    }

    private void onTextSelected(final String text) {
        if (mOnTextSelectedListener != null) {
            mOnTextSelectedListener.onTextSelected(text);
        }
        hide();
    }

    private class DataAdapter extends UnionTypeAdapter {

        private final List<String> mData;

        private DataAdapter(List<String> data) {
            mData = new ArrayList<>();
            if (data != null) {
                mData.addAll(data);
            }

            getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final List<UnionTypeItemObject> itemObjects = new ArrayList<>();
                        for (String text : mData) {
                            final DataObject<String> dataObject = new DataObject<>(text);
                            dataObject.putExtHolderItemClick1(holder -> {
                                final int position = holder.getAdapterPosition();
                                if (position < 0) {
                                    SampleLog.e("invalid position %s", position);
                                }
                                UnionTypeItemObject itemObject = holder.host.getAdapter().getItem(position);
                                //noinspection ConstantConditions,unchecked
                                final String text1 = ((DataObject<String>) itemObject.itemObject).object;
                                onTextSelected(text1);
                            });

                            UnionTypeItemObject itemObject = new UnionTypeItemObject(
                                    SampleUnionTypeMapper.UNION_TYPE_IMPL_IM_SIMPLE_TEXT,
                                    dataObject
                            );
                            itemObjects.add(itemObject);
                        }

                        groupArrayList.setGroupItems(0, itemObjects);
                    })
                    .commit();
        }

    }

}
