package com.masonsoft.imsdk.uikit.common.voicerecordgesture;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;

import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.IOUtil;

public class VoiceRecordGestureHelper {

    public VoiceRecordGestureHelper(@NonNull View anchorView) {
        anchorView.setOnTouchListener(new OnTouchListenerImpl());
    }

    protected void onVoiceRecordGestureStart() {
    }

    protected void onVoiceRecordGestureMove(boolean inside) {
    }

    protected void onVoiceRecordGestureEnd(boolean inside) {
    }

    private class OnTouchListenerImpl implements View.OnTouchListener {

        @Nullable
        private GestureInfo mGestureInfo;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                if (mGestureInfo != null) {
                    IOUtil.closeQuietly(mGestureInfo);
                }
                mGestureInfo = new GestureInfo(v, event);
                v.setPressed(true);
            }

            if (action == MotionEvent.ACTION_MOVE) {
                if (mGestureInfo != null) {
                    mGestureInfo.moveTo(event);
                }
            }

            if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL) {
                if (mGestureInfo != null) {
                    IOUtil.closeQuietly(mGestureInfo);
                    mGestureInfo = null;
                    v.setPressed(false);
                }
            }

            return mGestureInfo != null;
        }
    }

    private class GestureInfo implements Closeable {

        private final int mTouchSlop = ViewConfiguration.get(ContextUtil.getContext()).getScaledTouchSlop();
        private final RectF mAnchorViewRect;
        private final float mDownRawX;
        private final float mDownRawY;
        private boolean mTriggerMove;
        private boolean mInside;
        private boolean mClosed;

        private GestureInfo(View anchorView, MotionEvent actionDownEvent) {
            final int viewWidth = anchorView.getWidth();
            final int viewHeight = anchorView.getHeight();

            final int[] viewLocation = new int[2];
            anchorView.getLocationInWindow(viewLocation);
            mAnchorViewRect = new RectF();
            mAnchorViewRect.left = viewLocation[0];
            mAnchorViewRect.top = viewLocation[1];
            mAnchorViewRect.right = viewLocation[0] + viewWidth;
            mAnchorViewRect.bottom = viewLocation[1] + viewHeight;

            mDownRawX = actionDownEvent.getRawX();
            mDownRawY = actionDownEvent.getRawY();
            mInside = true;
            mTriggerMove = false;

            onVoiceRecordGestureStart();
        }

        private void moveTo(MotionEvent event) {
            float x = event.getRawX();
            float y = event.getRawY();
            float absDx = Math.abs(x - mDownRawX);
            float absDy = Math.abs(y - mDownRawY);
            if (!mTriggerMove && (absDx >= mTouchSlop || absDy >= mTouchSlop)) {
                mTriggerMove = true;
            }

            if (!mClosed && mTriggerMove) {
                mInside = mAnchorViewRect.contains(x, y);
                onVoiceRecordGestureMove(mInside);
            }
        }

        @Override
        public void close() {
            if (!mClosed) {
                mClosed = true;
                onVoiceRecordGestureEnd(mInside);
            }
        }
    }

}
