package com.masonsoft.imsdk.uikit.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import com.google.android.material.tabs.TabLayoutMediator;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationPickerDialog;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaPickerDialog;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotion2ViewHolderBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotion2ViewHolderItemViewHolderBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotionViewHolderBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotionViewHolderItemViewHolderBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitWidgetCustomSoftKeyboardLayerMoreViewHolderBinding;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.AssetUtil;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.lang.util.ViewUtil;

public class CustomSoftKeyboard extends FrameLayout {

    public CustomSoftKeyboard(@NonNull Context context) {
        super(context);
        initFromAttributes(context, null, 0, 0);
    }

    public CustomSoftKeyboard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs, 0, 0);
    }

    public CustomSoftKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomSoftKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private final DisposableHolder mPermissionRequest = new DisposableHolder();
    private ImsdkUikitWidgetCustomSoftKeyboardBinding mBinding;

    @NonNull
    private final Config mCustomConfig = new Config();
    @NonNull
    private final Config mSystemConfig = new Config();

    public class Config {
        // 音视频通话
        private boolean mShowRtc = true;
        // 位置
        private boolean mShowLocation = true;
        // 阅后即焚
        private boolean mShowSnapchat = true;

        @SuppressLint("NotifyDataSetChanged")
        public void setShowRtc(boolean showRtc) {
            if (mShowRtc != showRtc) {
                mShowRtc = showRtc;
                final RecyclerView.Adapter<?> adapter = mBinding.layerMorePager.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setShowLocation(boolean showLocation) {
            if (mShowLocation != showLocation) {
                mShowLocation = showLocation;
                final RecyclerView.Adapter<?> adapter = mBinding.layerMorePager.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setShowSnapchat(boolean showSnapchat) {
            if (mShowSnapchat != showSnapchat) {
                mShowSnapchat = showSnapchat;
                final RecyclerView.Adapter<?> adapter = mBinding.layerMorePager.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private static final String[] MEDIA_PICKER_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final String[] LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private void initFromAttributes(
            Context context,
            AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {

        mBinding = ImsdkUikitWidgetCustomSoftKeyboardBinding.inflate(
                LayoutInflater.from(context),
                this,
                true);

        initLayerEmoji();
        initLayerMore();

        showLayerEmoji();
    }

    public void showLayerEmoji() {
        ViewUtil.setVisibilityIfChanged(mBinding.layerEmoji, View.VISIBLE);
        ViewUtil.setVisibilityIfChanged(mBinding.layerMore, View.GONE);
    }

    public void showLayerMore() {
        ViewUtil.setVisibilityIfChanged(mBinding.layerEmoji, View.GONE);
        ViewUtil.setVisibilityIfChanged(mBinding.layerMore, View.VISIBLE);
    }

    @NonNull
    public Config getCustomConfig() {
        return mCustomConfig;
    }

    @NonNull
    public Config getSystemConfig() {
        return mSystemConfig;
    }

    public boolean isLayerEmojiShown() {
        return mBinding.layerEmoji.getVisibility() == View.VISIBLE;
    }

    public boolean isLayerMoreShown() {
        return mBinding.layerMore.getVisibility() == View.VISIBLE;
    }

    /**
     * 自定义键盘：表情
     */
    private void initLayerEmoji() {
        mBinding.layerEmojiPager.setAdapter(new LayerEmojiPagerAdapter());
        new TabLayoutMediator(
                mBinding.layerEmojiTabLayout,
                mBinding.layerEmojiPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setCustomView(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_emoji_emotion_tab_indicator_item);
                    } else if (position == 1) {
                        tab.setCustomView(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_emoji_emotion2_tab_indicator_item);
                    }
                }
        ).attach();
    }

    /**
     * 自定义键盘：更多
     */
    private void initLayerMore() {
        mBinding.layerMorePager.setAdapter(new LayerMorePagerAdapter());
    }

    public interface OnInputListener {
        void onInputText(String text);

        void onLottiePicked(String lottieId);

        void onDeleteOne();

        void onMediaPicked(@NonNull List<MediaData.MediaInfo> mediaInfoList);

        void onClickRtcAudio();

        void onClickRtcVideo();

        void onLocationPicked(@NonNull LocationInfo locationInfo, long zoom);

        void onClickSnapchatMode();
    }

    private OnInputListener mOnInputListener;

    public void setOnInputListener(OnInputListener onInputListener) {
        mOnInputListener = onInputListener;
    }

    private class LayerEmojiPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());

            if (viewType == 0) {
                return new EmotionViewHolder(parent, inflater) {
                    @Override
                    protected void onDeleteClick() {
                        super.onDeleteClick();

                        if (mOnInputListener != null) {
                            mOnInputListener.onDeleteOne();
                        }
                    }

                    @Override
                    protected void onEmotionClick(String name) {
                        super.onEmotionClick(name);

                        if (mOnInputListener != null) {
                            mOnInputListener.onInputText(name);
                        }
                    }
                };
            } else {
                Preconditions.checkArgument(viewType == 1);
                return new Emotion2ViewHolder(parent, inflater) {
                    @Override
                    protected void onEmotion2Click(String lottieId) {
                        super.onEmotion2Click(lottieId);

                        if (mOnInputListener != null) {
                            mOnInputListener.onLottiePicked(lottieId);
                        }
                    }
                };
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private class LayerMorePagerAdapter extends RecyclerView.Adapter<LayerMoreViewHolder> {

        @NonNull
        @Override
        public LayerMoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            return new LayerMoreViewHolder(inflater.inflate(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_more_view_holder, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull LayerMoreViewHolder holder, int position) {
            holder.onBind(position);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

    private class LayerMoreViewHolder extends RecyclerView.ViewHolder {

        private final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreViewHolderBinding mBinding;
        private final int mItemViewWidth = DimenUtil.dp2px(50);
        private final int mItemViewHeight = DimenUtil.dp2px(50);

        public LayerMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            mBinding = ImsdkUikitWidgetCustomSoftKeyboardLayerMoreViewHolderBinding.bind(itemView);
            mBinding.gridLayout.setColumnCount(4);
            mBinding.gridLayout.setRowCount(2);
        }

        public void onBind(int position) {
            final Context context = getContext();
            mBinding.gridLayout.removeAllViews();

            final int count = 4 * 2;
            int start = 0;
            {
                start++;
                inflateMediaItemView(context);
            }
            {
                if (mSystemConfig.mShowRtc && mCustomConfig.mShowRtc) {
                    start++;
                    inflateRtcAudioItemView(context);
                }
            }
            {
                if (mSystemConfig.mShowRtc && mCustomConfig.mShowRtc) {
                    start++;
                    inflateRtcVideoItemView(context);
                }
            }
            {
                if (mSystemConfig.mShowLocation && mCustomConfig.mShowLocation) {
                    start++;
                    inflateLocationItemView(context);
                }
            }
            {
                if (mSystemConfig.mShowSnapchat && mCustomConfig.mShowSnapchat) {
                    start++;
                    inflateSnapchatItemView(context);
                }
            }
            for (int i = start; i < count; i++) {
                inflateMoreEmptyItemView(context);
            }
        }

        private void inflateMediaItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_media);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_media);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                requestMediaPickerPermission();
            });
        }

        private void inflateRtcAudioItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_voice_call);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_rtc_audio);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onClickRtcAudio();
                }
            });
        }

        private void inflateRtcVideoItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_video_call);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_rtc_video);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onClickRtcVideo();
                }
            });
        }

        private void inflateLocationItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_location);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_location);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                requestLocationPermission();
            });
        }

        private void inflateSnapchatItemView(Context context) {
            final ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding binding =
                    ImsdkUikitWidgetCustomSoftKeyboardLayerMoreItemViewBinding.inflate(
                            LayoutInflater.from(context), mBinding.gridLayout, false);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            binding.getRoot().setLayoutParams(lp);

            binding.itemMedia.setImageResource(R.drawable.imsdk_uikit_ic_input_more_item_snapchat);
            binding.itemName.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_snapchat);
            mBinding.gridLayout.addView(binding.getRoot());

            ViewUtil.onClick(binding.getRoot(), v -> {
                if (mOnInputListener != null) {
                    mOnInputListener.onClickSnapchatMode();
                }
            });
        }

        private void inflateMoreEmptyItemView(Context context) {
            final Space itemView = new Space(context);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = mItemViewWidth;
            lp.height = mItemViewHeight;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            itemView.setLayoutParams(lp);

            mBinding.gridLayout.addView(itemView);
        }

    }

    private void requestMediaPickerPermission() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final RxPermissions rxPermissions = new RxPermissions(activity);
        mPermissionRequest.set(
                rxPermissions.request(MEDIA_PICKER_PERMISSION)
                        .subscribe(granted -> {
                            if (granted) {
                                onMediaPickerPermissionGranted();
                            } else {
                                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                                TipUtil.show(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        }));
    }

    private void onMediaPickerPermissionGranted() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final MediaPickerDialog mediaPickerDialog = new MediaPickerDialog(activity, activity.findViewById(Window.ID_ANDROID_CONTENT));
        mediaPickerDialog.setOnMediaPickListener(mediaInfoList -> {
            if (mediaInfoList.isEmpty()) {
                return false;
            }

            for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
                if (!mediaInfo.isImageMimeType()
                        && !mediaInfo.isVideoMimeType()) {
                    Throwable e = new Throwable("unknown mime type:" + mediaInfo.mimeType + ", uri:" + mediaInfo.uri);
                    MSIMUikitLog.e(e);
                    return false;
                }
            }

            if (mOnInputListener != null) {
                mOnInputListener.onMediaPicked(mediaInfoList);
            }

            return true;
        });
        mediaPickerDialog.show();
    }

    private void requestLocationPermission() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final RxPermissions rxPermissions = new RxPermissions(activity);
        mPermissionRequest.set(
                rxPermissions.request(LOCATION_PERMISSION)
                        .subscribe(granted -> {
                            if (granted) {
                                onLocationPermissionGranted();
                            } else {
                                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                                TipUtil.show(MSIMUikitConstants.ErrorLog.PERMISSION_REQUIRED);
                            }
                        }));
    }

    private void onLocationPermissionGranted() {
        final AppCompatActivity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final LocationPickerDialog locationPickerDialog = new LocationPickerDialog(
                activity, activity.findViewById(Window.ID_ANDROID_CONTENT)
        );
        locationPickerDialog.setOnLocationPickListener((locationInfo, zoom) -> {
            if (mOnInputListener != null) {
                mOnInputListener.onLocationPicked(locationInfo, zoom);
            }
            return true;
        });
        locationPickerDialog.show();
    }

    public static class EmotionLoader {

        private static boolean sInit;
        private static final String ASSET_DIR = "msimsdk/uikit/emotion";
        private static final String PLIST_FILENAME = "emoji.plist";

        private static List<Pair<String, String>> DATA_LIST = new ArrayList<>();
        private static Map<String, Pair<String, String>> MAP_LIST = new HashMap<>();

        public static void preload() {
            Threads.postBackground(EmotionLoader::preloadIfNeed);
        }

        private synchronized static void preloadIfNeed() {
            try {
                if (sInit) {
                    return;
                }
                sInit = true;

                final byte[] plistContent = AssetUtil.readAll(ASSET_DIR + "/" + PLIST_FILENAME, null, null);
                final NSArray nsArray = (NSArray) PropertyListParser.parse(plistContent);
                for (NSObject nsObject : nsArray.getArray()) {
                    final NSDictionary nsDictionary = (NSDictionary) nsObject;
                    //noinspection ConstantConditions
                    final String faceName = ((NSString) nsDictionary.get("face_name")).getContent();
                    final String assetFile = ASSET_DIR + "/" + faceName + "@2x.png";
                    final Pair<String, String> pair = Pair.create(faceName, assetFile);
                    DATA_LIST.add(pair);
                    MAP_LIST.put(faceName, pair);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public static int size() {
            preloadIfNeed();
            return DATA_LIST.size();
        }

        public static String getName(int index) {
            preloadIfNeed();
            return DATA_LIST.get(index).first;
        }

        public static String getValue(int index) {
            preloadIfNeed();
            return DATA_LIST.get(index).second;
        }

        public static String getValue(String name) {
            preloadIfNeed();
            //noinspection ConstantConditions
            return MAP_LIST.get(name).second;
        }

        public static String getAssetValue(int index) {
            return "asset:///" + getValue(index);
        }

        public static String getAssetValue(String name) {
            return "asset:///" + getValue(name);
        }

        public static boolean contains(String name) {
            preloadIfNeed();
            return MAP_LIST.containsKey(name);
        }
    }

    static class EmotionViewHolder extends RecyclerView.ViewHolder {

        private final ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotionViewHolderBinding mBinding;
        private final LayoutInflater mInflater;

        public EmotionViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater) {
            super(inflater.inflate(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_emoji_emotion_view_holder, parent, false));
            mBinding = ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotionViewHolderBinding.bind(itemView);
            mInflater = inflater;

            this.init(parent.getContext());
        }

        private void init(Context context) {
            ViewUtil.onClick(mBinding.actionDelete,
                    100L,
                    v -> EmotionViewHolder.this.onDeleteClick());

            final int spanCount = context.getResources().getInteger(R.integer.imsdk_uikit_widget_custom_soft_keyboard_emotion_span_count);
            final GridLayoutManager layoutManager = new GridLayoutManager(
                    context, spanCount, GridLayoutManager.VERTICAL, false
            );
            mBinding.recyclerView.setLayoutManager(layoutManager);
            mBinding.recyclerView.setAdapter(new EmotionAdapter(mInflater) {
                @Override
                protected void onEmotionItemClick(String name) {
                    super.onEmotionItemClick(name);
                    EmotionViewHolder.this.onEmotionClick(name);
                }
            });
        }

        protected void onDeleteClick() {
        }

        protected void onEmotionClick(String name) {
        }

        static class EmotionAdapter extends RecyclerView.Adapter<ItemViewHolder> {

            private final LayoutInflater mInflater;

            private EmotionAdapter(LayoutInflater inflater) {
                mInflater = inflater;
            }

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final ItemViewHolder viewHolder = new ItemViewHolder(parent, mInflater);

                ViewUtil.onClick(viewHolder.itemView,
                        100L,
                        v -> {
                            final int position = viewHolder.getAbsoluteAdapterPosition();
                            if (position < 0) {
                                return;
                            }
                            final String name = EmotionLoader.getName(position);
                            onEmotionItemClick(name);
                        });

                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
                final String assetValue = EmotionLoader.getAssetValue(position);
                holder.mItemBinding.imageLayout.setImageUrl(null, assetValue);
            }

            @Override
            public int getItemCount() {
                return EmotionLoader.size();
            }

            protected void onEmotionItemClick(String name) {
            }
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {

            private final ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotionViewHolderItemViewHolderBinding mItemBinding;

            ItemViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater) {
                super(inflater.inflate(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_emoji_emotion_view_holder_item_view_holder, parent, false));
                mItemBinding = ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotionViewHolderItemViewHolderBinding.bind(itemView);
            }
        }
    }

    public static class Emotion2Loader {

        private static boolean sInit;
        private static final String ASSET_DIR = "msimsdk/uikit/emotion2";
        private static final String PLIST_FILENAME = "emotion.plist";

        private static List<Pair<String, String>> DATA_LIST = new ArrayList<>();
        private static Map<String, Pair<String, String>> MAP_LIST = new HashMap<>();

        public static void preload() {
            Threads.postBackground(Emotion2Loader::preloadIfNeed);
        }

        private synchronized static void preloadIfNeed() {
            try {
                if (sInit) {
                    return;
                }
                sInit = true;

                final byte[] plistContent = AssetUtil.readAll(ASSET_DIR + "/" + PLIST_FILENAME, null, null);
                final NSArray nsArray = (NSArray) PropertyListParser.parse(plistContent);
                for (NSObject nsObject : nsArray.getArray()) {
                    final NSDictionary nsDictionary = (NSDictionary) nsObject;
                    //noinspection ConstantConditions
                    final String id = ((NSString) nsDictionary.get("id")).getContent();
                    //noinspection ConstantConditions
                    final String lottie = ((NSString) nsDictionary.get("lottie")).getContent();
                    final String assetFile = ASSET_DIR + "/" + lottie + ".json";
                    final Pair<String, String> pair = Pair.create(id, assetFile);
                    DATA_LIST.add(pair);
                    MAP_LIST.put(id, pair);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public static int size() {
            preloadIfNeed();
            return DATA_LIST.size();
        }

        public static String getLottieId(int index) {
            preloadIfNeed();
            return DATA_LIST.get(index).first;
        }

        public static String getValue(int index) {
            preloadIfNeed();
            return DATA_LIST.get(index).second;
        }

        public static String getValue(String lottieId) {
            preloadIfNeed();
            //noinspection ConstantConditions
            return MAP_LIST.get(lottieId).second;
        }

        public static String getAssetValue(int index) {
            return "asset:///" + getValue(index);
        }

        public static String getAssetValue(String lottieId) {
            return "asset:///" + getValue(lottieId);
        }

        public static String getDefaultValue() {
            preloadIfNeed();
            return ASSET_DIR + "/" + "emotion_default.json";
        }

        public static String getDefaultAssetValue() {
            return "asset:///" + getDefaultValue();
        }

        public static boolean contains(String lottieId) {
            preloadIfNeed();
            return MAP_LIST.containsKey(lottieId);
        }
    }

    static class Emotion2ViewHolder extends RecyclerView.ViewHolder {

        private final ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotion2ViewHolderBinding mBinding;
        private final LayoutInflater mInflater;

        public Emotion2ViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater) {
            super(inflater.inflate(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_emoji_emotion2_view_holder, parent, false));
            mBinding = ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotion2ViewHolderBinding.bind(itemView);
            mInflater = inflater;

            this.init(parent.getContext());
        }

        private void init(Context context) {
            final int spanCount = context.getResources().getInteger(R.integer.imsdk_uikit_widget_custom_soft_keyboard_emotion2_span_count);
            final GridLayoutManager layoutManager = new GridLayoutManager(
                    context, spanCount, GridLayoutManager.VERTICAL, false
            );
            mBinding.recyclerView.setLayoutManager(layoutManager);
            mBinding.recyclerView.setAdapter(new Emotion2Adapter(mInflater) {
                @Override
                protected void onEmotionItemClick(String lottieId) {
                    super.onEmotionItemClick(lottieId);
                    Emotion2ViewHolder.this.onEmotion2Click(lottieId);
                }
            });
        }

        protected void onEmotion2Click(String lottieId) {
        }

        static class Emotion2Adapter extends RecyclerView.Adapter<ItemViewHolder> {

            private final LayoutInflater mInflater;

            private Emotion2Adapter(LayoutInflater inflater) {
                mInflater = inflater;
            }

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final ItemViewHolder viewHolder = new ItemViewHolder(parent, mInflater);

                ViewUtil.onClick(viewHolder.itemView,
                        100L,
                        v -> {
                            final int position = viewHolder.getAbsoluteAdapterPosition();
                            if (position < 0) {
                                return;
                            }
                            final String lottieId = Emotion2Loader.getLottieId(position);
                            onEmotionItemClick(lottieId);
                        });

                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
                final String assetFilename = Emotion2Loader.getValue(position);
                holder.mItemBinding.lottieView.setAnimation(assetFilename);
            }

            @Override
            public int getItemCount() {
                return Emotion2Loader.size();
            }

            protected void onEmotionItemClick(String lottieId) {
            }
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {

            private final ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotion2ViewHolderItemViewHolderBinding mItemBinding;

            ItemViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater) {
                super(inflater.inflate(R.layout.imsdk_uikit_widget_custom_soft_keyboard_layer_emoji_emotion2_view_holder_item_view_holder, parent, false));
                mItemBinding = ImsdkUikitWidgetCustomSoftKeyboardLayerEmojiEmotion2ViewHolderItemViewHolderBinding.bind(itemView);
            }
        }
    }
}


