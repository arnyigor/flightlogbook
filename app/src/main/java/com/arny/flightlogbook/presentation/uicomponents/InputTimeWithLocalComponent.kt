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
import kotlin.math.abs

class InputTimeWithLocalComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var correctedTime: CorrectedTimePair? = null
    private var correctedUtcDiff: CorrectedTimePair? = null
    private var localTimeMin = 0
    private var utcDiffMin = 0
    private var utcTime = true

    private val binding =
        InputTimeWithLocalComponentBinding.inflate(LayoutInflater.from(context), this)

    val edtTime: EditText
        get() = binding.edtTime

    private val mainTimeUpdateListener = binding.edtTime.doAfterTextChanged { updateTime() }
    private val timeDiffUpdateListener = binding.edtTimeDiff.doAfterTextChanged { updateTime() }
    private var editorActionListener: ((actionId: Int) -> Unit)? = null
    private var textChangedListener: ((localTime: Int, utcDiff: Int) -> Unit)? = null
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
                    onFocusChangeListener?.invoke(localTimeMin)
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
                localTimeMin = 0
                edtTime.setText("")
                updateTime()
            }
            ivTimeDiffRemove.setOnClickListener {
                utcDiffMin = 0
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
                    edtTimeDiff.setText(
                        getSignStrTime(
                            signMinus = correctedUtcDiff?.sign != 1,
                            time = correctedUtcDiff?.strTime
                        )
                    )
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
            edtTimeDiff.setOnEditorActionListener { _, _, _ ->
                edtTime.requestFocus()
                true
            }
        }
    }

    private fun getSignStrTime(signMinus: Boolean, time: String?): String {
        var result = ""
        if (signMinus) {
            result += "-"
        }
        result += time
        return result
    }

    private fun updateTime() {
        correctedUtcDiff =
            getCorrectLocalDiffDayTime(binding.edtTimeDiff.text.toString(), utcDiffMin)
        utcDiffMin = correctedUtcDiff?.intTime ?: 0
        localTimeMin = correctedTime?.intTime ?: 0
        correctedTime = getCorrectDayTime(binding.edtTime.text.toString(), localTimeMin)
        localTimeMin = correctedTime?.intTime ?: 0
        val sign = correctedUtcDiff?.sign ?: 1
        val utcDiff = utcDiffMin * sign
        val localTime = localTimeMin
        textChangedListener?.invoke(localTime, utcDiff)
        refreshRemoveIconVisible()
        refreshRemoveTimeDiffIconVisible()
    }

    private fun setImeOptions(useDone: Boolean = false) {
        binding.edtTime.imeOptions = if (useDone)
            EditorInfo.IME_ACTION_DONE
        else
            EditorInfo.IME_ACTION_NEXT
    }

    fun setDateChangedListener(listener: (localTime: Int, timeDiff: Int) -> Unit) {
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
                refreshRemoveIconVisible()
                refreshRemoveTimeDiffIconVisible()
            }
        }
    }

    fun setUtcText(text: CharSequence?) {
        with(binding.edtTimeDiff) {
            if (this.text != text) {
                removeTextChangedListener(timeDiffUpdateListener)
                setText(text)
                addTextChangedListener(timeDiffUpdateListener)
                refreshRemoveIconVisible()
                refreshRemoveTimeDiffIconVisible()
            }
        }
    }

    fun setTime(time: Int) {
        localTimeMin = time
        setText(DateTimeUtils.strLogTime(time))
    }

    fun setUtcDiff(diff: Int) {
        utcTime = diff != 0
        if (utcTime) {
            utcDiffMin = diff
            setUtcText(
                getSignStrTime(
                    signMinus = diff < 0,
                    time = DateTimeUtils.strLogTime(abs(diff))
                )
            )
        }
        changeUtcLocal()
    }
}
