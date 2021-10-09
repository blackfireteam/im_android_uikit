package com.masonsoft.imsdk.uikit.common.fileupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.core.FileUploadProvider;
import com.masonsoft.imsdk.core.FileUploadResult;
import com.masonsoft.imsdk.core.OtherMessage;
import com.masonsoft.imsdk.core.OtherMessageManager;
import com.masonsoft.imsdk.core.SignGenerator;
import com.masonsoft.imsdk.core.observable.OtherMessageObservable;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.entity.CosKeyInfo;
import com.masonsoft.imsdk.uikit.message.packet.GetCosKeyMessagePacket;
import com.masonsoft.imsdk.uikit.util.FilenameUtil;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.idonans.core.Progress;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.FileUtil;
import io.github.idonans.core.util.Preconditions;
import io.reactivex.rxjava3.subjects.SingleSubject;

/**
 * 基于腾讯云对象存储实现的文件上传服务
 */
public class TencentOSSFileUploadProvider implements FileUploadProvider {

    private final CosKeyInfoProvider mCosKeyInfoProvider = new CosKeyInfoProvider();

    public TencentOSSFileUploadProvider() {
    }

    @NonNull
    @Override
    public FileUploadResult uploadFile(@NonNull String filePath, @Source int source, @Nullable String mimeType, @NonNull Progress progress) throws Throwable {
        final String fileExtension = FileUtil.getFileExtensionFromUrl(filePath);

        CosKeyInfo cosKeyInfo = mCosKeyInfoProvider.getCosKeyInfo();
        if (cosKeyInfo == null) {
            throw new IllegalStateException("cos key info is null");
        }

        SessionQCloudCredentials sessionQCloudCredentials = new SessionQCloudCredentials(
                cosKeyInfo.id.get(),
                cosKeyInfo.key.get(),
                cosKeyInfo.token.get(),
                cosKeyInfo.startTime.get(),
                cosKeyInfo.expTime.get()
        );
        if (!sessionQCloudCredentials.isValid()) {
            MSIMUikitLog.i("cos key info is invalid, remove current cos key info and fetch again");
            mCosKeyInfoProvider.removeCosKeyInfo(cosKeyInfo);
            cosKeyInfo = mCosKeyInfoProvider.getCosKeyInfo();

            if (cosKeyInfo == null) {
                throw new IllegalStateException("cos key info is null after fetch again");
            }

            sessionQCloudCredentials = new SessionQCloudCredentials(
                    cosKeyInfo.id.get(),
                    cosKeyInfo.key.get(),
                    cosKeyInfo.token.get(),
                    cosKeyInfo.startTime.get(),
                    cosKeyInfo.expTime.get()
            );
        }

        final SessionQCloudCredentials finalSessionQCloudCredentials = sessionQCloudCredentials;

        final QCloudCredentialProvider credentialProvider = new QCloudCredentialProvider() {
            @Override
            public QCloudCredentials getCredentials() throws QCloudClientException {
                return finalSessionQCloudCredentials;
            }

            @Override
            public void refresh() throws QCloudClientException {
                // ignore
            }
        };

        final CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(cosKeyInfo.region.get())
                .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
                .builder();
        final CosXmlService cosXmlService =
                new CosXmlService(ContextUtil.getContext(), serviceConfig, credentialProvider);

        final String bucket = cosKeyInfo.bucket.get();
        final String dir = createDir(source, mimeType, cosKeyInfo);
        final String cosPath = dir + "/Android_" + FilenameUtil.createUnionFilename(fileExtension, mimeType);
        //noinspection UnnecessaryLocalVariable
        final String srcPath = filePath;

        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, cosPath, srcPath);
        putObjectRequest.setProgressListener((complete, target) -> progress.set(target, complete));
        final PutObjectResult putObjectResult = cosXmlService.putObject(putObjectRequest);

        final String accessUrl = putObjectResult.accessUrl;
        Preconditions.checkNotNull(accessUrl);

        final FileUploadResult fileUploadResult = new FileUploadResult();
        fileUploadResult.url = accessUrl;
        return fileUploadResult;
    }

    @NonNull
    private String createDir(@Source int source, @Nullable String mimeType, @NonNull CosKeyInfo cosKeyInfo) {
        if (source == SOURCE_OTHER) {
            return cosKeyInfo.pathDemo.get() + "/common";
        } else {
            // SOURCE_CHAT
            String dir = "im_image";
            if (mimeType != null) {
                if (mimeType.startsWith("video/")) {
                    dir = "im_video";
                } else if (mimeType.startsWith("audio/")) {
                    dir = "im_voice";
                }
            }
            return cosKeyInfo.path.get() + "/" + dir;
        }
    }

    private static class CosKeyInfoProvider {

        private static final long TIMEOUT_MS = 20 * 1000L;

        private final Map<Long, CosKeyInfo> mCache = new HashMap<>();

        private synchronized void removeCosKeyInfo(@Nullable CosKeyInfo cosKeyInfo) {
            if (cosKeyInfo != null) {
                mCache.remove(cosKeyInfo.sessionUserId.get());
            }
        }

        @Nullable
        private synchronized CosKeyInfo getCosKeyInfo() {
            final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
            if (sessionUserId <= 0) {
                return null;
            }

            CosKeyInfo cosKeyInfo = mCache.get(sessionUserId);
            if (cosKeyInfo != null) {
                return cosKeyInfo;
            }

            cosKeyInfo = fetchQuietly();
            if (cosKeyInfo != null) {
                mCache.put(cosKeyInfo.sessionUserId.get(), cosKeyInfo);

                if (cosKeyInfo.sessionUserId.get() != sessionUserId) {
                    MSIMUikitLog.e("unexpected cos key info session user id not match %s -> %s",
                            cosKeyInfo.sessionUserId.get(), sessionUserId);
                }
            }
            return cosKeyInfo;
        }

        @Nullable
        private static CosKeyInfo fetchQuietly() {
            try {
                return fetch();
            } catch (Throwable e) {
                MSIMUikitLog.e(e);
            }
            return null;
        }

        @Nullable
        private static CosKeyInfo fetch() {
            final SingleSubject<ObjectWrapper> subject = SingleSubject.create();
            final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
            final long originSign = SignGenerator.nextSign();
            final GetCosKeyMessagePacket messagePacket = GetCosKeyMessagePacket.create(originSign);
            final OtherMessage otherMessage = new OtherMessage(sessionUserId, messagePacket);
            final OtherMessageObservable.OtherMessageObserver otherMessageObserver = new OtherMessageObservable.OtherMessageObserver() {
                @Override
                public void onOtherMessageLoading(long sign, @NonNull OtherMessage otherMessage) {
                }

                @Override
                public void onOtherMessageSuccess(long sign, @NonNull OtherMessage otherMessage) {
                    if (originSign != sign) {
                        return;
                    }

                    final CosKeyInfo cosKeyInfo = messagePacket.getCosKeyInfo();
                    subject.onSuccess(new ObjectWrapper(cosKeyInfo));
                }

                @Override
                public void onOtherMessageError(long sign, @NonNull OtherMessage otherMessage, int errorCode, String errorMessage) {
                    if (originSign != sign) {
                        return;
                    }

                    subject.onError(new IllegalArgumentException("errorCode:" + errorCode + ", errorMessage:" + errorMessage));
                }
            };
            OtherMessageObservable.DEFAULT.registerObserver(otherMessageObserver);
            OtherMessageManager.getInstance().enqueueOtherMessage(sessionUserId, originSign, otherMessage);
            final ObjectWrapper result = subject.timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).blockingGet();
            return (CosKeyInfo) result.getObject();
        }
    }

}
