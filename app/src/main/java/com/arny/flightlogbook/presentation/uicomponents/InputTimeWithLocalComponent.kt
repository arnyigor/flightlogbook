package com.arny.flightlogbook.presentation.uicomponents

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.arny.core.utils.DateTimeUtils
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.InputTimeWithLocalComponentBinding
import com.arny.flightlogbook.presentation.flights.addedit.models.CorrectedTimePair
import com.arny.flightlogbook.presentation.flights.addedit.models.getCorrectDayTime
import com.arny.flightlogbook.presentation.flights.addedit.models.getCorrectLocalDiffDayTime

class InputTimeWithLocalComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var correctedTime: CorrectedTimePair? = null
    private var correctedTimeDiff: CorrectedTimePair? = null
    private var timeInMin = 0
    private var timeDiffInMin = 0
    private var utcTime = true
    private val binding =
        InputTimeWithLocalComponentBinding.inflate(LayoutInflater.from(context), this)
    private val mainTimeUpdateListener = binding.edtTime.doAfterTextChanged { updateTime() }
    private val timeDiffUpdateListener = binding.edtTimeDiff.doAfterTextChanged { updateTime() }
    val timeIcon: ImageView
        get() = binding.ivTimeIcon
    val clearIcon: ImageView
        get() = binding.ivTimeRemove
    val edtTime: EditText
        get() = binding.edtTime

    private fun updateTime() {
        correctedTimeDiff =
            getCorrectLocalDiffDayTime(binding.edtTimeDiff.text.toString(), timeDiffInMin)
        timeDiffInMin = correctedTimeDiff?.intTime ?: 0
        timeInMin = correctedTime?.intTime ?: 0 + timeDiffInMin
        correctedTime = getCorrectDayTime(binding.edtTime.text.toString(), timeInMin)
        timeInMin = correctedTime?.intTime ?: 0
        textChangedListener?.invoke(timeInMin)
        refreshRemoveIconVisible()
        refreshRemoveTimeDiffIconVisible()
    }

    init {
        changeUtcLocal()
        with(binding) {
            edtTime.addTextChangedListener(mainTimeUpdateListener)
            edtTimeDiff.addTextChangedListener(timeDiffUpdateListener)
            edtTime.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    edtTime.setSelectAllOnFocus(false)
                    edtTime.setText(correctedTime?.strTime)
                    onFocusChangeListener?.invoke(timeInMin)
                }
                val depTime = edtTime.text.toString()
                if (depTime.isBlank()) {
                    if (hasFocus) {
                        edtTime.hint = context.getString(R.string.utc_time)
                        edtTime.hint = context.getString(R.string.str_time_zero)
                    } else {
                        edtTime.hint = null
                        edtTime.hint = context.getString(R.string.str_time_zero)
                    }
                } else {
                    edtTime.hint = context.getString(R.string.str_time_zero)
                    if (hasFocus && depTime == context.getString(R.string.str_time_zero)) {
                        edtTime.setSelectAllOnFocus(true)
                        edtTime.selectAll()
                    }
                }
            }
            ivTimeIcon.setOnClickListener { timeClickListener?.invoke() }
            ivTimeRemove.setOnClickListener {
                timeInMin = 0
                edtTime.setText("")
                updateTime()
            }
            ivTimeDiffRemove.setOnClickListener {
                timeDiffInMin = 0
                edtTimeDiff.setText("")
                updateTime()
            }
            tvCaption.setOnClickListener {
                utcTime = !utcTime
                changeUtcLocal()
            }

            edtTimeDiff.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    edtTimeDiff.setSelectAllOnFocus(false)
                    edtTimeDiff.setText(correctedTimeDiff?.strTime)
                    updateTime()
                }
                val depTime = edtTimeDiff.text.toString()
                if (depTime.isBlank()) {
                    if (hasFocus) {
                        edtTimeDiff.hint = context.getString(R.string.str_time_zero)
                    } else {
                        edtTimeDiff.hint = context.getString(R.string.str_time_zero)
                    }
                } else {
                    edtTimeDiff.hint = context.getString(R.string.str_time_zero)
                    if (hasFocus && depTime == context.getString(R.string.str_time_zero)) {
                        edtTimeDiff.setSelectAllOnFocus(true)
                        edtTimeDiff.selectAll()
                    }
                }
            }
        }
    }

    private var textChangedListener: ((Int) -> Unit)? = null
    private var onFocusChangeListener: ((Int) -> Unit)? = null
    private var timeClickListener: (() -> Unit)? = null

    fun setDateChangedListener(listener: (Int) -> Unit) {
        textChangedListener = listener
    }

    fun setFocusChangedListener(listener: (Int) -> Unit) {
        onFocusChangeListener = listener
    }

    fun setTimeIconClickListener(listener: () -> Unit) {
        timeClickListener = listener
    }

    private fun changeUtcLocal() {
        binding.tvCaption.text =
            context.getString(
                if (utcTime) {
                    R.string.utc_time
                } else {
                    R.string.local_time
                }
            )
        binding.tvLocalTimeDiff.isVisible = !utcTime
        binding.ivTimeDiffRemove.isVisible = !utcTime
        binding.edtTimeDiff.isVisible = !utcTime

    }

    fun refreshRemoveIconVisible() {
        binding.ivTimeRemove.isVisible = binding.edtTime.text.isNotBlank()
    }

    private fun refreshRemoveTimeDiffIconVisible() {
        binding.ivTimeDiffRemove.isVisible = binding.edtTimeDiff.text.isNotBlank()
    }

    fun setText(text: CharSequence?) {
        with(binding.edtTime) {
            if (this.text != text) {
                removeTextChangedListener(mainTimeUpdateListener)
                setText(text)
                addTextChangedListener(mainTimeUpdateListener)
            }
        }
        refreshRemoveIconVisible()
        refreshRemoveTimeDiffIconVisible()
    }

    fun setTime(time: Int) {
        timeInMin = time
        setText(DateTimeUtils.strLogTime(time))
    }
}
