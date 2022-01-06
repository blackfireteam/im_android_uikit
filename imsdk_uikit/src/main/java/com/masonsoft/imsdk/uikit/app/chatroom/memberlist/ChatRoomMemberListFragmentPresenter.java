package com.masonsoft.imsdk.uikit.app.chatroom.memberlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomInfo;
import com.masonsoft.imsdk.MSIMChatRoomMember;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMSessionListener;
import com.masonsoft.imsdk.MSIMSessionListenerProxy;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.widget.MSIMChatRoomStateChangedViewHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.dynamic.DynamicPresenter;
import io.github.idonans.uniontype.DeepDiff;
import io.github.idonans.uniontype.UnionTypeItemObject;

public class ChatRoomMemberListFragmentPresenter extends DynamicPresenter<ChatRoomMemberListFragment.ViewImpl> {

    private static final boolean DEBUG = true;

    private long mSessionUserId;
    @SuppressWarnings("FieldCanBeLocal")
    private long mChatRoomId;

    @Nullable
    private GlobalChatRoomManager.StaticChatRoomContext mChatRoomContext;
    private final MSIMChatRoomStateChangedViewHelper mChatRoomStateChangedViewHelper = new MSIMChatRoomStateChangedViewHelper() {
        @Override
        protected void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject) {
            MSIMChatRoomContext currentChatRoomContext = null;
            if (mChatRoomContext != null) {
                currentChatRoomContext = mChatRoomContext.getChatRoomContext();
            }

            if (currentChatRoomContext == chatRoomContext) {
                notifyChatRoomStateChanged();
            }
        }
    };
    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMSessionListener mSessionListener = new MSIMSessionListenerProxy(new MSIMSessionListener() {
        @Override
        public void onSessionChanged() {
        }

        @Override
        public void onSessionUserIdChanged() {
            init();
        }
    }, true);
    private final GlobalChatRoomManager.StaticChatRoomContext.OnStaticChatRoomContextChangedListener mOnStaticChatRoomContextChangedListener = new GlobalChatRoomManager.StaticChatRoomContext.OnStaticChatRoomContextChangedListener() {
        @Override
        public void onStaticChatRoomContextChanged(@NonNull GlobalChatRoomManager.StaticChatRoomContext context) {
            if (mChatRoomContext == context) {
                notifyChatRoomStateChanged();
            }
        }
    };

    private final BatchQueue<Object> mChatRoomStateChangedBatchQueue = new BatchQueue<>(false);

    @UiThread
    public ChatRoomMemberListFragmentPresenter(@NonNull ChatRoomMemberListFragment.ViewImpl view) {
        super(view);
        MSIMManager.getInstance().addSessionListener(mSessionListener);
        mChatRoomStateChangedBatchQueue.setConsumer(objects -> ChatRoomMemberListFragmentPresenter.this.onChatRoomStateChangedInternal());

        init();
    }

    private void init() {
        if (reInit()) {
            notifyChatRoomStateChanged();
        }
    }

    private boolean reInit() {
        final ChatRoomMemberListFragment.ViewImpl view = getView();
        if (view == null) {
            IMLog.v("abort reInit view is null");
            return false;
        }
        mChatRoomId = view.getChatRoomId();
        if (mChatRoomId <= 0) {
            IMLog.v("abort reInit chat room id is invalid:%s", mChatRoomId);
            return false;
        }
        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        if (mSessionUserId <= 0) {
            IMLog.v("abort reInit session user id is invalid:%s", mSessionUserId);
            return false;
        }
        mChatRoomContext = GlobalChatRoomManager.getInstance().getStaticChatRoomContext(mChatRoomId);
        if (mChatRoomContext == null) {
            IMLog.v("abort reInit chat room context is null");
            return false;
        }
        mChatRoomStateChangedViewHelper.setChatRoomContext(mChatRoomContext.getChatRoomContext());
        mChatRoomContext.addOnStaticChatRoomContextChangedListener(mOnStaticChatRoomContextChangedListener);
        return true;
    }

    private void notifyChatRoomStateChanged() {
        mChatRoomStateChangedBatchQueue.add(Boolean.TRUE);
    }

    @WorkerThread
    private void onChatRoomStateChangedInternal() {
        if (mChatRoomContext == null) {
            return;
        }

        final List<MSIMChatRoomMember> memberList;
        final MSIMChatRoomInfo chatRoomInfo = mChatRoomContext.getChatRoomContext().getChatRoomInfo();
        if (chatRoomInfo != null) {
            memberList = chatRoomInfo.getMembers();
        } else {
            memberList = new ArrayList<>();
        }

        Threads.postUi(() -> {
            final ChatRoomMemberListFragment.ViewImpl view = getView();
            if (view == null) {
                return;
            }
            if (mChatRoomContext == null) {
                return;
            }

            view.replaceMemberList(memberList, mChatRoomContext);
            view.onChatRoomStateChanged(mChatRoomContext, memberList.size());
        });
    }

    public long getChatRoomId() {
        return mChatRoomId;
    }

    @Nullable
    public GlobalChatRoomManager.StaticChatRoomContext getChatRoomContext() {
        return mChatRoomContext;
    }

    private final UnionTypeViewHolderListeners.OnItemClickListener mOnHolderItemClickListener = viewHolder -> {
        ChatRoomMemberListFragment.ViewImpl view = getView();
        if (view == null) {
            return;
        }

        // TODO
    };

    private final UnionTypeViewHolderListeners.OnItemLongClickListener mOnHolderItemLongClickListener = viewHolder -> {
        ChatRoomMemberListFragment.ViewImpl view = getView();
        if (view == null) {
            return;
        }

        if (mChatRoomContext == null) {
            MSIMUikitLog.e("unexpected. chat room context is null");
            return;
        }

        if (viewHolder.itemObject instanceof DataObject) {
            final Object object = ((DataObject) viewHolder.itemObject).object;
            if (object instanceof MSIMChatRoomMember) {
                view.showMenu(((MSIMChatRoomMember) object), mChatRoomContext);
            }
        }
    };

    @Nullable
    public UnionTypeItemObject createDefault(@Nullable MSIMChatRoomMember member) {
        if (member == null) {
            return null;
        }

        final DeepDiffDataObject dataObject = new DeepDiffDataObject(member);
        dataObject
                .putExtHolderItemClick1(mOnHolderItemClickListener)
                .putExtHolderItemLongClick1(mOnHolderItemLongClickListener);
        return UnionTypeItemObject.valueOf(
                IMUikitUnionTypeMapper.UNION_TYPE_IMPL_CHAT_ROOM_MEMBER,
                dataObject
        );
    }

    private static class DeepDiffDataObject extends DataObject implements DeepDiff {

        public DeepDiffDataObject(MSIMChatRoomMember member) {
            super(member);
        }

        @Override
        public boolean isSameItem(@Nullable Object other) {
            if (other instanceof DeepDiffDataObject) {
                final DeepDiffDataObject otherDataObject = (DeepDiffDataObject) other;
                if (this.object instanceof MSIMChatRoomMember
                        && otherDataObject.object instanceof MSIMChatRoomMember) {
                    final MSIMChatRoomMember thisMember = ((MSIMChatRoomMember) this.object);
                    final MSIMChatRoomMember otherMember = ((MSIMChatRoomMember) otherDataObject.object);
                    return Objects.equals(thisMember.getUserId(), otherMember.getUserId());
                }

                return Objects.equals(this.object, otherDataObject.object);
            }
            return false;
        }

        @Override
        public boolean isSameContent(@Nullable Object other) {
            if (other instanceof DeepDiffDataObject) {
                final DeepDiffDataObject otherDataObject = (DeepDiffDataObject) other;
                if (this.object instanceof MSIMChatRoomMember
                        && otherDataObject.object instanceof MSIMChatRoomMember) {

                    final MSIMChatRoomMember thisMember = ((MSIMChatRoomMember) this.object);
                    final MSIMChatRoomMember otherMember = ((MSIMChatRoomMember) otherDataObject.object);
                    return Objects.equals(thisMember.getUserId(), otherMember.getUserId())
                            && Objects.equals(thisMember.getRole(), otherMember.getRole())
                            && Objects.equals(thisMember.isMute(), otherMember.isMute());
                }

                return Objects.equals(this.object, otherDataObject.object);
            }
            return false;
        }
    }

}
