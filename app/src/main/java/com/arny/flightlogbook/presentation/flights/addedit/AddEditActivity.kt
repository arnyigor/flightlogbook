package com.arny.flightlogbook.presentation.flights.addedit

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.adapters.CustomRVLayoutManager
import com.arny.constants.CONSTS
import com.arny.domain.models.TimeToFlight
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.flighttypes.FlightTypesActivity
import com.arny.flightlogbook.presentation.planetypes.PlaneTypesActivity
import com.arny.flightlogbook.presentation.timetypes.TimesListActivity
import com.arny.helpers.interfaces._TextWatcher
import com.arny.helpers.utils.*
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_addedit.*
import kotlinx.android.synthetic.main.time_input_dialog_layout.view.*
import java.util.*

class AddEditActivity : MvpAppCompatActivity(), AddEditView, CalendarDatePickerDialogFragment.OnDateSetListener, View.OnClickListener {
    private var timesAdapter: FlightTimesAdapter? = null
    private var tvMotoResult: TextView? = null
    private var imm: InputMethodManager? = null
    private var time = ""

    @InjectPresenter
    lateinit var addEditPresenter: AddEditPresenter

    @ProvidePresenter
    fun provideAddEditPresenter(): AddEditPresenter {
        return AddEditPresenter()
    }

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
        initFlightTimesAdapter()
        initUI()
        addEditPresenter.initState(flightId)
    }

    private fun initFlightTimesAdapter() {
        timesAdapter = FlightTimesAdapter(object : FlightTimesAdapter.FlightTimesClickListener {
            override fun onEditFlightTime(position: Int, item: TimeToFlight) {
                var timeDialog: AlertDialog? = null
                timeDialog = createCustomLayoutDialog(R.layout.time_input_dialog_layout, {
                    var timeValue = ""
                    MaskedTextChangedListener.installOn(edt_time, CONSTS.STRINGS.LOG_TIME_FORMAT, object : MaskedTextChangedListener.ValueListener {
                        override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                            timeValue = formattedValue
                        }
                    })
                    chbox_add_flight_time.isChecked = item.addToFlightTime
                    edt_time.hint = "чч:мм"
                    edt_time.setText(DateTimeUtils.strLogTime(item.time))
                    iv_close.setOnClickListener {
                        timeDialog?.dismiss()
                    }
                    btn_time_dlg_ok.setOnClickListener {
                        addEditPresenter.onAddTimeChanged(timeValue, chbox_add_flight_time.isChecked, item, position)
                        timeDialog?.dismiss()
                    }
                })
            }

            override fun onDeleteFlightTime(position: Int, item: TimeToFlight) {
                timesAdapter?.remove(item)
                timeSummChange()
                rv_time_types.showSnackBar("Время удалено", "Отмена", 2000, action = {
                    timesAdapter?.add(position, item)
                    timeSummChange()
                })
            }

            override fun onItemClick(position: Int, item: TimeToFlight) {

            }
        })
        val customRVLayoutManager = CustomRVLayoutManager(this)
        customRVLayoutManager.setScrollEnabled(false)
        rv_time_types.layoutManager = customRVLayoutManager
        rv_time_types.adapter = timesAdapter
    }

    override fun updateFlightTimesAdapter(items: List<TimeToFlight>) {
        timesAdapter?.addAll(items)
    }

    override fun notifyAddTimeItemChanged(position: Int) {
        timesAdapter?.notifyItemChanged(position)
        timeSummChange()
    }

    override fun timeSummChange() {
        addEditPresenter.onTimeSummChange(timesAdapter?.getItems())
    }

    override fun setTotalFlightTime(flightTime: String) {
        tv_total_flight_time.text = flightTime
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(this, msg)
    }

    override fun setTotalTime(total: String) {
        tv_total_time.text = total
    }

    private fun initUI() {
        select_plane_type.setOnClickListener(this)
        select_flight_type.setOnClickListener(this)
        tv_add_time.setOnClickListener(this)
        iv_date.setOnClickListener(this)
        iv_date.setOnClickListener(this)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        edtDate?.setOnFocusChangeListener { _, hasFocus ->
            val empty = Utility.empty(edtDate?.text.toString())
            if (empty) {
                if (hasFocus) {
                    tilDate?.hint = getString(R.string.str_date)
                    edtDate?.hint = getString(R.string.str_date_format)
                } else {
                    tilDate?.hint = null
                    edtDate?.hint = getString(R.string.str_date)
                }
            } else {
                tilDate?.hint = getString(R.string.str_date)
                edtDate?.hint = getString(R.string.str_date)
                if (!hasFocus) {
                    val dat = edtDate?.text.toString()
                    val pattern = "^(3[01]|[12][0-9]|0[1-9]).(1[0-2]|0[1-9]).[0-9]{4}\$".toRegex()
                    val containsMatchIn = pattern.containsMatchIn(dat)
                    if (!containsMatchIn) {
                        edtDate.setText("")
                        ToastMaker.toastError(this@AddEditActivity, getString(R.string.date_time_input_error))
                    }
                }
            }
        }
        edtDate?.addTextChangedListener(MaskedTextChangedListener("[00].[00].[0000]", ArrayList(), false, edtDate, object : _TextWatcher {
                    override fun afterTextChanged(s: Editable) {
                        if (Utility.empty(edtDate.text.toString())) {
                            tilDate?.hint = getString(R.string.str_date)
                            edtDate?.hint = null
                        }
                    }
                }, object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                if (maskFilled) {
                    addEditPresenter.initDateFromMask(extractedValue)
                }
            }
        }))
        MaskedTextChangedListener.installOn(edtTime, CONSTS.STRINGS.LOG_TIME_FORMAT, object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                if (edtTime.text.toString().isBlank()) {
                    til_edt_time?.hint = getString(R.string.str_itemlogtime)
                    edtTime?.hint = null
                }
                time = extractedValue
            }
        })
        edtTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                addEditPresenter.correctingLogTime(time)
            }
            if (edtTime.text.toString().isBlank()) {
                if (hasFocus) {
                    til_edt_time?.hint = getString(R.string.str_itemlogtime)
                    edtTime?.hint = getString(R.string.str_itemlogtime_hint)
                } else {
                    til_edt_time?.hint = null
                    edtTime?.hint = getString(R.string.str_itemlogtime)
                }
            }
        }
        edtTime.setOnKeyListener { _, keyCode, event ->
            if (keyCode == EditorInfo.IME_ACTION_GO && event.action == KeyEvent.ACTION_UP) {
                Utility.hideSoftKeyboard(this@AddEditActivity)
                addEditPresenter.correctingLogTime(time)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_add_time -> {
                addEditPresenter.correctingLogTime(time)
                launchActivity<TimesListActivity>(CONSTS.REQUESTS.REQUEST_ADD_TIME) {
                    putExtra("is_request", true)
                }
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
            }
            R.id.iv_date -> {
            addEditPresenter.correctingLogTime(time)
            val cdp = CalendarDatePickerDialogFragment()
                    .setOnDateSetListener(this@AddEditActivity)
            cdp.show(supportFragmentManager, "fragment_date_picker_name")
        }
            R.id.select_plane_type -> {
                addEditPresenter.correctingLogTime(time)
                launchActivity<PlaneTypesActivity>(CONSTS.REQUESTS.REQUEST_SELECT_PLANE_TYPE) {
                    putExtra("is_request", true)
            }
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        }
            R.id.select_flight_type -> {
            addEditPresenter.correctingLogTime(time)
                launchActivity<FlightTypesActivity>(CONSTS.REQUESTS.REQUEST_SELECT_FLIGHT_TYPE) {
                    putExtra("is_request", true)
            }
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.i(AddEditActivity::class.java.simpleName, "onActivityResult: requestCode:$requestCode;resultCode:$resultCode;data:" + Utility.dumpIntent(data) )
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_SELECT_PLANE_TYPE -> {
                    addEditPresenter.setFlightPlaneType(data.getExtra<Long>(CONSTS.EXTRAS.EXTRA_PLANE_TYPE))
                }
                CONSTS.REQUESTS.REQUEST_SELECT_FLIGHT_TYPE -> {
                    addEditPresenter.setFlightType(data.getExtra<Long>(CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE))
                }
                CONSTS.REQUESTS.REQUEST_ADD_TIME -> {
                    val timeId = data.getExtra<Long>(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT_ID)
                    val timeTitle = data.getExtra<String>(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT_TITLE)
                    val time = data.getExtra<Int>(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT)
                    val addToFlight = data.getExtra<Boolean>(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT_ADD)
                    addEditPresenter.addFlightTime(timeId, timeTitle, time, addToFlight)
                }
            }
        }
    }

    override fun addFlightTimeToAdapter(timeFlightEntity: TimeToFlight) {
        timesAdapter?.add(timeFlightEntity)
    }

    override fun setPlaneTypeTitle(title: String?) {
        tvAirplaneType.text = title
    }

    override fun setToolbarTitle(string: String) {
        supportActionBar?.title = string
    }

    override fun setEdtTimeText(strLogTime: String?) {
        edtTime.setText(strLogTime)
    }

    override fun setDescription(desc: String) {
        edtDesc.setText(desc)
    }

    override fun setDate(date: String) {
        edtDate.setText(date)
    }

    override fun setLogTime(strLogTime: String?) {
        edtTime.setText(strLogTime)
        addEditPresenter.correctingLogTime(time)
    }

    override fun setRegNo(regNo: String?) {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_edit_menu, menu)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
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
                val timeItems = timesAdapter?.getItems()
                addEditPresenter.saveFlight(regNo, descr,timeItems)
            }
            R.id.action_moto -> {
                showMotoDialog()
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
        ToastMaker.toastSuccess(this,msg)
    }

    override fun onPressBack() {
        onBackPressed()
    }

    override fun setFligtTypeTitle(title: String) {
        tv_flight_type.text = title
    }

    override fun onDateSet(dialog: CalendarDatePickerDialogFragment, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        addEditPresenter.onDateSet(dayOfMonth, monthOfYear, year)
    }
}