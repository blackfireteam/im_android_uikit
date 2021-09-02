package com.masonsoft.imsdk.sample.uniontype.viewholder;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.sample.R;

import io.github.idonans.dynamic.uniontype.loadingstatus.impl.UnionTypeLoadingStatusViewHolder;
import io.github.idonans.uniontype.Host;

public class HomeSparkLoadNoMoreDataLargeViewHolder extends UnionTypeLoadingStatusViewHolder {

    public HomeSparkLoadNoMoreDataLargeViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_sample_union_type_impl_home_spark_load_no_more_data_large);
    }

}
