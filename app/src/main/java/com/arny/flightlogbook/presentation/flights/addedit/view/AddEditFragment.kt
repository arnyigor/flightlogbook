package com.arny.flightlogbook.presentation.flights.addedit.view

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.arny.core.AirportRequestType
import com.arny.core.CONSTS
import com.arny.core.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.databinding.FAddeditBinding
import com.arny.flightlogbook.domain.models.Airport
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.common.getName
import com.arny.flightlogbook.presentation.flights.addedit.presenter.AddEditPresenter
import com.arny.flightlogbook.presentation.uicomponents.InputTimeComponent
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.collections.ArrayList

class AddEditFragment : BaseMvpFragment(), AddEditView,
    CalendarDatePickerDialogFragment.OnDateSetListener,
    View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    private val args: AddEditFragmentArgs by navArgs()
    private lateinit var binding: FAddeditBinding
    private var timeInput: InputTimeComponent? = null
    private var customFieldValuesAdapter: CustomFieldValuesAdapter? = null
    private var tvMotoResult: TextView? = null

    @Inject
    lateinit var presenterProvider: Provider<AddEditPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }
    private val requestPermissionSaveData =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!shouldShowRequestPermissionRationale(PERMISSION)) {
                presenter.revokeSafeFile()
            }
            saveDataFlight()
        }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter.flightId = args.flightId
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            R.id.action_save -> {
                binding.root.requestFocus()
                presenter.checkAutoExportFile()
                true
            }
            R.id.action_remove -> {
                alertDialog(
                    context = requireContext(),
                    title = getString(R.string.str_delete),
                    btnCancelText = getString(R.string.str_cancel),
                    onConfirm = { presenter.removeFlight() }
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
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
            fabSave.setOnClickListener {
                binding.root.requestFocus()
                presenter.checkAutoExportFile()
            }
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
            nsvContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                val dy = scrollY - oldScrollY
                when {
                    dy > 0 && !fabSave.isShown -> fabSave.show()
                    dy < 0 && fabSave.isShown -> fabSave.hide()
                }
            })
            fabSave.hide()
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
            onValueChange = { item, value, position ->
                presenter.onCustomFieldValueChange(item, value, position)
            },
            onValueRemove = { item, position ->
                presenter.onCustomFieldValueDelete(item, position)
            },
            onValueTimeInChanges = { hasFocus ->
                presenter.onCustomFieldTimeInChanges(hasFocus)
            }
        )
        binding.rvCustomFields.apply {
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun isAutoMeasureEnabled(): Boolean = true
            }
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
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
        setFragmentResultListener(AirportRequestType.DEPARTURE.toString()) { _, data ->
            presenter.setDeparture(data.getParcelable(CONSTS.EXTRAS.EXTRA_AIRPORT))
        }
        setFragmentResultListener(AirportRequestType.ARRIVAL.toString()) { _, data ->
            presenter.setArrival(data.getParcelable(CONSTS.EXTRAS.EXTRA_AIRPORT))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivDate -> {
                CalendarDatePickerDialogFragment()
                    .setOnDateSetListener(this@AddEditFragment)
                    .show(childFragmentManager, null)
            }
            R.id.select_plane_type -> {
                requireView().findNavController().navigate(
                    AddEditFragmentDirections.actionAddEditFragmentToNavPlaneTypes(
                        isRequestField = true
                    )
                )
            }
            R.id.btnSelectFlightType -> {
                requireView().findNavController().navigate(
                    AddEditFragmentDirections.actionAddEditFragmentToNavFlightTypes(isRequestField = true)
                )
            }
            R.id.btnMoto -> showMotoDialog()
            R.id.vColor,
            R.id.tvColor -> presenter.colorClick()
            R.id.ivRemoveColor -> presenter.removeColor()
            R.id.btnAddField -> {
                findNavController().navigate(
                    AddEditFragmentDirections.actionAddEditFragmentToNavFields(isRequestField = true)
                )
            }
            R.id.tvDeparture -> {
                findNavController().navigate(
                    AddEditFragmentDirections.actionAddEditFragmentToNavAirports(
                        isRequest = true,
                        requestType = AirportRequestType.DEPARTURE
                    )
                )
            }
            R.id.tvArrival -> {
                findNavController().navigate(
                    AddEditFragmentDirections.actionAddEditFragmentToNavAirports(
                        isRequest = true,
                        requestType = AirportRequestType.ARRIVAL
                    )
                )
            }
        }
    }

    override fun setPlaneTypeTitle(planeType: PlaneType?) {
        binding.tvAirplaneType.text = String.format(
            Locale.getDefault(),
            "%s\n%s",
            getString(R.string.str_type),
            planeType?.getName(requireContext())
        )
    }

    override fun setToolbarTitle(title: Int) {
        this.title = getString(title)
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
            negativePair = Pair(R.string.str_cancel) { dialog -> dialog.cancel() },
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
        setFragmentResult(
            CONSTS.EXTRAS.EXTRA_ACTION_EDIT_FLIGHT,
            bundleOf()
        )
        requireView().findNavController().popBackStack()
    }

    override fun setFligtTypeTitle(title: String) {
        binding.tvFlightType.text = title
    }

    override fun setIfrSelected(selected: Boolean) {
        binding.radioGroupIfrVfr.check(if (selected) R.id.rbIfr else R.id.rbVfr)
    }

    override fun setFieldsList(list: List<CustomFieldValue>) {
        customFieldValuesAdapter?.submitList(list.toMutableList())
    }

    override fun notifyItemChanged(position: Int) {
        customFieldValuesAdapter?.notifyItemChanged(position)
    }

    override fun notifyItemRemoved(position: Int) {
        customFieldValuesAdapter?.notifyItemRemoved(position)
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

    override fun setIvLockFlightTimeIcon(@DrawableRes icon: Int) {
        binding.ivLockedFlightTime.setImageDrawable(requireContext().getDrawableCompat(icon))
    }

    override fun setIvLockDepartureTimeIcon(@DrawableRes icon: Int) {
        binding.ivLockedDepartureTime.setImageDrawable(requireContext().getDrawableCompat(icon))
    }

    override fun setIvLockArrivalTimeIcon(@DrawableRes icon: Int) {
        binding.ivLockedArrivalTime.setImageDrawable(requireContext().getDrawableCompat(icon))
    }

    private companion object {
        const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}
