package com.arny.flightlogbook.presenter.addedit

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapter.AircraftSpinnerAdapter
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.Local
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.presenter.base.BaseMvpActivity
import com.arny.flightlogbook.presenter.types.AirplaneTypesActivity
import com.arny.flightlogbook.utils.*
import com.arny.flightlogbook.utils.dialogs.InputDialogListener
import com.arny.flightlogbook.utils.dialogs.inputDialog
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_addedit.*
import org.joda.time.DateTime
import java.util.*

class AddEditActivity : BaseMvpActivity<AddEditContract.View, AddEditPresenter>(), AddEditContract.View, CalendarDatePickerDialogFragment.OnDateSetListener {
    private var motoCont: LinearLayout? = null
    private var strDesc: String? = null
    private var strDate: String? = null
    private var strTime: String? = null
    private var tvMotoResult: TextView? = null
    private var reg_no: String? = null
    private var day_night: Int = 0
    private var ifr_vfr: Int = 0
    private var flight_type: Int = 0
    private var airplane_type_id: Int = 0
    private var mDateTime: Long = 0
    private var mRowId: Int = 0
    private val typeList = ArrayList<String>()
    private var editable = false
    private var dateTimeListener: MaskedTextChangedListener? = null
    private var imm: InputMethodManager? = null
    private val disposable = CompositeDisposable()
    private var aAdapter: AircraftSpinnerAdapter? = null
    private var dialog: MaterialDialog? = null

    override fun initPresenter(): AddEditPresenter {
        return AddEditPresenter()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addedit)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorText))
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.str_edt)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initUI()
        if (Prefs.getBoolean(Consts.PrefsConsts.PREF_MOTO_TIME, false, this@AddEditActivity)) {
            showMotoBtn()
        }
        aAdapter = AircraftSpinnerAdapter(this)
        spin_aircraft_types.adapter = aAdapter
        spin_aircraft_types.setOnClickListener {
            val items = aAdapter?.items ?: arrayListOf()
            if (items.size == 0) {
                Toast.makeText(this@AddEditActivity, R.string.str_no_types, Toast.LENGTH_SHORT).show()
            }
        }
        spin_aircraft_types.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mPresenter.setAircraftType(aAdapter?.items?.getOrNull(position))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun initUI() {
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        motoCont = findViewById(R.id.motoContainer)
        val dateTimeChoose = findViewById<ImageView>(R.id.iv_date)
        edtDate?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && imm != null) {
                imm?.showSoftInput(edtDate, InputMethodManager.SHOW_IMPLICIT)
            }
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
                    if (!Utility.empty(dat) && !Utility.matcher("\\d{2}.\\d{2}.\\d{4}", dat)) {
                        ToastMaker.toastError(this@AddEditActivity, "Ошибка ввода даты")
                    }
                }
            }
        }
        dateTimeListener = MaskedTextChangedListener(
                "[00].[00].[0000]",
                ArrayList(),
                false,
                edtDate!!,
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable) {
                        if (Utility.empty(edtDate?.text.toString())) {
                            tilDate?.hint = getString(R.string.str_date)
                            edtDate?.hint = null
                        }
                    }
                }, object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                if (maskFilled) {
                    try {
                        mDateTime = DateTimeUtils.getDateTime(extractedValue, "ddMMyyyy").withTimeAtStartOfDay().getMillis()
                    } catch (e: Exception) {
                        setDayToday()
                        ToastMaker.toastError(this@AddEditActivity, "Ошибка ввода даты")
                    }

                }
            }
        }
        )
        edtDate?.addTextChangedListener(dateTimeListener)
        dateTimeChoose.setOnClickListener { v ->
            val cdp = CalendarDatePickerDialogFragment()
                    .setOnDateSetListener(this@AddEditActivity)
            cdp.show(supportFragmentManager, "fragment_date_picker_name")
        }
        edtRegNo.requestFocus()
        mRowId = getIntentExtra<Long>(intent, Consts.DB.COLUMN_ID)?.toInt() ?: 0
        editable = mRowId != 0
        if (editable) {
//            btnAddEdtItem?.text = getString(R.string.str_edt)
            title = getString(R.string.str_edt)
        } else {
//            btnAddEdtItem?.text = getString(R.string.str_add)
            title = getString(R.string.str_add)
        }
        edtTime?.setOnFocusChangeListener { view, inside ->
            if (inside) {
                edtTime?.setText("")
            }
            if (!inside) {
                mPresenter.correctLogTime(edtTime.text.toString())
            }
        }
        edtTime?.setOnClickListener { view -> edtTime?.setText("") }
        edtTime?.setOnKeyListener { view, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                mPresenter.correctLogTime(edtTime.text.toString())

                return@setOnKeyListener true
            }
            false
        }
