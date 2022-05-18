package com.arny.flightlogbook.customfields.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import com.arny.flightlogbook.customfields.R
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.toCustomFieldType

class CustomFieldView : LinearLayout {
    private var name: String? = null
    var switch: SwitchCompat? = null
    var editText: EditText? = null
    private var styleType: Int? = null
    private var type: String? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CustomFieldView, defStyle, 0)
        a.recycle()
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        when (type.toCustomFieldType()) {
            is CustomFieldType.Text -> {
                editText = EditText(context)
                editText?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                editText?.imeOptions = EditorInfo.IME_ACTION_NEXT
                editText?.inputType = InputType.TYPE_CLASS_TEXT
                addView(editText, 0)
            }
            is CustomFieldType.Bool -> {
                switch = SwitchCompat(context)
                switch?.text = name
                switch?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                addView(switch, 0)
            }
            is CustomFieldType.Number -> {
                editText = EditText(context)
                editText?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                editText?.imeOptions = EditorInfo.IME_ACTION_NEXT
                editText?.inputType = InputType.TYPE_CLASS_NUMBER
                editText?.gravity = Gravity.CENTER_HORIZONTAL
                addView(editText, 0)
            }
            is CustomFieldType.Time -> {
                editText = EditText(context)
                editText?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                editText?.imeOptions = EditorInfo.IME_ACTION_NEXT
                editText?.inputType = InputType.TYPE_CLASS_DATETIME
                editText?.gravity = Gravity.CENTER_HORIZONTAL
                addView(editText, 0)
            }
            is CustomFieldType.None -> {
            }
        }
    }

    fun init(type: CustomFieldType, name: String) {
        this.type = type.toString()
        this.name = name
        invalidateTextPaintAndMeasurements()
    }

    var value: Any?
        get() = returnValue()
        set(value) {
            setViewValue(value)
        }

    private fun setViewValue(value: Any?) {
        when (type.toCustomFieldType()) {
            is CustomFieldType.Text -> editText?.setText(value.toString())
            is CustomFieldType.Number -> editText?.setText(value.toString())
            is CustomFieldType.Time -> editText?.setText(value.toString())
            is CustomFieldType.Bool -> switch?.isChecked = value.toString().toBoolean() || value.toString() == "1"
            is CustomFieldType.None -> {
            }
        }
    }

    private fun returnValue(): Any? = when (type.toCustomFieldType()) {
        is CustomFieldType.Text -> editText?.text.toString()
        is CustomFieldType.Number -> editText?.text.toString()
        is CustomFieldType.Time -> editText?.text.toString()
        is CustomFieldType.Bool -> switch?.isChecked
        is CustomFieldType.None -> null
    }
}
