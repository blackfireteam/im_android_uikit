package com.masonsoft.imsdk.sample.uniontype.viewholder;

import android.app.Activity;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleUnionTypeImplHomeSparkBinding;
import com.masonsoft.imsdk.sample.entity.Spark;
import com.masonsoft.imsdk.sample.util.JsonUtil;
import com.masonsoft.imsdk.sample.util.StringUtil;
import com.masonsoft.imsdk.uikit.CustomIMMessageFactory;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.uikit.app.chat.SingleChatActivity;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.util.Objects;

import java.util.Map;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

/**
 * home spark
 */
public class HomeSparkViewHolder extends UnionTypeViewHolder {

    private final ImsdkSampleUnionTypeImplHomeSparkBinding mBinding;
    private final MSIMUserInfoLoader mUserInfoLoader;

    public HomeSparkViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_sample_union_type_impl_home_spark);
        mBinding = ImsdkSampleUnionTypeImplHomeSparkBinding.bind(itemView);

        mUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userInfo);

                HomeSparkViewHolder.this.onUserInfoLoad(userInfo);
            }
        };
    }

    public void updateLikeAndDislike(@FloatRange(from = -1, to = 1) float progress) {
        updateLikeAndDislike(progress, true);
    }

    private void updateLikeAndDislike(@FloatRange(from = -1, to = 1) float progress, boolean setValue) {
        final DataObject itemObject = getItemObject(DataObject.class);
        if (itemObject == null) {
            SampleLog.e("unexpected. item object is null");
            return;
        }
        final ExtraUiData extraUiData = ExtraUiData.valueOf(itemObject);
        if (setValue) {
            extraUiData.mLikeAndDislikeProgress = progress;
        }

        if (extraUiData.mLikeAndDislikeProgress > 0) {
            mBinding.indicatorLike.setAlpha(Math.min(1f, 0.2f + extraUiData.mLikeAndDislikeProgress));
            mBinding.indicatorDislike.setAlpha(0f);
        } else if (extraUiData.mLikeAndDislikeProgress < 0) {
            mBinding.indicatorLike.setAlpha(0f);
            mBinding.indicatorDislike.setAlpha(Math.min(1f, 0.2f - extraUiData.mLikeAndDislikeProgress));
        } else {
            mBinding.indicatorLike.setAlpha(0f);
            mBinding.indicatorDislike.setAlpha(0f);
        }
    }

    private void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
        final DataObject itemObject = getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }

        final Spark spark = itemObject.getObject(Spark.class);
        if (spark == null) {
            return;
        }

        if (spark.profile.getUid() != userInfo.getUserId()) {
            return;
        }

        mBinding.username.setUserInfo(userInfo);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final Spark spark = (Spark) itemObject.object;
        Preconditions.checkNotNull(spark);

        mUserInfoLoader.setUserInfo(MSIMUserInfo.mock(spark.profile.getUid()), false);

        mBinding.imageLayout.setImageUrl(null, spark.profile.getAvatar());
        mBinding.desc.setText(buildDescText(spark));

        updateLikeAndDislike(0, false);

        final ExtraUiData extraUiData = ExtraUiData.valueOf(itemObject);
        mBinding.actionLike.setSelected(extraUiData.mSendCustomLikedMessage);

        ViewUtil.onClick(itemView, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            SampleLog.v(Objects.defaultObjectTag(this) + " item click spark:%s", spark);
        });
        ViewUtil.onClick(mBinding.actionLike, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            if (!extraUiData.mSendCustomLikedMessage) {
                extraUiData.mSendCustomLikedMessage = true;
                mBinding.actionLike.setSelected(true);

                // send like message
                sendLikeMessage(spark.profile.getUid());
            }
        });
        ViewUtil.onClick(mBinding.actionChat, v -> {
            final Activity innerActivity = host.getActivity();
            if (innerActivity == null) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            if (spark.profile.getUid() <= 0) {
                SampleLog.e(MSIMUikitConstants.ErrorLog.INVALID_USER_ID);
                return;
            }

            SingleChatActivity.start(innerActivity, spark.profile.getUid());
        });
    }

    private String buildDescText(@NonNull Spark spark) {
        final Map<String, Object> map = JsonUtil.toMapOrEmpty(spark.profile.getCustom());
        final String department = StringUtil.toStringOrEmpty(map.get("department"));
        final String workplace = StringUtil.toStringOrEmpty(map.get("workplace"));
        final StringBuilder builder = new StringBuilder();
        if (!department.isEmpty()) {
            builder.append(department);
            builder.append(" ");
        }
        if (!workplace.isEmpty()) {
            builder.append(workplace);
            builder.append(" ");
        }
        return builder.toString();
    }

    private void sendLikeMessage(long targetUserId) {
        final MSIMMessage message = CustomIMMessageFactory.createCustomMessageLike();
        MSIMManager.getInstance().getMessageManager().sendMessage(
                MSIMManager.getInstance().getSessionUserId(),
                message,
                targetUserId
        );
    }

    private static class ExtraUiData {

        private static final String KEY_UI_DATA = "extra:ui_data_20210419";
        /**
         * [-1, 0) 不喜欢<br>
         * (0, 1] 喜欢<br>
         */
        private float mLikeAndDislikeProgress;
        private boolean mSendCustomLikedMessage;

        private static ExtraUiData valueOf(DataObject dataObject) {
            ExtraUiData extraUiData = dataObject.getExtObject(KEY_UI_DATA, null);
            if (extraUiData == null) {
                extraUiData = new ExtraUiData();
                dataObject.putExtObject(KEY_UI_DATA, extraUiData);
            }
            return extraUiData;
        }

    }

}
