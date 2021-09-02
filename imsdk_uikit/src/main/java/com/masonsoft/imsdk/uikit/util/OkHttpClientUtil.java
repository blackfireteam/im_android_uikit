package com.masonsoft.imsdk.uikit.util;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OkHttpClientUtil {

    private OkHttpClientUtil() {
    }

    @NonNull
    public static OkHttpClient.Builder createDefaultOkHttpClientBuilder() {
        try {
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier((hostname, session) -> true);
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
        }

        return new OkHttpClient.Builder()
                .hostnameVerifier((hostname, session) -> true);
    }

    @NonNull
    public static OkHttpClient createDefaultOkHttpClient() {
        return createDefaultOkHttpClientBuilder().build();
    }

}
