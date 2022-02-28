package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.masonsoft.imsdk.uikit.R;

public class SnapchatContainer extends FrameLayout {

    public SnapchatContainer(Context context) {
        this(context, null);
    }

    public SnapchatContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnapchatContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.SnapchatContainer);
    }

    public SnapchatContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean mSnapchat;
    private Drawable mSnapchatOverlay;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnapchatContainer, defStyleAttr,
                defStyleRes);
        mSnapchat = a.getBoolean(R.styleable.SnapchatContainer_snapchat, mSnapchat);
        mSnapchatOverlay = a.getDrawable(R.styleable.SnapchatContainer_snapchatOverlay);
        a.recycle();
    }

    public boolean isSnapchat() {
        return this.mSnapchat;
    }

    public void setSnapchat(boolean snapchat) {
        if (mSnapchat != snapchat) {
            mSnapchat = snapchat;
            invalidate();
        }
    }

    public void setSnapchatOverlay(Drawable snapchatOverlay) {
        if (mSnapchatOverlay != snapchatOverlay) {
            mSnapchatOverlay = snapchatOverlay;
            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        final Drawable overlay = mSnapchatOverlay;
        if (overlay == null || !mSnapchat) {
            return;
        }

        final int width = getWidth();
        final int height = getHeight();

        overlay.setBounds(0, 0, width, height);
        overlay.draw(canvas);
    }

}
