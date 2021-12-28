package com.masonsoft.imsdk.uikit.app.chat;

import android.Manifest;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.MSIMCallback;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageFactory;
import com.masonsoft.imsdk.MSIMWeakCallback;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.uikit.MSIMRtcMessageManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.common.media.audio.AudioRecordManager;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.VisibleRecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleBottomActionsDialog;
import com.masonsoft.imsdk.uikit.common.softkeyboard.SoftKeyboardHelper;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitSingleChatFragmentBinding;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.EditTextUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.uikit.widget.CustomSoftKeyboard;
import com.masonsoft.imsdk.util.Objects;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.idonans.core.AbortSignal;
import io.github.idonans.core.FormValidator;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.PermissionUtil;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.UnionTypeStatusPageView;
import io.github.idonans.dynamic.uniontype.loadingstatus.UnionTypeLoadingStatus;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 单聊页面
 *
 * @see com.masonsoft.imsdk.MSIMConstants.ConversationType#C2C
 */
public class SingleChatFragment extends SystemInsetsFragment {

    public static SingleChatFragment newInstance(long targetUserId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.TARGET_USER_ID, targetUserId);
        SingleChatFragment fragment = new SingleChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final DisposableHolder mPermissionRequest = new DisposableHolder();
    private static final String[] VOICE_RECORD_PERMISSION = {
            Manifest.permission.RECORD_AUDIO,
    };

    private long mTargetUserId;
    @Nullable
    private ImsdkUikitSingleChatFragmentBinding mBinding;
    @Nullable
    private SoftKeyboardHelper mSoftKeyboardHelper;
    private LocalEnqueueCallback mEnqueueCallback;
    private VoiceRecordGestureHelper mVoiceRecordGestureHelper;
    private final AudioRecordManager.OnAudioRecordListener mOnAudioRecordListener = new OnAudioRecordListenerImpl();

