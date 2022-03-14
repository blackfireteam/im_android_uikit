package com.masonsoft.imsdk.uikit.util;

import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.drawee.ViewDraweeSpan;

/**
 * EditText 相关辅助类
 */
public class EditTextUtil {

    private EditTextUtil() {
    }

    /**
     * 根据光标位置的不同插入数据
     */
    public static void insertText(@NonNull EditText view, CharSequence text) {
        int selectionStart = view.getSelectionStart();
        if (selectionStart >= 0) {
            int selectionEnd = view.getSelectionEnd();
            if (selectionEnd >= 0 && selectionEnd > selectionStart) {
                view.getText().replace(selectionStart, selectionEnd, text);
            } else {
                view.getText().insert(selectionStart, text);
            }

            ViewDraweeSpan.updateTargetView(view.getText(), view);
        } else {
            MSIMUikitLog.e("insertText invalid selectionStart:%s", selectionStart);
        }
    }

    /**
     * 根据光标位置的不同删除数据
     */
    public static void deleteOne(@NonNull EditText view) {
        final KeyListener keyListener = view.getKeyListener();
        if (keyListener == null) {
            MSIMUikitLog.e("unexpected. key listener is null.");
            return;
        }
        final Editable editable = view.getEditableText();
        if (editable == null) {
            MSIMUikitLog.e("unexpected. editable is null.");
            return;
        }

        final long timeNow = System.currentTimeMillis();
        keyListener.onKeyDown(
                view,
                editable,
                KeyEvent.KEYCODE_DEL,
                new KeyEvent(timeNow, timeNow, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0)
        );

        /*
        int selectionStart = view.getSelectionStart();
        if (selectionStart >= 0) {
            int selectionEnd = view.getSelectionEnd();
            if (selectionEnd >= 0 && selectionEnd > selectionStart) {
                view.getText().delete(selectionStart, selectionEnd);
            } else if (selectionStart > 0) {
                // 如果是 emoji, 需要删除紧挨着的两个 char
                if (selectionStart > 1) {
                    char char1 = view.getText().charAt(selectionStart - 2);
                    char char2 = view.getText().charAt(selectionStart - 1);
                    if (Character.isSurrogatePair(char1, char2)) {
                        view.getText().delete(selectionStart - 2, selectionStart);
                        return;
                    }
                }
                view.getText().delete(selectionStart - 1, selectionStart);
            }
        } else {
            MSIMUikitLog.e("deleteOne invalid selectionStart:%s", selectionStart);
        }
        */
    }

}
