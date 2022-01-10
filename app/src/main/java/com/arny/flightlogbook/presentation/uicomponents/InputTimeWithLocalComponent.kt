package com.arny.flightlogbook.presentation.uicomponents

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
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
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var correctedTime: CorrectedTimePair? = null
    private var correctedTimeDiff: CorrectedTimePair? = null
    private var timeInMin = 0
    private var timeDiffInMin = 0
    private var utcTime = true

    private val binding =
        InputTimeWithLocalComponentBinding.inflate(LayoutInflater.from(context), this)

    val edtTime: EditText
        get() = binding.edtTime

    private val mainTimeUpdateListener = binding.edtTime.doAfterTextChanged { updateTime() }
    private val timeDiffUpdateListener = binding.edtTimeDiff.doAfterTextChanged { updateTime() }
    private var editorActionListener: ((actionId: Int) -> Unit)? = null
    private var textChangedListener: ((Int) -> Unit)? = null
    private var onFocusChangeListener: ((Int) -> Unit)? = null
    private var timeClickListener: (() -> Unit)? = null

    init {
        val att = context.obtainStyledAttributes(
            attrs, R.styleable.InputTimeWithLocalComponent, defStyleAttr, 0
        )
        setImeOptions(
            att.getBoolean(
                R.styleable.InputTimeWithLocalComponent_imeTypeDone,
                false
            )
        )
        att.recycle()
        changeUtcLocal()
        val utcTimeString = context.getString(R.string.utc_time)
        val timeZero = context.getString(R.string.str_time_zero)
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
                        edtTime.hint = utcTimeString
                        edtTime.hint = timeZero
                    } else {
                        edtTime.hint = null
                        edtTime.hint = timeZero
                    }
                } else {
                    edtTime.hint = timeZero
                    if (hasFocus && depTime == timeZero) {
                        edtTime.setSelectAllOnFocus(true)
                        edtTime.selectAll()
                    }
                }
            }
            ivTimeIcon.setOnClickListener { timeClickListener?.invoke() }
            ivTimeRemove.setOnClickListener {
                timeInMin = 0
                edtTime.setText("")
            }
            ivTimeDiffRemove.setOnClickListener {
                timeDiffInMin = 0
                edtTimeDiff.setText("")
            }
            tvCaption.setOnClickListener {
                this@InputTimeWithLocalComponent.utcTime = !this@InputTimeWithLocalComponent.utcTime
                changeUtcLocal()
            }
            edtTimeDiff.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    edtTimeDiff.setSelectAllOnFocus(false)
                    edtTimeDiff.setText(getStrTimeDiff())
                    updateTime()
                }
                val depTime = edtTimeDiff.text.toString()
                if (depTime.isBlank()) {
                    if (hasFocus) {
                        edtTimeDiff.hint = timeZero
                    } else {
                        edtTimeDiff.hint = timeZero
                    }
                } else {
                    edtTimeDiff.hint = timeZero
                    if (hasFocus && depTime == timeZero) {
                        edtTimeDiff.setSelectAllOnFocus(true)
                        edtTimeDiff.selectAll()
                    }
                }
            }
            edtTime.setOnEditorActionListener { _, actionId, _ ->
                editorActionListener?.invoke(actionId)
                true
            }
        }
    }

    private fun getStrTimeDiff(): String {
        var result = ""
        if (correctedTimeDiff?.sign != 1) {
            result += "-"
        }
        result += correctedTimeDiff?.strTime
        return result
    }

    private fun updateTime() {
        correctedTimeDiff =
            getCorrectLocalDiffDayTime(binding.edtTimeDiff.text.toString(), timeDiffInMin)
        timeDiffInMin = correctedTimeDiff?.intTime ?: 0
        timeInMin = correctedTime?.intTime ?: 0
        correctedTime = getCorrectDayTime(binding.edtTime.text.toString(), timeInMin)
        timeInMin = correctedTime?.intTime ?: 0
        val sign = correctedTimeDiff?.sign ?: 1
        val timeDifferent = timeInMin - (timeDiffInMin * sign)
        textChangedListener?.invoke(timeDifferent)
        refreshRemoveIconVisible()
        refreshRemoveTimeDiffIconVisible()
    }

    private fun setImeOptions(useDone: Boolean = false) {
        binding.edtTime.imeOptions = if (useDone)
            EditorInfo.IME_ACTION_DONE
        else
            EditorInfo.IME_ACTION_NEXT
    }

    fun setDateChangedListener(listener: (Int) -> Unit) {
        textChangedListener = listener
    }

    fun setOnEditorActionListener(listener: (actionId: Int) -> Unit) {
        this.editorActionListener = listener
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
        binding.ivTimeDiffRemove.isVisible =
            binding.edtTimeDiff.isVisible && binding.edtTimeDiff.text.isNotBlank()
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
