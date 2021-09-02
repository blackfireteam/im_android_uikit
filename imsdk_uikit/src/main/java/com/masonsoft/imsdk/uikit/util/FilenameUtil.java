package com.masonsoft.imsdk.uikit.util;

import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.util.UUID;

public class FilenameUtil {

    public static String createUnionFilename(@Nullable String fileExtension, @Nullable String mimeType) {
        String filename = UUID.randomUUID().toString().replace("-", "");
        if (fileExtension != null) {
            filename += "." + fileExtension.toLowerCase();
        }
        if (mimeType != null) {
            final String mimeTypeExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (mimeTypeExtension != null) {
                if (mimeTypeExtension.startsWith(".")) {
                    filename += mimeTypeExtension;
                } else {
                    filename += "." + mimeTypeExtension;
                }
            }
        }
        return filename;
    }

}
