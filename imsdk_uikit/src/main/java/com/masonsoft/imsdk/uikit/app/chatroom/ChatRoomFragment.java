package com.masonsoft.imsdk.uikit.app.chatroom;

import android.Manifest;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
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
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMChatRoomMessageFactory;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMWeakCallback;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.app.chatroom.settings.ChatRoomSettingsActivity;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.common.media.audio.AudioRecordManager;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.VisibleRecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleBottomActionsDialog;
import com.masonsoft.imsdk.uikit.common.softkeyboard.SoftKeyboardHelper;
import com.masonsoft.imsdk.uikit.common.voicerecordgesture.VoiceRecordGestureHelper;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitChatRoomFragmentBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.EditTextUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.uikit.widget.CustomSoftKeyboard;
import com.masonsoft.imsdk.util.Objects;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.idonans.core.AbortSignal;
import io.github.idonans.core.FormValidator;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.PermissionUtil;
import io.github.idonans.dynamic.DynamicView;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 聊天室页面
 */
public class ChatRoomFragment extends SystemInsetsFragment {

    public static ChatRoomFragment newInstance(long chatRoomId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, chatRoomId);
        ChatRoomFragment fragment = new ChatRoomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final DisposableHolder mPermissionRequest = new DisposableHolder();
    private static final String[] VOICE_RECORD_PERMISSION = {
            Manifest.permission.RECORD_AUDIO,
    };

    private long mChatRoomId;
    @Nullable
    private ImsdkUikitChatRoomFragmentBinding mBinding;
    @Nullable
    private SoftKeyboardHelper mSoftKeyboardHelper;
    private LocalEnqueueCallback mEnqueueCallback;
    private VoiceRecordGestureHelper mVoiceRecordGestureHelper;
    private final AudioRecordManager.OnAudioRecordListener mOnAudioRecordListener = new OnAudioRecordListenerImpl();

