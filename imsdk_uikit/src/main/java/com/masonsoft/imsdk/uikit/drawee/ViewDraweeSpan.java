package com.masonsoft.imsdk.uikit.drawee;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.widget.text.span.BetterImageSpan;

import io.github.idonans.core.util.ContextUtil;

public class ViewDraweeSpan extends BetterImageSpan {

    private final DraweeHolder<?> mDraweeHolder;

    public ViewDraweeSpan(
            DraweeHolder<?> draweeHolder, @BetterImageSpanAlignment int verticalAlignment, int sizePx) {
        super(draweeHolder.getTopLevelDrawable(), verticalAlignment);
        mDraweeHolder = draweeHolder;
        //noinspection ConstantConditions
        draweeHolder.getTopLevelDrawable().setBounds(0, 0, sizePx, sizePx);
        mDraweeHolder.onAttach();
    }

    public void setTargetView(View targetView) {
        mDraweeHolder.getTopLevelDrawable().setCallback(targetView);
        mDraweeHolder.getTopLevelDrawable().invalidateSelf();
    }

    public static ViewDraweeSpan create(final String assetFilename, int sizePx) {
        final GenericDraweeHierarchy hierarchy =
                GenericDraweeHierarchyBuilder.newInstance(ContextUtil.getContext().getResources())
                        .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .setPlaceholderImage(new ColorDrawable(Color.TRANSPARENT))
                        .setPlaceholderImageScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .setFailureImage(new ColorDrawable(Color.TRANSPARENT))
                        .setFailureImageScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .build();
        final DraweeHolder<GenericDraweeHierarchy> draweeHolder = new DraweeHolder<>(hierarchy);
        final PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder();
        builder.setUri(assetFilename);
        builder.setAutoPlayAnimations(false);
        draweeHolder.setController(builder.build());

        final ViewDraweeSpan draweeSpan = new ViewDraweeSpan(draweeHolder, BetterImageSpan.ALIGN_CENTER, sizePx);
        return draweeSpan;
    }

    public static CharSequence createViewDraweeSpanStringBuilder(final String name, final String assetFilename, int sizePx) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(name);
        builder.setSpan(ViewDraweeSpan.create(assetFilename, sizePx), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static void updateTargetView(CharSequence text, TextView targetView) {
        if (text instanceof Spanned) {
            final ViewDraweeSpan[] viewDraweeSpans = ((Spanned) text).getSpans(0, text.length(), ViewDraweeSpan.class);
            if (viewDraweeSpans != null) {
                for (ViewDraweeSpan viewDraweeSpan : viewDraweeSpans) {
                    viewDraweeSpan.setTargetView(targetView);
                }
            }
        }
    }

}