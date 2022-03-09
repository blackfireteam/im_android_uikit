package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.media.player.MediaPlayerDelegate;
import com.masonsoft.imsdk.uikit.common.media.player.MediaPlayerView;

import io.github.idonans.lang.util.ViewUtil;

public class MicroLifecycleVideoView extends MicroLifecycleFrameLayout {

    public MicroLifecycleVideoView(Context context) {
        this(context, null);
    }

    public MicroLifecycleVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MicroLifecycleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MicroLifecycleVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    private Uri mVideoUri;

    @Nullable
    private View mInternalControlView;

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
    private ImageLayout mCoverImage;

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
        setContentView(R.layout.imsdk_uikit_widget_micro_lifecycle_video_view);
        mActionClose = findViewById(R.id.action_close);
        mExoPlayInterceptView = findViewById(R.id.exo_play_intercept_view);
        mExoPauseInterceptView = findViewById(R.id.exo_pause_intercept_view);
        mVideoView = findViewById(R.id.internal_video_view);
        mCoverImage = findViewById(R.id.cover_image);
        mInternalControlView = findViewById(R.id.internal_control_view);

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

            mVideoView.setControllerVisibilityListener(visibility -> {
                if (mOnControlViewVisibilityChangedListener != null) {
                    mOnControlViewVisibilityChangedListener.onControlViewVisibilityChanged(visibility);
                }
            });
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

    public void updateActionCloseFeature(boolean enable) {
        if (mActionClose != null) {
            ViewUtil.setVisibilityIfChanged(mActionClose, enable ? View.VISIBLE : View.GONE);
        }
    }

    public void updateControlViewFeature(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        if (mInternalControlView != null) {
            mInternalControlView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }
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

    public void setVideoUri(@Nullable Uri videoUri) {
        this.setVideoUri(videoUri, 0);
    }

    public void setVideoUri(@Nullable Uri videoUri, int thumbSize) {
        mVideoUri = videoUri;

        if (mCoverImage != null) {
            if (mVideoUri != null) {
                if (thumbSize > 0) {
                    mCoverImage.setImageUrl(
                            ImageRequestBuilder.newBuilderWithSource(mVideoUri)
                                    .setResizeOptions(ResizeOptions.forSquareSize(thumbSize))
                                    .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                                    .build(),
                            ImageRequestBuilder.newBuilderWithSource(mVideoUri)
                                    .build()
                    );
                } else {
                    mCoverImage.setImageUrl(
                            null,
                            ImageRequestBuilder.newBuilderWithSource(mVideoUri)
                                    .build()
                    );
                }


            } else {
                mCoverImage.setImageUrl("");
            }
        }

        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.initPlayerIfNeed(mMediaPlayerView, getVideoUrl(), true, isResumed(), false);
        }
    }

    @Nullable
    private String getVideoUrl() {
        if (mVideoUri != null) {
            return mVideoUri.toString();
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

    public interface OnControlViewVisibilityChangedListener {
        void onControlViewVisibilityChanged(int visibility);
    }

    private OnControlViewVisibilityChangedListener mOnControlViewVisibilityChangedListener;

    public void setOnControlViewVisibilityChangedListener(OnControlViewVisibilityChangedListener listener) {
        mOnControlViewVisibilityChangedListener = listener;
    }

}
