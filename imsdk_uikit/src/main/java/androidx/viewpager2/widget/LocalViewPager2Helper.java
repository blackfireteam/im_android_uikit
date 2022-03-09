package androidx.viewpager2.widget;

import androidx.recyclerview.widget.RecyclerView;

public class LocalViewPager2Helper {

    public static RecyclerView getRecyclerView(ViewPager2 viewPager) {
        return viewPager.mRecyclerView;
    }

}
