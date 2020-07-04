package com.arny.flightlogbook.customfields.presenter

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import com.arny.flightlogbook.customfields.R
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.toCustomFieldType
import com.google.android.material.textfield.TextInputEditText


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
            CustomFieldType.TYPE_NUMBER_INT -> TODO()
            CustomFieldType.TYPE_TIME_INT -> TODO()
            CustomFieldType.TYPE_NUMBER_LONG -> TODO()
            CustomFieldType.TYPE_NUMBER_DOUBLE -> TODO()
            CustomFieldType.TYPE_BOOLEAN -> {
                switch?.isChecked = value.toString().toBoolean() || value.toString() == "1"
            }
            CustomFieldType.TYPE_DATE -> TODO()
            CustomFieldType.TYPE_NONE -> TODO()
        }
    }

    private fun returnValue(): Any? {
        return when (type.toCustomFieldType()) {
            CustomFieldType.TYPE_TEXT -> editText?.text.toString()
            CustomFieldType.TYPE_NUMBER_INT -> TODO()
            CustomFieldType.TYPE_TIME_INT -> TODO()
            CustomFieldType.TYPE_NUMBER_LONG -> TODO()
            CustomFieldType.TYPE_NUMBER_DOUBLE -> TODO()
            CustomFieldType.TYPE_BOOLEAN -> switch?.isChecked
            CustomFieldType.TYPE_DATE -> TODO()
            CustomFieldType.TYPE_NONE -> {
            }
        }
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    private fun invalidateTextPaintAndMeasurements() {
        when (type.toCustomFieldType()) {
            CustomFieldType.TYPE_TEXT -> {
                editText = TextInputEditText(context)
                editText?.hint = name
                editText?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//                editText?.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                addView(editText, 0)
            }
            CustomFieldType.TYPE_BOOLEAN -> {
                switch = Switch(context)
                switch?.text = name
                switch?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//                switch?.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                addView(switch, 0)
            }
            CustomFieldType.TYPE_NUMBER_INT -> TODO()
            CustomFieldType.TYPE_TIME_INT -> TODO()
            CustomFieldType.TYPE_NUMBER_LONG -> TODO()
            CustomFieldType.TYPE_NUMBER_DOUBLE -> TODO()
            CustomFieldType.TYPE_DATE -> TODO()
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
