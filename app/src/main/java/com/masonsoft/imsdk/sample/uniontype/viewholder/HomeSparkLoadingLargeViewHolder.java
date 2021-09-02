package com.masonsoft.imsdk.sample.uniontype.viewholder;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.sample.R;

import io.github.idonans.dynamic.uniontype.loadingstatus.impl.UnionTypeLoadingStatusViewHolder;
import io.github.idonans.uniontype.Host;

public class HomeSparkLoadingLargeViewHolder extends UnionTypeLoadingStatusViewHolder {

    public HomeSparkLoadingLargeViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_sample_union_type_impl_home_spark_loading_large);
    }

}
