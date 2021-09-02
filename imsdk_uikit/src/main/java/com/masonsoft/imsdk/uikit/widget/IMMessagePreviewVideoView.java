package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMVideoElement;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.media.player.MediaPlayerDelegate;
import com.masonsoft.imsdk.uikit.common.media.player.MediaPlayerView;

import io.github.idonans.lang.util.ViewUtil;

public class IMMessagePreviewVideoView extends MicroLifecycleFrameLayout {

    public IMMessagePreviewVideoView(Context context) {
        this(context, null);
    }

    public IMMessagePreviewVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMMessagePreviewVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMMessagePreviewVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    private MSIMMessage mMessage;

    @Nullable
    private View mActionClose;

    @Nullable
    private View mExoPlayInterceptView;

    @Nullable
    private View mExoPauseInterceptView;

    @Nullable
    private PlayerView mVideoView;
    @Nullable
    private MediaPlayerView mMediaPlayerView;

    @Nullable
    private IMImageView mCoverImage;

    @Nullable
    private MediaPlayerDelegate mMediaPlayerDelegate;

    @Nullable
    private Player mInnerPlayer;
    private final Player.EventListener mInnerPlayerListener = new Player.EventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            manualPauseIfNeed();
        }
    };

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setContentView(R.layout.imsdk_sample_widget_im_message_preview_video_view);
        mActionClose = findViewById(R.id.action_close);
        mExoPlayInterceptView = findViewById(R.id.exo_play_intercept_view);
        mExoPauseInterceptView = findViewById(R.id.exo_pause_intercept_view);
        mVideoView = findViewById(R.id.video_view);
        mCoverImage = findViewById(R.id.cover_image);

        if (mVideoView != null) {
            mMediaPlayerView = player -> {
                if (mVideoView != null) {
                    mVideoView.setPlayer(player);
                }
                if (mInnerPlayer != player) {
                    if (mInnerPlayer != null) {
                        mInnerPlayer.removeListener(mInnerPlayerListener);
                    }
                    mInnerPlayer = player;
                    if (mInnerPlayer != null) {
                        mInnerPlayer.addListener(mInnerPlayerListener);
                    }
                }
            };
        }

        ViewUtil.onClick(mActionClose, v -> {
            if (mOnActionCloseClickListener != null) {
                mOnActionCloseClickListener.onActionCloseClick();
            }
        });
        ViewUtil.onClick(mExoPlayInterceptView, v -> toggleWithManual());
        ViewUtil.onClick(mExoPauseInterceptView, v -> toggleWithManual());
        mMediaPlayerDelegate = new MediaPlayerDelegate();
        setDefaultManual(true);
    }

    private void manualPauseIfNeed() {
        final Player player = mInnerPlayer;
        if (player == null) {
            return;
        }
        if (player.getPlaybackState() == Player.STATE_ENDED) {
            if (isResumed()) {
                toggleWithManual();
            }
            player.seekTo(0);
        }
    }

    public void setMessage(@Nullable MSIMMessage message) {
        mMessage = message;

        if (mCoverImage != null) {
            mCoverImage.setChatMessage(message);
        }
        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.initPlayerIfNeed(mMediaPlayerView, getVideoUrl(), true, isResumed(), false);
        }
    }

    @Nullable
    private String getVideoUrl() {
        if (mMessage != null) {
            final MSIMVideoElement element = mMessage.getVideoElement();
            if (element != null) {
                return element.getUrl();
            }
        }
        return null;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.initPlayerIfNeed(mMediaPlayerView, getVideoUrl(), false, isResumed(), false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.pausePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.releasePlayer();
        }
    }

    public interface OnActionCloseClickListener {
        void onActionCloseClick();
    }

    private OnActionCloseClickListener mOnActionCloseClickListener;

    public void setOnActionCloseClickListener(OnActionCloseClickListener listener) {
        mOnActionCloseClickListener = listener;
    }

}
