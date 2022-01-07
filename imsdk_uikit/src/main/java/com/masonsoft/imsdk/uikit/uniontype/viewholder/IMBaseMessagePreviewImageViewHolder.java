package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMImageElement;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.util.UrlUtil;
import com.masonsoft.imsdk.uikit.widget.ThumbPhotoDraweeView;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;

public class IMBaseMessagePreviewImageViewHolder extends IMBaseMessageViewHolder {

    private final ThumbPhotoDraweeView mImage;

    public IMBaseMessagePreviewImageViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_im_base_message_preview_image);
        mImage = itemView.findViewById(R.id.image);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);

        final List<String> firstAvailableUrls = new ArrayList<>();
        final MSIMImageElement element = baseMessage.getImageElement();
        if (element != null) {
            final String localPath = element.getPath();
            if (localPath != null) {
                firstAvailableUrls.add(localPath);
            }
            final String url = element.getUrl();
            if (url != null) {
                firstAvailableUrls.add(url);
            }
        }
        setImageUrl(null, firstAvailableUrls.toArray(new String[]{}));

        mImage.setOnClickListener(v -> {
            UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(this);
            }
        });
    }

    public void setImageUrl(@Nullable String thumb, @Nullable String... firstAvailable) {
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
