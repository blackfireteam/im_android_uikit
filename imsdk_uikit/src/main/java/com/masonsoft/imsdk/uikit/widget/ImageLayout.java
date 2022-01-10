package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.util.UrlUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.github.idonans.core.util.DimenUtil;

public class ImageLayout extends ClipLayout {

    public ImageLayout(Context context) {
        this(context, null);
    }

    public ImageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    public static final int SCALE_TYPE_CENTER_CROP = 0;
    public static final int SCALE_TYPE_CENTER_INSIDE = 1;
    public static final int SCALE_TYPE_FIX_WIDTH = 2;
    public static final int SCALE_TYPE_FIT_CENTER = 3;

    @IntDef({SCALE_TYPE_CENTER_CROP, SCALE_TYPE_CENTER_INSIDE, SCALE_TYPE_FIX_WIDTH, SCALE_TYPE_FIT_CENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleType {
    }

    @ScaleType
    private int mScaleType = SCALE_TYPE_CENTER_CROP;

    private Drawable mPlaceHolderLoading;
    private Drawable mPlaceHolderFail;

    private SimpleDraweeView mDraweeView;

    // 默认 1.5 倍屏幕大小
    private float mImageResizePercent = 1.5f;
    private int mImageResize = -1;
    private boolean mSmallCache;
    private boolean mAutoPlay = true;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageLayout, defStyleAttr,
                defStyleRes);
        mScaleType = a.getInt(R.styleable.ImageLayout_imageScaleType, mScaleType);
        mPlaceHolderLoading = a.getDrawable(R.styleable.ImageLayout_imagePlaceHolderLoading);
        mPlaceHolderFail = a.getDrawable(R.styleable.ImageLayout_imagePlaceHolderFail);
        mImageResizePercent = a.getFloat(R.styleable.ImageLayout_imageResizePercent, mImageResizePercent);
        mImageResize = a.getDimensionPixelOffset(R.styleable.ImageLayout_imageResize, mImageResize);
        mSmallCache = a.getBoolean(R.styleable.ImageLayout_smallCache, mSmallCache);
        mAutoPlay = a.getBoolean(R.styleable.ImageLayout_autoPlay, mAutoPlay);
        a.recycle();

        if (mPlaceHolderLoading == null) {
            mPlaceHolderLoading = new ColorDrawable(0xFFf4f4f4);
        }
        if (mPlaceHolderFail == null) {
            mPlaceHolderFail = new ColorDrawable(0xFFf4f4f4);
        }
        mDraweeView = new SimpleDraweeView(context);

        GenericDraweeHierarchy hierarchy =
                GenericDraweeHierarchyBuilder.newInstance(context.getResources())
                        .setActualImageScaleType(getScaleType(mScaleType))
                        .setPlaceholderImage(mPlaceHolderLoading)
                        .setPlaceholderImageScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .setFailureImage(mPlaceHolderFail)
                        .setFailureImageScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .build();
        mDraweeView.setHierarchy(hierarchy);
        addView(mDraweeView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Nullable
    public ResizeOptions createResizeOptions() {
        if (mImageResize > 0) {
            return ResizeOptions.forSquareSize(mImageResize);
        }

        if (mImageResizePercent > 0) {
            return ResizeOptions.forSquareSize((int) (mImageResizePercent * DimenUtil.getSmallScreenWidth()));
        }

        return null;
    }

    @NonNull
    public ImageRequest.CacheChoice createCacheChoice() {
        return mSmallCache ? ImageRequest.CacheChoice.SMALL : ImageRequest.CacheChoice.DEFAULT;
    }

    public void setScaleType(@ScaleType int scaleType) {
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            mDraweeView.getHierarchy().setActualImageScaleType(getScaleType(mScaleType));
        }
    }

    public void setImageUrl(@Nullable String thumb, @Nullable String... firstAvailable) {
        ImageRequest thumbRequest = null;
        if (thumb != null) {
            thumbRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(UrlUtil.alignUrl(thumb)))
                    .setResizeOptions(createResizeOptions())
                    .setCacheChoice(createCacheChoice())
                    .build();
        }

        if (firstAvailable == null || firstAvailable.length <= 0) {
            setImageUrl(thumbRequest);
            return;
        }

        final ImageRequest[] firstAvailableRequest = new ImageRequest[firstAvailable.length];
        for (int i = 0; i < firstAvailable.length; i++) {
            final String url = firstAvailable[i];
            if (url == null) {
                firstAvailableRequest[i] = null;
                continue;
            }
            firstAvailableRequest[i] = ImageRequestBuilder.newBuilderWithSource(Uri.parse(UrlUtil.alignUrl(url)))
                    .setResizeOptions(createResizeOptions())
                    .setCacheChoice(createCacheChoice())
                    .build();
        }
        setImageUrl(thumbRequest, firstAvailableRequest);
    }

    public void setImageUrl(@Nullable ImageRequest thumb, @Nullable ImageRequest... firstAvailable) {
        final PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder();
        builder.setOldController(mDraweeView.getController())
                .setLowResImageRequest(thumb);
        if (firstAvailable != null && firstAvailable.length > 0) {
            builder.setFirstAvailableImageRequests(firstAvailable);
        }
        builder.setAutoPlayAnimations(mAutoPlay);
        mDraweeView.setController(builder.build());
    }

    private static ScalingUtils.ScaleType getScaleType(@ScaleType int scaleType) {
        switch (scaleType) {
            case SCALE_TYPE_FIX_WIDTH:
                return ScaleTypeFixWidth.INSTANCE;
            case SCALE_TYPE_CENTER_INSIDE:
                return ScalingUtils.ScaleType.CENTER_INSIDE;
            case SCALE_TYPE_FIT_CENTER:
                return ScalingUtils.ScaleType.FIT_CENTER;
            case SCALE_TYPE_CENTER_CROP:
            default:
                return ScalingUtils.ScaleType.CENTER_CROP;
        }
    }

    private static class ScaleTypeFixWidth extends ScalingUtils.AbstractScaleType {

        public static final ScalingUtils.ScaleType INSTANCE = new ScaleTypeFixWidth();

        @Override
        public void getTransformImpl(
                Matrix outTransform,
                Rect parentRect,
                int childWidth,
                int childHeight,
                float focusX,
                float focusY,
                float scaleX,
                float scaleY) {

            float scale, dx, dy;

            scale = scaleX;
            dx = parentRect.left;
            dy = parentRect.top + (parentRect.height() - childHeight * scale) * 0.5f;

            outTransform.setScale(scale, scale);
            outTransform.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        }

        @NonNull
        @Override
        public String toString() {
            return "fix_width";
        }
    }

}
