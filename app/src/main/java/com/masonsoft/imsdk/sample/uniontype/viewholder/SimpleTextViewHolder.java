package com.masonsoft.imsdk.sample.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleUnionTypeImplSimpleTextBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

/**
 * 简单文本样式
 */
public class SimpleTextViewHolder extends UnionTypeViewHolder {

    private final ImsdkSampleUnionTypeImplSimpleTextBinding mBinding;

    public SimpleTextViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_sample_union_type_impl_simple_text);
        mBinding = ImsdkSampleUnionTypeImplSimpleTextBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final String text = (String) itemObject.object;

        mBinding.text.setText(text);

        ViewUtil.onClick(itemView, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(SimpleTextViewHolder.this);
            }
        });
    }

}
