package com.arny.flightlogbook.presentation.uicomponents

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.arny.core.utils.DateTimeUtils
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.InputTimeComponentBinding
import com.arny.flightlogbook.presentation.flights.addedit.models.CorrectedTimePair
import com.arny.flightlogbook.presentation.flights.addedit.models.getCorrectDayTime

class InputTimeComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = InputTimeComponentBinding.inflate(LayoutInflater.from(context), this)
    private var onDateChangeListener: ((Int) -> Unit)? = null
    private var timeClickListener: (() -> Unit)? = null
    private var editorActionListener: ((actionId: Int) -> Unit)? = null
    private var edited: Boolean = true
    private var showZero: Boolean = false
    private var correctedTime: CorrectedTimePair? = null
    private var timeInMin = 0
    val edtTime: EditText
        get() = binding.edtTime

    init {
        val utcTime = context.getString(R.string.utc_time)
        val utcTimeZero = context.getString(R.string.str_time_zero)
        val att = context.obtainStyledAttributes(
            attrs, R.styleable.InputTimeComponent, defStyleAttr, 0
        )
        setCaptionVisible(att.getBoolean(R.styleable.InputTimeComponent_captionVisible, true))
        setEditable(att.getBoolean(R.styleable.InputTimeComponent_editable, true))
        showZero = att.getBoolean(R.styleable.InputTimeComponent_showzero, false)
        setImeOptions(
            att.getBoolean(
                R.styleable.InputTimeComponent_imeDone,
                false
            )
        )
        att.recycle()
        with(binding) {
            edtTime.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    updateTime()
                    edtTime.setText(correctedTime?.strTime)
                }
                val depTime = edtTime.text.toString()
                if (depTime.isBlank()) {
                    if (hasFocus) {
                        edtTime.hint = utcTime
                        edtTime.hint = utcTimeZero
                    } else {
                        edtTime.hint = null
                        edtTime.hint = utcTimeZero
                    }
                } else {
                    edtTime.hint = utcTimeZero
                    if (hasFocus && depTime == utcTimeZero) {
                        edtTime.setSelectAllOnFocus(true)
                        edtTime.selectAll()
                    }
                }
            }
            ivTimeIcon.setOnClickListener {
                timeClickListener?.invoke()
            }
            ivTimeRemove.setOnClickListener {
                timeInMin = 0
                edtTime.setText("")
                updateTime()
            }
            edtTime.setOnEditorActionListener { _, actionId, _ ->
                editorActionListener?.invoke(actionId)
                updateTime()
                true
            }
        }
    }

    private fun updateTime(triggerListener: Boolean = true) {
        correctedTime = getCorrectDayTime(binding.edtTime.text.toString(), timeInMin)
        timeInMin = correctedTime?.intTime ?: 0
        if (triggerListener) {
            onDateChangeListener?.invoke(timeInMin)
        }
        refreshRemoveIconVisible()
    }

    private fun setEditable(edited: Boolean) {
        this.edited = edited
        with(binding) {
            ivTimeIcon.isVisible = edited
            edtTime.isClickable = edited
            edtTime.isFocusable = edited
            edtTime.isFocusableInTouchMode = edited
            edtTime.isLongClickable = edited
        }
    }

    fun setDateChangedListener(listener: (Int) -> Unit) {
        onDateChangeListener = listener
    }

    fun setTimeIconClickListener(listener: () -> Unit) {
        timeClickListener = listener
    }

    fun refreshRemoveIconVisible() {
        if (edited) {
            binding.ivTimeRemove.isVisible = binding.edtTime.text.isNotBlank()
        }
    }

    fun setCaption(text: CharSequence?) {
        binding.tvCaption.text = text
    }

    private fun setImeOptions(useDone: Boolean = false) {
        binding.edtTime.imeOptions = if (useDone)
            EditorInfo.IME_ACTION_DONE
        else
            EditorInfo.IME_ACTION_NEXT
    }

    private fun setCaptionVisible(visible: Boolean) {
        binding.tvCaption.isVisible = visible
    }

    fun setText(text: CharSequence?) {
        with(binding.edtTime) {
            if (this.text != text) {
                setText(text)
            }
        }
        refreshRemoveIconVisible()
    }

    fun setTime(time: Int) {
        timeInMin = time
        val s = if (time == 0) {
            ""
        } else {
            DateTimeUtils.strLogTime(time)
        }
        binding.edtTime.setText(s)
        refreshRemoveIconVisible()
    }

    fun setOnEditorActionListener(listener: (actionId: Int) -> Unit) {
        this.editorActionListener = listener
    }
}
