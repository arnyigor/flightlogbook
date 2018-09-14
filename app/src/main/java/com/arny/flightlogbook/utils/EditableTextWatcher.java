package com.arny.flightlogbook.utils;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * An extension of TextWatcher which stops further callbacks being called as a result of a change
 * happening within the callbacks themselves.
 */
public abstract class EditableTextWatcher implements TextWatcher {

    private boolean editing;

    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (editing)
            return;

        editing = true;
        try {
            beforeTextChange(s, start, count, after);
        } finally {
            editing = false;
        }
    }

    abstract void beforeTextChange(CharSequence s, int start, int count, int after);

    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) {
        if (editing)
            return;

        editing = true;
        try {
            onTextChange(s, start, before, count);
        } finally {
            editing = false;
        }
    }

    abstract void onTextChange(CharSequence s, int start, int before, int count);

    @Override
    public final void afterTextChanged(Editable s) {
        if (editing)
            return;

        editing = true;
        try {
            afterTextChange(s);
        } finally {
            editing = false;
        }
    }

    public boolean isEditing() {
        return editing;
    }

    abstract void afterTextChange(Editable s);
}