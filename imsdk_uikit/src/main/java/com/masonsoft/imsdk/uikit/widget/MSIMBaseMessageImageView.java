package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMImageElement;
import com.masonsoft.imsdk.MSIMLocationElement;
import com.masonsoft.imsdk.MSIMVideoElement;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.appcontext.AppContext;
import io.github.idonans.core.util.Preconditions;

public class MSIMBaseMessageImageView extends ImageLayout {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMBaseMessageImageView(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, 0);
    }

    @Nullable
    private MSIMBaseMessage mBaseMessage;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
    }

    public void setBaseMessage(@Nullable MSIMBaseMessage baseMessage) {
        mBaseMessage = baseMessage;
        this.onBaseMessageUpdate(mBaseMessage);
    }

    @Nullable
    public MSIMBaseMessage getBaseMessage() {
        return mBaseMessage;
    }

    protected void onBaseMessageUpdate(@Nullable MSIMBaseMessage baseMessage) {
        final List<String> firstAvailableUrls = new ArrayList<>();

        if (baseMessage != null) {
            final int messageType = baseMessage.getMessageType();

            if (messageType == MSIMConstants.MessageType.IMAGE) {
                final MSIMImageElement element = baseMessage.getImageElement();
                Preconditions.checkNotNull(element);
                final String localPath = element.getPath();
                if (localPath != null) {
                    firstAvailableUrls.add(localPath);
                }
                final String url = element.getUrl();
                if (url != null) {
                    firstAvailableUrls.add(url);
                }
                if (DEBUG) {
                    MSIMUikitLog.v("%s image message localPath:%s, url:%s",
                            Objects.defaultObjectTag(this), localPath, url);
                }
            } else if (messageType == MSIMConstants.MessageType.VIDEO) {
                final MSIMVideoElement element = baseMessage.getVideoElement();
                Preconditions.checkNotNull(element);
                final String localThumbPath = element.getThumbPath();
                if (localThumbPath != null) {
                    firstAvailableUrls.add(localThumbPath);
                }
                final String thumbUrl = element.getThumbUrl();
                if (thumbUrl != null) {
                    firstAvailableUrls.add(thumbUrl);
                }
                if (DEBUG) {
                    MSIMUikitLog.v("%s video message localThumbPath:%s, thumbUrl:%s",
                            Objects.defaultObjectTag(this), localThumbPath, thumbUrl);
                }
            } else if (messageType == MSIMConstants.MessageType.LOCATION) {
                final MSIMLocationElement element = baseMessage.getLocationElement();
                Preconditions.checkNotNull(element);
                final String staticLocationUrl = MSIMUikitConstants.buildStaticAMapUrl(
                        element.getLat(),
                        element.getLng(),
                        (int) element.getZoom()
                );
                firstAvailableUrls.add(staticLocationUrl);
                if (DEBUG) {
                    MSIMUikitLog.v("%s location message staticLocationUrl:%s",
                            Objects.defaultObjectTag(this), staticLocationUrl);
                }
            } else {
                MSIMUikitLog.e(Objects.defaultObjectTag(this) + " not support type %s", messageType);
            }
        }

        setImageUrl(null, firstAvailableUrls.toArray(new String[]{}));
    }

}