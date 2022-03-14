package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.widget.CustomSoftKeyboard;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;

/**
 * 自定义表情
 */
public abstract class IMBaseMessageCustomEmotion2ViewHolder extends IMBaseMessageViewHolder {

    private final LottieAnimationView mLottieView;

    public IMBaseMessageCustomEmotion2ViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mLottieView = itemView.findViewById(R.id.lottie_view);
    }

    public IMBaseMessageCustomEmotion2ViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mLottieView = itemView.findViewById(R.id.lottie_view);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);
        Preconditions.checkNotNull(baseMessage);

        String emotionUrl = CustomSoftKeyboard.Emotion2Loader.getDefaultValue();

        final String lottieId = baseMessage.getBody();
        if (CustomSoftKeyboard.Emotion2Loader.contains(lottieId)) {
            emotionUrl = CustomSoftKeyboard.Emotion2Loader.getValue(lottieId);
        }

        mLottieView.setAnimation(emotionUrl);
    }

}
