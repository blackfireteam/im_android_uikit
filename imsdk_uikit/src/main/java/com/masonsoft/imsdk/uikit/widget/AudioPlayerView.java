package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.util.Util;
import com.masonsoft.imsdk.uikit.common.media.player.MediaPlayerView;

public class AudioPlayerView extends ResizeAudioView implements MediaPlayerView {

    public AudioPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public AudioPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AudioPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private static final int MAX_UPDATE_INTERVAL_MS = 1000;
    public static final int MIN_UPDATE_INTERVAL_MS = 200;

    @Nullable
    protected Player mPlayer;
    private ComponentListener mComponentListener;
    private Runnable mUpdateProgressAction;
    private boolean mAttachedToWindow;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mComponentListener = new ComponentListener();
        mUpdateProgressAction = this::updateProgress;
    }

    @Override
    public void setPlayer(@Nullable Player player) {
        if (mPlayer == player) {
            return;
        }

        if (mPlayer != null) {
            mPlayer.addListener(mComponentListener);
            Player.AudioComponent oldAudioComponent = mPlayer.getAudioComponent();
            if (oldAudioComponent != null) {
                oldAudioComponent.addAudioListener(mComponentListener);
            }
        }

        mPlayer = player;
        updateAll();

        if (player != null) {
            Player.AudioComponent newAudioComponent = player.getAudioComponent();
            if (newAudioComponent != null) {
                newAudioComponent.addAudioListener(mComponentListener);
            }
            player.addListener(mComponentListener);
        }
    }

    private final class ComponentListener implements AudioListener, Player.EventListener {
        @Override
        public void onPlaybackStateChanged(int state) {
            updatePlayPauseStatus();
            updateProgress();
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            updatePlayPauseStatus();
            updateProgress();
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            updateProgress();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            updateProgress();
        }
    }

    private void updateAll() {
        updatePlayPauseStatus();
        updateProgress();
    }

    private void updatePlayPauseStatus() {
        updatePlayPauseView(shouldShowPauseButton());
    }

    private void updateProgress() {
        if (!isVisible() || !mAttachedToWindow) {
            return;
        }

        long position = 0;
        long bufferedPosition = 0;
        long duration = 0;
        if (mPlayer != null) {
            position = mPlayer.getContentPosition();
            bufferedPosition = mPlayer.getContentBufferedPosition();
            duration = mPlayer.getDuration();
        }

        updateProgressView(position, bufferedPosition, duration);

        removeCallbacks(mUpdateProgressAction);

        int playbackState = mPlayer == null ? Player.STATE_IDLE : mPlayer.getPlaybackState();
        if (mPlayer != null && mPlayer.isPlaying()) {
            long mediaTimeDelayMs = MAX_UPDATE_INTERVAL_MS;

            // Limit delay to the start of the next full second to ensure position display is smooth.
            long mediaTimeUntilNextFullSecondMs = 1000 - position % 1000;
            mediaTimeDelayMs = Math.min(mediaTimeDelayMs, mediaTimeUntilNextFullSecondMs);

            // Calculate the delay until the next update in real time, taking playbackSpeed into account.
            float playbackSpeed = mPlayer.getPlaybackParameters().speed;
            long delayMs =
                    playbackSpeed > 0 ? (long) (mediaTimeDelayMs / playbackSpeed) : MAX_UPDATE_INTERVAL_MS;

            // Constrain the delay to avoid too frequent / infrequent updates.
            delayMs = Util.constrainValue(delayMs, MIN_UPDATE_INTERVAL_MS, MAX_UPDATE_INTERVAL_MS);
            postDelayed(mUpdateProgressAction, delayMs);
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            postDelayed(mUpdateProgressAction, MAX_UPDATE_INTERVAL_MS);
        }
    }

    public interface OnPlayerStateUpdateListener {
        void onPlayerPlayPauseUpdate(boolean shouldShowPauseButton);

        void onPlayerProgressUpdate(long position, long bufferedPosition, long duration);
    }

    @Nullable
    private OnPlayerStateUpdateListener mOnPlayerStateUpdateListener;

    public void setOnPlayerStateUpdateListener(@Nullable OnPlayerStateUpdateListener listener) {
        mOnPlayerStateUpdateListener = listener;
    }

    protected void updatePlayPauseView(boolean shouldShowPauseButton) {
        if (mOnPlayerStateUpdateListener != null) {
            mOnPlayerStateUpdateListener.onPlayerPlayPauseUpdate(shouldShowPauseButton);
        }
    }

    protected void updateProgressView(long position, long bufferedPosition, long duration) {
        if (mOnPlayerStateUpdateListener != null) {
            mOnPlayerStateUpdateListener.onPlayerProgressUpdate(position, bufferedPosition, duration);
        }
    }

    private boolean shouldShowPauseButton() {
        return mPlayer != null
                && mPlayer.getPlaybackState() != Player.STATE_ENDED
                && mPlayer.getPlaybackState() != Player.STATE_IDLE
                && mPlayer.getPlayWhenReady();
    }

    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        updateAll();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
        removeCallbacks(mUpdateProgressAction);
    }

}