    private UnionTypeAdapter mDataAdapter;
    private ChatRoomFragmentPresenter mPresenter;
    private ViewImpl mViewImpl;
    private MicroLifecycleComponentManager mMicroLifecycleComponentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mChatRoomId = args.getLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, mChatRoomId);
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
        mBinding = ImsdkUikitChatRoomFragmentBinding.inflate(inflater, container, false);
        mBinding.customSoftKeyboard.setShowRtc(false);
        mBinding.customSoftKeyboard.setShowLocation(false);
        mBinding.customSoftKeyboard.setShowFlashImage(false);

        ViewUtil.onClick(mBinding.topBarBack, v -> ActivityUtil.requestBackPressed(ChatRoomFragment.this));
        mBinding.topBarTitle.setChatRoomContext(null);

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
        recyclerView.setAdapter(adapter);

        mViewImpl = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new ChatRoomFragmentPresenter(mViewImpl);

        mBinding.keyboardEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3000)});
        mSoftKeyboardHelper = new SoftKeyboardHelper(
                mBinding.softKeyboardListenerLayout,
                mBinding.keyboardEditText,
                mBinding.customSoftKeyboard) {
            @Override
            protected boolean isTouchOutside(float rawX, float rawY) {
                final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
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
                final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
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
                final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
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
        mBinding.customSoftKeyboard.setOnInputListener(new CustomSoftKeyboard.OnInputListenerAdapter() {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showBottomActions() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final List<String> actions = Lists.newArrayList("设置", "模拟并发消息");
        final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                activity,
                actions
        );
        dialog.setOnActionClickListener((index, actionText) -> {
            if (index == 0) {
                // 设置
                if (mChatRoomId <= 0) {
                    MSIMUikitLog.e("unexpected. chat room id:" + mChatRoomId);
                    return;
                }
                ChatRoomSettingsActivity.start(activity, mChatRoomId);
            } else if (index == 1) {
                // 模拟并发消息
                if (mPresenter == null) {
                    MSIMUikitLog.e("unexpected. presenter is null");
                    return;
                }
                mockMultiMessages(mPresenter.getChatRoomContext());
            }
        });
        dialog.show();
    }

    private static void mockMultiMessages(final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
        if (chatRoomContext == null) {
            MSIMUikitLog.e("unexpected. chat room context is null");
            return;
        }
        final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        final int size = 10;
        for (int i = 1; i <= size; i++) {
            final int index = i;
            Threads.postBackground(() -> {
                final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createTextMessage(
                        "[" + time + "] mock concurrent message [" + index + "/" + size + "]",
                        chatRoomContext.getChatRoomContext()
                );
                chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                        sessionUserId,
                        message
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
        final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }

        final Editable editable = binding.keyboardEditText.getText();
        if (editable == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.EDITABLE_IS_NULL);
            return;
        }
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(true);
        final String text = editable.toString().trim();
        final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createTextMessage(
                text,
                chatRoomContext.getChatRoomContext()
        );
        chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                chatRoomContext.getSessionUserId(),
                message,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    private void submitMediaMessage(@NonNull List<MediaData.MediaInfo> mediaInfoList) {
        final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
            mEnqueueCallback = new LocalEnqueueCallback(false);
            final MSIMChatRoomMessage message;
            if (mediaInfo.isVideoMimeType()) {
                message = MSIMChatRoomMessageFactory.createVideoMessage(
                        mediaInfo.uri,
                        chatRoomContext.getChatRoomContext()
                );
            } else {
                message = MSIMChatRoomMessageFactory.createImageMessage(
                        mediaInfo.uri,
                        chatRoomContext.getChatRoomContext()
                );
            }
            chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                    chatRoomContext.getSessionUserId(),
                    message,
                    new MSIMWeakCallback<>(mEnqueueCallback)
            );
        }
    }

    private void submitAudioMessage(final String audioFilePath) {
        final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(true);
        final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createAudioMessage(
                audioFilePath,
                chatRoomContext.getChatRoomContext()
        );
        chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                chatRoomContext.getSessionUserId(),
                message,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    private void submitLocationMessage(@NonNull LocationInfo locationInfo, long zoom) {
        final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
        if (binding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
            return;
        }
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(false);
        final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createLocationMessage(
                locationInfo.title,
                locationInfo.subTitle,
                locationInfo.lat,
                locationInfo.lng,
                zoom,
                chatRoomContext.getChatRoomContext()
        );
        chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                chatRoomContext.getSessionUserId(),
                message,
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

    class ViewImpl implements DynamicView {

        @NonNull
        private final UnionTypeAdapter mAdapter;
        private final int GROUP_CONTENT = 0;

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            mAdapter = adapter;
        }

        public long getChatRoomId() {
            return ChatRoomFragment.this.mChatRoomId;
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

                final ImsdkUikitChatRoomFragmentBinding unsafeBinding = mBinding;
                unsafeBinding.getRoot().postDelayed(() -> ViewUtil.setVisibilityIfChanged(unsafeBinding.recordingVolumeLayer, View.GONE), 800L);
            } else {
                final ImsdkUikitChatRoomFragmentBinding unsafeBinding = mBinding;
                unsafeBinding.getRoot().postDelayed(() -> ViewUtil.setVisibilityIfChanged(unsafeBinding.recordingVolumeLayer, View.GONE), 300L);
            }
        }

        public void onChatRoomStateChanged(@NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            binding.topBarTitle.setChatRoomContext(chatRoomContext.getChatRoomContext());
        }

        public void onAppendMessages(@NonNull List<MSIMChatRoomMessage> messageList, @NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            final ObjectWrapper autoScrollToEnd = new ObjectWrapper(null);
            mAdapter.getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final List<UnionTypeItemObject> contentList = new ArrayList<>();
                        for (MSIMChatRoomMessage message : messageList) {
                            final UnionTypeItemObject unionTypeItemObject = mPresenter.createDefault(message);
                            if (unionTypeItemObject != null) {
                                contentList.add(unionTypeItemObject);
                            } else {
                                MSIMUikitLog.e("unexpected. onAppendMessages create UnionTypeItemObject is null. MSIMChatRoomMessage:%s", message);
                            }
                        }
                        groupArrayList.appendGroupItems(GROUP_CONTENT, contentList);
                    })
                    .commit(() -> {
                        final int count = mAdapter.getItemCount();
                        if (count <= 0) {
                            autoScrollToEnd.setObject(Boolean.TRUE);
                        } else {
                            //noinspection ConstantConditions
                            int lastPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                            if (lastPosition < 0) {
                                autoScrollToEnd.setObject(Boolean.TRUE);
                            } else {
                                if (lastPosition >= count - 1) {
                                    autoScrollToEnd.setObject(Boolean.TRUE);
                                }
                            }
                        }
                    }, () -> {
                        if (autoScrollToEnd.getObject() == Boolean.TRUE && isResumed()) {
                            scrollToPosition(binding.recyclerView, mAdapter.getItemCount() - 1);
                        } else {
                            // 有新消息，显示向下的箭头
                            showNewMessagesTipView();
                        }
                    });
        }

        public void onUpdateMessages(@NonNull List<MSIMChatRoomMessage> messageList, @NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            mAdapter.getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        for (MSIMChatRoomMessage message : messageList) {
                            final UnionTypeItemObject unionTypeItemObject = mPresenter.createDefault(message);
                            final List<UnionTypeItemObject> list = groupArrayList.getGroupItems(GROUP_CONTENT);
                            if (list != null) {
                                for (int i = list.size() - 1; i >= 0; i--) {
                                    final UnionTypeItemObject object = list.get(i);
                                    final DataObject dataObject = object.getItemObject(DataObject.class);
                                    if (dataObject != null) {
                                        final MSIMChatRoomMessage m = dataObject.getObject(MSIMChatRoomMessage.class);
                                        if (m != null) {
                                            if (m.equals(message)) {
                                                if (unionTypeItemObject == null) {
                                                    // 该消息不可见
                                                    list.remove(i);
                                                } else {
                                                    // 该消息可见但是内容可能发生了变化
                                                    list.set(i, unionTypeItemObject);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                    .commit();
        }

        public void onReceivedTipMessageList(@NonNull List<CharSequence> tipMessageList) {
            final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            final ObjectWrapper autoScrollToEnd = new ObjectWrapper(null);
            mAdapter.getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final List<UnionTypeItemObject> contentList = new ArrayList<>();
                        for (CharSequence tipMessage : tipMessageList) {
                            final UnionTypeItemObject unionTypeItemObject = mPresenter.createTipMessageDefault(tipMessage);
                            if (unionTypeItemObject != null) {
                                contentList.add(unionTypeItemObject);
                            } else {
                                MSIMUikitLog.e("unexpected. onReceivedTipMessageList create UnionTypeItemObject is null. tipMessage:%s", tipMessage);
                            }
                        }
                        groupArrayList.appendGroupItems(GROUP_CONTENT, contentList);
                    })
                    .commit(() -> {
                        final int count = mAdapter.getItemCount();
                        if (count <= 0) {
                            autoScrollToEnd.setObject(Boolean.TRUE);
                        } else {
                            //noinspection ConstantConditions
                            int lastPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                            if (lastPosition < 0) {
                                autoScrollToEnd.setObject(Boolean.TRUE);
                            } else {
                                if (lastPosition >= count - 1) {
                                    autoScrollToEnd.setObject(Boolean.TRUE);
                                }
                            }
                        }
                    }, () -> {
                        if (autoScrollToEnd.getObject() == Boolean.TRUE && isResumed()) {
                            scrollToPosition(binding.recyclerView, mAdapter.getItemCount() - 1);
                        } else {
                            // 有新消息，显示向下的箭头
                            showNewMessagesTipView();
                        }
                    });
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

                final ImsdkUikitChatRoomFragmentBinding binding = mBinding;
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
