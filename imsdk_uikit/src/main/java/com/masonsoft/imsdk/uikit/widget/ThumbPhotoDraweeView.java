package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.request.ImageRequest;
import com.masonsoft.imsdk.uikit.widget.zoomable.DoubleTapGestureListener;
import com.masonsoft.imsdk.uikit.widget.zoomable.ZoomableDraweeView;

public class ThumbPhotoDraweeView extends ZoomableDraweeView {

    public ThumbPhotoDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public ThumbPhotoDraweeView(Context context) {
        super(context);
        init();
    }

    public ThumbPhotoDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThumbPhotoDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Nullable
    private View.OnClickListener mOnClickListener;

    private void init() {
        setAllowTouchInterceptionWhileZoomed(true);
        setIsLongpressEnabled(false);
        setTapListener(new DoubleTapGestureListener(this) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(ThumbPhotoDraweeView.this);
                }
                return true;
            }
        });
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setImageUrl(@Nullable ImageRequest thumb, @Nullable ImageRequest... firstAvailable) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(getController())
                .setLowResImageRequest(thumb)
                .setFirstAvailableImageRequests(firstAvailable)
                .build();
        setController(controller);
    }

}
