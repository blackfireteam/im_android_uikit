package com.masonsoft.imsdk.sample.uniontype;

import com.masonsoft.imsdk.sample.uniontype.viewholder.DiscoverUserViewHolder;
import com.masonsoft.imsdk.sample.uniontype.viewholder.HomeSparkViewHolder;
import com.masonsoft.imsdk.sample.uniontype.viewholder.SimpleTextViewHolder;

import io.github.idonans.dynamic.uniontype.loadingstatus.UnionTypeLoadingStatus;

public class SampleUnionTypeMapper extends UnionTypeLoadingStatus {

    private static int sNextUnionType = 1;

    public static final int UNION_TYPE_IMPL_IM_HOME_SPARK = sNextUnionType++; // 首页中的一条 spark
    public static final int UNION_TYPE_IMPL_IM_DISCOVER_USER = sNextUnionType++; // 发现页中的一个 user 信息
    public static final int UNION_TYPE_IMPL_IM_SIMPLE_TEXT = sNextUnionType++; // 简单文本显示样式，例如用于登录页选择最近使用的地址

    public SampleUnionTypeMapper() {
        put(UNION_TYPE_IMPL_IM_HOME_SPARK, HomeSparkViewHolder::new);
        put(UNION_TYPE_IMPL_IM_DISCOVER_USER, DiscoverUserViewHolder::new);
        put(UNION_TYPE_IMPL_IM_SIMPLE_TEXT, SimpleTextViewHolder::new);
    }

}
