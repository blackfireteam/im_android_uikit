package com.masonsoft.imsdk.uikit.common.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.common.media.audio.AudioRecordManager;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.softkeyboard.SoftKeyboardHelper;
import com.masonsoft.imsdk.uikit.common.voicerecordgesture.VoiceRecordGestureHelper;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCustomInputFragmentBinding;
import com.masonsoft.imsdk.uikit.util.EditTextUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.uikit.widget.CustomSoftKeyboard;
import com.masonsoft.imsdk.util.Objects;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.util.List;

import io.github.idonans.core.FormValidator;
import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.PermissionUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 支持自定义输入的基础页面
 */
public abstract class CustomInputFragment extends SystemInsetsFragment {

    private final DisposableHolder mPermissionRequest = new DisposableHolder();
    private static final String[] VOICE_RECORD_PERMISSION = {
            Manifest.permission.RECORD_AUDIO,
    };

    @Nullable
    private ImsdkUikitCustomInputFragmentBinding mBinding;
    @Nullable
    private SoftKeyboardHelper mSoftKeyboardHelper;
    private VoiceRecordGestureHelper mVoiceRecordGestureHelper;
    private final AudioRecordManager.OnAudioRecordListener mOnAudioRecordListener = new OnAudioRecordListenerImpl();
    private Theme mTheme = new Theme();

    public boolean onBackPressed() {
        if (mSoftKeyboardHelper != null) {
            return mSoftKeyboardHelper.onBackPressed();
        }
        return false;
    }

    /**
     * @return 当前是否处于阅后即焚模式
     */
    public boolean isSnapchatMode() {
        return mTheme instanceof ThemeSnapchat;
    }

    /**
     * @return 当前主题是否是特殊的 mode. 特殊的 mode 可以切换回普通 mode.
     */
    public boolean isThemeMode() {
        return mTheme instanceof ThemeMode;
    }

    public void setTheme(Theme theme) {
        if (mTheme != theme) {
            mTheme = theme;
            applyTheme();
        }
    }

    protected ImsdkUikitCustomInputFragmentBinding getCustomBinding() {
        return mBinding;
    }

    protected boolean isTouchOutsideAdapter(float rawX, float rawY) {
        final ImsdkUikitCustomInputFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return false;
        }

        int[] outLocation = new int[2];
        binding.keyboardTopLine.getLocationInWindow(outLocation);
        boolean isTouchOutside = rawY <= outLocation[1];

        MSIMUikitLog.v("isTouchOutsideAdapter touch raw:[%s,%s], keyboard top line location:[%s,%s], isTouchOutside:%s",
                rawX, rawY, outLocation[0], outLocation[1], isTouchOutside);

