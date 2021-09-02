package com.masonsoft.imsdk.uikit.app;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

public class SystemInsetsFragment extends Fragment {

    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view.isAttachedToWindow()) {
            ViewCompat.requestApplyInsets(view);
        } else {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    ViewCompat.requestApplyInsets(v);
                    v.removeOnAttachStateChangeListener(this);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

}
