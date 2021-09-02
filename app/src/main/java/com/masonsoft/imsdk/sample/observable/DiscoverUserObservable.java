package com.masonsoft.imsdk.sample.observable;

import com.masonsoft.imsdk.util.WeakObservable;

/**
 * @see com.masonsoft.imsdk.sample.im.DiscoverUserManager
 */
public class DiscoverUserObservable extends WeakObservable<DiscoverUserObservable.DiscoverUserObserver> {

    public static final DiscoverUserObservable DEFAULT = new DiscoverUserObservable();

    public interface DiscoverUserObserver {
        void onDiscoverUserOnline(long userId);

        void onDiscoverUserOffline(long userId);
    }

    public void notifyDiscoverUserOnline(long userId) {
        forEach(discoverUserObserver -> discoverUserObserver.onDiscoverUserOnline(userId));
    }

    public void notifyDiscoverUserOffline(long userId) {
        forEach(discoverUserObserver -> discoverUserObserver.onDiscoverUserOffline(userId));
    }

}
