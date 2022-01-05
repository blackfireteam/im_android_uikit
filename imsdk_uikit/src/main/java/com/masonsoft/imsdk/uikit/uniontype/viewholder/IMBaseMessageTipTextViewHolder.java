package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplImBaseMessageTipTextBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class IMBaseMessageTipTextViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplImBaseMessageTipTextBinding mBinding;

    public IMBaseMessageTipTextViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_base_message_tip_text);
        mBinding = ImsdkUikitUnionTypeImplImBaseMessageTipTextBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final String text = String.valueOf(itemObject.object);
        mBinding.text.setText(text);
    }

}
