package com.arny.flightlogbook.presentation.uicomponents

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.utils.DateTimeUtils
import com.arny.flightlogbook.databinding.InputTimeWithLocalComponentBinding
import com.arny.flightlogbook.domain.models.CorrectedTimePair
import com.arny.flightlogbook.domain.models.getCorrectDayTime
import com.arny.flightlogbook.domain.models.getCorrectLocalDiffDayTime
import kotlin.math.abs

class InputTimeWithLocalComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var correctedUtcTime: CorrectedTimePair? = null
    private var correctedUtcDiff: CorrectedTimePair? = null
    private var utcTime = 0
    private var utcDiff = 0
    private var isUtcState = true

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
        val utcTimeString = context.getString(R.string.utc_time)
        val timeZero = context.getString(R.string.str_time_zero)
        with(binding) {
            edtTime.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    edtTime.setSelectAllOnFocus(false)
                    edtTime.setText(correctedUtcTime?.strTime)
                    onFocusChangeListener?.invoke(utcTime)
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
                utcTime = 0
                edtTime.setText("")
                updateTime()
            }
            ivTimeDiffRemove.setOnClickListener {
                utcDiff = 0
                edtTimeDiff.setText("")
                updateTime()
            }
            tvCaption.setOnClickListener {
                isUtcState = !isUtcState
                toggleUtcDiffVisible(isUtcState)
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
            edtTime.addTextChangedListener(mainTimeUpdateListener)
            edtTimeDiff.addTextChangedListener(timeDiffUpdateListener)
            toggleUtcDiffVisible(isUtcState)
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
            getCorrectLocalDiffDayTime(binding.edtTimeDiff.text.toString(), utcDiff)
        utcDiff = correctedUtcDiff?.intTime ?: 0
        utcTime = correctedUtcTime?.intTime ?: 0
        correctedUtcTime = getCorrectDayTime(binding.edtTime.text.toString(), utcTime)
        utcTime = correctedUtcTime?.intTime ?: 0
        val sign = correctedUtcDiff?.sign ?: 1
        val utcDiff = utcDiff * sign
        textChangedListener?.invoke(utcTime, utcDiff)
        refreshRemoveIconVisible()
        refreshRemoveTimeDiffIconVisible()
    }

    private fun recalculateUtcTime() {
        if (isUtcState) {
            utcTime = 0 // TODO смена локального и UTC времени
        } else {
            val sign = correctedUtcDiff?.sign ?: 1
            val utcDiffSigned = utcDiff * sign
            utcTime -= utcDiffSigned
        }
        updateTime()
        setText(DateTimeUtils.strLogTime(utcTime))
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

    private fun toggleUtcDiffVisible(visible: Boolean) {
        binding.tvCaption.text =
            context.getString(
                if (visible) {
                    R.string.utc_time
                } else {
                    R.string.local_time
                }
            )
        binding.tvLocalTimeDiff.isVisible = !visible
        binding.ivTimeDiffRemove.isVisible = !visible
        binding.edtTimeDiff.isVisible = !visible
        recalculateUtcTime()
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
        utcTime = time
        setText(DateTimeUtils.strLogTime(time))
    }

    fun setUtcDiff(diff: Int) {
        isUtcState = diff != 0
        if (isUtcState) {
            utcDiff = diff
            setUtcText(
                getSignStrTime(
                    signMinus = diff < 0,
                    time = DateTimeUtils.strLogTime(abs(diff))
                )
            )
        }
        toggleUtcDiffVisible(diff == 0)
    }
}