    private UnionTypeAdapter mDataAdapter;
    private SingleChatFragmentPresenter mPresenter;
    private ViewImpl mViewImpl;
    private MicroLifecycleComponentManager mMicroLifecycleComponentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mTargetUserId = args.getLong(MSIMUikitConstants.ExtrasKey.TARGET_USER_ID, mTargetUserId);
        }
    }

    private static void smoothScrollToPosition(RecyclerView recyclerView, int position) {
        MSIMUikitLog.v("smoothScrollToPosition recyclerView:%s position:%s", recyclerView, position);
        recyclerView.smoothScrollToPosition(position);
    }

    private static void scrollToPosition(RecyclerView recyclerView, int position) {
        MSIMUikitLog.v("scrollToPosition recyclerView:%s position:%s", recyclerView, position);
        recyclerView.scrollToPosition(position);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkUikitSingleChatFragmentBinding.inflate(inflater, container, false);

        ViewUtil.onClick(mBinding.topBarBack, v -> ActivityUtil.requestBackPressed(SingleChatFragment.this));
        mBinding.topBarTitle.setTargetUserId(mTargetUserId);
        mBinding.beingTypedView.setTarget(MSIMManager.getInstance().getSessionUserId(), mTargetUserId);

        ViewUtil.onClick(mBinding.topBarMore, v -> showBottomActions());

        final RecyclerView recyclerView = mBinding.recyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                recyclerView.getContext(),
                RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if (mDataAdapter != null && lastPosition >= 0) {
                        if (lastPosition == mDataAdapter.getItemCount() - 1) {
                            // 滚动到最底部
                            hideNewMessagesTipView();
                            sendMarkAsRead();
                        }
                    }
                }
            }
        });
        mMicroLifecycleComponentManager = new VisibleRecyclerViewMicroLifecycleComponentManager(recyclerView, getLifecycle());

        UnionTypeAdapter adapter = new UnionTypeAdapterImpl();
        adapter.setHost(Host.Factory.create(this, recyclerView, adapter));
        adapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
        mDataAdapter = adapter;
        mViewImpl = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new SingleChatFragmentPresenter(mViewImpl);
        mViewImpl.setPresenter(mPresenter);
        recyclerView.setAdapter(adapter);

        mBinding.keyboardEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3000)});
        mSoftKeyboardHelper = new SoftKeyboardHelper(
                mBinding.softKeyboardListenerLayout,
                mBinding.keyboardEditText,
                mBinding.customSoftKeyboard) {
            @Override
            protected boolean isTouchOutside(float rawX, float rawY) {
                final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
                if (binding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return false;
                }

                int[] outLocation = new int[2];
                binding.keyboardTopLine.getLocationInWindow(outLocation);
                boolean isTouchOutside = rawY <= outLocation[1];

                MSIMUikitLog.v("isTouchOutside touch raw:[%s,%s], keyboard top line location:[%s,%s], isTouchOutside:%s",
                        rawX, rawY, outLocation[0], outLocation[1], isTouchOutside);

                return isTouchOutside;
            }

            @Override
            protected void onSoftKeyboardLayoutShown(boolean customSoftKeyboard, boolean systemSoftKeyboard) {
                final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
                if (binding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }

                Threads.postUi(() -> {
                    int count = mDataAdapter.getItemCount();
                    if (count > 0) {
                        //noinspection ConstantConditions
                        final int firstPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        final int lastPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        final int archPosition = Math.max(0, count - 3);

                        boolean scrollWithAnimation = false;
                        if (archPosition >= firstPosition && archPosition <= lastPosition) {
                            scrollWithAnimation = true;
                        }

                        MSIMUikitLog.v("onSoftKeyboardLayoutShown scrollWithAnimation:%s, firstPosition:%s, count:%s",
                                scrollWithAnimation, firstPosition, count);
                        if (scrollWithAnimation) {
                            smoothScrollToPosition(binding.recyclerView, count - 1);
                        } else {
                            binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);
                                    binding.recyclerView.removeOnScrollListener(this);
                                    MSIMUikitLog.v("onSoftKeyboardLayoutShown scrollWithAnimation:false addOnScrollListener onScrolled");
                                    smoothScrollToPosition(binding.recyclerView, mDataAdapter.getItemCount() - 1);
                                }
                            });
                            scrollToPosition(binding.recyclerView, archPosition);
                        }
                    }
                });

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

            @Override
            protected void onAllSoftKeyboardLayoutHidden() {
                final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
                if (binding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                ViewUtil.setVisibilityIfChanged(binding.keyboardEmoji, View.VISIBLE);
                ViewUtil.setVisibilityIfChanged(binding.keyboardEmojiSystemSoftKeyboard, View.GONE);
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
                if (mViewImpl != null) {
                    mViewImpl.updateAudioRecording(inside);
                }
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
                MSIMRtcMessageManager.getInstance().startRtcMessage(mTargetUserId, null, false);
            }

            @Override
            public void onClickRtcVideo() {
                MSIMRtcMessageManager.getInstance().startRtcMessage(mTargetUserId, null, true);
            }
        });

        mBinding.keyboardEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mPresenter != null) {
                    mPresenter.setBeingTyped();
                }
            }
        });

        AudioRecordManager.getInstance().setOnAudioRecordListener(mOnAudioRecordListener);

        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoice, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceSystemSoftKeyboard, View.GONE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardEditText, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardVoiceRecordText, View.GONE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardEmoji, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.keyboardEmojiSystemSoftKeyboard, View.GONE);

        mPresenter.requestInit();

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendMarkAsRead();
    }

    private void showBottomActions() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final List<String> actions = Lists.newArrayList("模拟并发消息");
        final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                activity,
                actions
        );
        dialog.setOnActionClickListener((index, actionText) -> {
            if (index == 0) {
                // 模拟并发消息
                final long targetUserId = mTargetUserId;
                mockMultiMessages(targetUserId);
            }
        });
        dialog.show();
    }

    private static void mockMultiMessages(final long targetUserId) {
        final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        final int size = 10;
        for (int i = 1; i <= size; i++) {
            final int index = i;
            Threads.postBackground(() -> {
                final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                final MSIMMessage message = MSIMMessageFactory.createTextMessage("[" + time + "] mock concurrent message [" + index + "/" + size + "]");
                MSIMManager.getInstance().getMessageManager().sendMessage(
                        sessionUserId,
                        message,
                        targetUserId
                );
            });
        }
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
        final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        final Editable editable = binding.keyboardEditText.getText();
        if (editable == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.EDITABLE_IS_NULL);
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(true);
        final String text = editable.toString().trim();
        final MSIMMessage message = MSIMMessageFactory.createTextMessage(text);
        MSIMManager.getInstance().getMessageManager().sendMessage(
                MSIMManager.getInstance().getSessionUserId(),
                message,
                mTargetUserId,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    private void submitMediaMessage(@NonNull List<MediaData.MediaInfo> mediaInfoList) {
        final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
            mEnqueueCallback = new LocalEnqueueCallback(false);
            final MSIMMessage message;
            if (mediaInfo.isVideoMimeType()) {
                message = MSIMMessageFactory.createVideoMessage(mediaInfo.uri);
            } else {
                message = MSIMMessageFactory.createImageMessage(mediaInfo.uri);
            }
            MSIMManager.getInstance().getMessageManager().sendMessage(
                    MSIMManager.getInstance().getSessionUserId(),
                    message,
                    mTargetUserId,
                    new MSIMWeakCallback<>(mEnqueueCallback)
            );
        }
    }

    private void submitAudioMessage(final String audioFilePath) {
        final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(true);
        final MSIMMessage message = MSIMMessageFactory.createAudioMessage(audioFilePath);
        MSIMManager.getInstance().getMessageManager().sendMessage(
                MSIMManager.getInstance().getSessionUserId(),
                message,
                mTargetUserId,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    private void showNewMessagesTipView() {
        // TODO
        // ViewUtil.setVisibilityIfChanged(mActionNewMessages, View.VISIBLE);
    }

    private void hideNewMessagesTipView() {
        // TODO
        // ViewUtil.setVisibilityIfChanged(mActionNewMessages, View.GONE);
    }

    private void clearPresenter() {
        if (mPresenter != null) {
            mPresenter.setAbort();
            mPresenter = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearPresenter();
        mBinding = null;
        mViewImpl = null;
        if (AudioRecordManager.getInstance().getOnAudioRecordListener() == mOnAudioRecordListener) {
            AudioRecordManager.getInstance().setOnAudioRecordListener(null);
        }
        mVoiceRecordGestureHelper = null;
    }

    private class UnionTypeAdapterImpl extends UnionTypeAdapter implements MicroLifecycleComponentManagerHost {
        @Override
        public MicroLifecycleComponentManager getMicroLifecycleComponentManager() {
            return mMicroLifecycleComponentManager;
        }
    }

    private void sendMarkAsRead() {
        MSIMUikitLog.v(Objects.defaultObjectTag(this) + " sendMarkAsRead targetUserId:%s", mTargetUserId);
        MSIMManager.getInstance().getMessageManager().markAsRead(
                MSIMManager.getInstance().getSessionUserId(),
                mTargetUserId
        );
    }

    private class OnAudioRecordListenerImpl implements AudioRecordManager.OnAudioRecordListener {

        @Override
        public void onAudioRecordStart() {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordStart");
            if (mViewImpl != null) {
                mViewImpl.showAudioRecording();
            }
        }

        @Override
        public void onAudioRecordProgress(long duration) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordProgress duration:%s", duration);
        }

        @Override
        public void onAudioRecordError() {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordError");
            if (mViewImpl != null) {
                mViewImpl.hideAudioRecoding(false, true);
            }
        }

        @Override
        public void onAudioRecordCancel(boolean lessThanMinDuration) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordCancel lessThanMinDuration:%s", lessThanMinDuration);
            if (mViewImpl != null) {
                mViewImpl.hideAudioRecoding(lessThanMinDuration, false);
            }
        }

        @Override
        public void onAudioRecordCompletedSuccess(@NonNull String audioRecorderFile, boolean reachMaxDuration) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onAudioRecordCompletedSuccess audioRecorderFile:%s, reachMaxDuration:%s", audioRecorderFile, reachMaxDuration);
            if (mViewImpl != null) {
                mViewImpl.hideAudioRecoding(false, false);

                // 发送语音消息
                submitAudioMessage(audioRecorderFile);
            }
        }
    }

    class ViewImpl extends UnionTypeStatusPageView<GeneralResult> {

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            super(adapter);
            setAlwaysHideNoMoreData(true);
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

                final ImsdkUikitSingleChatFragmentBinding unsafeBinding = mBinding;
                unsafeBinding.getRoot().postDelayed(() -> ViewUtil.setVisibilityIfChanged(unsafeBinding.recordingVolumeLayer, View.GONE), 800L);
            } else {
                final ImsdkUikitSingleChatFragmentBinding unsafeBinding = mBinding;
                unsafeBinding.getRoot().postDelayed(() -> ViewUtil.setVisibilityIfChanged(unsafeBinding.recordingVolumeLayer, View.GONE), 300L);
            }
        }

        public long getTargetUserId() {
            return mTargetUserId;
        }

        private void showInitRequest(boolean delay) {
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final SingleChatFragmentPresenter presenter = (SingleChatFragmentPresenter) getPresenter();
                        if (presenter == null) {
                            return;
                        }
                        if (!presenter.getInitRequestStatus().isLoading()) {
                            return;
                        }

                        if (delay) {
                            Threads.postUi(() -> {
                                if (presenter.getInitRequestStatus().isLoading()) {
                                    showInitRequest(false);
                                }
                            }, 150L);
                            return;
                        }

                        // 清除当前页面内容
                        groupArrayList.removeAll();
                        // 显示一个全屏的 loading
                        groupArrayList.setGroupItems(
                                getGroupHeader(),
                                Lists.newArrayList(
                                        new UnionTypeItemObject(UnionTypeLoadingStatus.UNION_TYPE_LOADING_STATUS_LOADING_LARGE, new Object())
                                )
                        );
                    })
                    .commit();
        }

        @Override
        public void onInitRequest() {
            showInitRequest(true);
        }

        @Override
        public void onInitRequestResult(@NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            super.onInitRequestResult(result);

            final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            if (result.items != null && !result.items.isEmpty()) {
                getAdapter().getData().beginTransaction()
                        .commit(() -> {
                            final int count = getAdapter().getItemCount();
                            if (count > 0 && isResumed()) {
                                scrollToPosition(binding.recyclerView, count - 1);
                                sendMarkAsRead();
                            }
                        });
            }
        }

        private void showPrePageRequest(boolean delay) {
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final SingleChatFragmentPresenter presenter = (SingleChatFragmentPresenter) getPresenter();
                        if (presenter == null) {
                            return;
                        }
                        if (!presenter.getPrePageRequestStatus().isLoading()) {
                            return;
                        }

                        if (delay) {
                            Threads.postUi(() -> {
                                if (presenter.getPrePageRequestStatus().isLoading()) {
                                    showPrePageRequest(false);
                                }
                            }, 150L);
                            return;
                        }

                        // 使用小的 loading
                        groupArrayList.setGroupItems(
                                getGroupHeader(),
                                Lists.newArrayList(
                                        new UnionTypeItemObject(UnionTypeLoadingStatus.UNION_TYPE_LOADING_STATUS_LOADING_SMALL, new Object())
                                )
                        );
                    })
                    .commit();
        }

        @Override
        public void onPrePageRequest() {
            showPrePageRequest(true);
        }

        private void showNextPageRequest(boolean delay) {
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final SingleChatFragmentPresenter presenter = (SingleChatFragmentPresenter) getPresenter();
                        if (presenter == null) {
                            return;
                        }
                        if (!presenter.getNextPageRequestStatus().isLoading()) {
                            return;
                        }

                        if (delay) {
                            Threads.postUi(() -> {
                                if (presenter.getNextPageRequestStatus().isLoading()) {
                                    showNextPageRequest(false);
                                }
                            }, 150L);
                            return;
                        }

                        // 使用小的 loading
                        groupArrayList.setGroupItems(
                                getGroupFooter(),
                                Lists.newArrayList(
                                        new UnionTypeItemObject(UnionTypeLoadingStatus.UNION_TYPE_LOADING_STATUS_LOADING_SMALL, new Object())
                                )
                        );
                    })
                    .commit();
        }

        @Override
        public void onNextPageRequest() {
            showNextPageRequest(true);
        }

        @Override
        public void onNextPageRequestResult(@NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            super.onNextPageRequestResult(result);

            final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            if (result.items != null && !result.items.isEmpty()) {
                getAdapter().getData().beginTransaction()
                        .commit(() -> {
                            final int count = getAdapter().getItemCount();
                            final int footerCount = getAdapter().getGroupItemsSize(getGroupFooter());
                            if (count > 0) {
                                boolean autoScroll = false;
                                //noinspection ConstantConditions
                                int lastPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                                if (lastPosition >= count - (footerCount + 1) - result.items.size()) {
                                    // 当前滚动到最后
                                    autoScroll = true;
                                }
                                if (autoScroll && isResumed()) {
                                    scrollToPosition(binding.recyclerView, count - 1);
                                    sendMarkAsRead();
                                } else {
                                    // 显示向下的箭头
                                    showNewMessagesTipView();
                                }
                            }
                        });
            }
        }
    }

    private class LocalEnqueueCallback implements MSIMCallback<GeneralResult>, AbortSignal {

        private final boolean mClearEditTextWhenSuccess;

        private LocalEnqueueCallback(boolean clearEditTextWhenSuccess) {
            this.mClearEditTextWhenSuccess = clearEditTextWhenSuccess;
        }

        @Override
        public void onCallback(@NonNull GeneralResult result) {
            if (isAbort()) {
                return;
            }
            Threads.postUi(() -> {
                if (isAbort()) {
                    return;
                }

                final ImsdkUikitSingleChatFragmentBinding binding = mBinding;
                if (binding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                MSIMUikitLog.v("onCallback %s", result);

                if (result.isSuccess()) {
                    if (mClearEditTextWhenSuccess) {
                        // 消息发送成功之后，清空输入框
                        binding.keyboardEditText.setText(null);
                    }
                } else {
                    TipUtil.showOrDefault(result.message);
                }
            });
        }

        @Override
        public boolean isAbort() {
            return mEnqueueCallback != this;
        }
    }

}
