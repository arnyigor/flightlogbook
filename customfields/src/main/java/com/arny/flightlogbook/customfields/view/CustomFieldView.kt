package com.arny.flightlogbook.customfields.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import com.arny.flightlogbook.customfields.R
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.toCustomFieldType

class CustomFieldView : LinearLayout {
    private var name: String? = null
    private var switch: Switch? = null
    private var editText: EditText? = null
    private var styleType: Int? = null
    private var type: String? = null

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
            CustomFieldType.TYPE_TEXT -> {
                editText?.setText(value.toString())
            }
            CustomFieldType.TYPE_NUMBER -> {
                editText?.setText(value.toString())
            }
            CustomFieldType.TYPE_TIME -> {
                editText?.setText(value.toString())
            }
            CustomFieldType.TYPE_BOOLEAN -> {
                switch?.isChecked = value.toString().toBoolean() || value.toString() == "1"
            }
            CustomFieldType.TYPE_NONE -> { }
        }
    }

    private fun returnValue(): Any? {
        return when (type.toCustomFieldType()) {
            CustomFieldType.TYPE_TEXT -> editText?.text.toString()
            CustomFieldType.TYPE_NUMBER -> editText?.text.toString()
            CustomFieldType.TYPE_TIME -> editText?.text.toString()
            CustomFieldType.TYPE_BOOLEAN -> switch?.isChecked
            CustomFieldType.TYPE_NONE -> null
        }
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    private fun invalidateTextPaintAndMeasurements() {
        when (type.toCustomFieldType()) {
            CustomFieldType.TYPE_TEXT -> {
                editText = EditText(context)
                editText?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                editText?.inputType = InputType.TYPE_CLASS_TEXT
                addView(editText, 0)
            }
            CustomFieldType.TYPE_BOOLEAN -> {
                switch = Switch(context)
                switch?.text = name
                switch?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                addView(switch, 0)
            }
            CustomFieldType.TYPE_NUMBER -> {
                editText = EditText(context)
                editText?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                editText?.inputType = InputType.TYPE_CLASS_NUMBER
                addView(editText, 0)
            }
            CustomFieldType.TYPE_TIME -> {
                editText = EditText(context)
                editText?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                editText?.inputType = InputType.TYPE_DATETIME_VARIATION_TIME
                addView(editText, 0)
            }
            CustomFieldType.TYPE_NONE -> {
            }
        }
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
}
