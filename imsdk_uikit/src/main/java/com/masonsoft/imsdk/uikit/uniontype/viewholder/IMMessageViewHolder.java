package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.common.TopActivity;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.impopup.IMChatMessageMenuDialog;
import com.masonsoft.imsdk.uikit.common.impreview.IMImageOrVideoPreviewDialog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ClipboardUtil;
import com.masonsoft.imsdk.uikit.util.FileDownloadHelper;
import com.masonsoft.imsdk.uikit.util.FormatUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.uikit.widget.IMMessageRevokeStateFrameLayout;
import com.masonsoft.imsdk.uikit.widget.IMMessageRevokeTextView;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.NetUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.core.util.SystemUtil;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public abstract class IMMessageViewHolder extends UnionTypeViewHolder {

    protected static final boolean DEBUG = true;

    private static final FileDownloadHelper FILE_DOWNLOAD_HELPER = new FileDownloadHelper();

    static {
        FILE_DOWNLOAD_HELPER.setOnFileDownloadListener(new FileDownloadHelper.OnSampleFileDownloadListener(true, new FileDownloadHelper.OnFileDownloadListener() {
            @Override
            public void onDownloadSuccess(String id, String localFilePath, String serverUrl) {
                if (SystemUtil.addToMediaStore(new File(localFilePath))) {
                    TipUtil.show(R.string.imsdk_uikit_tip_success_add_to_media_store);
                } else {
                    TipUtil.show(R.string.imsdk_uikit_tip_download_success);
                }
            }

            @Override
            public void onDownloadFail(String id, String serverUrl, Throwable e) {
                if (!NetUtil.hasActiveNetwork()) {
                    TipUtil.showNetworkError();
                    return;
                }
                TipUtil.show(R.string.imsdk_uikit_tip_download_fail);
            }
        }));
    }

    @SuppressLint("CheckResult")
    private static void download(Host host, String downloadUrl) {
        Activity innerActivity = host.getActivity();
        if (innerActivity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (!(innerActivity instanceof AppCompatActivity)) {
            MSIMUikitLog.e("activity is not AppCompatActivity: %s", innerActivity);
            return;
        }
        FragmentManager fm = ((AppCompatActivity) innerActivity).getSupportFragmentManager();
        if (fm.isStateSaved()) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.FRAGMENT_MANAGER_STATE_SAVED);
            return;
        }

        //noinspection ResultOfMethodCallIgnored
        new RxPermissions((FragmentActivity) innerActivity)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (!granted) {
                        TipUtil.show(R.string.imsdk_uikit_tip_require_permission_storage);
                        return;
                    }
                    FILE_DOWNLOAD_HELPER.enqueueFileDownload(null, downloadUrl);
                });
    }

    @Nullable
    private final IMMessageRevokeStateFrameLayout mMessageRevokeStateFrameLayout;
    @Nullable
    private final IMMessageRevokeTextView mMessageRevokeTextView;

    @Nullable
    private final TextView mMessageTime;

    public IMMessageViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mMessageRevokeStateFrameLayout = itemView.findViewById(R.id.message_revoke_state_layout);
        mMessageRevokeTextView = itemView.findViewById(R.id.message_revoke_text_view);

        mMessageTime = itemView.findViewById(R.id.message_time);
    }

    public IMMessageViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mMessageRevokeStateFrameLayout = itemView.findViewById(R.id.message_revoke_state_layout);
        mMessageRevokeTextView = itemView.findViewById(R.id.message_revoke_text_view);

        mMessageTime = itemView.findViewById(R.id.message_time);
    }

    @Override
    public void onBindUpdate() {
        //noinspection unchecked
        final DataObject<MSIMMessage> itemObject = (DataObject<MSIMMessage>) this.getItemObject(Object.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMMessage message = itemObject.object;

        final long sessionUserId = message.getSessionUserId();
        final int conversationType = message.getConversationType();
        final long targetUserId = message.getTargetUserId();
        final long messageId = message.getMessageId();

        if (mMessageRevokeStateFrameLayout != null) {
            mMessageRevokeStateFrameLayout.setMessage(message);
        }
        if (mMessageRevokeTextView != null) {
            mMessageRevokeTextView.setTargetUserId(message.getFromUserId());
        }

        if (mMessageTime != null) {
            updateMessageTimeView(mMessageTime, itemObject);
        }
    }

    /**
     * 获取一个时间间隔 ms，当与上一条消息的时间超过此间隔时，表示需要显示时间. 如果返回的时间间隔不大于 0, 则表示总是显示时间
     */
    protected long getShowTimeDuration() {
        return TimeUnit.MINUTES.toMillis(5);
    }

    protected boolean needShowTime(DataObject<MSIMMessage> dataObject) {
        final long showTimeDuration = getShowTimeDuration();
        if (showTimeDuration <= 0) {
            return true;
        }

        boolean needShowTime = true;
        if (dataObject != null) {
            if (dataObject.object != null) {
                final long currentMessageTime = dataObject.object.getTimeMs();
                if (currentMessageTime <= 0) {
                    Throwable e = new IllegalArgumentException("invalid timeMs " + dataObject.object);
                    MSIMUikitLog.e(e);
                }

                int position = getAdapterPosition();
                if (position > 0) {
                    UnionTypeItemObject preObject = host.getAdapter().getItem(position - 1);
                    if (preObject != null) {
                        //noinspection rawtypes
                        if (preObject.itemObject instanceof DataObject
                                && ((DataObject) preObject.itemObject).object instanceof MSIMMessage) {
                            //noinspection rawtypes
                            MSIMMessage preMessage = (MSIMMessage) ((DataObject) preObject.itemObject).object;
                            final long preMessageTime = preMessage.getTimeMs();
                            if (preMessageTime <= 0) {
                                Throwable e = new IllegalArgumentException("invalid timeMs " + preMessage);
                                MSIMUikitLog.e(e);
                            }
                            needShowTime = currentMessageTime - preMessageTime >= showTimeDuration;
                        }
                    }
                }
            }
        }

        return needShowTime;
    }

    protected void updateMessageTimeView(TextView messageTimeView, DataObject<MSIMMessage> dataObject) {
        if (messageTimeView == null) {
            MSIMUikitLog.v("updateMessageTimeView ignore null messageTimeView");
            return;
        }
        final boolean needShowTime = needShowTime(dataObject);
        if (!needShowTime) {
            ViewUtil.setVisibilityIfChanged(messageTimeView, View.GONE);
            return;
        }

        long currentMessageTime = -1;
        if (dataObject != null && dataObject.object != null) {
            currentMessageTime = dataObject.object.getTimeMs();
        }
        if (currentMessageTime <= 0) {
            MSIMUikitLog.v("invalid current message time: %s", currentMessageTime);
            ViewUtil.setVisibilityIfChanged(messageTimeView, View.GONE);
            return;
        }

        messageTimeView.setText(formatTime(currentMessageTime));
        ViewUtil.setVisibilityIfChanged(messageTimeView, View.VISIBLE);
    }

    protected String formatTime(long time) {
        return FormatUtil.getHumanTimeDistance(time, new FormatUtil.DefaultDateFormatOptions());
    }

    public static class Helper {

        /**
         * 竖向默认消息模式
         */
        @Nullable
        public static UnionTypeItemObject createDefault(DataObject<MSIMMessage> dataObject, long sessionUserId) {
            // 区分消息是收到的还是发送的
            final boolean received = dataObject.object.isReceived();
            final int messageType = dataObject.object.getMessageType();

            // 已撤回的消息
            if (messageType == MSIMConstants.MessageType.REVOKED) {
                return received
                        ? new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_RECEIVED,
                        dataObject)
                        : new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_SEND,
                        dataObject);
            }

            // 文本消息
            if (messageType == MSIMConstants.MessageType.TEXT) {
                return received
                        ? new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_TEXT_RECEIVED,
                        dataObject)
                        : new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_TEXT_SEND,
                        dataObject);
            }

            // 图片消息
            if (messageType == MSIMConstants.MessageType.IMAGE) {
                return received ? new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_RECEIVED,
                        dataObject)
                        : new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_SEND,
                        dataObject);
            }

            // 语音消息
            if (messageType == MSIMConstants.MessageType.AUDIO) {
                return received ? new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VOICE_RECEIVED,
                        dataObject)
                        : new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VOICE_SEND,
                        dataObject);
            }

            // 视频消息
            if (messageType == MSIMConstants.MessageType.VIDEO) {
                return received ? new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_RECEIVED,
                        dataObject)
                        : new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_SEND,
                        dataObject);
            }

            // 自定义消息
            if (MSIMConstants.MessageType.isCustomMessage(messageType)) {
                return received ? new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_RECEIVED,
                        dataObject)
                        : new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_SEND,
                        dataObject);
            }

            // fallback
            return received ? new UnionTypeItemObject(
                    IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_RECEIVED,
                    dataObject)
                    : new UnionTypeItemObject(
                    IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_SEND,
                    dataObject);
        }

        /**
         * 横向全屏预览模式
         */
        @Nullable
        public static UnionTypeItemObject createPreviewDefault(DataObject<MSIMMessage> dataObject, long sessionUserId) {
            // 区分消息是收到的还是发送的
            final boolean received = dataObject.object.isReceived();
            final long messageType = dataObject.object.getMessageType();

            // 视频消息
            if (messageType == MSIMConstants.MessageType.VIDEO) {
                return new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO,
                        dataObject);
            }

            // 图片消息
            if (messageType == MSIMConstants.MessageType.IMAGE) {
                return new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE,
                        dataObject);
            }

            MSIMUikitLog.e("createPreviewDefault unknown message type %s", dataObject.object);
            return null;
        }

        @Nullable
        private static HolderFinder getHolderFinder(@NonNull UnionTypeViewHolder holder) {
            clearHolderFinderTag(holder);

            int position = holder.getAdapterPosition();
            if (position < 0) {
                MSIMUikitLog.e("invalid position %s", position);
                return null;
            }
            UnionTypeItemObject itemObject = holder.host.getAdapter().getItem(position);
            if (itemObject == null) {
                MSIMUikitLog.e("item object is null");
                return null;
            }
            if (!(itemObject.itemObject instanceof DataObject)) {
                MSIMUikitLog.e("item object is not data object");
                return null;
            }
            final DataObject<?> dataObject = (DataObject<?>) itemObject.itemObject;
            if (!(dataObject.object instanceof MSIMMessage)) {
                MSIMUikitLog.e("item object's data object's object is not MSIMMessage");
                return null;
            }

            final MSIMMessage message = (MSIMMessage) dataObject.object;
            Activity innerActivity = holder.host.getActivity();
            if (innerActivity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return null;
            }
            if (innerActivity != TopActivity.getInstance().getResumed()) {
                MSIMUikitLog.e("activity is not the top activity");
                return null;
            }
            Lifecycle lifecycle = null;
            Fragment fragment = holder.host.getFragment();
            if (fragment != null) {
                lifecycle = fragment.getLifecycle();
            } else {
                if (innerActivity instanceof AppCompatActivity) {
                    lifecycle = ((AppCompatActivity) innerActivity).getLifecycle();
                }
            }
            if (lifecycle == null) {
                MSIMUikitLog.e("lifecycle is null");
                return null;
            }

            // 区分消息是收到的还是发送的
            final boolean received = message.isReceived();
            HolderFinder holderFinder = new HolderFinder();
            holderFinder.holder = holder;
            holderFinder.position = position;
            holderFinder.itemObject = itemObject;
            holderFinder.dataObject = dataObject;
            holderFinder.message = message;
            holderFinder.innerActivity = innerActivity;
            holderFinder.lifecycle = lifecycle;
            holderFinder.received = received;
            return holderFinder;
        }

        private interface OnHolderFinderRefreshCallback {
            void onHolderFinderRefresh(@Nullable HolderFinder holderFinder);
        }

        private static void refreshHolderFinderAsync(@NonNull final HolderFinder input, @NonNull OnHolderFinderRefreshCallback callback) {
            final Object tag = new Object();
            setHolderFinderTag(input.holder, tag);
            Threads.postBackground(() -> {
                if (isHolderFinderTagChanged(input.holder, tag)) {
                    MSIMUikitLog.v("ignore. holder finder tag changed");
                    return;
                }
                final MSIMMessage message = MSIMManager.getInstance().getMessageManager().getMessage(
                        input.message.getSessionUserId(),
                        input.message.getConversationType(),
                        input.message.getTargetUserId(),
                        input.message.getMessageId()
                );
                if (isHolderFinderTagChanged(input.holder, tag)) {
                    MSIMUikitLog.v("ignore. holder finder tag changed");
                    return;
                }
                Threads.runOnUi(() -> {
                    if (isHolderFinderTagChanged(input.holder, tag)) {
                        MSIMUikitLog.v("ignore. holder finder tag changed");
                        return;
                    }
                    if (message == null) {
                        callback.onHolderFinderRefresh(null);
                    } else {
                        input.message = message;
                        callback.onHolderFinderRefresh(input);
                    }
                });
            });
        }

        public static class HolderFinder {
            public UnionTypeViewHolder holder;
            public int position;
            public UnionTypeItemObject itemObject;
            public DataObject<?> dataObject;
            public MSIMMessage message;
            public Activity innerActivity;
            public Lifecycle lifecycle;

            // 区分消息是收到的还是发送的
            public boolean received;
        }

        public static void showPreview(UnionTypeViewHolder holder, long targetUserId) {
            final HolderFinder holderFinder = getHolderFinder(holder);
            if (holderFinder == null) {
                MSIMUikitLog.e("showPreview holderFinder is null");
                return;
            }

            final long messageType = holderFinder.message.getMessageType();
            if (messageType == MSIMConstants.MessageType.IMAGE
                    || messageType == MSIMConstants.MessageType.VIDEO) {
                // 图片或者视频
                new IMImageOrVideoPreviewDialog(
                        holderFinder.lifecycle,
                        holderFinder.innerActivity,
                        holderFinder.innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                        holderFinder.message,
                        targetUserId
                ).show();
                return;
            }

            MSIMUikitLog.e("showPreview other message type %s", holderFinder.message);
        }

        private static void clearHolderFinderTag(UnionTypeViewHolder holder) {
            setHolderFinderTag(holder, new Object());
        }

        private static void setHolderFinderTag(UnionTypeViewHolder holder, Object tag) {
            holder.itemView.setTag(R.id.imsdk_uikit_holder_finder_tag, tag);
        }

        private static boolean isHolderFinderTagChanged(UnionTypeViewHolder holder, Object tag) {
            return holder.itemView.getTag(R.id.imsdk_uikit_holder_finder_tag) != tag;
        }

        public static void showMenu(UnionTypeViewHolder holder) {
            final HolderFinder holderFinder = getHolderFinder(holder);
            if (holderFinder == null) {
                MSIMUikitLog.e("holder finder is null");
                return;
            }
            refreshHolderFinderAsync(holderFinder, refreshHolderFinder -> {
                if (refreshHolderFinder == null) {
                    MSIMUikitLog.e("refreshHolderFinderAsync refreshHolderFinder is null");
                    return;
                }
                if (showMenuInternal(refreshHolderFinder)) {
                    ViewUtil.requestParentDisallowInterceptTouchEvent(refreshHolderFinder.holder.itemView);
                }
            });
        }

        private static boolean showMenuInternal(@NonNull final HolderFinder holderFinder) {
            final long messageType = holderFinder.message.getMessageType();
            final int MENU_ID_COPY = 1;
            final int MENU_ID_RECALL = 2;
            if (messageType == MSIMConstants.MessageType.TEXT) {
                // 文字
                View anchorView = holderFinder.holder.itemView.findViewById(R.id.message_text);
                if (anchorView == null) {
                    MSIMUikitLog.v("showMenu MessageType.TEXT R.id.message_text not found");
                    return false;
                }

                if (anchorView.getWidth() <= 0 || anchorView.getHeight() <= 0) {
                    MSIMUikitLog.v("showMenu anchor view not layout");
                    return false;
                }

                final List<String> menuList = new ArrayList<>();
                final List<Integer> menuIdList = new ArrayList<>();

                menuList.add(I18nResources.getString(R.string.imsdk_uikit_menu_copy));
                menuIdList.add(MENU_ID_COPY);
                if (!holderFinder.received) {
                    if (holderFinder.message.getSendStatus(MSIMConstants.SendStatus.SUCCESS) == MSIMConstants.SendStatus.SUCCESS) {
                        menuList.add(I18nResources.getString(R.string.imsdk_uikit_menu_recall));
                        menuIdList.add(MENU_ID_RECALL);
                    }
                }

                final IMChatMessageMenuDialog menuDialog = new IMChatMessageMenuDialog(
                        holderFinder.innerActivity,
                        holderFinder.innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                        anchorView,
                        0,
                        menuList,
                        menuIdList);
                menuDialog.setOnIMMenuClickListener((menuId, menuText, menuView) -> {
                    if (menuId == MENU_ID_COPY) {
                        // 复制
                        ClipboardUtil.copy(holderFinder.message.getTextElement().getText());
                    } else if (menuId == MENU_ID_RECALL) {
                        // 撤回
                        revoke(holderFinder.holder);
                    } else {
                        MSIMUikitLog.e("IMChatMessageMenuDialog onItemMenuClick invalid menuId:%s, menuText:%s, menuView:%s",
                                menuId, menuText, menuView);
                    }
                });
                menuDialog.show();
                return true;
            }
            if (messageType == MSIMConstants.MessageType.IMAGE) {
                // 图片
                View anchorView = holderFinder.holder.itemView.findViewById(R.id.resize_image_view);
                if (anchorView == null) {
                    MSIMUikitLog.v("showMenu MessageType.IMAGE R.id.resize_image_view not found");
                    return false;
                }

                if (anchorView.getWidth() <= 0 || anchorView.getHeight() <= 0) {
                    MSIMUikitLog.v("showMenu anchor view not layout");
                    return false;
                }

                final List<String> menuList = new ArrayList<>();
                final List<Integer> menuIdList = new ArrayList<>();

                if (!holderFinder.received) {
                    if (holderFinder.message.getSendStatus(MSIMConstants.SendStatus.SUCCESS) == MSIMConstants.SendStatus.SUCCESS) {
                        menuList.add(I18nResources.getString(R.string.imsdk_uikit_menu_recall));
                        menuIdList.add(MENU_ID_RECALL);
                    }
                }

                if (menuList.size() <= 0) {
                    return false;
                }
                IMChatMessageMenuDialog menuDialog = new IMChatMessageMenuDialog(
                        holderFinder.innerActivity,
                        holderFinder.innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                        anchorView,
                        0,
                        menuList,
                        menuIdList);
                menuDialog.setOnIMMenuClickListener((menuId, menuText, menuView) -> {
                    if (menuId == MENU_ID_RECALL) {
                        // 撤回
                        revoke(holderFinder.holder);
                    } else {
                        MSIMUikitLog.e("showMenu onItemMenuClick invalid menuId:%s, menuText:%s, menuView:%s",
                                menuId, menuText, menuView);
                    }
                });
                menuDialog.show();
                return true;
            }

            MSIMUikitLog.e("imMessage type is unknown %s", holderFinder.message);
            return false;
        }

        /**
         * 撤回
         */
        private static void revoke(UnionTypeViewHolder holder) {
            final HolderFinder holderFinder = getHolderFinder(holder);
            if (holderFinder == null) {
                MSIMUikitLog.e("revoke getHolderFinder return null");
                return;
            }
            final MSIMMessage message = holderFinder.message;
            MSIMManager.getInstance().getMessageManager().revoke(
                    message.getSessionUserId(),
                    message
            );
        }

    }

}
