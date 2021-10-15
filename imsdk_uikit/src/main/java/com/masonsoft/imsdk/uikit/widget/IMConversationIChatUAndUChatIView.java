package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMConversationExt;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import io.github.idonans.appcontext.AppContext;

public class IMConversationIChatUAndUChatIView extends IMConversationDynamicTextView {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMConversationIChatUAndUChatIView(Context context) {
        this(context, null);
    }

    public IMConversationIChatUAndUChatIView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMConversationIChatUAndUChatIView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMConversationIChatUAndUChatIView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
        if (isInEditMode()) {
            setText("⬆");
        }
    }

    @Override
    protected void onConversationChanged(@Nullable MSIMConversation conversation, @Nullable Object customObject) {
        if (conversation == null) {
            setText("");
            return;
        }

        final MSIMConversationExt ext = conversation.getExt();
        String txt = "";
        if (ext.isIChatU()) {
            txt += "⬆";
        }
        if (ext.isUChatI()) {
            txt += "⬇";
        }
        setText(txt);
    }

}
