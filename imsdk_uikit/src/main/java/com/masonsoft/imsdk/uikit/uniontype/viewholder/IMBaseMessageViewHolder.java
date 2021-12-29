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
import androidx.core.util.Predicate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.masonsoft.imsdk.MSIMBaseMessage;
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
import com.masonsoft.imsdk.uikit.widget.MSIMBaseMessageRevokeTextView;
import com.masonsoft.imsdk.uikit.widget.MSIMBaseMessageRevokeStateFrameLayout;
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
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public abstract class IMBaseMessageViewHolder extends UnionTypeViewHolder {

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
    private final MSIMBaseMessageRevokeStateFrameLayout mMessageRevokeStateFrameLayout;
    @Nullable
    private final MSIMBaseMessageRevokeTextView mMessageRevokeTextView;

    @Nullable
    private final TextView mMessageTime;

    public IMBaseMessageViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mMessageRevokeStateFrameLayout = itemView.findViewById(R.id.message_revoke_state_layout);
        mMessageRevokeTextView = itemView.findViewById(R.id.message_revoke_text_view);

        mMessageTime = itemView.findViewById(R.id.message_time);
    }

    public IMBaseMessageViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mMessageRevokeStateFrameLayout = itemView.findViewById(R.id.message_revoke_state_layout);
        mMessageRevokeTextView = itemView.findViewById(R.id.message_revoke_text_view);

        mMessageTime = itemView.findViewById(R.id.message_time);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = (MSIMBaseMessage) itemObject.object;

        if (mMessageRevokeStateFrameLayout != null) {
            mMessageRevokeStateFrameLayout.setBaseMessage(baseMessage);
        }
        if (mMessageRevokeTextView != null) {
            mMessageRevokeTextView.setTargetUserId(baseMessage.getFromUserId());
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

    protected boolean needShowTime(DataObject dataObject) {
        final long showTimeDuration = getShowTimeDuration();
        if (showTimeDuration <= 0) {
            return true;
        }

        boolean needShowTime = true;
        if (dataObject != null) {
            if (dataObject.object != null) {
                final MSIMBaseMessage baseMessage = (MSIMBaseMessage) dataObject.object;
                final long currentMessageTime = baseMessage.getTimeMs();
                if (currentMessageTime <= 0) {
                    Throwable e = new IllegalArgumentException("invalid timeMs " + dataObject.object);
                    MSIMUikitLog.e(e);
                }

                int position = getAdapterPosition();
                if (position > 0) {
                    UnionTypeItemObject preObject = host.getAdapter().getItem(position - 1);
                    if (preObject != null) {
                        if (preObject.itemObject instanceof DataObject
                                && ((DataObject) preObject.itemObject).object instanceof MSIMBaseMessage) {
                            MSIMBaseMessage preMessage = (MSIMBaseMessage) ((DataObject) preObject.itemObject).object;
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

    protected void updateMessageTimeView(TextView messageTimeView, DataObject dataObject) {
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
            if (dataObject.object instanceof MSIMBaseMessage) {
                currentMessageTime = ((MSIMBaseMessage) dataObject.object).getTimeMs();
            }
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
        public static UnionTypeItemObject createDefault(DataObject dataObject) {
            // 区分消息是收到的还是发送的
            final MSIMBaseMessage baseMessage = dataObject.getObject();
            final boolean received = baseMessage.isReceived();
            final int messageType = baseMessage.getMessageType();

            // 已撤回的消息
            if (messageType == MSIMConstants.MessageType.REVOKED) {
                return received
                        ? UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_RECEIVED,
                        dataObject)
                        : UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_SEND,
                        dataObject);
            }

            // 文本消息
            if (messageType == MSIMConstants.MessageType.TEXT) {
                return received
                        ? UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_TEXT_RECEIVED,
                        dataObject)
                        : UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_TEXT_SEND,
                        dataObject);
            }

            // 图片消息
            if (messageType == MSIMConstants.MessageType.IMAGE) {
                return received ? UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_RECEIVED,
                        dataObject)
                        : UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_SEND,
                        dataObject);
            }

            // 语音消息
            if (messageType == MSIMConstants.MessageType.AUDIO) {
                return received ? UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VOICE_RECEIVED,
                        dataObject)
                        : UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VOICE_SEND,
                        dataObject);
            }

            // 视频消息
            if (messageType == MSIMConstants.MessageType.VIDEO) {
                return received ? UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_RECEIVED,
                        dataObject)
                        : UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_SEND,
                        dataObject);
            }

            // 自定义消息
            if (MSIMConstants.MessageType.isCustomMessage(messageType)) {
                return received ? UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_RECEIVED,
                        dataObject)
                        : UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_SEND,
                        dataObject);
            }

            // fallback
            return received ? UnionTypeItemObject.valueOf(
                    IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_RECEIVED,
                    dataObject)
                    : UnionTypeItemObject.valueOf(
                    IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_SEND,
                    dataObject);
        }

        /**
         * 横向全屏预览模式
         */
        @Nullable
        public static UnionTypeItemObject createPreviewDefault(DataObject dataObject) {
            final MSIMBaseMessage baseMessage = dataObject.getObject();
            // 区分消息是收到的还是发送的
            final boolean received = baseMessage.isReceived();
            final long messageType = baseMessage.getMessageType();

            // 视频消息
            if (messageType == MSIMConstants.MessageType.VIDEO) {
                return UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO,
                        dataObject);
            }

            // 图片消息
            if (messageType == MSIMConstants.MessageType.IMAGE) {
                return UnionTypeItemObject.valueOf(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE,
                        dataObject);
            }

            MSIMUikitLog.e("createPreviewDefault unknown message type %s", dataObject.object);
            return null;
        }

        @Nullable
        private static HolderFinder getHolderFinder(@NonNull UnionTypeViewHolder holder, @Nullable Predicate<MSIMBaseMessage> selector /*向前和向后选择的过滤器*/) {
            clearHolderFinderTag(holder);

            int position = holder.getAdapterPosition();
            if (position < 0) {
                MSIMUikitLog.e("invalid position %s", position);
                return null;
            }

            final UnionTypeAdapter adapter = holder.host.getAdapter();
            UnionTypeItemObject itemObject = adapter.getItem(position);
            if (itemObject == null) {
                MSIMUikitLog.e("item object is null");
                return null;
            }
            if (!(itemObject.itemObject instanceof DataObject)) {
                MSIMUikitLog.e("item object is not data object");
                return null;
            }
            final DataObject dataObject = (DataObject) itemObject.itemObject;
            if (!(dataObject.object instanceof MSIMBaseMessage)) {
                MSIMUikitLog.e("item object's data object's object is not MSIMMessage");
                return null;
            }

            final MSIMBaseMessage baseMessage = (MSIMBaseMessage) dataObject.object;
            int count = adapter.getItemCount();
            final int maxOffset = 10;
            // 向前选择 [position - maxOffset, position)
            final List<MSIMBaseMessage> preMessageList = new ArrayList<>();
            if (selector != null) {
                for (int i = position - maxOffset; i < position; i++) {
                    if (i < 0) {
                        continue;
                    }
                    UnionTypeItemObject object = adapter.getItem(i);
                    if (object == null) {
                        continue;
                    }
                    if (!(object.itemObject instanceof DataObject)) {
                        continue;
                    }
                    final DataObject obj = (DataObject) object.itemObject;
                    if (!(obj.object instanceof MSIMBaseMessage)) {
                        continue;
                    }
                    final MSIMBaseMessage m = (MSIMBaseMessage) obj.object;
                    if (selector.test(m)) {
                        preMessageList.add(m);
                    }
                }
            }
            // 向后选择 (position, position + maxOffset]
            final List<MSIMBaseMessage> nextMessageList = new ArrayList<>();
            if (selector != null) {
                for (int i = position + 1; i <= position + maxOffset; i++) {
                    if (i >= count) {
                        break;
                    }
                    UnionTypeItemObject object = adapter.getItem(i);
                    if (object == null) {
                        continue;
                    }
                    if (!(object.itemObject instanceof DataObject)) {
                        continue;
                    }
                    final DataObject obj = (DataObject) object.itemObject;
                    if (!(obj.object instanceof MSIMBaseMessage)) {
                        continue;
                    }
                    final MSIMBaseMessage m = (MSIMBaseMessage) obj.object;
                    if (selector.test(m)) {
                        nextMessageList.add(m);
                    }
                }
            }

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
            final boolean received = baseMessage.isReceived();
            HolderFinder holderFinder = new HolderFinder();
            holderFinder.holder = holder;
            holderFinder.position = position;
            holderFinder.itemObject = itemObject;
            holderFinder.dataObject = dataObject;
            holderFinder.baseMessage = baseMessage;
            holderFinder.innerActivity = innerActivity;
            holderFinder.lifecycle = lifecycle;
            holderFinder.received = received;
            holderFinder.preMessageList = preMessageList;
            holderFinder.nextMessageList = nextMessageList;
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

                final MSIMBaseMessage baseMessage;
                if (input.baseMessage instanceof MSIMMessage) {
                    // MSIMMessage 从数据库中读取最新的值
                    final MSIMMessage message = (MSIMMessage) input.baseMessage;
                    baseMessage = MSIMManager.getInstance().getMessageManager().getMessage(
                            message.getSessionUserId(),
                            message.getConversationType(),
                            message.getTargetUserId(),
                            message.getMessageId()
                    );
                } else {
                    // 使用当前值
                    baseMessage = input.baseMessage;
                }

                if (isHolderFinderTagChanged(input.holder, tag)) {
                    MSIMUikitLog.v("ignore. holder finder tag changed");
                    return;
                }
                Threads.runOnUi(() -> {
                    if (isHolderFinderTagChanged(input.holder, tag)) {
                        MSIMUikitLog.v("ignore. holder finder tag changed");
                        return;
                    }
                    if (baseMessage == null) {
                        callback.onHolderFinderRefresh(null);
                    } else {
                        input.baseMessage = baseMessage;
                        callback.onHolderFinderRefresh(input);
                    }
                });
            });
        }

        public static class HolderFinder {
            public UnionTypeViewHolder holder;
            public int position;
            public UnionTypeItemObject itemObject;
            public DataObject dataObject;
            public MSIMBaseMessage baseMessage;
            public List<MSIMBaseMessage> preMessageList;
            public List<MSIMBaseMessage> nextMessageList;
            public Activity innerActivity;
            public Lifecycle lifecycle;

            // 区分消息是收到的还是发送的
            public boolean received;
        }

        public static void showPreview(UnionTypeViewHolder holder) {
            final HolderFinder holderFinder = getHolderFinder(holder, msimBaseMessage -> {
                final long messageType = msimBaseMessage.getMessageType();
                return messageType == MSIMConstants.MessageType.IMAGE
                        || messageType == MSIMConstants.MessageType.VIDEO;
            });
            if (holderFinder == null) {
                MSIMUikitLog.e("showPreview holderFinder is null");
                return;
            }

            final long messageType = holderFinder.baseMessage.getMessageType();
            if (messageType == MSIMConstants.MessageType.IMAGE
                    || messageType == MSIMConstants.MessageType.VIDEO) {
                // 图片或者视频
                final int index = holderFinder.preMessageList.size();
                final List<MSIMBaseMessage> messageList = new ArrayList<>(holderFinder.preMessageList);
                messageList.add(holderFinder.baseMessage);
                messageList.addAll(holderFinder.nextMessageList);
                new IMImageOrVideoPreviewDialog(
                        holderFinder.lifecycle,
                        holderFinder.innerActivity,
                        holderFinder.innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                        messageList,
                        index
                ).show();
                return;
            }

            MSIMUikitLog.e("showPreview other message type %s", holderFinder.baseMessage);
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
            final HolderFinder holderFinder = getHolderFinder(holder, null);
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
            final long messageType = holderFinder.baseMessage.getMessageType();
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
                    if (holderFinder.baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS) == MSIMConstants.SendStatus.SUCCESS) {
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
                        ClipboardUtil.copy(holderFinder.baseMessage.getTextElement().getText());
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
                    if (holderFinder.baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS) == MSIMConstants.SendStatus.SUCCESS) {
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

            MSIMUikitLog.e("imMessage type is unknown %s", holderFinder.baseMessage);
            return false;
        }

        /**
         * 撤回
         */
        private static void revoke(UnionTypeViewHolder holder) {
            final HolderFinder holderFinder = getHolderFinder(holder, null);
            if (holderFinder == null) {
                MSIMUikitLog.e("revoke getHolderFinder return null");
                return;
            }
            final MSIMBaseMessage baseMessage = holderFinder.baseMessage;
            if (baseMessage instanceof MSIMMessage) {
                MSIMManager.getInstance().getMessageManager().revoke(
                        baseMessage.getSessionUserId(),
                        (MSIMMessage) baseMessage
                );
            } else {
                // TODO FIXME
                MSIMUikitLog.e("revoke MSIMBaseMessage not impl: %s", baseMessage);
            }
        }

    }

}
