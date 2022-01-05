package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.MSIMAudioElement;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.RecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.real.Real;
import com.masonsoft.imsdk.uikit.common.microlifecycle.real.RealHost;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.widget.AudioPlayerView;
import com.masonsoft.imsdk.uikit.widget.MSIMBaseMessageAudioView;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;

public abstract class IMBaseMessageVoiceViewHolder extends IMBaseMessageViewHolder {

    protected final MSIMBaseMessageAudioView mAudioView;
    protected final ImageView mAudioImageFlag;
    protected final TextView mAudioDurationText;

    @Nullable
    private LocalMicroLifecycle mLocalMicroLifecycle;

    public IMBaseMessageVoiceViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mAudioView = itemView.findViewById(R.id.audio_view);
        mAudioImageFlag = itemView.findViewById(R.id.audio_image_flag);
        mAudioDurationText = itemView.findViewById(R.id.audio_duration_text);
    }

    public IMBaseMessageVoiceViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mAudioView = itemView.findViewById(R.id.audio_view);
        mAudioImageFlag = itemView.findViewById(R.id.audio_image_flag);
        mAudioDurationText = itemView.findViewById(R.id.audio_duration_text);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = (MSIMBaseMessage) itemObject.object;

        mAudioView.setBaseMessage(baseMessage);

        long durationMs = 0L;
        final MSIMAudioElement element = baseMessage.getAudioElement();
        if (element != null) {
            durationMs = element.getDurationMs();
        }

        if (durationMs < 1000L) {
            durationMs = 1000L;
        }
        mAudioDurationText.setText(durationMs / 1000 + " ''");

        mAudioView.setOnLongClickListener(v -> {
            final UnionTypeViewHolderListeners.OnItemLongClickListener listener = itemObject.getExtHolderItemLongClick1();
            if (listener != null) {
                listener.onItemLongClick(this);
            }
            return true;
        });
        mAudioView.setOnPlayerStateUpdateListener(new AudioPlayerView.OnPlayerStateUpdateListener() {
            @Override
            public void onPlayerPlayPauseUpdate(boolean shouldShowPauseButton) {
                if (shouldShowPauseButton) {
                    Drawable drawable = mAudioImageFlag.getDrawable();
                    if (!(drawable instanceof AnimationDrawable)) {
                        mAudioImageFlag.setImageResource(R.drawable.imsdk_uikit_voice_message_playing_anim);
                        drawable = mAudioImageFlag.getDrawable();
                    }
                    if (drawable instanceof AnimationDrawable) {
                        ((AnimationDrawable) drawable).start();
                    }
                } else {
                    mAudioImageFlag.setImageResource(R.drawable.imsdk_uikit_voice_msg_playing_3);
                }
            }

            @Override
            public void onPlayerProgressUpdate(long position, long bufferedPosition, long duration) {
            }
        });

        createLocalMicroLifecycle();
    }

    private void createLocalMicroLifecycle() {
        if (mLocalMicroLifecycle == null) {
            UnionTypeAdapter adapter = host.getAdapter();
            if (adapter instanceof MicroLifecycleComponentManagerHost) {
                MicroLifecycleComponentManager microLifecycleComponentManager = ((MicroLifecycleComponentManagerHost) adapter).getMicroLifecycleComponentManager();
                if (microLifecycleComponentManager != null) {
                    mLocalMicroLifecycle = createLocalMicroLifecycle(microLifecycleComponentManager);
                }
            }
        }
    }

    @NonNull
    private LocalMicroLifecycle createLocalMicroLifecycle(@NonNull MicroLifecycleComponentManager microLifecycleComponentManager) {
        return new LocalMicroLifecycle(microLifecycleComponentManager);
    }

    private class LocalMicroLifecycle extends RecyclerViewMicroLifecycleComponentManager.ViewHolderMicroLifecycleComponent implements RealHost {

        public LocalMicroLifecycle(@NonNull MicroLifecycleComponentManager microLifecycleComponentManager) {
            super(microLifecycleComponentManager);
        }

        @Nullable
        @Override
        public RecyclerView.ViewHolder getViewHolder() {
            return IMBaseMessageVoiceViewHolder.this;
        }

        @Override
        public Real getReal() {
            return mAudioView;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (mAudioView != null) {
                mAudioView.performCreate();
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mAudioView != null) {
                mAudioView.performStart();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mAudioView != null) {
                mAudioView.performResume();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mAudioView != null) {
                mAudioView.performPause();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mAudioView != null) {
                mAudioView.performStop();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mLocalMicroLifecycle = null;
            if (mAudioView != null) {
                mAudioView.performDestroy();
            }
        }
    }

}
