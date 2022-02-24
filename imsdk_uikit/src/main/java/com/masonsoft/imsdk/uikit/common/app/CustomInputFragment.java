package com.masonsoft.imsdk.uikit.common.app;

import android.Manifest;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import io.github.idonans.core.util.PermissionUtil;
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
    private final OnBackPressedCallbackImpl mOnBackPressedCallback = new OnBackPressedCallbackImpl();

    private class OnBackPressedCallbackImpl extends OnBackPressedCallback {

        public OnBackPressedCallbackImpl() {
            super(false);
        }

        @Override
        public void handleOnBackPressed() {
            if (mSoftKeyboardHelper != null) {
                mSoftKeyboardHelper.onBackPressed();
            }
        }
    }

    protected ImsdkUikitCustomInputFragmentBinding getCustomBinding() {
        return mBinding;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOnBackPressedCallback.setEnabled(false);
        requireActivity().getOnBackPressedDispatcher().addCallback(mOnBackPressedCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mOnBackPressedCallback.setEnabled(false);
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
                mOnBackPressedCallback.setEnabled(true);
                onSoftKeyboardLayoutShownAdapter(customSoftKeyboard, systemSoftKeyboard);
            }

            @Override
            protected void onAllSoftKeyboardLayoutHidden() {
                mOnBackPressedCallback.setEnabled(false);
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
                submitMediaMessage(mediaInfoList);
            }

            @Override
            public void onClickRtcAudio() {
                submitClickRtcAudio();
            }

            @Override
            public void onClickRtcVideo() {
                submitClickRtcVideo();
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
                submitLocationMessage(locationInfo, zoom);
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

        this.submitTextMessage(text);
    }

    protected abstract void submitTextMessage(String text);

    protected abstract void submitMediaMessage(@NonNull List<MediaData.MediaInfo> mediaInfoList);

    protected abstract void submitAudioMessage(final String audioFilePath);

    protected abstract void submitLocationMessage(@NonNull LocationInfo locationInfo, long zoom);

    protected abstract void submitClickRtcAudio();

    protected abstract void submitClickRtcVideo();

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mOnBackPressedCallback.setEnabled(false);
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
            submitAudioMessage(audioRecorderFile);
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
        mBinding.recordingVolumeIcon.setImageResource(R.drawable.imsdk_uikit_recording_volume);
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
                mBinding.recordingVolumeIcon.setImageResource(R.drawable.imsdk_uikit_recording_volume);
                drawable = mBinding.recordingVolumeIcon.getDrawable();
            }

            if (drawable instanceof AnimationDrawable) {
                ((AnimationDrawable) drawable).start();
            }
            mBinding.recordingVolumeTip.setText(R.string.imsdk_uikit_voice_record_down_cancel_send);
        } else {
            mBinding.recordingVolumeIcon.setImageResource(R.drawable.imsdk_uikit_ic_volume_dialog_cancel);
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
            mBinding.recordingVolumeIcon.setImageResource(R.drawable.imsdk_uikit_ic_volume_dialog_length_short);
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

}
