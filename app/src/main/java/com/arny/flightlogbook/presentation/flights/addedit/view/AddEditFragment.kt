package com.arny.flightlogbook.presentation.flights.addedit.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.CustomRVLayoutManager
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.flights.addedit.presenter.AddEditPresenter
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import com.arny.helpers.interfaces._TextWatcher
import com.arny.helpers.utils.*
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.f_addedit.*
import moxy.ktx.moxyPresenter
import java.util.*

class AddEditFragment : BaseMvpFragment(), AddEditView,
        CalendarDatePickerDialogFragment.OnDateSetListener,
        View.OnClickListener {

    companion object {
        fun getInstance(bundle: Bundle? = null) = AddEditFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private val compositeDisposable = CompositeDisposable()
    private var customFieldValuesAdapter: CustomFieldValuesAdapter? = null
    private lateinit var rxPermissions: RxPermissions
    private var sFlightTime = ""
    private var sNightTime = ""
    private var sDepTime = ""
    private var sArrivalTime = ""
    private var sGroundTime = ""
    private var currentTitle = R.string.str_add_flight
    private var tvMotoResult: TextView? = null
    private var appRouter: AppRouter? = null

    private val addEditPresenter by moxyPresenter { AddEditPresenter() }

    override fun getLayoutId(): Int = R.layout.f_addedit

    override fun getTitle(): String = getString(currentTitle)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        addEditPresenter.flightId = getExtra<Long>(CONSTS.DB.COLUMN_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rxPermissions = RxPermissions(this)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> addEditPresenter.checkAutoExportFile()
            R.id.action_remove -> {
                alertDialog(
                        context = requireContext(),
                        title = getString(R.string.str_delete),
                        btnCancelText = getString(R.string.str_cancel),
                        onConfirm = { addEditPresenter.removeFlight() }
                )
            }
        }
        return true
    }

    override fun setTotalFlightTime(flightTime: String) {
        tvTotalTime.text = flightTime
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(requireContext(), msg)
    }

    override fun setTotalTime(total: String) {
        tvTotalTime.text = total
    }

    private fun initUI() {
        select_plane_type.setOnClickListener(this)
        btnSelectFlightType.setOnClickListener(this)
        btnMoto.setOnClickListener(this)
        ivDate.setOnClickListener(this)
        tvColor.setOnClickListener(this)
        tvDeparture.setOnClickListener(this)
        tvArrival.setOnClickListener(this)
        vColor.setOnClickListener(this)
        ivRemoveColor.setOnClickListener(this)
        radioGroupIfrVfr.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbVfr -> addEditPresenter.setVfrIfr(0)
                else -> addEditPresenter.setVfrIfr(1)
            }
        }
        btnAddField.setOnClickListener(this)
        onDateTimeChanges()
        onFlightTimeChanges()
        onNightTimeChanges()
        onGroundTimeChanges()
        onDepartureTimeChanges()
        onArrivalTimeChanges()
        onCustomViewsInit()
    }

    private fun onDepartureTimeChanges() {
        val timeZero = getTimeZero()
        edtDepartureTime.addTextChangedListener {
            if (it.toString().isBlank()) {
                edtDepartureTime.hint = timeZero
            }
            sDepTime = it.toString()
        }
        edtDepartureTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                edtDepartureTime.setSelectAllOnFocus(false)
                addEditPresenter.correctDepartureTime(sDepTime)
            }
            val depTime = edtDepartureTime.text.toString()
            if (depTime.isBlank()) {
                if (hasFocus) {
                    tilDepTime?.hint = getString(R.string.utc_time)
                    edtDepartureTime?.hint = timeZero
                } else {
                    tilDepTime?.hint = null
                    edtDepartureTime?.hint = timeZero
                }
            } else {
                tilDepTime?.hint = getString(R.string.utc_time)
                edtDepartureTime?.hint = timeZero
                if (hasFocus && depTime == timeZero) {
                    edtDepartureTime.setSelectAllOnFocus(true)
                    edtDepartureTime.selectAll()
                }
            }
        }
        edtDepartureTime.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    addEditPresenter.correctDepartureTime(sDepTime)
                    edtArrivalTime.requestFocus()
                    true
                }
                else -> false
            }
        }
    }

    private fun onArrivalTimeChanges() {
        val timeZero = getTimeZero()
        edtArrivalTime.addTextChangedListener {
            if (it.toString().isBlank()) {
                edtArrivalTime.hint = timeZero
            }
            sArrivalTime = it.toString()
        }
        edtArrivalTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                edtArrivalTime.setSelectAllOnFocus(false)
                addEditPresenter.correctArrivalTime(sArrivalTime)
            }
            val arrivalTime = edtArrivalTime.text.toString()
            if (arrivalTime.isBlank()) {
                if (hasFocus) {
                    tilArrTime?.hint = getString(R.string.utc_time)
                    edtArrivalTime?.hint = timeZero
                } else {
                    tilArrTime?.hint = null
                    edtArrivalTime?.hint = timeZero
                }
            } else {
                if (hasFocus && arrivalTime == timeZero) {
                    edtArrivalTime.setSelectAllOnFocus(true)
                    edtArrivalTime.selectAll()
                }
            }
        }
        edtArrivalTime.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    addEditPresenter.correctArrivalTime(sArrivalTime)
                    edtFlightTime.requestFocus()
                    true
                }
                else -> false
            }
        }
    }

    private fun onCustomViewsInit() {
        customFieldValuesAdapter =
                CustomFieldValuesAdapter(object : CustomFieldValuesAdapter.OnFieldChangeListener {
                    override fun onValueChange(item: CustomFieldValue, value: String) {
                        addEditPresenter.onCustomFieldValueChange(item, value)
                    }

                    override fun onValueRemove(position: Int, item: CustomFieldValue) {
                        addEditPresenter.onCustomFieldValueDelete(position)
                    }
                })
        rvCustomFields.apply {
            layoutManager = CustomRVLayoutManager(requireContext()).apply {
                setScrollEnabled(false)
            }
            adapter = customFieldValuesAdapter
        }
    }

    private fun onNightTimeChanges() {
        val timeZero = getTimeZero()
        edtNightTime.addTextChangedListener {
            if (it.toString().isBlank()) {
                edtNightTime.hint = timeZero
            }
            sNightTime = it.toString()
        }
        edtNightTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                edtNightTime.setSelectAllOnFocus(false)
                addEditPresenter.correctNightTime(sNightTime)
            }
            val nightTime = edtNightTime.text.toString()
            if (nightTime.isBlank()) {
                if (hasFocus) {
                    edtNightTime?.hint = timeZero
                } else {
                    edtNightTime?.hint = null
                }
            } else {
                if (hasFocus && nightTime == timeZero) {
                    edtNightTime.setSelectAllOnFocus(true)
                    edtNightTime.selectAll()
                }
            }
        }
        edtNightTime.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    addEditPresenter.correctNightTime(sNightTime)
                    edtGroundTime.requestFocus()
                    true
                }
                else -> false
            }
        }
    }

    private fun onGroundTimeChanges() {
        val timeZero = getTimeZero()
        edtGroundTime.addTextChangedListener {
            if (it.toString().isBlank()) {
                edtGroundTime.hint = timeZero
            }
            sGroundTime = it.toString()
        }
        edtGroundTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                edtGroundTime.setSelectAllOnFocus(false)
                addEditPresenter.correctGroundTime(sGroundTime)
            }
            val grTime = edtGroundTime.text.toString()
            if (grTime.isBlank()) {
                if (hasFocus) {
                    edtGroundTime?.hint = timeZero
                } else {
                    edtGroundTime?.hint = null
                }
            } else {
                if (hasFocus && grTime == timeZero) {
                    edtGroundTime.setSelectAllOnFocus(true)
                    edtGroundTime.selectAll()
                }
            }
        }
        edtGroundTime.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    addEditPresenter.correctGroundTime(sGroundTime)
                    Utility.hideSoftKeyboard(requireContext())
                    true
                }
                else -> false
            }
        }
    }

    private fun onDateTimeChanges() {
        tiedt_date?.setOnFocusChangeListener { _, hasFocus ->
            val empty = Utility.empty(tiedt_date?.text.toString())
            if (empty) {
                if (hasFocus) {
                    tilDate?.hint = getString(R.string.str_date)
                    tiedt_date?.hint = getString(R.string.str_date_format)
                } else {
                    tilDate?.hint = null
                    tiedt_date?.hint = getString(R.string.str_date)
                }
            } else {
                tilDate?.hint = getString(R.string.str_date)
                tiedt_date?.hint = getString(R.string.str_date)
                if (!hasFocus) {
                    val dat = tiedt_date?.text.toString()
                    val pattern = "^(3[01]|[12][0-9]|0[1-9]).(1[0-2]|0[1-9]).[0-9]{4}\$".toRegex()
                    val containsMatchIn = pattern.containsMatchIn(dat)
                    if (!containsMatchIn) {
                        tiedt_date.setText("")
                        ToastMaker.toastError(
                                requireContext(),
                                getString(R.string.date_time_input_error)
                        )
                    }
                }
            }
        }
        tiedt_date?.addTextChangedListener(
                MaskedTextChangedListener(
                        "[00].[00].[0000]",
                        ArrayList(),
                        false,
                        tiedt_date,
                        object : _TextWatcher {
                            override fun afterTextChanged(s: Editable) {
                                if (Utility.empty(tiedt_date.text.toString())) {
                                    tilDate?.hint = getString(R.string.str_date)
                                    tiedt_date?.hint = null
                                }
                            }
                        },
                        object : MaskedTextChangedListener.ValueListener {
                            override fun onTextChanged(
                                    maskFilled: Boolean,
                                    extractedValue: String,
                                    formattedValue: String
                            ) {
                                if (maskFilled && tiedt_date.isFocused) {
                                    addEditPresenter.initDateFromMask(extractedValue)
                                }
                            }
                        })
        )
    }

    private fun onFlightTimeChanges() {
        val timeZero = getTimeZero()
        edtFlightTime.addTextChangedListener {
            if (it.toString().isBlank()) {
                edtFlightTime.hint = timeZero
            }
            sFlightTime = it.toString()
        }
        edtFlightTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                edtFlightTime.setSelectAllOnFocus(false)
                addEditPresenter.correctFlightTime(sFlightTime)
            }
            val flTime = edtFlightTime.text.toString()
            if (flTime.isBlank()) {
                if (hasFocus) {
                    edtFlightTime?.hint = timeZero
                } else {
                    edtFlightTime?.hint = null
                }
            } else {
                if (hasFocus && flTime == timeZero) {
                    edtFlightTime.setSelectAllOnFocus(true)
                    edtFlightTime.selectAll()
                }
            }
        }
        edtFlightTime.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    addEditPresenter.correctFlightTime(sFlightTime)
                    edtNightTime.requestFocus()
                    true
                }
                else -> false
            }
        }
    }

    private fun getTimeZero() = getString(R.string.str_time_zero)

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivDate -> {
                addEditPresenter.correctFlightTime(sFlightTime)
                val cdp = CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(this@AddEditFragment)
                cdp.show(childFragmentManager, "fragment_date_picker_name")
            }
            R.id.select_plane_type -> {
                addEditPresenter.correctFlightTime(sFlightTime)
                appRouter?.navigateTo(
                        NavigateItems.PLANE_TYPE_SELECT,
                        true,
                        bundleOf(CONSTS.REQUESTS.REQUEST to true),
                        requestCode = CONSTS.REQUESTS.REQUEST_SELECT_PLANE_TYPE,
                        targetFragment = this@AddEditFragment
                )
            }
            R.id.btnSelectFlightType -> {
                addEditPresenter.correctFlightTime(sFlightTime)
                appRouter?.navigateTo(
                        NavigateItems.FLIGHT_TYPE_SELECT,
                        true,
                        bundleOf(CONSTS.REQUESTS.REQUEST to true),
                        requestCode = CONSTS.REQUESTS.REQUEST_SELECT_FLIGHT_TYPE,
                        targetFragment = this@AddEditFragment
                )
            }
            R.id.btnMoto -> showMotoDialog()
            R.id.vColor,
            R.id.tvColor -> addEditPresenter.colorClick()
            R.id.ivRemoveColor -> addEditPresenter.removeColor()
            R.id.btnAddField -> {
                appRouter?.navigateTo(
                        NavigateItems.ITEM_SELECT_FIELD,
                        true,
                        bundleOf(CONSTS.REQUESTS.REQUEST to true),
                        requestCode = CONSTS.REQUESTS.REQUEST_SELECT_CUSTOM_FIELD,
                        targetFragment = this@AddEditFragment
                )
            }
            R.id.tvDeparture -> {
                appRouter?.navigateTo(
                        NavigateItems.AIRPORT_SELECT,
                        true,
                        bundleOf(CONSTS.REQUESTS.REQUEST to true),
                        requestCode = CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_DEPARTURE,
                        targetFragment = this@AddEditFragment
                )
            }
            R.id.tvArrival -> {
                appRouter?.navigateTo(
                        NavigateItems.AIRPORT_SELECT,
                        true,
                        bundleOf(CONSTS.REQUESTS.REQUEST to true),
                        requestCode = CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_ARRIVAL,
                        targetFragment = this@AddEditFragment
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_SELECT_PLANE_TYPE -> addEditPresenter.setFlightPlaneType(
                        data.getExtra<Long>(
                                CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID
                        )
                )
                CONSTS.REQUESTS.REQUEST_SELECT_FLIGHT_TYPE -> addEditPresenter.setFlightType(
                        data.getExtra<Long>(
                                CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE
                        )
                )
                CONSTS.REQUESTS.REQUEST_SELECT_CUSTOM_FIELD -> addEditPresenter.addCustomField(
                        data.getExtra<Long>(
                                CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID
                        )
                )
                CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_DEPARTURE -> addEditPresenter.setDeparture(
                        data?.getParcelableExtra(
                                CONSTS.EXTRAS.EXTRA_AIRPORT
                        )
                )
                CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_ARRIVAL -> addEditPresenter.setArrival(
                        data?.getParcelableExtra(
                                CONSTS.EXTRAS.EXTRA_AIRPORT
                        )
                )
            }
        }
    }

    override fun setPlaneTypeTitle(title: String?) {
        tvAirplaneType.text = title
    }

    override fun setToolbarTitle(title: Int) {
        currentTitle = title
        updateTitle()
    }

    override fun setEdtFlightTimeText(strLogTime: String?) {
        edtFlightTime.setText(strLogTime)
    }

    override fun setEdtNightTimeText(nightTimeText: String) {
        edtNightTime.setText(nightTimeText)
    }

    override fun setDescription(desc: String) {
        edtDesc.setText(desc)
    }

    override fun setDate(date: String) {
        tiedt_date.setText(date)
    }

    override fun setEdtGroundTimeText(groundTimeText: String) {
        edtGroundTime.setText(groundTimeText)
    }

    override fun requestStorageAndSave() {
        rxPermissions.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
                ?.subscribe { granted ->
                    if (granted) {
                        saveDataFlight()
                    }
                }?.addTo(compositeDisposable)
    }

    private fun saveDataFlight() {
        addEditPresenter.saveFlight(
                edtDesc.text.toString(),
                sFlightTime,
                sGroundTime,
                sNightTime
        )
    }

    override fun saveFlight() {
        saveDataFlight()
    }

    override fun setMotoTimeResult(motoTime: String?) {
        tvMotoResult?.text = motoTime
    }

    private fun showMotoDialog() {
        requireContext().createCustomLayoutDialog(
                layout = R.layout.moto,
                title = getString(R.string.str_moto),
                positivePair = Pair(R.string.str_ok, { dialog ->
                    dialog.dismiss()
                    addEditPresenter.setMotoResult()
                }),
                negativePair = Pair(R.string.str_cancel, { dialog -> dialog.cancel() }),
        ) {
            val edtMotoStart = findViewById<EditText>(R.id.edtStartMoto)
            val edtMotoFinish = findViewById<EditText>(R.id.edtFinishMoto)
            tvMotoResult = findViewById(R.id.tvMotoresult)
            edtMotoStart.doAfterTextChanged { edt ->
                if (edtMotoStart.isFocused) {
                    addEditPresenter.onMotoTimeChange(edt.toString(), edtMotoFinish.text.toString())
                }
            }
            edtMotoFinish.doAfterTextChanged { edt ->
                if (edtMotoFinish.isFocused) {
                    addEditPresenter.onMotoTimeChange(edtMotoStart.text.toString(), edt.toString())
                }
            }
        }
    }

    override fun toastSuccess(msg: String?) {
        ToastMaker.toastSuccess(requireContext(), msg)
    }

    override fun setResultOK() {
        appRouter?.onReturnResult(null, Activity.RESULT_OK)
    }

    override fun setFligtTypeTitle(title: String) {
        tvFlightType.text = title
    }

    override fun setIfrSelected(selected: Boolean) {
        radioGroupIfrVfr.check(if (selected) R.id.rbIfr else R.id.rbVfr)
    }

    override fun setFieldsList(list: List<CustomFieldValue>) {
        customFieldValuesAdapter?.addAll(list)
    }

    override fun notifyCustomFieldUpdate(item: CustomFieldValue) {
        customFieldValuesAdapter?.notifyDataSetChanged()
    }

    override fun setViewColor(color: Int) {
        vColor.setBackgroundColor(color)
    }

    override fun setRemoveColorVisible(visible: Boolean) {
        ivRemoveColor.isVisible = visible
    }

    override fun onDateSet(
            dialog: CalendarDatePickerDialogFragment,
            year: Int,
            monthOfYear: Int,
            dayOfMonth: Int
    ) {
        addEditPresenter.onDateSet(dayOfMonth, monthOfYear, year)
    }

    override fun onColorSelect(colors: IntArray) {
        MaterialDialog(requireContext()).show {
            title(R.string.select_color)
            colorChooser(colors, initialSelection = Color.BLUE) { _, color ->
                addEditPresenter.onColorSelected(color)
            }
            positiveButton(R.string.select)
        }
    }

    override fun setCustomFieldsVisible(visible: Boolean) {
        rvCustomFields.isVisible = visible
        btnAddField.isVisible = visible
    }

    override fun setDeparture(departure: Airport?) {
        tvDeparture.text =
                getString(R.string.string_format_two_strings, departure?.iata, "(${departure?.icao})")

    }

    override fun setArrival(arrival: Airport?) {
        tvArrival.text =
                getString(R.string.string_format_two_strings, arrival?.iata, "(${arrival?.icao})")
    }

    override fun setEdtDepTimeText(depTime: String) {
        edtDepartureTime.setText(depTime)
    }

    override fun setEdtArrTimeText(arrTime: String) {
        edtArrivalTime.setText(arrTime)
    }
}
