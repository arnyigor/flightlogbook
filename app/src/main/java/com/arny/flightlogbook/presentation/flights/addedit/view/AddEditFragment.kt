package com.arny.flightlogbook.presentation.flights.addedit.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.arny.core.CONSTS
import com.arny.core.utils.*
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.databinding.FAddeditBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.flights.addedit.presenter.AddEditPresenter
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import com.arny.flightlogbook.presentation.uicomponents.InputTimeComponent
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import moxy.ktx.moxyPresenter

class AddEditFragment : BaseMvpFragment(), AddEditView,
    CalendarDatePickerDialogFragment.OnDateSetListener,
    View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    companion object {
        private const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        fun getInstance(bundle: Bundle? = null) = AddEditFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private lateinit var binding: FAddeditBinding
    private var timeInput: InputTimeComponent? = null
    private var customFieldValuesAdapter: CustomFieldValuesAdapter? = null
    private var currentTitle = R.string.str_add_flight
    private var tvMotoResult: TextView? = null
    private var appRouter: AppRouter? = null
    private val presenter by moxyPresenter { AddEditPresenter() }
    private val requestPermissionSaveData =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!shouldShowRequestPermissionRationale(PERMISSION)) {
                presenter.revokeSafeFile()
            }
            saveDataFlight()
        }

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
        presenter.flightId = getExtra<Long>(CONSTS.DB.COLUMN_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FAddeditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initResultListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> presenter.checkAutoExportFile()
            R.id.action_remove -> {
                alertDialog(
                    context = requireContext(),
                    title = getString(R.string.str_delete),
                    btnCancelText = getString(R.string.str_cancel),
                    onConfirm = { presenter.removeFlight() }
                )
            }
        }
        return true
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        timeInput?.setTime((hourOfDay * 60) + minute)
        timeInput?.requestFocus()
        timeInput?.rootView?.requestFocus()
    }

    private fun openTimeDialog(input: InputTimeComponent) {
        this.timeInput = input
        TimePickerDialog(requireContext(), this, 0, 0, true).show()
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(requireContext(), msg)
    }

    override fun setTotalTime(total: String) {
        binding.tvTotalTime.text = total
    }

    private fun initUI() {
        with(binding) {
            selectPlaneType.setOnClickListener(this@AddEditFragment)
            btnSelectFlightType.setOnClickListener(this@AddEditFragment)
            btnMoto.setOnClickListener(this@AddEditFragment)
            ivDate.setOnClickListener(this@AddEditFragment)
            tvColor.setOnClickListener(this@AddEditFragment)
            tvDeparture.setOnClickListener(this@AddEditFragment)
            tvArrival.setOnClickListener(this@AddEditFragment)
            vColor.setOnClickListener(this@AddEditFragment)
            ivRemoveColor.setOnClickListener(this@AddEditFragment)
            radioGroupIfrVfr.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rbVfr -> presenter.setVfrIfr(0)
                    else -> presenter.setVfrIfr(1)
                }
            }
            btnAddField.setOnClickListener(this@AddEditFragment)
        }
        onDateTimeChanges()
        onDepartureTimeChanges()
        onArrivalTimeChanges()
        onFlightTimeChanges()
        onNightTimeChanges()
        onGroundTimeChanges()
        onCustomViewsInit()
    }

    private fun onDepartureTimeChanges() {
        with(binding) {
            edtDepartureTime.setTimeIconClickListener {
                openTimeDialog(edtDepartureTime)
            }
            edtDepartureTime.setDateChangedListener { localTime ->
                presenter.setDepartureTime(localTime)
            }
            edtDepartureTime.setOnEditorActionListener { actionId ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        edtArrivalTime.requestFocus()
                    }
                }
            }
        }
    }

    private fun onArrivalTimeChanges() {
        with(binding) {
            edtArrivalTime.setTimeIconClickListener {
                openTimeDialog(edtArrivalTime)
            }
            edtArrivalTime.setDateChangedListener { utc ->
                presenter.setArrivalTime(utc)
            }
            edtArrivalTime.setOnEditorActionListener { actionId ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        edtNightTime.requestFocus()
                    }
                }
            }
        }
    }

    private fun onFlightTimeChanges() {
        with(binding) {
            edtFlightTime.setTimeIconClickListener {
                openTimeDialog(edtFlightTime)
            }
            edtFlightTime.setDateChangedListener { utc ->
                presenter.setFlightTime(utc)
            }
            edtFlightTime.setOnEditorActionListener { actionId ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        edtNightTime.requestFocus()
                    }
                }
            }
        }
    }

    private fun onCustomViewsInit() {
        customFieldValuesAdapter = CustomFieldValuesAdapter(
            onValueChange = { item, value ->
                presenter.onCustomFieldValueChange(item, value)
            },
            onValueRemove = { position ->
                presenter.onCustomFieldValueDelete(position)
            }
        )
        binding.rvCustomFields.apply {
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically(): Boolean = false
            }
            adapter = customFieldValuesAdapter
        }
    }

    private fun onNightTimeChanges() {
        with(binding) {
            edtNightTime.setTimeIconClickListener {
                openTimeDialog(edtNightTime)
            }
            edtNightTime.setDateChangedListener {
                presenter.setNightTime(it)
            }
            edtNightTime.setOnEditorActionListener { actionId ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        edtGroundTime.requestFocus()
                    }
                }
            }
        }
    }

    private fun onGroundTimeChanges() {
        with(binding) {
            edtGroundTime.setTimeIconClickListener {
                openTimeDialog(edtGroundTime)
            }
            edtGroundTime.setDateChangedListener {
                presenter.setGroundTime(it)
            }
            edtGroundTime.setOnEditorActionListener { actionId ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        edtGroundTime.edtTime.hideSoftKeyboard()
                    }
                }
            }
        }
    }

    private fun onDateTimeChanges() = with(binding) {
        tiedtDate.setOnFocusChangeListener { _, hasFocus ->
            val empty = Utility.empty(tiedtDate.text.toString())
            if (empty) {
                if (hasFocus) {
                    tilDate.hint = getString(R.string.str_date)
                    tiedtDate.hint = getString(R.string.str_date_format)
                } else {
                    tilDate.hint = null
                    tiedtDate.hint = getString(R.string.str_date)
                }
            } else {
                tilDate.hint = getString(R.string.str_date)
                tiedtDate.hint = getString(R.string.str_date)
                if (!hasFocus) {
                    val dat = tiedtDate.text.toString()
                    val pattern = "^(3[01]|[12][0-9]|0[1-9]).(1[0-2]|0[1-9]).[0-9]{4}\$".toRegex()
                    val containsMatchIn = pattern.containsMatchIn(dat)
                    if (!containsMatchIn) {
                        tiedtDate.setText("")
                        ToastMaker.toastError(
                            requireContext(),
                            getString(R.string.date_time_input_error)
                        )
                    }
                }
            }
        }
        tiedtDate.addTextChangedListener(
            MaskedTextChangedListener(
                "[00].[00].[0000]",
                ArrayList(),
                false,
                tiedtDate,
                object : _TextWatcher {
                    override fun afterTextChanged(s: Editable) {
                        if (Utility.empty(tiedtDate.text.toString())) {
                            tilDate.hint = getString(R.string.str_date)
                            tiedtDate.hint = null
                        }
                    }
                },
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(
                        maskFilled: Boolean,
                        extractedValue: String,
                        formattedValue: String
                    ) {
                        if (maskFilled && tiedtDate.isFocused) {
                            presenter.initDateFromMask(extractedValue)
                        }
                    }
                })
        )
    }

    private fun initResultListeners() {
        setFragmentResultListener(CONSTS.REQUESTS.REQUEST_PLANE_TYPE) { _, data ->
            presenter.setFlightPlaneType(data.getExtra(CONSTS.EXTRAS.EXTRA_PLANE_TYPE_ID))
        }
        setFragmentResultListener(CONSTS.REQUESTS.REQUEST_FLIGHT_TYPE) { _, data ->
            presenter.setFlightType(data.getExtra(CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE))
        }
        setFragmentResultListener(CONSTS.REQUESTS.REQUEST_CUSTOM_FIELD) { _, data ->
            presenter.addCustomField(data.getExtra(CONSTS.EXTRAS.EXTRA_CUSTOM_FIELD_ID))
        }
        setFragmentResultListener(CONSTS.REQUESTS.REQUEST_AIRPORT_DEPARTURE) { _, data ->
            presenter.setDeparture(data.getParcelable(CONSTS.EXTRAS.EXTRA_AIRPORT))
        }
        setFragmentResultListener(CONSTS.REQUESTS.REQUEST_AIRPORT_ARRIVAL) { _, data ->
            presenter.setArrival(data.getParcelable(CONSTS.EXTRAS.EXTRA_AIRPORT))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivDate -> {
                val cdp = CalendarDatePickerDialogFragment()
                    .setOnDateSetListener(this@AddEditFragment)
                cdp.show(childFragmentManager, "fragment_date_picker_name")
            }
            R.id.select_plane_type -> {
                appRouter?.navigateTo(
                    NavigateItems.PLANE_TYPE_SELECT,
                    true,
                    bundleOf(CONSTS.REQUESTS.REQUEST to true),
                    requestCode = CONSTS.REQUESTS.REQUEST_SELECT_PLANE_TYPE,
                    targetFragment = this@AddEditFragment
                )
            }
            R.id.btnSelectFlightType -> {
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
            R.id.tvColor -> presenter.colorClick()
            R.id.ivRemoveColor -> presenter.removeColor()
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
                    bundleOf(
                        CONSTS.REQUESTS.REQUEST to true,
                        CONSTS.REQUESTS.REQUEST_AIRPORT to CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_DEPARTURE
                    ),
                    targetFragment = this@AddEditFragment
                )
            }
            R.id.tvArrival -> {
                appRouter?.navigateTo(
                    NavigateItems.AIRPORT_SELECT,
                    true,
                    bundleOf(
                        CONSTS.REQUESTS.REQUEST to true,
                        CONSTS.REQUESTS.REQUEST_AIRPORT to CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_ARRIVAL
                    ),
                    requestCode = CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_ARRIVAL,
                    targetFragment = this@AddEditFragment
                )
            }
        }
    }

    override fun setPlaneTypeTitle(title: String?) {
        binding.tvAirplaneType.text = title
    }

    override fun setToolbarTitle(title: Int) {
        currentTitle = title
        updateTitle()
    }

    override fun setEdtFlightTimeText(strLogTime: String?) {
        binding.edtFlightTime.setText(strLogTime)
    }

    override fun setEdtNightTimeText(nightTimeText: String) {
        binding.edtNightTime.setText(nightTimeText)
    }

    override fun setDescription(desc: String) {
        binding.edtDesc.setText(desc)
    }

    override fun setDate(date: String) {
        binding.tiedtDate.setText(date)
    }

    override fun setEdtGroundTimeText(groundTimeText: String) {
        binding.edtGroundTime.setText(groundTimeText)
    }

    override fun requestStorageAndSave() {
        requestPermissionSaveData.launch(PERMISSION)
    }

    private fun saveDataFlight() {
        presenter.saveFlight(binding.edtDesc.text.toString())
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
            positivePair = Pair(R.string.str_ok) { dialog ->
                dialog.dismiss()
                presenter.setMotoResult()
            },
            negativePair = Pair(R.string.str_cancel, { dialog -> dialog.cancel() }),
        ) {
            val edtMotoStart = findViewById<EditText>(R.id.edtStartMoto)
            val edtMotoFinish = findViewById<EditText>(R.id.edtFinishMoto)
            tvMotoResult = findViewById(R.id.tvMotoresult)
            edtMotoStart.doAfterTextChanged { edt ->
                if (edtMotoStart.isFocused) {
                    presenter.onMotoTimeChange(edt.toString(), edtMotoFinish.text.toString())
                }
            }
            edtMotoFinish.doAfterTextChanged { edt ->
                if (edtMotoFinish.isFocused) {
                    presenter.onMotoTimeChange(edtMotoStart.text.toString(), edt.toString())
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
        binding.tvFlightType.text = title
    }

    override fun setIfrSelected(selected: Boolean) {
        binding.radioGroupIfrVfr.check(if (selected) R.id.rbIfr else R.id.rbVfr)
    }

    override fun setFieldsList(list: List<CustomFieldValue>) {
        customFieldValuesAdapter?.submitList(list)
        binding.rvCustomFields.requestLayout()
    }

    override fun setViewColor(color: Int) {
        binding.vColor.setBackgroundColor(color)
    }

    override fun setRemoveColorVisible(visible: Boolean) {
        binding.ivRemoveColor.isVisible = visible
    }

    override fun onDateSet(
        dialog: CalendarDatePickerDialogFragment,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) {
        presenter.onDateSet(dayOfMonth, monthOfYear, year)
    }

    @SuppressLint("CheckResult")
    override fun onColorSelect(colors: IntArray) {
        MaterialDialog(requireContext()).show {
            title(R.string.select_color)
            colorChooser(colors, initialSelection = Color.BLUE) { _, color ->
                presenter.onColorSelected(color)
            }
            positiveButton(R.string.select)
        }
    }

    override fun setCustomFieldsVisible(visible: Boolean) {
        binding.rvCustomFields.isVisible = visible
        binding.btnAddField.isVisible = visible
    }

    override fun setDeparture(departure: Airport?) {
        binding.tvDeparture.text =
            getString(R.string.string_format_two_strings, departure?.iata, "(${departure?.icao})")

    }

    override fun setArrival(arrival: Airport?) {
        binding.tvArrival.text =
            getString(R.string.string_format_two_strings, arrival?.iata, "(${arrival?.icao})")
    }

    override fun setEdtDepUtcTime(depTime: Int) {
        binding.edtDepartureTime.setTime(depTime)
    }

    override fun setEdtArrUtcTime(arrTime: Int) {
        binding.edtArrivalTime.setTime(arrTime)
    }

}
