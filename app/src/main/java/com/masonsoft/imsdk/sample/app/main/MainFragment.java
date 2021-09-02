package com.masonsoft.imsdk.sample.app.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.app.discover.DiscoverFragment;
import com.masonsoft.imsdk.sample.app.home.HomeFragment;
import com.masonsoft.imsdk.sample.app.mine.MineFragment;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleMainFragmentBinding;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.app.conversation.ConversationFragment;

public class MainFragment extends SystemInsetsFragment {

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private ImsdkSampleMainFragmentBinding mBinding;

    private static final int FRAGMENT_ID_HOME = 0;
    private static final int FRAGMENT_ID_DISCOVER = 1;
    private static final int FRAGMENT_ID_CONVERSATION = 2;
    private static final int FRAGMENT_ID_MINE = 3;

    @IntDef({FRAGMENT_ID_HOME, FRAGMENT_ID_DISCOVER, FRAGMENT_ID_CONVERSATION, FRAGMENT_ID_MINE})
    private @interface FragmentId {
    }

    private int mCurrentFragmentId = -1;

    private static final String FRAGMENT_TAG_HOME = "fragment_home_20210416";
    private static final String FRAGMENT_TAG_DISCOVER = "fragment_discover_20210416";
    private static final String FRAGMENT_TAG_CONVERSATION = "fragment_conversation_20210416";
    private static final String FRAGMENT_TAG_MINE = "fragment_mine_20210416";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkSampleMainFragmentBinding.inflate(inflater, container, false);
        mBinding.mainBottomBarContainer.setOnSystemInsetsListener((left, top, right, bottom) ->
                SampleLog.v("mainBottomBarContainer onSystemInsets: left:%s, top:%s, right:%s, bottom:%s", left, top, right, bottom)
        );
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.mainBottomBar.setOnTabClickListener(this::syncTabSelected);
        syncTabSelected(0);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBinding = null;
    }

    private void syncTabSelected(int index) {
        SampleLog.v("syncTabSelected index:%s", index);
        if (mBinding != null) {
            if (mBinding.mainBottomBar.getCurrentItem() != index) {
                mBinding.mainBottomBar.setCurrentItem(index);
            }
            showFragment(index);
        }
    }

    private void showFragment(@FragmentId int fragmentId) {
        if (mCurrentFragmentId == fragmentId) {
            SampleLog.v("ignore. showFragment mCurrentFragmentId is already %s", fragmentId);
            return;
        }


        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        boolean needCommit = false;

        if (fm.isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        final Fragment homeFragment = getFragment(FRAGMENT_ID_HOME, fragmentId == FRAGMENT_ID_HOME);
        final Fragment discoverFragment = getFragment(FRAGMENT_ID_DISCOVER, fragmentId == FRAGMENT_ID_DISCOVER);
        final Fragment conversationFragment = getFragment(FRAGMENT_ID_CONVERSATION, fragmentId == FRAGMENT_ID_CONVERSATION);
        final Fragment mineFragment = getFragment(FRAGMENT_ID_MINE, fragmentId == FRAGMENT_ID_MINE);

        if (homeFragment != null) {
            if (fragmentId == FRAGMENT_ID_HOME) {
                ft.show(homeFragment).setMaxLifecycle(homeFragment, Lifecycle.State.RESUMED);
            } else {
                ft.hide(homeFragment).setMaxLifecycle(homeFragment, Lifecycle.State.STARTED);
            }
            needCommit = true;
        }

        if (discoverFragment != null) {
            if (fragmentId == FRAGMENT_ID_DISCOVER) {
                ft.show(discoverFragment).setMaxLifecycle(discoverFragment, Lifecycle.State.RESUMED);
            } else {
                ft.hide(discoverFragment).setMaxLifecycle(discoverFragment, Lifecycle.State.STARTED);
            }
            needCommit = true;
        }

        if (conversationFragment != null) {
            if (fragmentId == FRAGMENT_ID_CONVERSATION) {
                ft.show(conversationFragment).setMaxLifecycle(conversationFragment, Lifecycle.State.RESUMED);
            } else {
                ft.hide(conversationFragment).setMaxLifecycle(conversationFragment, Lifecycle.State.STARTED);
            }
            needCommit = true;
        }

        if (mineFragment != null) {
            if (fragmentId == FRAGMENT_ID_MINE) {
                ft.show(mineFragment).setMaxLifecycle(mineFragment, Lifecycle.State.RESUMED);
            } else {
                ft.hide(mineFragment).setMaxLifecycle(mineFragment, Lifecycle.State.STARTED);
            }
            needCommit = true;
        }

        if (needCommit) {
            mCurrentFragmentId = fragmentId;
            ft.commitNow();
        }
    }

    private static String getFragmentTag(@FragmentId int fragmentId) {
        if (fragmentId == FRAGMENT_ID_HOME) {
            return FRAGMENT_TAG_HOME;
        }
        if (fragmentId == FRAGMENT_ID_DISCOVER) {
            return FRAGMENT_TAG_DISCOVER;
        }
        if (fragmentId == FRAGMENT_ID_CONVERSATION) {
            return FRAGMENT_TAG_CONVERSATION;
        }
        if (fragmentId == FRAGMENT_ID_MINE) {
            return FRAGMENT_TAG_MINE;
        }
        final Throwable e = new IllegalArgumentException("unexpected fragmentId:" + fragmentId);
        throw new RuntimeException(e);
    }

    private static Fragment newFragment(@FragmentId int fragmentId) {
        if (fragmentId == FRAGMENT_ID_HOME) {
            return HomeFragment.newInstance();
        }
        if (fragmentId == FRAGMENT_ID_DISCOVER) {
            return DiscoverFragment.newInstance();
        }
        if (fragmentId == FRAGMENT_ID_CONVERSATION) {
            return ConversationFragment.newInstance();
        }
        if (fragmentId == FRAGMENT_ID_MINE) {
            return MineFragment.newInstance();
        }
        final Throwable e = new IllegalArgumentException("unexpected fragmentId:" + fragmentId);
        throw new RuntimeException(e);
    }

    @Nullable
    private Fragment getFragment(@FragmentId int fragmentId, boolean create) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        boolean needCommit = false;

        if (fm.isStateSaved()) {
            SampleLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return null;
        }

        final String fragmentTag = getFragmentTag(fragmentId);
        Fragment fragment = fm.findFragmentByTag(fragmentTag);
        if (fragment == null && create) {
            fragment = newFragment(fragmentId);
            ft.add(R.id.system_insets_layer_layout, fragment, fragmentTag);
            needCommit = true;
        }

        if (needCommit) {
            ft.commitNow();
        }
        return fragment;
    }

}
