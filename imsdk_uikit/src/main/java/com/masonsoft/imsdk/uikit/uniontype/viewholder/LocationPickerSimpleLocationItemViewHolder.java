package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplLocationPickerSimpleLocationItemBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class LocationPickerSimpleLocationItemViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplLocationPickerSimpleLocationItemBinding mBinding;

    public LocationPickerSimpleLocationItemViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_location_picker_simple_location_item);
        mBinding = ImsdkUikitUnionTypeImplLocationPickerSimpleLocationItemBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final LocationInfo locationInfo = itemObject.getObject(LocationInfo.class);
        final ObjectWrapper locationInfoSelectedWrapper = itemObject.getExtObjectObject1(null);
        Preconditions.checkNotNull(locationInfoSelectedWrapper);
        final boolean pickPosition = itemObject.getExtObjectBoolean1(false);

        if (pickPosition) {
            mBinding.text1.setText(R.string.imsdk_uikit_hint_location_pick_position);
            if (locationInfo.title != null) {
                mBinding.text1.append(locationInfo.title);
            }
        } else {
            mBinding.text1.setText(locationInfo.title);
        }
        mBinding.text2.setText(locationInfo.subTitle);

        if (locationInfoSelectedWrapper.getObject() == locationInfo) {
            ViewUtil.setVisibilityIfChanged(mBinding.flagSelect, View.VISIBLE);
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.flagSelect, View.INVISIBLE);
        }

        ViewUtil.onClick(itemView, v -> {
            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(this);
            }
        });
    }

}
