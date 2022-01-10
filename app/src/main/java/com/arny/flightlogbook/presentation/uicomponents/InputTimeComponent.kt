package com.arny.flightlogbook.presentation.uicomponents

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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

    private var correctedTime: CorrectedTimePair? = null
    private var timeInMin = 0
    private val binding = InputTimeComponentBinding.inflate(LayoutInflater.from(context), this)
    private val watcher = binding.edtTime.doAfterTextChanged { updateTime() }
    val timeIcon: ImageView
        get() = binding.ivTimeIcon
    val clearIcon: ImageView
        get() = binding.ivTimeRemove
    val edtTime: EditText
        get() = binding.edtTime

    private fun updateTime() {
        correctedTime = getCorrectDayTime(binding.edtTime.text.toString(), timeInMin)
        timeInMin = correctedTime?.intTime ?: 0
        textChangedListener?.invoke(timeInMin)
        refreshRemoveIconVisible()
    }

    init {
        val att = context.obtainStyledAttributes(
            attrs, R.styleable.InputTimeComponent, defStyleAttr, 0
        )
        setCaptionVisible(att.getBoolean(R.styleable.InputTimeComponent_captionVisible, true))
        att.recycle()
        binding.edtTime.addTextChangedListener(watcher)
        binding.edtTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.edtTime.setSelectAllOnFocus(false)
                binding.edtTime.setText(correctedTime?.strTime)
            }
            val depTime = binding.edtTime.text.toString()
            if (depTime.isBlank()) {
                if (hasFocus) {
                    binding.edtTime.hint = context.getString(R.string.utc_time)
                    binding.edtTime.hint = context.getString(R.string.str_time_zero)
                } else {
                    binding.edtTime.hint = null
                    binding.edtTime.hint = context.getString(R.string.str_time_zero)
                }
            } else {
                binding.edtTime.hint = context.getString(R.string.str_time_zero)
                if (hasFocus && depTime == context.getString(R.string.str_time_zero)) {
                    binding.edtTime.setSelectAllOnFocus(true)
                    binding.edtTime.selectAll()
                }
            }
        }
        binding.ivTimeIcon.setOnClickListener { timeClickListener?.invoke() }
        binding.ivTimeRemove.setOnClickListener {
            timeInMin = 0
            binding.edtTime.setText("")
            updateTime()
        }

        binding.edtTime.setOnEditorActionListener { _, actionId, event ->
            editorActionListener?.invoke(actionId, event)
            true
        }
    }

    private var textChangedListener: ((Int) -> Unit)? = null
    private var timeClickListener: (() -> Unit)? = null
    private var editorActionListener: ((actionId: Int, event: KeyEvent?) -> Unit)? = null

    fun setDateChangedListener(listener: (Int) -> Unit) {
        textChangedListener = listener
    }

    fun setTimeIconClickListener(listener: () -> Unit) {
        timeClickListener = listener
    }

    fun refreshRemoveIconVisible() {
        binding.ivTimeRemove.isVisible = binding.edtTime.text.isNotBlank()
    }

    fun setCaption(text: CharSequence?) {
        binding.tvCaption.text = text
    }

    fun setCaptionVisible(visible: Boolean) {
        binding.tvCaption.isVisible = visible
    }

    fun setText(text: CharSequence?) {
        with(binding.edtTime) {
            if (this.text != text) {
                removeTextChangedListener(watcher)
                setText(text)
                addTextChangedListener(watcher)
            }
        }
        refreshRemoveIconVisible()
    }

    fun setTime(time: Int) {
        timeInMin = time
        binding.edtTime.setText(DateTimeUtils.strLogTime(time))
        refreshRemoveIconVisible()
    }

    fun setOnEditorActionListener(listener: (actionId: Int, event: KeyEvent?) -> Unit) {
        this.editorActionListener = listener
    }
}
