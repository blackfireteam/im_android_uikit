package com.masonsoft.imsdk.uikit.common.microlifecycle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.WeakHashMap;

@UiThread
public abstract class MicroLifecycleComponentManager implements Closeable, LifecycleEventObserver {

    protected static final boolean DEBUG = true;
    private static final Object EMPTY_OBJECT = new Object();
    @NonNull
    protected final Lifecycle mLifecycle;
    private final WeakHashMap<MicroLifecycleComponent, Object> mMicroLifecycleComponentsRef = new WeakHashMap<>();

    public MicroLifecycleComponentManager(@NonNull Lifecycle lifecycle) {
        mLifecycle = lifecycle;
        mLifecycle.addObserver(this);
    }

    @NonNull
    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    public void putComponent(@NonNull MicroLifecycleComponent microLifecycleComponent) {
        mMicroLifecycleComponentsRef.put(microLifecycleComponent, EMPTY_OBJECT);
        microLifecycleComponent.getLifecycleRegistry().handleLifecycleEvent(mLifecycle.getCurrentState());
    }

    public void removeComponent(@NonNull MicroLifecycleComponent microLifecycleComponent) {
        mMicroLifecycleComponentsRef.remove(microLifecycleComponent);
    }

    @NonNull
    public Collection<MicroLifecycleComponent> copyComponents() {
        mMicroLifecycleComponentsRef.size();
        return Lists.newArrayList(mMicroLifecycleComponentsRef.keySet());
    }

    protected void dispatchLifecycleEvent(@NonNull Lifecycle.Event event) {
        if (DEBUG) {
            MSIMUikitLog.v("dispatchLifecycleEvent %s", event);
        }
        Collection<MicroLifecycleComponent> microLifecycleComponents = copyComponents();
        for (MicroLifecycleComponent microLifecycleComponent : microLifecycleComponents) {
            if (microLifecycleComponent != null) {
                microLifecycleComponent.getLifecycleRegistry().handleLifecycleEvent(event);
            }
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (DEBUG) {
            MSIMUikitLog.v("onStateChanged %s", event);
        }
        dispatchLifecycleEvent(event);
    }

    @CallSuper
    @Override
    public void close() {
        mLifecycle.removeObserver(this);
        dispatchLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    /**
     * ????????? microLifecycleComponent ????????????????????? UI ????????????????????????????????????????????????????????? MicroLifecycleComponent#onResume
     */
    public abstract boolean isInPerfectArea(@NonNull MicroLifecycleComponent microLifecycleComponent);

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ?????????
     * ?????????????????????????????????????????????A????????????????????????????????? ViewHolder????????????A????????????????????? ViewHolder???.
     * ??????????????????????????? ???A????????????????????? ViewHolder????????? RecyclerView ?????????????????????????????????????????????A?????????????????? onResume.
     * ?????? RecyclerView ????????????????????????A????????????????????? ViewHolder??? ??????????????????????????????????????????????????????A?????????????????? onPause.
     * ????????????A?????????????????????????????? MicroLifecycleComponent ????????????
     * ???A?????????????????????????????? Fragment ?????? Activity ?????????????????????????????? Fragment ?????? Activity ?????????????????????????????? onCreate???
     * ?????????A?????????????????????????????? onCreate ?????????
     * ????????????????????? MicroLifecycleComponent ??????????????????????????? Fragment. MicroLifecycleComponent ????????? Fragment ?????????????????????.
     */
    public static abstract class MicroLifecycleComponent implements LifecycleOwner {

        @NonNull
        private final MicroLifecycleComponentManager mMicroLifecycleComponentManager;
        @NonNull
        private final MicroLifecycleComponentRegistry mLifecycleRegistry;
        private final MicroLifecycleComponentObserver mMicroLifecycleComponentObserver;

        public MicroLifecycleComponent(@NonNull MicroLifecycleComponentManager microLifecycleComponentManager) {
            mMicroLifecycleComponentManager = microLifecycleComponentManager;
            mLifecycleRegistry = new MicroLifecycleComponentRegistry(this);
            mMicroLifecycleComponentObserver = new MicroLifecycleComponentObserver();
            mLifecycleRegistry.addObserver(mMicroLifecycleComponentObserver);
            microLifecycleComponentManager.putComponent(this);
        }

        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return mLifecycleRegistry;
        }

        @NonNull
        public MicroLifecycleComponentRegistry getLifecycleRegistry() {
            return mLifecycleRegistry;
        }

        @NonNull
        public MicroLifecycleComponentManager getMicroLifecycleComponentManager() {
            return mMicroLifecycleComponentManager;
        }

        @CallSuper
        public void onCreate() {
            if (DEBUG) {
                MSIMUikitLog.v("onCreate @%s", hashCode());
            }
        }

        @CallSuper
        public void onStart() {
            if (DEBUG) {
                MSIMUikitLog.v("onStart @%s", hashCode());
            }
        }

        @CallSuper
        public void onResume() {
            if (DEBUG) {
                MSIMUikitLog.v("onResume @%s", hashCode());
            }
        }

        @CallSuper
        public void onPause() {
            if (DEBUG) {
                MSIMUikitLog.v("onPause @%s", hashCode());
            }
        }

        @CallSuper
        public void onStop() {
            if (DEBUG) {
                MSIMUikitLog.v("onStop @%s", hashCode());
            }
        }

        @CallSuper
        public void onDestroy() {
            if (DEBUG) {
                MSIMUikitLog.v("onDestroy @%s", hashCode());
            }
            mMicroLifecycleComponentManager.removeComponent(this);
        }

        private class MicroLifecycleComponentObserver implements LifecycleEventObserver {

            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                MSIMUikitLog.v("MicroLifecycleComponentObserver onStateChanged %s", event);
                if (event == Lifecycle.Event.ON_CREATE) {
                    onCreate();
                } else if (event == Lifecycle.Event.ON_START) {
                    onStart();
                } else if (event == Lifecycle.Event.ON_RESUME) {
                    onResume();
                } else if (event == Lifecycle.Event.ON_PAUSE) {
                    onPause();
                } else if (event == Lifecycle.Event.ON_STOP) {
                    onStop();
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    onDestroy();
                } else {
                    MSIMUikitLog.w("unexpected lifecycle event %s", event);
                }
            }

        }
    }