//        spinDayNight?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>,
//                                        itemSelected: View, selectedItemPosition: Int, selectedId: Long) {
//                day_night = selectedItemPosition
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//        spinVfrIfr?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>,
//                                        itemSelected: View, selectedItemPosition: Int, selectedId: Long) {
//                ifr_vfr = selectedItemPosition
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//        btnAddAirplaneTypes?.setOnClickListener { view -> AddAirplaneTypes() }
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

    override fun updateAircaftTypes(types: List<AircraftType>) {
        aAdapter?.clear()
        aAdapter?.addAll(types)
    }

    override fun setLogTime(strLogTime: String?) {
        edtTime.setText(strLogTime)
        mPresenter.correctLogTime(edtTime.text.toString())
    }

    override fun setRegNo(regNo: String?) {
    }


    override fun setSpinDayNight(daynight: Int) {
    }

    override fun setSpinIfrVfr(ifrvfr: Int) {
    }

    override fun setFlightType(flighttype: Int) {
    }

    fun addEdit() {
        var canEdit: Boolean
        try {
            if (!edtTime?.text.toString().contains(":")) {
                mPresenter.correctLogTime(edtTime.text.toString())
            }
            canEdit = true
        } catch (e: Exception) {
            ToastMaker.toastError(this, e.message)
            canEdit = false
        }

        if (!canEdit) {
            return
        }
        disposable.add(mainThreadObservable(Observable.fromCallable { saveState(edtDesc?.text.toString(), edtTime?.text.toString(), edtRegNo?.text.toString()) })
                .subscribe({ b ->
                    val res = b ?: false
                    if (res) {
                        ToastMaker.toastSuccess(this, getString(R.string.item_updated))
                        finish()
                    }
                }, { throwable -> ToastMaker.toastError(this, throwable.message) }))
    }

    private fun setDayToday() {
        mDateTime = DateTime.now().withTimeAtStartOfDay().millis
        edtDate?.setText(DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy"))
    }


    override fun onResume() {
        super.onResume()
        mPresenter.initState(getIntentExtra<Long>(intent, Consts.DB.COLUMN_ID))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            R.id.action_type_edit -> {
                val AirplanesActivity = Intent(this@AddEditActivity, AirplaneTypesActivity::class.java)
                startActivity(AirplanesActivity)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setMotoTimeResult(motoTime: String?) {
        tvMotoResult?.setText(motoTime)
    }

    private fun showMotoBtn() {
        val lButtonParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val btn = Button(this)
        btn.background = ContextCompat.getDrawable(this, R.drawable.btn_bg_green)
        btn.setTextColor(ContextCompat.getColor(this, R.color.bpWhite))
        btn.layoutParams = lButtonParams
        btn.setOnClickListener { v -> showMoto() }
        btn.text = getString(R.string.str_moto_btn)
        motoCont?.addView(btn)
    }


    private fun showMoto() {
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
                mPresenter.onMotoTimeChange(startString, finishString)
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
                mPresenter.onMotoTimeChange(startString, finishString)
            }
        })
        alertDialog.setTitle(getString(R.string.str_moto))
        alertDialog.setCancelable(false).setPositiveButton(getString(R.string.str_ok)) { dialog, id ->
            mPresenter.setMotoResult()
        }.setNegativeButton(getString(R.string.str_cancel)) { dialog, id -> dialog.cancel() }
        alertDialog.show()
    }


    private fun fillEdtTime(time: Int) {
        if (time != 0) {
            edtTime.setText(DateTimeUtils.strLogTime(time))
        } else {
            edtTime.setText("00:00")
        }
    }

    private fun addAirplaneTypes() {
        dialog = inputDialog(this, getString(R.string.str_add_airplane_types), inputType = InputType.TYPE_CLASS_TEXT, dialogListener = object : InputDialogListener {
            override fun onCancel() {

            }

            override fun onConfirm(content: String?) {
                if (content == null || content.isBlank()) {
                    toastError(getString(R.string.aircraft_type_not_correct))
                    return
                }
                mPresenter.addAircraftType(content)
                dialog?.dismiss()
            }
        })
    }


    private fun fillInputs() {
        if (mRowId != 0) {
            disposable.add(mainThreadObservable<Flight>(Observable.fromCallable<Flight> { Local.getFlightItem(mRowId, this@AddEditActivity) }).subscribe({ flight ->
                Log.d(AddEditActivity::class.java.simpleName, "fillInputs: flight:" + flight!!)
                if (flight == null) {
                    initEmptyflight()
                    Observable.fromCallable { Local.getFlightItem(mRowId, this@AddEditActivity) }.subscribe()
                    return@subscribe
                }
                strDesc = flight.description
                edtDesc?.setText(strDesc)
                mDateTime = flight.datetime ?: 0
                strDate = DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy")
                edtDate?.setText(strDate)
                logTime = flight.logtime ?: 0
                reg_no = flight.reg_no
                edtRegNo.setText(reg_no)
                edtTime.setText(DateTimeUtils.strLogTime(logTime))
                airplane_type_id = flight.aircraft_id ?: 0
                var airplanetypetitle = flight.airplanetypetitle
                if (Utility.empty(airplanetypetitle)) {
                    val aircraftTypeItem = Local.getTypeItem(airplane_type_id, this@AddEditActivity)
                    val airplType = aircraftTypeItem?.typeName
                    airplanetypetitle = if (Utility.empty(airplType)) getString(R.string.str_type_empty) else getString(R.string.str_type) + " " + airplType
                }
                tvAirplaneType.text = airplanetypetitle
                day_night = flight.daynight ?: 0
//                spinDayNight.setSelection(day_night)
                ifr_vfr = flight.ifrvfr ?: 0
//                spinVfrIfr.setSelection(ifr_vfr)
                flight_type = flight.flighttype ?: 0
//                spinFlightType.setSelection(flight_type)
            }, { throwable -> ToastMaker.toastError(this, throwable.message) }))
        } else {
            initEmptyflight()
        }
    }

    private fun initEmptyflight() {
        edtDesc!!.setText("")
        edtDate!!.setText("")
        strDesc = ""
        strDate = ""
        reg_no = ""
        mDateTime = DateTime.now().withTimeAtStartOfDay().millis
        logTime = 0
        day_night = 0
        ifr_vfr = 0
        flight_type = 0
        disposable.add(mainThreadObservable(Observable.fromCallable { Local.getTypeList(this@AddEditActivity) }).subscribe({ types ->
            typeList.clear()
            for (aircraftType in types) {
                typeList.add(aircraftType.typeName ?: "")
            }
            if (typeList.size > 0) {
                airplane_type_id = 1
                tvAirplaneType!!.text = String.format("%s %s", getString(R.string.str_type), typeList[0])
            } else {
                airplane_type_id = 0
                tvAirplaneType!!.text = getString(R.string.str_no_types)
            }
        }, { throwable -> ToastMaker.toastError(this, throwable.message) }))
//        spinDayNight!!.setSelection(day_night)
//        spinVfrIfr!!.setSelection(ifr_vfr)
//        spinFlightType!!.setSelection(flight_type)
    }

    @Throws(Exception::class)
    private fun saveState(strDesc: String, strTime: String, reg_no: String): Boolean {
        Log.d(AddEditActivity::class.java.simpleName, "saveState ")
        this.strDesc = strDesc
        this.strTime = strTime
        this.reg_no = reg_no
        if (mRowId == 0) {
            val res = Local.addFlight(mDateTime, logTime, this.reg_no, airplane_type_id, day_night, ifr_vfr, flight_type, this.strDesc, this@AddEditActivity)
            return res > 0
        } else {
            return Local.updateFlight(mDateTime, logTime, this.reg_no, airplane_type_id, day_night, ifr_vfr, flight_type, this.strDesc, mRowId, this@AddEditActivity)
        }
    }

    override fun onDateSet(dialog: CalendarDatePickerDialogFragment, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        strDate = dayOfMonth.toString() + " " + (monthOfYear + 1) + " " + year
        try {
            mDateTime = DateTimeUtils.getDateTime(strDate, "dd MM yyyy").withTimeAtStartOfDay().millis
        } catch (e: Exception) {
            e.printStackTrace()
            ToastMaker.toastError(this@AddEditActivity, "Ошибка ввода даты")
        }

        edtDate!!.setText(DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy"))
    }
}