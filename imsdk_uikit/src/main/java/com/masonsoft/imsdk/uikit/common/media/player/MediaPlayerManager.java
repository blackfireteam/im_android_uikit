package com.masonsoft.imsdk.uikit.common.media.player;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.database.DefaultDatabaseProvider;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.masonsoft.imsdk.uikit.util.OkHttpClientUtil;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.io.File;

import io.github.idonans.core.Singleton;
import io.github.idonans.core.manager.ProcessManager;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.FileUtil;

public class MediaPlayerManager {

    private static final Singleton<MediaPlayerManager> INSTANCE = new Singleton<MediaPlayerManager>() {
        @Override
        protected MediaPlayerManager create() {
            return new MediaPlayerManager();
        }
    };

    public static MediaPlayerManager getInstance() {
        return INSTANCE.get();
    }

    private static final String CACHE_DIR_NAME = "media_player_cache_20210510";
    private static final String USER_AGENT = "media_player";
    private static final String DATABASE_NAME = "media_player_20210510";
    private static final int DATABASE_VERSION = 1;

    @Nullable
    private final DataSource.Factory mDataSourceFactory;

    private MediaPlayerManager() {
        mDataSourceFactory = createDataSourceFactory();
    }

    @Nullable
    private DataSource.Factory createDataSourceFactory() {
        try {
            DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder(ContextUtil.getContext()).build();
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                    ContextUtil.getContext(),
                    defaultBandwidthMeter,
                    new OkHttpDataSource.Factory(OkHttpClientUtil.createDefaultOkHttpClient())
                            .setUserAgent(USER_AGENT));

            File cacheDir = createMediaCacheDir();
            if (cacheDir == null) {
                MSIMUikitLog.e("cache dir is null, ignore cache.");
                return dataSourceFactory;
            }

            Cache cache = new SimpleCache(
                    cacheDir,
                    new NoOpCacheEvictor(),
                    new DefaultDatabaseProvider(
                            new SQLiteOpenHelper(
                                    ContextUtil.getContext(),
                                    ProcessManager.getInstance().getProcessTag() + "_" + DATABASE_NAME,
                                    null,
                                    DATABASE_VERSION) {
                                @Override
                                public void onCreate(SQLiteDatabase db) {
                                }

                                @Override
                                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                                }
                            }));
            return new CacheDataSource.Factory()
                    .setCache(cache)
                    .setUpstreamDataSourceFactory(dataSourceFactory)
                    .setCacheReadDataSourceFactory(new FileDataSource.Factory())
                    .setCacheWriteDataSinkFactory(
                            new CacheDataSink.Factory()
                                    .setCache(cache)
                                    .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)
                    )
                    .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private File createMediaCacheDir() {
        try {
            File parent = FileUtil.getAppCacheDir();
            File cacheDir = new File(parent, CACHE_DIR_NAME);
            if (!FileUtil.createDir(cacheDir)) {
                throw new IllegalStateException("fail to create media cache dir, parent:" + parent + ", cacheDirName:" + CACHE_DIR_NAME);
            }
            FileUtil.createNewFileQuietly(new File(cacheDir, ".nomedia"));
            return cacheDir;
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public MediaPlayer getMediaPlayer(String url, boolean autoPlay, boolean loop) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        if (mDataSourceFactory == null) {
            return null;
        }

        try {
            ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(mDataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(url));
            return new MediaPlayer(mediaSource, autoPlay, loop);
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
            e.printStackTrace();
        }
        return null;
    }

}
