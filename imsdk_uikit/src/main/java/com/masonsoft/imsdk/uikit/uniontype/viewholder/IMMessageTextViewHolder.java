package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMTextElement;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.uniontype.Host;

public abstract class IMMessageTextViewHolder extends IMMessageViewHolder {

    private final TextView mMessageText;

    public IMMessageTextViewHolder(@NonNull Host host, int layout) {
        super(host, layout);
        mMessageText = itemView.findViewById(R.id.message_text);
    }

    public IMMessageTextViewHolder(@NonNull Host host, @NonNull View itemView) {
        super(host, itemView);
        mMessageText = itemView.findViewById(R.id.message_text);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindItemObject(int position, @NonNull DataObject<MSIMMessage> itemObject) {
        super.onBindItemObject(position, itemObject);
        final MSIMMessage message = itemObject.object;

        String text = null;
        final MSIMTextElement element = message.getTextElement();
        if (element != null) {
            text = element.getText();
        }
        mMessageText.setText(text);

        GestureDetectorCompat gestureDetectorCompat = new GestureDetectorCompat(mMessageText.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
                if (listener != null) {
                    listener.onItemClick(IMMessageTextViewHolder.this);
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
