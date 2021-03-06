package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMTextElement;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.drawee.ViewDraweeSpan;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;

public abstract class IMBaseMessageTextViewHolder extends IMBaseMessageViewHolder {

    private final TextView mMessageText;

    public IMBaseMessageTextViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mMessageText = itemView.findViewById(R.id.message_text);
    }

    public IMBaseMessageTextViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mMessageText = itemView.findViewById(R.id.message_text);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindUpdate() {
        super.onBindUpdate();

        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MSIMBaseMessage baseMessage = itemObject.getObject(MSIMBaseMessage.class);

        String text = null;
        final MSIMTextElement element = baseMessage.getTextElement();
        if (element != null) {
            text = element.getText();
        }
        mMessageText.setText(text);
        ViewDraweeSpan.rebuildTargetViewText(mMessageText);

        GestureDetectorCompat gestureDetectorCompat = new GestureDetectorCompat(mMessageText.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
                if (listener != null) {
                    listener.onItemClick(IMBaseMessageTextViewHolder.this);
                }
                return true;
            }
        });
        mMessageText.setOnTouchListener((v, event) -> gestureDetectorCompat.onTouchEvent(event));

        mMessageText.setOnLongClickListener(v -> {
            final UnionTypeViewHolderListeners.OnItemLongClickListener listener = itemObject.getExtHolderItemLongClick1();
            if (listener != null) {
                listener.onItemLongClick(this);
            }
            return true;
        });
    }

}
