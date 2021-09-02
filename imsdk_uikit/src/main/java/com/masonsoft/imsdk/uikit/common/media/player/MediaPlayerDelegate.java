package com.masonsoft.imsdk.uikit.common.media.player;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import io.github.idonans.core.util.IOUtil;

public class MediaPlayerDelegate {

    @Nullable
    private MediaPlayer mMediaPlayer;
    @Nullable
    private MediaPlayerView mMediaPlayerView;

    public void initPlayerIfNeed(
            @Nullable MediaPlayerView mediaPlayerView,
            @Nullable String url,
            boolean force,
            boolean play) {
        initPlayerIfNeed(mediaPlayerView, url, force, play, true);
    }

    public void initPlayerIfNeed(
            @Nullable MediaPlayerView mediaPlayerView,
            @Nullable String url,
            boolean force,
            boolean play,
            boolean loop) {
        if (mMediaPlayerView != mediaPlayerView) {
            if (mMediaPlayerView != null && mediaPlayerView != null) {
                SimpleExoPlayer player = null;
                if (mMediaPlayer != null) {
                    player = mMediaPlayer.getPlayer();
                }
                mediaPlayerView.setPlayer(player);
                releasePlayerView(mMediaPlayerView);
            } else {
                releasePlayerView(mMediaPlayerView);
                releasePlayerView(mediaPlayerView);
            }
        }
        mMediaPlayerView = mediaPlayerView;

        if (force) {
            releasePlayer();
        }

        if (TextUtils.isEmpty(url)) {
            releasePlayer();
            return;
        }

        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayerManager.getInstance().getMediaPlayer(url, play, loop);
                if (mMediaPlayer == null) {
                    releasePlayer();
                    return;
                }
                if (mMediaPlayer.getPlayer() == null) {
                    releasePlayer();
                    return;
                }

                if (mMediaPlayerView != null) {
                    mMediaPlayerView.setPlayer(mMediaPlayer.getPlayer());
                }
            } else {
                SimpleExoPlayer player = mMediaPlayer.getPlayer();
                if (player != null) {
                    player.setPlayWhenReady(play);
                }
            }
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
            e.printStackTrace();
        }

    }

    @Nullable
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void releasePlayer() {
        try {
            releasePlayerView(mMediaPlayerView);
            IOUtil.closeQuietly(mMediaPlayer);
            mMediaPlayer = null;
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
            e.printStackTrace();
        }
    }

    private void releasePlayerView(MediaPlayerView mediaPlayerView) {
        if (mediaPlayerView != null) {
            mediaPlayerView.setPlayer(null);
        }
    }

    public void pausePlayer() {
        try {
            if (mMediaPlayer != null) {
                SimpleExoPlayer player = mMediaPlayer.getPlayer();
                if (player != null) {
                    player.setPlayWhenReady(false);
                }
            }
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
            e.printStackTrace();
        }
    }

}
