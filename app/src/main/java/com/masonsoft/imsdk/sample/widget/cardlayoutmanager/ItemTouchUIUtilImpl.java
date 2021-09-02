/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.masonsoft.imsdk.sample.widget.cardlayoutmanager;

import android.graphics.Canvas;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.sample.SampleLog;

import io.github.idonans.core.util.DimenUtil;

/**
 * Package private class to keep implementations. Putting them inside ItemTouchUIUtil makes them
 * public API, which is not desired in this case.
 */
class ItemTouchUIUtilImpl implements ItemTouchUIUtil {

    static final ItemTouchUIUtil INSTANCE = new ItemTouchUIUtilImpl();

    private final int PIVOT_Y_BOTTOM = DimenUtil.dp2px(88);
    private final Interpolator TRANSLATION_Y_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private final Interpolator ROTATE_INTERPOLATOR = new AccelerateInterpolator();

    @Override
    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY,
                       int actionState, boolean isCurrentlyActive) {
        final int childCount = recyclerView.getChildCount();
        int index = recyclerView.indexOfChild(view);
        if (childCount <= 0 || index < 0) {
            SampleLog.e(new RuntimeException("unexpected index:" + index + " or childCount:" + childCount));
            return;
        }

        float progress = Math.abs(dX) / recyclerView.getWidth();
        progress = MathUtils.clamp(progress, 0f, 1f);
        final float originProgress = progress;
        progress = 0.9f + progress * 0.1f;
        SampleLog.v("onDraw progress:" + progress);

        for (int i = index - 1; i >= 0; i--) {
            View childView = recyclerView.getChildAt(i);
            childView.setRotation(0f);
            childView.setTranslationX(0f);
            childView.setTranslationY(0f);
            childView.setScaleX(progress);
            childView.setScaleY(progress);
            childView.setPivotX(childView.getWidth() * 0.5f);
            childView.setPivotY(childView.getHeight() - PIVOT_Y_BOTTOM);
        }

        view.setTranslationX(dX * 1.3f);
        view.setTranslationY(TRANSLATION_Y_INTERPOLATOR.getInterpolation(originProgress) * view.getWidth() * 0.25f);
        view.setScaleX(1f);
        view.setScaleY(1f);

        final float signum = Math.signum(dX);
        view.setPivotX(view.getWidth() * 0.5f + signum * view.getWidth() * 0.25f * (1 + ROTATE_INTERPOLATOR.getInterpolation(originProgress)));
        view.setPivotY(view.getHeight() - PIVOT_Y_BOTTOM - view.getWidth() * 0.25f);

        final float rotation = signum * 30 * ROTATE_INTERPOLATOR.getInterpolation(originProgress);
        view.setRotation(rotation);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY,
                           int actionState, boolean isCurrentlyActive) {
    }

    @Override
    public void clearView(View view) {
        // view.setTranslationX(0f);
        // view.setTranslationY(0f);
    }

    @Override
    public void onSelected(View view) {
    }

}
