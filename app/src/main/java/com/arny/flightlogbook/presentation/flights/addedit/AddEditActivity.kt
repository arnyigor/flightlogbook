package com.arny.flightlogbook.presentation.flights.addedit

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color.BLUE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.arny.constants.CONSTS
import com.arny.domain.models.TimeToFlight
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.flighttypes.FlightTypesActivity
import com.arny.flightlogbook.presentation.planetypes.PlaneTypesActivity
import com.arny.helpers.interfaces._TextWatcher
import com.arny.helpers.utils.*
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_addedit.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class AddEditActivity :
        MvpAppCompatActivity(),
        AddEditView,
        CalendarDatePickerDialogFragment.OnDateSetListener,
        View.OnClickListener {
    private var dateMaskListener: MaskedTextChangedListener? = null
    private var tvMotoResult: TextView? = null
    private var sFlightTime = ""
    private var sNightTime = ""
    private var sGroundTime = ""
    @InjectPresenter
    lateinit var addEditPresenter: AddEditPresenter

    @ProvidePresenter
    fun provideAddEditPresenter() = AddEditPresenter()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addedit)
        setupActionBar(R.id.edit_toolbar) {
            title = getString(R.string.str_add_flight)
            this?.setDisplayHomeAsUpEnabled(true)
        }
        val flightId = getExtra<Long>(CONSTS.DB.COLUMN_ID)
        if (flightId != null) {
            supportActionBar?.title = getString(R.string.str_edt_flight)
        }
        initUI()
        addEditPresenter.initState(flightId)
    }

    private fun initUI() {
        select_plane_type.setOnClickListener(this)
        btnSelectFlightType.setOnClickListener(this)
        btn_moto.setOnClickListener(this)
        iv_date.setOnClickListener(this)
        onDateTimeChanges()
        onFlightTimeChanges()
        onNightTimeChanges()
        onGroundTimeChanges()
    }

    override fun setTitle(title: String?) {
        tiedt_title.setText(title)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
    }

    override fun setTotalFlightTime(flightTime: String) {
        tvTotalTime.text = flightTime
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(this, msg)
    }

    override fun setTotalTime(total: String) {
        tvTotalTime.text = total
    }

    private fun onNightTimeChanges() {
        edtNightTime.setInputMask {
            if (edtNightTime.text.toString().isBlank()) {
                edtNightTime.hint = getString(R.string.str_time_zero)
            }
            sNightTime = it
        }
        edtNightTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                addEditPresenter.correctNightTime(sNightTime)
            }
            if (edtNightTime.text.toString().isBlank()) {
                if (hasFocus) {
                    edtNightTime.hint = getString(R.string.str_time_zero)
                } else {
                    edtNightTime.hint = null
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
        edtGroundTime.setInputMask {
            if (edtGroundTime.text.toString().isBlank()) {
                edtGroundTime.hint = getString(R.string.str_time_zero)
            }
            sGroundTime = it
        }
        edtGroundTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                addEditPresenter.correctGroundTime(sGroundTime)
            }
            if (edtGroundTime.text.toString().isBlank()) {
                if (hasFocus) {
                    edtGroundTime.hint = getString(R.string.str_time_zero)
                } else {
                    edtGroundTime.hint = null
                }
            }
        }
        edtGroundTime.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    addEditPresenter.correctGroundTime(sGroundTime)
                    Utility.hideSoftKeyboard(this@AddEditActivity)
                    true
                }
                else -> false
            }
        }
    }

    private fun onDateTimeChanges() {
        tiedt_date.setOnFocusChangeListener { _, hasFocus ->
            val empty = Utility.empty(tiedt_date.text.toString())
            if (empty) {
                if (hasFocus) {
                    til_date.hint = getString(R.string.str_date)
                    tiedt_date.hint = getString(R.string.str_date_format)
                } else {
                    til_date.hint = null
                    tiedt_date.hint = getString(R.string.str_date)
                }
            } else {
                til_date.hint = getString(R.string.str_date)
                tiedt_date.hint = getString(R.string.str_date)
                if (!hasFocus) {
                    val dat = tiedt_date.text.toString()
                    val pattern = "^(3[01]|[12][0-9]|0[1-9]).(1[0-2]|0[1-9]).[0-9]{4}\$".toRegex()
                    val containsMatchIn = pattern.containsMatchIn(dat)
                    if (!containsMatchIn) {
                        tiedt_date.setText("")
                        ToastMaker.toastError(this@AddEditActivity, getString(R.string.date_time_input_error))
                    }
                }
            }
        }
        dateMaskListener = MaskedTextChangedListener("[00].[00].[0000]", ArrayList(), false, tiedt_date, object : _TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (Utility.empty(tiedt_date.text.toString())) {
                    til_date.hint = getString(R.string.str_date)
                    tiedt_date.hint = null
                }
            }
        }, object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                if (maskFilled) {
                    addEditPresenter.initDateFromMask(extractedValue)
                }
            }
        })
        tiedt_date.addTextChangedListener(dateMaskListener)
    }

    private fun EditText.setInputMask(formatMask: String = CONSTS.STRINGS.LOG_TIME_FORMAT, onMasked: (value: String) -> Unit = {}) {
        val listener = MaskedTextChangedListener.installOn(this, formatMask, object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                onMasked.invoke(extractedValue)
            }
        })
        this.hint = listener.placeholder()
        this.onFocusChangeListener = listener
    }

    private fun onFlightTimeChanges() {
        edtFlightTime.setInputMask {
            if (edtFlightTime.text.toString().isBlank()) {
                edtFlightTime.hint = getString(R.string.str_time_zero)
            }
            sFlightTime = it
        }
        edtFlightTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                addEditPresenter.correctFlightTime(sFlightTime)
            }
            if (edtFlightTime.text.toString().isBlank()) {
                if (hasFocus) {
                    edtFlightTime.hint = getString(R.string.str_time_zero)
                } else {
                    edtFlightTime.hint = null
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_date -> {
                addEditPresenter.correctFlightTime(sFlightTime)
                val cdp = CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(this@AddEditActivity)
                cdp.show(supportFragmentManager, "fragment_date_picker_name")
            }
            R.id.select_plane_type -> {
                addEditPresenter.correctFlightTime(sFlightTime)
                launchActivity<PlaneTypesActivity>(CONSTS.REQUESTS.REQUEST_SELECT_PLANE_TYPE) {
                    putExtra("is_request", true)
                }
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
            }
            R.id.btnSelectFlightType -> {
                addEditPresenter.correctFlightTime(sFlightTime)
                launchActivity<FlightTypesActivity>(CONSTS.REQUESTS.REQUEST_SELECT_FLIGHT_TYPE) {
                    putExtra("is_request", true)
                }
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
            }
            R.id.btn_moto -> {
                showMotoDialog()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_SELECT_PLANE_TYPE -> {
                    addEditPresenter.setFlightPlaneType(data.getExtra<Long>(CONSTS.EXTRAS.EXTRA_PLANE_TYPE))
                }
                CONSTS.REQUESTS.REQUEST_SELECT_FLIGHT_TYPE -> {
                    addEditPresenter.setFlightType(data.getExtra<Long>(CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE))
                }
            }
        }
    }

    override fun addFlightTimeToAdapter(timeFlightEntity: TimeToFlight) {
    }

    override fun setPlaneTypeTitle(title: String?) {
        tvAirplaneType.text = title
    }

    override fun setToolbarTitle(string: String) {
        supportActionBar?.title = string
    }

    override fun setEdtTime(strLogTime: String?) {
        edtFlightTime.setText(strLogTime)
    }

    override fun setEdtNightTime(nightTimeText: String) {
        edtNightTime.setText(nightTimeText)
    }

    override fun setDescription(desc: String) {
        edtDesc.setText(desc)
    }

    override fun setDate(date: String) {
        tiedt_date.removeTextChangedListener(dateMaskListener)
        tiedt_date.setText(date)
        tiedt_date.addTextChangedListener(dateMaskListener)
    }

    override fun setEdtGroundTime(groundTimeText: String) {
        edtGroundTime.setText(groundTimeText)
    }

    override fun setRegNo(regNo: String?) {
        edtRegNo.setText(regNo)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_save -> {
                val descr = edtDesc.text.toString()
                val regNo = edtRegNo.text.toString()
                val titleText = tiedt_title.text.toString()
                addEditPresenter.saveFlight(regNo, descr, sFlightTime, sGroundTime, sNightTime,titleText)
            }
            R.id.action_remove -> {
                alertDialog(
                        context = this,
                        title = getString(R.string.str_delete),
                        btnCancelText = getString(R.string.str_cancel),
                        onConfirm = { addEditPresenter.removeFlight() }
                )
            }
            R.id.action_color -> {
                addEditPresenter.menuColorClick()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setMotoTimeResult(motoTime: String?) {
        tvMotoResult?.text = motoTime
    }

    private fun showMotoDialog() {
        val li = LayoutInflater.from(this@AddEditActivity)
        val xmlView = li.inflate(R.layout.moto, null)
        val alertDialog = AlertDialog.Builder(this@AddEditActivity)
        alertDialog.setView(xmlView)
        val edtMotoStart = xmlView.findViewById(R.id.edtStartMoto) as EditText
        val edtMotoFinish = xmlView.findViewById(R.id.edtFinishMoto) as EditText
        tvMotoResult = xmlView.findViewById(R.id.tvMotoresult) as TextView
        edtMotoStart.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val startString = edtMotoStart.text.toString()
                val finishString = edtMotoFinish.text.toString()
                addEditPresenter.onMotoTimeChange(startString, finishString)
            }
        })
        edtMotoFinish.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val startString = edtMotoStart.text.toString()
                val finishString = edtMotoFinish.text.toString()
                addEditPresenter.onMotoTimeChange(startString, finishString)
            }
        })
        alertDialog.setTitle(getString(R.string.str_moto))
        alertDialog.setCancelable(false).setPositiveButton(getString(R.string.str_ok)) { _, id ->
            addEditPresenter.setMotoResult()
        }.setNegativeButton(getString(R.string.str_cancel)) { dialog, _ -> dialog.cancel() }
        alertDialog.show()
    }

    override fun toastSuccess(msg: String?) {
        ToastMaker.toastSuccess(this, msg)
    }

    override fun setResultOK() {
        putExtras(Activity.RESULT_OK)
    }

    override fun onPressBack() {
        onBackPressed()
    }

    override fun setFligtTypeTitle(title: String) {
        tvFlightTitle.text = title
    }

    override fun onDateSet(dialog: CalendarDatePickerDialogFragment, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        addEditPresenter.onDateSet(dayOfMonth, monthOfYear, year)
    }

    override fun onColorSelect(colors: IntArray) {
        MaterialDialog(this).show {
            title(R.string.select_color)
            colorChooser(colors, initialSelection = BLUE) { _, color ->
                 addEditPresenter.onColorSelected(color)
            }
            positiveButton(R.string.select)
        }
    }
}