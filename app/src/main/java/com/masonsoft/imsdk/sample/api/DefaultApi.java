package com.masonsoft.imsdk.sample.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.core.OtherMessage;
import com.masonsoft.imsdk.core.OtherMessageManager;
import com.masonsoft.imsdk.core.SignGenerator;
import com.masonsoft.imsdk.core.observable.OtherMessageObservable;
import com.masonsoft.imsdk.sample.Constants;
import com.masonsoft.imsdk.sample.LocalSettingsManager;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.entity.Init;
import com.masonsoft.imsdk.sample.entity.Spark;
import com.masonsoft.imsdk.sample.im.FetchSparkMessagePacket;
import com.masonsoft.imsdk.uikit.util.OkHttpClientUtil;
import com.masonsoft.imsdk.uikit.util.RequestSignUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.idonans.core.util.Preconditions;
import io.reactivex.rxjava3.subjects.SingleSubject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class DefaultApi {

    private static final long TIMEOUT_MS = 20 * 1000L;
    private static final OkHttpClient SHARE_HTTP_CLIENT = createDefaultApiOkHttpClient();

    private DefaultApi() {
    }

    private static OkHttpClient createDefaultApiOkHttpClient() {
        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (SampleLog.getLogLevel() <= Log.VERBOSE) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else if (SampleLog.getLogLevel() <= Log.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        return OkHttpClientUtil.createDefaultOkHttpClient()
                .newBuilder()
                .addInterceptor(logging)
                .build();
    }

    @Nullable
    private static <T> T requestApiServer(final String apiUrlPath, final Map<String, Object> requestArgs, @Nullable TypeToken<T> type) {
        final String appSecret = Constants.APP_SECRET;
        final int nonce = (int) (Math.random() * 1000000000);
        final long timestamp = System.currentTimeMillis() / 1000;
        final String sign = RequestSignUtil.calSign(appSecret, nonce, timestamp);

        final String requestArgsAsJson = new Gson().toJson(requestArgs);
        final RequestBody requestBody = RequestBody.create(requestArgsAsJson, MediaType.parse("application/json;charset=utf-8"));

        final LocalSettingsManager.Settings settings = LocalSettingsManager.getInstance().getSettings();
        final String url = settings.apiServer + apiUrlPath;
        final Request request = new Request.Builder()
                .addHeader("nonce", String.valueOf(nonce))
                .addHeader("timestamp", String.valueOf(timestamp))
                .addHeader("sig", sign)
                .url(url)
                .post(requestBody)
                .build();

        //noinspection UnnecessaryLocalVariable
        final OkHttpClient okHttpClient = SHARE_HTTP_CLIENT;
        try (final Response response = okHttpClient.newCall(request).execute()) {
            final String json = response.body().string();
            final JsonObject jsonObject = new Gson().fromJson(json, new TypeToken<JsonObject>() {
            }.getType());
            final int code = jsonObject.get("code").getAsInt();
            final String message = jsonObject.get("msg").getAsString();
            if (code != 0) {
                throw new ApiResponseException(code, message);
            }

            final JsonElement data = jsonObject.get("data");
            if (data != null && type != null) {
                return new Gson().fromJson(data, type.getType());
            } else {
                return null;
            }
        } catch (Throwable e) {
            if (e instanceof ApiResponseException) {
                throw (ApiResponseException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public static Init getImToken(long userId) {
        final String url = "/user/iminit";

        final Map<String, Object> requestArgs = new HashMap<>();
        requestArgs.put("uid", userId);
        requestArgs.put("ctype", 0);

        return Preconditions.checkNotNull(requestApiServer(url, requestArgs, new TypeToken<Init>() {
        }));
    }

    @NonNull
    public static Object reg(long userId, String nickname) {
        final String url = "/user/reg";

        final Map<String, Object> requestArgs = new HashMap<>();
        requestArgs.put("uid", userId);
        requestArgs.put("nick_name", nickname);
        requestArgs.put("avatar", "https://msim-test-1252460681.cos.na-siliconvalley.myqcloud.com/pers/612FA7A3-144E-4978-A75C-9D9277167292.jpeg");
        requestArgs.put("gold", 1);
        requestArgs.put("gold_exp", TimeUnit.DAYS.toSeconds(360) + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        requestArgs.put("approved", 1);
        requestArgs.put("disabled", 0);
        requestArgs.put("blocked", 0);
        requestArgs.put("hold", 0);
        requestArgs.put("deleted", 0);
        requestArgs.put("verified", 1);
        requestArgs.put("gender", 1);

        requestApiServer(url, requestArgs, null);
        return new Object();
    }

    @NonNull
    public static Object updateAvatar(long userId, String avatar) {
        final String url = "/user/update";

        final Map<String, Object> requestArgs = new HashMap<>();
        requestArgs.put("uid", userId);
        requestArgs.put("avatar", avatar);

        requestApiServer(url, requestArgs, null);
        return new Object();
    }

    @NonNull
    public static Object updateNickname(long userId, String nickname) {
        final String url = "/user/update";

        final Map<String, Object> requestArgs = new HashMap<>();
        requestArgs.put("uid", userId);
        requestArgs.put("nick_name", nickname);

        requestApiServer(url, requestArgs, null);
        return new Object();
    }

    @NonNull
    public static Object updateGold(long userId, boolean gold) {
        final String url = "/user/update";

        final Map<String, Object> requestArgs = new HashMap<>();
        requestArgs.put("uid", userId);
        requestArgs.put("gold", MSIMConstants.trueOfFalse(gold));
        if (gold) {
            requestArgs.put("gold_exp", (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(7));
        }

        requestApiServer(url, requestArgs, null);
        return new Object();
    }

    @NonNull
    public static Object updateVerified(long userId, boolean verified) {
        final String url = "/user/update";

        final Map<String, Object> requestArgs = new HashMap<>();
        requestArgs.put("uid", userId);
        requestArgs.put("verified", MSIMConstants.trueOfFalse(verified));

        requestApiServer(url, requestArgs, null);
        return new Object();
    }

    public static List<Spark> getSparks() {
        final SingleSubject<List<Spark>> subject = SingleSubject.create();
        final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
        final long originSign = SignGenerator.nextSign();
        final FetchSparkMessagePacket messagePacket = FetchSparkMessagePacket.create(originSign);
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

                final List<Spark> sparkList = new ArrayList<>(messagePacket.getSparkList());

                // 存储用户头像与昵称
                for (Spark spark : sparkList) {
                    MSIMManager.getInstance().getUserInfoManager().insertOrUpdateUserInfo(
                            new MSIMUserInfo.Editor(spark.userId)
                                    .setAvatar(spark.avatar)
                                    .setNickname(spark.nickname)
                    );
                }

                subject.onSuccess(sparkList);
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
        return subject.timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).blockingGet();
    }

}
