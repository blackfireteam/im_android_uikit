package com.masonsoft.imsdk.sample;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.util.Objects;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.Singleton;
import io.github.idonans.core.manager.StorageManager;
import io.github.idonans.core.thread.Threads;

public class LocalSettingsManager {

    private static final Singleton<LocalSettingsManager> INSTANCE = new Singleton<LocalSettingsManager>() {
        @Override
        protected LocalSettingsManager create() {
            return new LocalSettingsManager();
        }
    };

    public static LocalSettingsManager getInstance() {
        return INSTANCE.get();
    }

    private static final String KEY_SETTINGS = "key:settings_20210420";

    @NonNull
    private Settings mSettings = new Settings();
    @NonNull
    private final ApiServerLru mApiServerLru = new ApiServerLru();

    private LocalSettingsManager() {
        restore();
        mApiServerLru.restore();
    }

    public void start() {
        SampleLog.v(Objects.defaultObjectTag(this) + " start");

        if (mSettings.hasValidSession()) {
            MSIMManager.getInstance().setSession(mSettings.imToken, mSettings.imServer);
        }
    }

    private void restore() {
        try {
            final String json = StorageManager.getInstance().get(Constants.SAMPLE_STORAGE_NAMESPACE, KEY_SETTINGS);
            if (!TextUtils.isEmpty(json)) {
                final Settings settings = new Gson().fromJson(json, new TypeToken<Settings>() {
                }.getType());
                if (settings != null) {
                    mSettings = settings;
                }
            }
        } catch (Throwable e) {
            SampleLog.e(e);
        }
    }

    public void clearToken() {
        final Settings settings = getSettings();
        settings.imToken = null;
        setSettings(settings);
    }

    @NonNull
    public Settings getSettings() {
        return mSettings.copy();
    }

    public void setSettings(@Nullable Settings settings) {
        if (settings == null) {
            mSettings = new Settings();
        } else {
            mSettings = settings.copy();
        }
        Threads.postBackground(() -> {
            try {
                final String json = new Gson().toJson(mSettings.copy());
                StorageManager.getInstance().set(Constants.SAMPLE_STORAGE_NAMESPACE, KEY_SETTINGS, json);
            } catch (Throwable e) {
                SampleLog.e(e);
            }
        });
    }

    public static class Settings {

        public String apiServer;
        public String imToken;
        public String imServer;

        public boolean hasValidSession() {
            return !TextUtils.isEmpty(this.imServer)
                    && !TextUtils.isEmpty(this.imToken);
        }

        private Settings copy() {
            final Settings target = new Settings();
            target.apiServer = this.apiServer;
            target.imToken = this.imToken;
            target.imServer = this.imServer;
            return target;
        }
    }

    @NonNull
    public ApiServerLru getApiServerLru() {
        return mApiServerLru;
    }

    /**
     * 记录用户输入的 api server 历史记录，lru
     */
    public static class ApiServerLru {
        private static final String KEY_API_SERVER_LRU = "key:ApiServerLru_20211102";
        private static final int MAX_SIZE = 5;
        private final List<String> mCache = new ArrayList<>();

        private ApiServerLru() {
            // 添加默认值
            mCache.add("https://im.ekfree.com:18789");
            mCache.add("https://192.168.50.189:18789");
        }

        public void addApiServer(@Nullable String apiServer) {
            this.addApiServer(apiServer, false, true);
        }

        private void addApiServer(@Nullable String apiServer, boolean append, boolean save) {
            boolean changed = false;
            if (!TextUtils.isEmpty(apiServer)) {
                //noinspection ConstantConditions
                apiServer = apiServer.trim();
                synchronized (mCache) {
                    mCache.remove(apiServer);
                    if (append) {
                        mCache.add(apiServer);
                    } else {
                        mCache.add(0, apiServer);
                    }

                    if (mCache.size() > MAX_SIZE) {
                        mCache.remove(mCache.size() - 1);
                    }
                }
                changed = true;
            }
            if (changed && save) {
                saveAsync();
            }
        }

        @NonNull
        public final List<String> allApiServer() {
            synchronized (mCache) {
                return new ArrayList<>(mCache);
            }
        }

        private void restore() {
            try {
                final String json = StorageManager.getInstance().get(Constants.SAMPLE_STORAGE_NAMESPACE, KEY_API_SERVER_LRU);
                if (!TextUtils.isEmpty(json)) {
                    final List<String> apiServerList = new Gson().fromJson(json, new TypeToken<List<String>>() {
                    }.getType());
                    if (apiServerList != null) {
                        for (String apiServer : apiServerList) {
                            addApiServer(apiServer, true, false);
                        }
                    }
                }
            } catch (Throwable e) {
                SampleLog.e(e);
            }
        }

        private void saveAsync() {
            Threads.postBackground(() -> {
                try {
                    final List<String> cache = allApiServer();
                    final String json = new Gson().toJson(cache);
                    StorageManager.getInstance().set(Constants.SAMPLE_STORAGE_NAMESPACE, KEY_API_SERVER_LRU, json);
                } catch (Throwable e) {
                    SampleLog.e(e);
                }
            });
        }

    }

}
