package com.masonsoft.imsdk.uikit.common.media.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.video.VideoListener;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.io.Closeable;

import io.github.idonans.core.util.ContextUtil;

public class MediaPlayer implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    @Nullable
    private SimpleExoPlayer mPlayer;

    MediaPlayer(@NonNull ProgressiveMediaSource mediaSource, boolean autoPlay, boolean loop) {
        mPlayer = new SimpleExoPlayer.Builder(ContextUtil.getContext())
                .setTrackSelector(new DefaultTrackSelector(ContextUtil.getContext()))
                .build();
        mPlayer.setPlayWhenReady(autoPlay);
        mPlayer.setRepeatMode(loop ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
        mPlayer.addAudioListener(new AudioListener() {
            @Override
            public void onAudioSessionIdChanged(int audioSessionId) {
                if (DEBUG) {
                    MSIMUikitLog.v("onAudioSessionIdChanged audioSessionId:%s", audioSessionId);
                }
            }

            @Override
            public void onAudioAttributesChanged(AudioAttributes audioAttributes) {
                if (DEBUG) {
                    MSIMUikitLog.v("onAudioAttributesChanged");
                }
            }

            @Override
            public void onVolumeChanged(float volume) {
                if (DEBUG) {
                    MSIMUikitLog.v("onVolumeChanged volume:%s", volume);
                }
            }
        });
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
                if (DEBUG) {
                    MSIMUikitLog.v("onTimelineChanged reason:%s", reason);
                }
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                if (DEBUG) {
                    MSIMUikitLog.v("onTracksChanged");
                }
            }

            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                if (DEBUG) {
                    MSIMUikitLog.v("onIsLoadingChanged isLoading:%s", isLoading);
                }
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (DEBUG) {
                    MSIMUikitLog.v("onPlaybackStateChanged state:%s", state);
                }
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                if (DEBUG) {
                    MSIMUikitLog.v("onPlayWhenReadyChanged playWhenReady:%s reason:%s", playWhenReady, reason);
                }
            }

            @Override
            public void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {
                if (DEBUG) {
                    MSIMUikitLog.v("onPlaybackSuppressionReasonChanged playbackSuppressionReason:%s", playbackSuppressionReason);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (DEBUG) {
                    MSIMUikitLog.v("onIsPlayingChanged isPlaying:%s", isPlaying);
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                if (DEBUG) {
                    MSIMUikitLog.v("onRepeatModeChanged repeatMode:%s", repeatMode);
                }
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                if (DEBUG) {
                    MSIMUikitLog.v("onShuffleModeEnabledChanged shuffleModeEnabled:%s", shuffleModeEnabled);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if (DEBUG) {
                    MSIMUikitLog.v(error, "onPlayerError");
                    error.printStackTrace();
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                if (DEBUG) {
                    MSIMUikitLog.v("onPositionDiscontinuity reason:%s", reason);
                }
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                if (DEBUG) {
                    MSIMUikitLog.v("onPlaybackParametersChanged");
                }
            }
        });
        mPlayer.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                if (DEBUG) {
                    MSIMUikitLog.v("onVideoSizeChanged width:%s, height:%s, unappliedRotationDegrees:%s, pixelWidthHeightRatio:%s",
                            width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
                }
            }

            @Override
            public void onSurfaceSizeChanged(int width, int height) {
                if (DEBUG) {
                    MSIMUikitLog.v("onSurfaceSizeChanged width:%s, height:%s", width, height);
                }
            }

            @Override
            public void onRenderedFirstFrame() {
                if (DEBUG) {
                    MSIMUikitLog.v("onRenderedFirstFrame");
                }
            }
        });
        mPlayer.addAnalyticsListener(new EventLogger(null, "MediaPlayer"));

        mPlayer.setMediaSource(mediaSource);
        mPlayer.prepare();
    }

    @Nullable
    public SimpleExoPlayer getPlayer() {
        return mPlayer;
    }

    @Override
    public void close() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

}
