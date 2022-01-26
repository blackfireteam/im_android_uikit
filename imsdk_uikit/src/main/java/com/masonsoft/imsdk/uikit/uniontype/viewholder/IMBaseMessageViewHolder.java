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
import androidx.core.util.Pair;
import androidx.core.util.Predicate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomInfo;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMLocationElement;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.common.TopActivity;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.impopup.IMBaseMessageMenuDialog;
import com.masonsoft.imsdk.uikit.common.impreview.IMImageOrVideoPreviewDialog;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.common.locationpreview.LocationPreviewDialog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ClipboardUtil;
import com.masonsoft.imsdk.uikit.util.FileDownloadHelper;
import com.masonsoft.imsdk.uikit.util.FormatUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
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
import io.github.idonans.uniontype.UnionTypeMapper;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public abstract class IMBaseMessageViewHolder extends MSIMSelfUpdateUnionTypeViewHolder {

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
    private final TextView mMessageTime;

    public IMBaseMessageViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mMessageTime = itemView.findViewById(R.id.message_time);
    }

    public IMBaseMessageViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mMessageTime = itemView.findViewById(R.id.message_time);
    }

    @Override
    protected int getBestUnionType(@NonNull DataObject dataObject) {
        return Helper.getDefaultUnionType(dataObject);
    }

    @Override
    public void onBindUpdate() {
        super.onBindUpdate();
        final DataObject dataObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(dataObject);
        final MSIMBaseMessage baseMessage = dataObject.getObject(MSIMBaseMessage.class);
        Preconditions.checkNotNull(baseMessage);

        if (mMessageTime != null) {
            updateMessageTimeView(mMessageTime);
        }
    }

    /**
     * 获取一个时间间隔 ms，当与上一条消息的时间超过此间隔时，表示需要显示时间. 如果返回的时间间隔不大于 0, 则表示总是显示时间
     */
    protected long getShowTimeDuration() {
        return TimeUnit.MINUTES.toMillis(5);
    }

    protected boolean needShowTime(@Nullable DataObject dataObject) {
        final long showTimeDuration = getShowTimeDuration();
        if (showTimeDuration <= 0) {
            return true;
        }

        boolean needShowTime;
        if (dataObject != null) {
            needShowTime = true;
            if (dataObject.object instanceof MSIMBaseMessage) {
                final MSIMBaseMessage baseMessage = (MSIMBaseMessage) dataObject.object;
                final long currentMessageTime = baseMessage.getTimeMs();
                if (currentMessageTime <= 0) {
                    Throwable e = new IllegalArgumentException("invalid timeMs " + dataObject.object);
                    MSIMUikitLog.e(e);
                }

                int position = getAdapterPosition();
                while (position > 0) {
                    // 找到前一个 MSIMBaseMessage
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
                            break;
                        }
                    }
                    position--;
                }
            } else {
                // 当前条目的内容不是 MSIMBaseMessage，不需要显示时间。例如：是一个 text tip message
                needShowTime = false;
            }
        } else {
            // 当前条目为 null, 不显示时间
            needShowTime = false;
        }

        return needShowTime;
    }

    protected void updateMessageTimeView(@Nullable TextView messageTimeView) {
        if (messageTimeView == null) {
            MSIMUikitLog.v("updateMessageTimeView ignore null messageTimeView");
            return;
        }

        final DataObject dataObject = getItemObject(DataObject.class);
        if (dataObject == null) {
            MSIMUikitLog.v("updateMessageTimeView current itemObject is not DataObject");
            ViewUtil.setVisibilityIfChanged(messageTimeView, View.GONE);
            return;
        }

        if (!(dataObject.object instanceof MSIMBaseMessage)) {
            MSIMUikitLog.v("updateMessageTimeView current dataObject's object is not MSIMBaseMessage");
            ViewUtil.setVisibilityIfChanged(messageTimeView, View.GONE);
            return;
        }

        final boolean needShowTime = needShowTime(dataObject);
        if (!needShowTime) {
            ViewUtil.setVisibilityIfChanged(messageTimeView, View.GONE);
            return;
        }

        long currentMessageTime = ((MSIMBaseMessage) dataObject.object).getTimeMs();
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
         *
         * @see io.github.idonans.uniontype.UnionTypeMapper#UNION_TYPE_NULL
         */
        public static int getDefaultUnionType(DataObject dataObject) {
            if (dataObject == null) {
                return UnionTypeMapper.UNION_TYPE_NULL;
            }

            final MSIMBaseMessage baseMessage = dataObject.getObject(MSIMBaseMessage.class);
            if (baseMessage == null) {
                return UnionTypeMapper.UNION_TYPE_NULL;
            }

            final boolean received = baseMessage.isReceived();
            final int messageType = baseMessage.getMessageType();

            // 不可见消息
            if (!MSIMConstants.MessageType.isVisibleMessage(messageType)) {
                return UnionTypeMapper.UNION_TYPE_NULL;
            }

            // 已撤回的消息
            if (messageType == MSIMConstants.MessageType.REVOKED) {
                return received
                        ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_RECEIVED
                        : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_REVOKE_SEND;
            }

            // 文本消息
            if (messageType == MSIMConstants.MessageType.TEXT) {
                return received
                        ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_TEXT_RECEIVED
                        : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_TEXT_SEND;
            }

            // 图片消息
            if (messageType == MSIMConstants.MessageType.IMAGE) {
                return received
                        ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_RECEIVED
                        : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_IMAGE_SEND;
            }

            // 语音消息
            if (messageType == MSIMConstants.MessageType.AUDIO) {
                return received
                        ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VOICE_RECEIVED
                        : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VOICE_SEND;
            }

            // 视频消息
            if (messageType == MSIMConstants.MessageType.VIDEO) {
                return received
                        ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_RECEIVED
                        : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_VIDEO_SEND;
            }

            // 位置消息
            if (messageType == MSIMConstants.MessageType.LOCATION) {
                return received
                        ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_RECEIVED
                        : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_LOCATION_SEND;
            }

            // 自定义消息
            if (MSIMConstants.MessageType.isCustomMessage(messageType)) {
                return received
                        ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_RECEIVED
                        : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_FIRST_CUSTOM_MESSAGE_SEND;
            }

            // fallback
            return received
                    ? IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_RECEIVED
                    : IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_DEFAULT_SEND;
        }

        /**
         * 横向全屏预览模式
         *
         * @see io.github.idonans.uniontype.UnionTypeMapper#UNION_TYPE_NULL
         */
        public static int getPreviewUnionType(DataObject dataObject) {
            if (dataObject == null) {
                return UnionTypeMapper.UNION_TYPE_NULL;
            }

            final MSIMBaseMessage baseMessage = dataObject.getObject(MSIMBaseMessage.class);
            if (baseMessage == null) {
                return UnionTypeMapper.UNION_TYPE_NULL;
            }

            final int messageType = baseMessage.getMessageType();

            // 视频消息
            if (messageType == MSIMConstants.MessageType.VIDEO) {
                return IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_VIDEO;
            }

            // 图片消息
            if (messageType == MSIMConstants.MessageType.IMAGE) {
                return IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_PREVIEW_IMAGE;
            }

            MSIMUikitLog.e("getPreviewUnionType unexpected message type:%s", messageType);
            return UnionTypeMapper.UNION_TYPE_NULL;
        }

        @Nullable
        private static HolderFinder getHolderFinder(@NonNull UnionTypeViewHolder holder, @Nullable Predicate<Pair<MSIMBaseMessage, MSIMBaseMessage>> selector /*向前和向后选择的过滤器*/) {
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
            if (itemObject != holder.unionTypeItemObject) {
                MSIMUikitLog.e("unexpected. adapter get item result is not same as holder.unionTypeItemObject");
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
                    if (selector.test(Pair.create(baseMessage, m))) {
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
                    if (selector.test(Pair.create(baseMessage, m))) {
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

        private static boolean isSamePreviewGroupMessageType(int messageType1, int messageType2) {
            if (messageType1 == MSIMConstants.MessageType.IMAGE ||
                    messageType1 == MSIMConstants.MessageType.VIDEO) {
                return messageType2 == MSIMConstants.MessageType.IMAGE
                        || messageType2 == MSIMConstants.MessageType.VIDEO;
            }

            return false;
        }

        public static void showPreview(UnionTypeViewHolder holder) {
            final HolderFinder holderFinder = getHolderFinder(holder, pair ->
                    isSamePreviewGroupMessageType(
                            pair.first.getMessageType(),
                            pair.second.getMessageType()
                    ));
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

            if (messageType == MSIMConstants.MessageType.LOCATION) {
                // 位置
                final MSIMLocationElement element = holderFinder.baseMessage.getLocationElement();
                Preconditions.checkNotNull(element);
                if (!(holderFinder.innerActivity instanceof AppCompatActivity)) {
                    MSIMUikitLog.e("unexpected. inner activity is not AppCompatActivity");
                    return;
                }

                final LocationInfo locationInfo = new LocationInfo();
                locationInfo.lat = element.getLat();
                locationInfo.lng = element.getLng();
                locationInfo.title = element.getTitle();
                locationInfo.subTitle = element.getSubTitle();
                new LocationPreviewDialog(
                        (AppCompatActivity) holderFinder.innerActivity,
                        holderFinder.innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                        locationInfo,
                        (int) element.getZoom()
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
            final long serverMessageId = holderFinder.baseMessage.getServerMessageId();

            // 复制
            final int menuIdCopy = 0;
            // 撤回
            final int menuIdRecall = 1;
            // 删除
            final int menuIdDelete = 2;
            final String[] menuArray = {
                    I18nResources.getString(R.string.imsdk_uikit_menu_copy),
                    I18nResources.getString(R.string.imsdk_uikit_menu_recall),
                    I18nResources.getString(R.string.imsdk_uikit_menu_delete),
            };

            // 是否有删除消息的权限
            boolean canDeleteMessage = false;
            if (holderFinder.baseMessage instanceof MSIMChatRoomMessage) {
                final MSIMChatRoomContext chatRoomContext = ((MSIMChatRoomMessage) holderFinder.baseMessage).getChatRoomContext();
                final MSIMChatRoomInfo chatRoomInfo = chatRoomContext.getChatRoomInfo();
                if (chatRoomInfo != null) {
                    canDeleteMessage = chatRoomInfo.hasActionDeleteMessage();
                }
            }

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

                menuList.add(menuArray[menuIdCopy]);
                menuIdList.add(menuIdCopy);
                if (!holderFinder.received) {
                    if (holderFinder.baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS) == MSIMConstants.SendStatus.SUCCESS) {
                        menuList.add(menuArray[menuIdRecall]);
                        menuIdList.add(menuIdRecall);
                    }
                }

                if (canDeleteMessage && serverMessageId > 0) {
                    menuList.add(menuArray[menuIdDelete]);
                    menuIdList.add(menuIdDelete);
                }

                final IMBaseMessageMenuDialog menuDialog = new IMBaseMessageMenuDialog(
                        holderFinder.innerActivity,
                        holderFinder.innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                        anchorView,
                        0,
                        menuList,
                        menuIdList);
                menuDialog.setOnIMMenuClickListener((menuId, menuText, menuView) -> {
                    if (menuId == menuIdCopy) {
                        // 复制
                        ClipboardUtil.copy(holderFinder.baseMessage.getTextElement().getText());
                    } else if (menuId == menuIdRecall) {
                        // 撤回
                        revoke(holderFinder.holder);
                    } else if (menuId == menuIdDelete) {
                        // 删除
                        delete(holderFinder.holder);
                    } else {
                        MSIMUikitLog.e("IMBaseMessageMenuDialog onItemMenuClick invalid menuId:%s, menuText:%s, menuView:%s",
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
                        menuList.add(menuArray[menuIdRecall]);
                        menuIdList.add(menuIdRecall);
                    }
                }

                if (canDeleteMessage && serverMessageId > 0) {
                    menuList.add(menuArray[menuIdDelete]);
                    menuIdList.add(menuIdDelete);
                }

                if (menuList.size() <= 0) {
                    return false;
                }
                final IMBaseMessageMenuDialog menuDialog = new IMBaseMessageMenuDialog(
                        holderFinder.innerActivity,
                        holderFinder.innerActivity.findViewById(Window.ID_ANDROID_CONTENT),
                        anchorView,
                        0,
                        menuList,
                        menuIdList);
                menuDialog.setOnIMMenuClickListener((menuId, menuText, menuView) -> {
                    if (menuId == menuIdRecall) {
                        // 撤回
                        revoke(holderFinder.holder);
                    } else if (menuId == menuIdDelete) {
                        // 删除
                        delete(holderFinder.holder);
                    } else {
                        MSIMUikitLog.e("IMBaseMessageMenuDialog onItemMenuClick invalid menuId:%s, menuText:%s, menuView:%s",
                                menuId, menuText, menuView);
                    }
                });
                menuDialog.show();
                return true;
            }

            MSIMUikitLog.e("showMenuInternal message type is unknown %s", holderFinder.baseMessage);
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
                final MSIMMessage message = (MSIMMessage) baseMessage;
                MSIMManager.getInstance().getMessageManager().revoke(
                        message.getSessionUserId(),
                        message
                );
            } else if (baseMessage instanceof MSIMChatRoomMessage) {
                final MSIMChatRoomMessage message = (MSIMChatRoomMessage) baseMessage;
                message.getChatRoomContext().getChatRoomManager().revokeMessage(
                        message.getSessionUserId(),
                        message
                );
            } else {
                MSIMUikitLog.e("revoke MSIMBaseMessage not impl: %s", baseMessage);
            }
        }

        /**
         * 删除
         */
        private static void delete(UnionTypeViewHolder holder) {
            final HolderFinder holderFinder = getHolderFinder(holder, null);
            if (holderFinder == null) {
                MSIMUikitLog.e("delete getHolderFinder return null");
                return;
            }
            final MSIMBaseMessage baseMessage = holderFinder.baseMessage;
            if (baseMessage instanceof MSIMChatRoomMessage) {
                final MSIMChatRoomMessage message = (MSIMChatRoomMessage) baseMessage;
                final long serverMessageId = message.getServerMessageId();
                if (serverMessageId > 0) {
                    message.getChatRoomContext().getChatRoomManager().deleteMessages(
                            message.getSessionUserId(),
                            Lists.newArrayList(serverMessageId)
                    );
                } else {
                    MSIMUikitLog.e("delete MSIMChatRoomMessage's server message id is invalid:%s", serverMessageId);
                }
            } else {
                MSIMUikitLog.e("delete MSIMBaseMessage is not MSIMChatRoomMessage");
            }
        }

    }

}
