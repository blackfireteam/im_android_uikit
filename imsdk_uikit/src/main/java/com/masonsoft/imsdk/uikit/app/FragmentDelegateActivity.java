package com.masonsoft.imsdk.uikit.app;

import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

/**
 * @since 1.0
 */
public class FragmentDelegateActivity extends AppCompatActivity {

    public interface FragmentDelegateCreator {
        Fragment createFragmentDelegate();
    }

    protected void setFragmentDelegate(@NonNull String fragmentDelegateTag, @NonNull FragmentDelegateCreator creator) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        boolean needCommit = false;

        if (fm.isStateSaved()) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        Fragment fragmentDelegate = fm.findFragmentByTag(fragmentDelegateTag);
        if (fragmentDelegate == null) {
            fragmentDelegate = creator.createFragmentDelegate();
            ft.add(Window.ID_ANDROID_CONTENT, fragmentDelegate, fragmentDelegateTag);
            needCommit = true;
        }

        if (needCommit) {
            ft.commitNow();
        }
    }

    @Nullable
    protected Fragment getFragmentDelegate(String fragmentDelegateTag) {
        return getSupportFragmentManager().findFragmentByTag(fragmentDelegateTag);
    }

}