    public static class MicroLifecycleComponentRegistry extends LifecycleRegistry {

        private final WeakReference<MicroLifecycleComponent> mMicroLifecycleComponentRef;

        public MicroLifecycleComponentRegistry(@NonNull MicroLifecycleComponent microLifecycleComponent) {
            super(microLifecycleComponent);
            mMicroLifecycleComponentRef = new WeakReference<>(microLifecycleComponent);
        }

        @Nullable
        private Event getEventOfState(@Nullable State state) {
            if (state == null) {
                return null;
            }
            if (state.isAtLeast(State.RESUMED)) {
                return Event.ON_RESUME;
            } else if (state.isAtLeast(State.STARTED)) {
                return Event.ON_START;
            } else if (state.isAtLeast(State.CREATED)) {
                return Event.ON_CREATE;
            }
            return null;
        }

        void handleLifecycleEvent(State state) {
            Event event = getEventOfState(state);
            if (event == null) {
                return;
            }
            handleLifecycleEvent(event);
        }

        @Override
        public void handleLifecycleEvent(@NonNull Event event) {
            MicroLifecycleComponent microLifecycleComponent = mMicroLifecycleComponentRef.get();
            if (microLifecycleComponent == null) {
                if (DEBUG) {
                    MSIMUikitLog.e("microLifecycleComponent is null");
                }
                return;
            }
            Boolean playerInPerfectArea = null;
            MicroLifecycleComponentManager microLifecycleComponentManager = microLifecycleComponent.getMicroLifecycleComponentManager();
            if (event == Event.ON_RESUME) {
                playerInPerfectArea = isInPerfectArea(microLifecycleComponentManager, microLifecycleComponent, playerInPerfectArea);
                if (!playerInPerfectArea) {
                    // ??????????????????????????? microLifecycleComponent ?????? onResume
                    // ????????????????????? Event.ON_PAUSE ?????? Event.ON_START ??????????????????
                    event = Event.ON_PAUSE;
                }
            }

            super.handleLifecycleEvent(event);
        }

        private static boolean isInPerfectArea(@NonNull MicroLifecycleComponentManager microLifecycleComponentManager,
                                               @NonNull MicroLifecycleComponent microLifecycleComponent,
                                               Boolean pre) {
            if (pre != null) {
                return pre;
            }
            return microLifecycleComponentManager.isInPerfectArea(microLifecycleComponent);
        }

    }

}