        return isTouchOutside;
    }

    protected void onSoftKeyboardLayoutShownAdapter(boolean customSoftKeyboard, boolean systemSoftKeyboard) {
        final ImsdkUikitCustomInputFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        if (customSoftKeyboard) {
            if (binding.customSoftKeyboard.isLayerEmojiShown()) {
                ViewUtil.setVisibilityIfChanged(binding.keyboardEmoji, View.GONE);
                ViewUtil.setVisibilityIfChanged(binding.keyboardEmojiSystemSoftKeyboard, View.VISIBLE);
            } else if (binding.customSoftKeyboard.isLayerMoreShown()) {
                ViewUtil.setVisibilityIfChanged(binding.keyboardEmoji, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(binding.keyboardEmojiSystemSoftKeyboard, View.GONE);
            } else {
                final Throwable e = new IllegalStateException();
                MSIMUikitLog.e(e);
            }
        } else {
            ViewUtil.setVisibilityIfChanged(binding.keyboardEmoji, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(binding.keyboardEmojiSystemSoftKeyboard, View.GONE);
        }

        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoice, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceSystemSoftKeyboard, View.GONE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardEditText, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceRecordText, View.GONE);
    }

    protected void onAllSoftKeyboardLayoutHiddenAdapter() {
        final ImsdkUikitCustomInputFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }
        ViewUtil.setVisibilityIfChanged(binding.keyboardEmoji, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(binding.keyboardEmojiSystemSoftKeyboard, View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkUikitCustomInputFragmentBinding.inflate(inflater, container, false);
        Preconditions.checkNotNull(mTheme);
        applyTheme();

        mBinding.keyboardEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3000)});
        mSoftKeyboardHelper = new SoftKeyboardHelper(
                mBinding.softKeyboardListenerLayout,
                mBinding.keyboardEditText,
                mBinding.customSoftKeyboard) {
            @Override
            protected boolean isTouchOutside(float rawX, float rawY) {
                return isTouchOutsideAdapter(rawX, rawY);
            }

            @Override
            protected void onSoftKeyboardLayoutShown(boolean customSoftKeyboard, boolean systemSoftKeyboard) {
                onSoftKeyboardLayoutShownAdapter(customSoftKeyboard, systemSoftKeyboard);
            }

            @Override
            protected void onAllSoftKeyboardLayoutHidden() {
                onAllSoftKeyboardLayoutHiddenAdapter();
            }
        };
        mVoiceRecordGestureHelper = new VoiceRecordGestureHelper(mBinding.keyboardVoiceRecordText) {
            @Override
            protected void onVoiceRecordGestureStart() {
                MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onVoiceRecordGestureStart");
                if (hasVoiceRecordPermission()) {
                    AudioRecordManager.getInstance().startAudioRecord();
                } else {
                    requestVoiceRecordPermission();
                }
            }

            @Override
            protected void onVoiceRecordGestureMove(boolean inside) {
                MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onVoiceRecordGestureMove inside:%s", inside);
                updateAudioRecording(inside);
            }

            @Override
            protected void onVoiceRecordGestureEnd(boolean inside) {
                MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onVoiceRecordGestureEnd inside:%s", inside);
                if (inside) {
                    AudioRecordManager.getInstance().stopAudioRecord();
                } else {
                    AudioRecordManager.getInstance().cancelAudioRecord();
                }
            }
        };

        final EditText keyboardEditText = mBinding.keyboardEditText;
        final View keyboardSubmit = mBinding.keyboardSubmit;
        final View keyboardMore = mBinding.keyboardMore;
        FormValidator.bind(
                new FormValidator.InputView[]{
                        new FormValidator.InputViewFactory.TextViewInputView(keyboardEditText) {
                            @Override
                            public boolean isContentEnable() {
                                final Editable editable = keyboardEditText.getText();
                                if (editable == null) {
                                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.EDITABLE_IS_NULL);
                                    return false;
                                }
                                final String content = editable.toString();
                                return content.trim().length() > 0;
                            }
                        }
                },
                new FormValidator.SubmitView[]{
                        new FormValidator.SubmitViewFactory.SimpleSubmitView(keyboardSubmit) {
                            @Override
                            public void setSubmitEnable(boolean enable) {
                                ViewUtil.setVisibilityIfChanged(keyboardSubmit, enable ? View.VISIBLE : View.GONE);
                                ViewUtil.setVisibilityIfChanged(keyboardMore, enable ? View.GONE : View.VISIBLE);
                            }
                        }});
        ViewUtil.onClick(mBinding.keyboardSubmit, v -> submitTextMessage());
        ViewUtil.onClick(mBinding.keyboardVoice, v -> {
            if (mBinding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            if (mSoftKeyboardHelper == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                return;
            }

            ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoice, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceSystemSoftKeyboard, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardEditText, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceRecordText, View.VISIBLE);
            mSoftKeyboardHelper.requestHideAllSoftKeyboard();
        });
        ViewUtil.onClick(mBinding.keyboardVoiceSystemSoftKeyboard, v -> {
            if (mBinding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            if (mSoftKeyboardHelper == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                return;
            }

            ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoice, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceSystemSoftKeyboard, View.GONE);
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardEditText, View.VISIBLE);
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceRecordText, View.GONE);
            mSoftKeyboardHelper.requestShowSystemSoftKeyboard();
        });
        ViewUtil.onClick(mBinding.keyboardEmoji, v -> {
            if (mBinding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            if (mSoftKeyboardHelper == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                return;
            }
            mBinding.customSoftKeyboard.showLayerEmoji();
            mSoftKeyboardHelper.requestShowCustomSoftKeyboard();
        });
        ViewUtil.onClick(mBinding.keyboardEmojiSystemSoftKeyboard, v -> {
            if (mBinding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            if (mSoftKeyboardHelper == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                return;
            }
            mSoftKeyboardHelper.requestShowSystemSoftKeyboard();
        });
        ViewUtil.onClick(mBinding.keyboardMore, v -> {
            if (mBinding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            if (mSoftKeyboardHelper == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                return;
            }
            mBinding.customSoftKeyboard.showLayerMore();
            mSoftKeyboardHelper.requestShowCustomSoftKeyboard();
        });
        ViewUtil.onClick(mBinding.keyboardCancelMode, v -> {
            if (mBinding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }
            if (mSoftKeyboardHelper == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                return;
            }
            mSoftKeyboardHelper.requestHideAllSoftKeyboard();
            setTheme(new Theme());
        });
        mBinding.customSoftKeyboard.setOnInputListener(new CustomSoftKeyboard.OnInputListener() {
            @Override
            public void onInputText(CharSequence text) {
                if (mBinding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                EditTextUtil.insertText(mBinding.keyboardEditText, text);
            }

            @Override
            public void onDeleteOne() {
                if (mBinding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                EditTextUtil.deleteOne(mBinding.keyboardEditText);
            }

            @Override
            public void onMediaPicked(@NonNull List<MediaData.MediaInfo> mediaInfoList) {
                MSIMUikitLog.v("onImagePicked size:%s", mediaInfoList.size());
                if (mBinding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                if (mSoftKeyboardHelper == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                    return;
                }
                mSoftKeyboardHelper.requestHideAllSoftKeyboard();
                submitMediaMessage(isSnapchatMode(), mediaInfoList);
            }

            @Override
            public void onFlashImagePicked(@NonNull List<MediaData.MediaInfo> mediaInfoList) {
                MSIMUikitLog.v("onFlashImagePicked size:%s", mediaInfoList.size());
                if (mBinding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                if (mSoftKeyboardHelper == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                    return;
                }
                mSoftKeyboardHelper.requestHideAllSoftKeyboard();
                submitFlashImageMessage(isSnapchatMode(), mediaInfoList);
            }

            @Override
            public void onClickRtcAudio() {
                submitClickRtcAudio(isSnapchatMode());
            }

            @Override
            public void onClickRtcVideo() {
                submitClickRtcVideo(isSnapchatMode());
            }

            @Override
            public void onLocationPicked(@NonNull LocationInfo locationInfo, long zoom) {
                MSIMUikitLog.v("onLocationPicked zoom:%s", zoom);
                if (mBinding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                if (mSoftKeyboardHelper == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
                    return;
                }
                mSoftKeyboardHelper.requestHideAllSoftKeyboard();
                submitLocationMessage(isSnapchatMode(), locationInfo, zoom);
            }

            @Override
            public void onClickSnapchatMode() {
                MSIMUikitLog.v("onClickSnapchatMode");
                submitClickSnapchatMode();
            }
        });

        AudioRecordManager.getInstance().setOnAudioRecordListener(mOnAudioRecordListener);

        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoice, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceSystemSoftKeyboard, View.GONE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardEditText, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceRecordText, View.GONE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardEmoji, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardEmojiSystemSoftKeyboard, View.GONE);

        return mBinding.getRoot();
    }

    private boolean hasVoiceRecordPermission() {
        return PermissionUtil.isAllGranted(VOICE_RECORD_PERMISSION);
    }

    private void requestVoiceRecordPermission() {
        MSIMUikitLog.v("requestVoiceRecordPermission");

        final Activity activity = getActivity();
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_NOT_FOUND_IN_FRAGMENT);
            return;
        }

        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        //noinspection CastCanBeRemovedNarrowingVariableType
        final RxPermissions rxPermissions = new RxPermissions((FragmentActivity) activity);
        mPermissionRequest.set(
                rxPermissions.request(VOICE_RECORD_PERMISSION)
                        .subscribe(granted -> {
                            if (granted) {
                                onVoiceRecordPermissionGranted();
                            } else {
                                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                                TipUtil.show(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        }));
    }

    private void onVoiceRecordPermissionGranted() {
        MSIMUikitLog.v("onVoiceRecordPermissionGranted");
    }

    private void submitTextMessage() {
        final ImsdkUikitCustomInputFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        final Editable editable = binding.keyboardEditText.getText();
        if (editable == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.EDITABLE_IS_NULL);
            return;
        }

        final String text = editable.toString().trim();
        if (TextUtils.isEmpty(text)) {
            MSIMUikitLog.e("unexpected. submitTextMessage text is empty");
            return;
        }

        this.submitTextMessage(isSnapchatMode(), text);
    }

    protected abstract void submitTextMessage(boolean snapchat, String text);

    protected abstract void submitMediaMessage(boolean snapchat, @NonNull List<MediaData.MediaInfo> mediaInfoList);

    protected abstract void submitFlashImageMessage(boolean snapchat, @NonNull List<MediaData.MediaInfo> mediaInfoList);

    protected abstract void submitAudioMessage(boolean snapchat, final String audioFilePath);

    protected abstract void submitLocationMessage(boolean snapchat, @NonNull LocationInfo locationInfo, long zoom);

    protected abstract void submitClickRtcAudio(boolean snapchat);

    protected abstract void submitClickRtcVideo(boolean snapchat);

    /**
     * 点击了阅后即焚。切换到阅后即焚模式
     */
    protected void submitClickSnapchatMode() {
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }
        if (mSoftKeyboardHelper == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.SOFT_KEYBOARD_HELPER_IS_NULL);
            return;
        }

        setTheme(new ThemeSnapchatImpl());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBinding = null;
        if (AudioRecordManager.getInstance().getOnAudioRecordListener() == mOnAudioRecordListener) {
            AudioRecordManager.getInstance().setOnAudioRecordListener(null);
        }
        mVoiceRecordGestureHelper = null;
    }

    private class OnAudioRecordListenerImpl implements AudioRecordManager.OnAudioRecordListener {

        @Override
        public void onAudioRecordStart() {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordStart");
            showAudioRecording();
        }

        @Override
        public void onAudioRecordProgress(long duration) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordProgress duration:%s", duration);
        }

        @Override
        public void onAudioRecordError() {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordError");
            hideAudioRecoding(false, true);
        }

        @Override
        public void onAudioRecordCancel(boolean lessThanMinDuration) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordCancel lessThanMinDuration:%s", lessThanMinDuration);
            hideAudioRecoding(lessThanMinDuration, false);
        }

        @Override
        public void onAudioRecordCompletedSuccess(@NonNull String audioRecorderFile, boolean reachMaxDuration) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordCompletedSuccess audioRecorderFile:%s, reachMaxDuration:%s", audioRecorderFile, reachMaxDuration);
            hideAudioRecoding(false, false);

            // 发送语音消息
            submitAudioMessage(isSnapchatMode(), audioRecorderFile);
        }
    }

    public void clearInputText() {
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        mBinding.keyboardEditText.setText(null);
    }

    private void showAudioRecording() {
        if (getChildFragmentManager().isStateSaved()) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        ViewUtil.setVisibilityIfChanged(mBinding.recordingVolumeLayer, View.VISIBLE);
        mBinding.recordingVolumeIcon.setImageResource(mTheme.recordingVolumeIconGoing());
        final Drawable drawable = mBinding.recordingVolumeIcon.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable) drawable).start();
        }
        mBinding.recordingVolumeTip.setText(R.string.imsdk_uikit_voice_record_down_cancel_send);
    }

    private void updateAudioRecording(boolean inside) {
        if (getChildFragmentManager().isStateSaved()) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        if (inside) {
            Drawable drawable = mBinding.recordingVolumeIcon.getDrawable();
            if (!(drawable instanceof AnimationDrawable)) {
                mBinding.recordingVolumeIcon.setImageResource(mTheme.recordingVolumeIconGoing());
                drawable = mBinding.recordingVolumeIcon.getDrawable();
            }

            if (drawable instanceof AnimationDrawable) {
                ((AnimationDrawable) drawable).start();
            }
            mBinding.recordingVolumeTip.setText(R.string.imsdk_uikit_voice_record_down_cancel_send);
        } else {
            mBinding.recordingVolumeIcon.setImageResource(mTheme.recordingVolumeIconDialogCancel());
            mBinding.recordingVolumeTip.setText(R.string.imsdk_uikit_voice_record_up_cancel_send);
        }
    }

    private void hideAudioRecoding(final boolean tooShort, final boolean fail) {
        if (getChildFragmentManager().isStateSaved()) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        if (mBinding.recordingVolumeLayer.getVisibility() == View.GONE) {
            MSIMUikitLog.w("unexpected. hideAudioRecoding recordingVolumeLayer already gone");
            return;
        }

        final Drawable drawable = mBinding.recordingVolumeIcon.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable) drawable).stop();
        }

        if (tooShort || fail) {
            mBinding.recordingVolumeIcon.setImageResource(mTheme.recordingVolumeIconDialogLengthShort());
            if (tooShort) {
                mBinding.recordingVolumeTip.setText(R.string.imsdk_uikit_voice_record_say_time_short);
            } else {
                mBinding.recordingVolumeTip.setText(R.string.imsdk_uikit_voice_record_fail);
            }

            final ImsdkUikitCustomInputFragmentBinding unsafeBinding = mBinding;
            unsafeBinding.getRoot().postDelayed(() -> ViewUtil.setVisibilityIfChanged(unsafeBinding.recordingVolumeLayer, View.GONE), 800L);
        } else {
            final ImsdkUikitCustomInputFragmentBinding unsafeBinding = mBinding;
            unsafeBinding.getRoot().postDelayed(() -> ViewUtil.setVisibilityIfChanged(unsafeBinding.recordingVolumeLayer, View.GONE), 300L);
        }
    }

    private void applyTheme() {
        if (mBinding == null) {
            return;
        }

        final Theme theme = mTheme;
        if (theme == null) {
            return;
        }

        mBinding.softKeyboardListenerLayout.setBackground(theme.pageBackground());
        mBinding.customTopBarContainer.setBackground(theme.topBarContainerBackground());
        mBinding.customTopBarBottomDivider.setBackground(theme.topBarBottomDividerBackground());
        mBinding.majorContainer.setBackground(theme.majorContainerBackground());
        mBinding.keyboardBackground.setBackground(theme.keyboardBackground());
        mBinding.keyboardBackgroundDivider.setBackground(theme.keyboardBackgroundDivider());
        mBinding.keyboardVoiceSystemSoftKeyboard.setImageDrawable(theme.keyboardSystemSoftKeyboardSrc());
        mBinding.keyboardVoice.setImageDrawable(theme.keyboardVoiceSrc());
        mBinding.keyboardEditText.setBackground(theme.keyboardEditTextBackground());
        mBinding.keyboardEditText.setTextColor(theme.keyboardEditTextColor());
        mBinding.keyboardEditText.setHintTextColor(theme.keyboardEditTextColorHint());
        mBinding.keyboardVoiceRecordText.setBackground(theme.keyboardVoiceRecordTextBackground());
        mBinding.keyboardVoiceRecordText.setTextColor(theme.keyboardVoiceRecordTextColor());
        mBinding.keyboardEmoji.setImageDrawable(theme.keyboardEmojiSrc());
        mBinding.keyboardEmojiSystemSoftKeyboard.setImageDrawable(theme.keyboardSystemSoftKeyboardSrc());
        mBinding.keyboardMore.setImageDrawable(theme.keyboardMoreSrc());
        mBinding.keyboardCancelMode.setImageDrawable(theme.keyboardCancelModeSrc());
        mBinding.keyboardSubmit.setBackground(theme.keyboardSubmitBackground());
        mBinding.keyboardSubmit.setTextColor(theme.keyboardSubmitTextColor());
        mBinding.customSoftKeyboard.setBackground(theme.customSoftKeyboardBackground());
        mBinding.recordingVolumeLayer.setBackground(theme.recordingVolumeLayerBackground());
        mBinding.recordingVolumeTip.setTextColor(theme.recordingVolumeTipTextColor());

        if (isThemeMode()) {
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardCancelMode, View.VISIBLE);
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.keyboardCancelMode, View.GONE);
        }

        if (isSnapchatMode()) {
            // 阅后即焚模式下，不支持输入音视频电话、位置
            mBinding.customSoftKeyboard.getSystemConfig().setShowRtc(false);
            mBinding.customSoftKeyboard.getSystemConfig().setShowLocation(false);
            mBinding.customSoftKeyboard.getSystemConfig().setShowSnapchat(false);
        } else {
            mBinding.customSoftKeyboard.getSystemConfig().setShowRtc(true);
            mBinding.customSoftKeyboard.getSystemConfig().setShowLocation(true);
            mBinding.customSoftKeyboard.getSystemConfig().setShowSnapchat(true);
        }
    }

    public interface ThemeMode {
    }

    /**
     * 阅后即焚主题
     *
     * @see ThemeSnapchatImpl
     */
    public interface ThemeSnapchat extends ThemeMode {
    }

    /**
     * 定义自定义键盘的主题
     */
    public static class Theme {

        protected Context context() {
            return ContextUtil.getContext();
        }

        /**
         * @return 默认的分割线背景
         */
        Drawable defaultDividerDrawable() {
            return new ColorDrawable(0x80CCCCCC);
        }

        /**
         * @return 页面整体的背景
         */
        Drawable pageBackground() {
            return new ColorDrawable(Color.WHITE);
        }

        /**
         * @return top bar container 背景
         */
        Drawable topBarContainerBackground() {
            return new ColorDrawable(Color.WHITE);
        }

        /**
         * @return top bar bottom 分割线背景
         */
        Drawable topBarBottomDividerBackground() {
            return this.defaultDividerDrawable();
        }

        /**
         * @return 主体背景
         */
        Drawable majorContainerBackground() {
            return new ColorDrawable(0xFFF2F4F5);
        }

        /**
         * @return 键盘背景
         */
        Drawable keyboardBackground() {
            return new ColorDrawable(0xFFFaFaFa);
        }

        /**
         * @return 键盘分割线背景
         */
        Drawable keyboardBackgroundDivider() {
            return this.defaultDividerDrawable();
        }

        /**
         * @return 切换到系统键盘图标
         */
        Drawable keyboardSystemSoftKeyboardSrc() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_input_keyboard_selector);
        }

        /**
         * @return 切换到语音输入图标
         */
        Drawable keyboardVoiceSrc() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_input_voice_selector);
        }

        /**
         * @return 输入文字的背景
         */
        Drawable keyboardEditTextBackground() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_input_background);
        }

        /**
         * @return 输入文字的颜色
         */
        int keyboardEditTextColor() {
            return 0xFF333333;
        }

        /**
         * @return 输入文字的 hint 颜色
         */
        int keyboardEditTextColorHint() {
            return 0xFF999999;
        }

        /**
         * @return 语音输入时的提醒文字背景
         */
        Drawable keyboardVoiceRecordTextBackground() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_input_background_selector);
        }

        /**
         * @return 语音输入时的提醒文字颜色
         */
        int keyboardVoiceRecordTextColor() {
            return 0xFF333333;
        }

        /**
         * @return 切换到 emoji 输入图标
         */
        Drawable keyboardEmojiSrc() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_input_emoji_selector);
        }

        /**
         * @return 切换到更多输入图标
         */
        Drawable keyboardMoreSrc() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_input_more_selector);
        }

        /**
         * @return 取消当前模式图标
         */
        Drawable keyboardCancelModeSrc() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_input_cancel_mode_selector);
        }

        /**
         * @return 发送按钮的背景
         */
        Drawable keyboardSubmitBackground() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_button_background_selector);
        }

        /**
         * @return 发送按钮的文字颜色
         */
        int keyboardSubmitTextColor() {
            return Color.WHITE;
        }

        /**
         * @return 自定义键盘背景
         */
        Drawable customSoftKeyboardBackground() {
            return new ColorDrawable(0xFFF2F4F5);
        }

        /**
         * @return 语音输入时的录音背景
         */
        Drawable recordingVolumeLayerBackground() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_volume_dialog_bg);
        }

        /**
         * @return 正在录音
         */
        @DrawableRes
        int recordingVolumeIconGoing() {
            return R.drawable.imsdk_uikit_recording_volume;
        }

        /**
         * @return 录音取消
         */
        @DrawableRes
        int recordingVolumeIconDialogCancel() {
            return R.drawable.imsdk_uikit_ic_volume_dialog_cancel;
        }

        /**
         * @return 录音时长太短
         */
        @DrawableRes
        int recordingVolumeIconDialogLengthShort() {
            return R.drawable.imsdk_uikit_ic_volume_dialog_length_short;
        }

        /**
         * @return 录音提醒文字颜色
         */
        int recordingVolumeTipTextColor() {
            return Color.WHITE;
        }
    }

    public static class ThemeSnapchatImpl extends Theme implements ThemeSnapchat {
        @Override
        Drawable keyboardBackground() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_input_theme_snapchat_bg);
        }

        @Override
        Drawable customSoftKeyboardBackground() {
            return AppCompatResources.getDrawable(context(), R.drawable.imsdk_uikit_ic_input_theme_snapchat_bg);
        }

        @Override
        int keyboardEditTextColor() {
            return 0xFFFF6531;
        }

        @Override
        int keyboardVoiceRecordTextColor() {
            return 0xFFFF6531;
        }

        @Override
        int keyboardSubmitTextColor() {
            return 0xFFFF6531;
        }
    }

}
