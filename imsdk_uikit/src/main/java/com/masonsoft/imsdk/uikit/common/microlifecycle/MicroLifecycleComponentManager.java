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
     * 判断该 microLifecycleComponent 是否处于最佳的 UI 位置。如果没有处于最佳位置，则不能触发 MicroLifecycleComponent#onResume
     */
    public abstract boolean isInPerfectArea(@NonNull MicroLifecycleComponent microLifecycleComponent);

    /**
     * 用以声明一个或一组具有独立生命周期的组件。该组件的生命周期处于整体的生命周期之内，同时具有特殊业务需求的生命周期调整性。
     * 例如：
     * 声明一个生命周期组件，命名为“A周期组件”，它对应一个 ViewHolder，称为“A周期组件对应的 ViewHolder”.
     * 业务需求是当且仅当 “A周期组件对应的 ViewHolder”正在 RecyclerView 的中心处并且前台展示时，触发“A周期组件”的 onResume.
     * 随着 RecyclerView 的滑动或切换，“A周期组件对应的 ViewHolder” 被移动到中心之外，或者被移除，触发“A周期组件”的 onPause.
     * 那么此“A周期组件”就可以使用 MicroLifecycleComponent 来实现。
     * “A周期组件”同时会受到 Fragment 或者 Activity 生命周期的影响，如果 Fragment 或者 Activity 的生命周期当前仅处于 onCreate，
     * 那么“A周期组件”也至多处于 onCreate 状态。
     * 可以简单的理解 MicroLifecycleComponent 相当于一个缩小版的 Fragment. MicroLifecycleComponent 具有与 Fragment 相似的生命周期.
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
                    // 没有处于正确区域的 microLifecycleComponent 不能 onResume
                    // 此处周期调整为 Event.ON_PAUSE 或者 Event.ON_START 的结果一样。
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
