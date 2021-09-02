package com.masonsoft.imsdk.sample;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.util.Objects;

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

    private LocalSettingsManager() {
        restore();
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

}
