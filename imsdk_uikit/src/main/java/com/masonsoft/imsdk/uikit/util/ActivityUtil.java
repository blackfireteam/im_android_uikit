package com.masonsoft.imsdk.uikit.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

public class ActivityUtil {

    private ActivityUtil() {
    }

    public static boolean requestBackPressed(Fragment fragment) {
        if (fragment != null) {
            final Activity activity = fragment.getActivity();
            if (activity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
                return false;
            }
            if (activity.isFinishing()) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_FINISHING);
                return false;
            }
            activity.onBackPressed();
            return true;
        } else {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_IS_NULL);
            return false;
        }
    }

    @Nullable
    public static Activity getActivity(@Nullable Context context) {
        while (context != null) {
            if (context instanceof Activity) {
                return (Activity) context;
            }

            if (context instanceof ContextWrapper) {
                final Context baseContext = ((ContextWrapper) context).getBaseContext();
                if (baseContext == context) {
                    return null;
                }
                context = baseContext;
            } else {
                return null;
            }
        }
        return null;
    }

    @Nullable
    public static AppCompatActivity getActiveAppCompatActivity(@Nullable Context context) {
        final Activity activity = getActivity(context);
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return null;
        }
        if (!(activity instanceof AppCompatActivity)) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NOT_APP_COMPAT_ACTIVITY);
            return null;
        }

        final AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
        if (appCompatActivity.isFinishing()) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_FINISHING);
            return null;
        }
        if (appCompatActivity.getSupportFragmentManager().isStateSaved()) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return null;
        }
        return appCompatActivity;
    }

}
