package com.masonsoft.imsdk.uikit.common.imfipreview;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMFlashImageElement;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.util.UrlUtil;
import com.masonsoft.imsdk.uikit.widget.ThumbPhotoDraweeView;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;

public class IMBaseMessageFlashImagePreviewDialog implements ViewBackLayer.OnBackPressedListener {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final ViewDialog mViewDialog;
    private final ThumbPhotoDraweeView mImage;
    @SuppressWarnings("FieldCanBeLocal")
    private final View mFlashImageOverlay;

    public IMBaseMessageFlashImagePreviewDialog(
            Activity activity,
            ViewGroup parentView,
            MSIMBaseMessage baseMessage) {
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_im_base_message_flash_image_preview)
                .setParentView(parentView)
                .setOnBackPressedListener(this)
                .dimBackground(true)
                .setCancelable(false)
                .create();

        //noinspection ConstantConditions
        mImage = mViewDialog.getContentView().findViewById(R.id.image);
        mFlashImageOverlay = mViewDialog.getContentView().findViewById(R.id.flash_image_overlay);
        ViewUtil.onClick(mImage, v -> hide());

        final List<String> firstAvailableUrls = new ArrayList<>();
        boolean showOverlay = false;
        boolean isFlashImage = false;
        if (baseMessage instanceof MSIMMessage) {
            final MSIMMessage message = (MSIMMessage) baseMessage;
            final MSIMFlashImageElement element = message.getFlashImageElement();
            if (element != null) {
                isFlashImage = true;
                final String localPath = element.getPath();
                if (localPath != null) {
                    firstAvailableUrls.add(localPath);
                }
                final String url = element.getUrl();
                if (url != null) {
                    firstAvailableUrls.add(url);
                }

                showOverlay = element.isReadForSessionUser();
            }
        }
        setImageUrl(null, firstAvailableUrls.toArray(new String[]{}));
        ViewUtil.setVisibilityIfChanged(mFlashImageOverlay, showOverlay ? View.VISIBLE : View.GONE);

        if (isFlashImage) {
            // 将闪照标记为已读
            Preconditions.checkNotNull(baseMessage);
            MSIMManager.getInstance().getMessageManager().readFlashImage(
                    baseMessage.getSessionUserId(),
                    (MSIMMessage) baseMessage
            );
        }
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        mViewDialog.hide(false);
    }

    @Override
    public boolean onBackPressed() {
        hide();
        return true;
    }

    private void setImageUrl(@SuppressWarnings("SameParameterValue") @Nullable String thumb, @Nullable String... firstAvailable) {
        ImageRequest thumbRequest = null;
        if (thumb != null) {
            thumbRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(UrlUtil.alignUrl(thumb)))
                    .setResizeOptions(createResizeOptions())
                    .setCacheChoice(createCacheChoice())
                    .build();
        }

        if (firstAvailable == null || firstAvailable.length <= 0) {
            mImage.setImageUrl(thumbRequest);
            return;
        }

        final ImageRequest[] firstAvailableRequest = new ImageRequest[firstAvailable.length];
        for (int i = 0; i < firstAvailable.length; i++) {
            final String url = firstAvailable[i];
            if (url == null) {
                firstAvailableRequest[i] = null;
                continue;
            }
            firstAvailableRequest[i] = ImageRequestBuilder.newBuilderWithSource(Uri.parse(UrlUtil.alignUrl(url)))
                    .setResizeOptions(createResizeOptions())
                    .setCacheChoice(createCacheChoice())
                    .build();
        }
        mImage.setImageUrl(thumbRequest, firstAvailableRequest);
    }

    private ResizeOptions createResizeOptions() {
        return ResizeOptions.forSquareSize((int) (1.5f * DimenUtil.getSmallScreenWidth()));
    }

    private ImageRequest.CacheChoice createCacheChoice() {
        return ImageRequest.CacheChoice.DEFAULT;
    }

}
