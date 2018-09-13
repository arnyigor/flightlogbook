package com.arny.flightlogbook.presenter.addedit

import android.annotation.SuppressLint
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
import com.arny.arnylib.interfaces.InputDialogListener
import com.arny.arnylib.presenter.base.BaseMvpActivity
import com.arny.arnylib.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.Local
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepository
import com.arny.flightlogbook.presenter.types.AirplaneTypesActivity
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_addedit.*
import org.joda.time.DateTime
import java.math.BigDecimal
import java.util.*

class AddEditActivity : BaseMvpActivity<AddEditContract.View, AddEditPresenter>(), AddEditContract.View, CalendarDatePickerDialogFragment.OnDateSetListener {
    override val mPresenter = AddEditPresenter(MainRepository())
    private var motoCont: LinearLayout? = null
    private var strDesc: String? = null
    private var strDate: String? = null
    private var strTime: String? = null
    private var airplane_type: String? = null
    private var reg_no: String? = null
    private var day_night: Int = 0
    private var ifr_vfr: Int = 0
    private var flight_type: Int = 0
    private var logTime: Int = 0
    private var logHours: Int = 0
    private var logMinutes: Int = 0
    private var airplane_type_id: Int = 0
    private var mDateTime: Long = 0
    private var mRowId: Int = 0
    private var mMotoStart: Float = 0.toFloat()
    private var mMotoFinish: Float = 0.toFloat()
    private var mMotoResult: Float = 0.toFloat()
    private val typeList = ArrayList<String>()
    private var editable = false
    private var dateTimeListener: MaskedTextChangedListener? = null
    private var imm: InputMethodManager? = null
    private val disposable = CompositeDisposable()

    override fun setDescription(desc: String) {

    }

    override fun setDate(date: String) {
    }

    override fun updateAircaftTypes(types: List<AircraftType>) {
    }

    override fun setDateTime(mDateTime: String) {
    }

    override fun setLogTime(strLogTime: String?) {
    }

    override fun setRegNo(regNo: String?) {
    }

    override fun setPlaneType(airplanetypetitle: String?) {
    }

    override fun setSpinDayNight(daynight: Int) {
    }

    override fun setSpinIfrVfr(ifrvfr: Int) {
    }

    override fun setFlightType(flighttype: Int) {
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
        if (Config.getBoolean("motoCheckPref", false, this@AddEditActivity)) {
            showMotoBtn()
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
            cdp.show(supportFragmentManager, FRAG_TAG_DATE_PICKER)
        }
        edtRegNo.requestFocus()
        mRowId = getIntentExtra<Int>(intent, Consts.DB.COLUMN_ID) ?: 0
        editable = mRowId != 0
        if (editable) {
            btnAddEdtItem?.text = getString(R.string.str_edt)
            title = getString(R.string.str_edt)
        } else {
            btnAddEdtItem?.text = getString(R.string.str_add)
            title = getString(R.string.str_add)
        }
        edtTime?.setOnFocusChangeListener { view, inside ->
            if (inside) {
                edtTime?.setText("")
            }
            if (!inside) {
                try {
                    correctLogTime()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        edtTime?.setOnClickListener { view -> edtTime?.setText("") }
        edtTime?.setOnKeyListener { view, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                try {
                    correctLogTime()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return@setOnKeyListener true
            }
            false
        }
        btnAddEdtItem?.setOnClickListener { v ->
            var canEdit: Boolean
            try {
                if (!edtTime?.text.toString().contains(":")) {
                    correctLogTime()
                }
                canEdit = true
            } catch (e: Exception) {
                ToastMaker.toastError(this, e.message)
                canEdit = false
            }

            if (!canEdit) {
                return@setOnClickListener
            }
            disposable.add(Utility.mainThreadObservable(Observable.fromCallable { saveState(edtDesc?.text.toString(), edtTime?.text.toString(), edtRegNo?.text.toString()) })
                    .subscribe({ b ->
                        val res = b ?: false
                        if (res) {
                            ToastMaker.toastSuccess(this, getString(R.string.item_updated))
                            finish()
                        }
                    }, { throwable -> ToastMaker.toastError(this, throwable.message) }))
        }
        spinDayNight?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        itemSelected: View, selectedItemPosition: Int, selectedId: Long) {
                day_night = selectedItemPosition
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spinVfrIfr?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        itemSelected: View, selectedItemPosition: Int, selectedId: Long) {
                ifr_vfr = selectedItemPosition
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        btnAddAirplaneTypes?.setOnClickListener { view -> AddAirplaneTypes() }
        tvAirplaneType?.setOnClickListener { view ->
            disposable.add(Utility.mainThreadObservable(Observable.fromCallable { Local.getTypeList(this@AddEditActivity) })
                    .subscribe { types ->
                        typeList.clear()
                        for (aircraftType in types) {
                            aircraftType.typeName?.let { typeList.add(it) }
                        }
                        if (typeList.size > 0) {
                            showAirplaneTypes()
                        } else {
                            Toast.makeText(this@AddEditActivity, R.string.str_no_types, Toast.LENGTH_SHORT).show()
                        }
                    })
        }
    }

    private fun setDayToday() {
        mDateTime = DateTime.now().withTimeAtStartOfDay().millis
        edtDate?.setText(DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy"))
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        try {
            savedInstanceState.putInt(LOGTIME_STATE_KEY, logTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val id = getIntentExtra<Int>(intent, Consts.DB.COLUMN_ID)
        mPresenter.initState(id)
        fillInputs()
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        logTime = savedInstanceState.getInt(LOGTIME_STATE_KEY)
        fillEdtTime(logTime)
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

    private fun showAirplaneTypes() {
        val cs = typeList.toTypedArray<CharSequence>()
        val typesBuilder = AlertDialog.Builder(this)
        typesBuilder.setTitle(getString(R.string.str_type))
        typesBuilder.setItems(cs) { dialog, item ->
            airplane_type = typeList[item]
            val aircraftType = Local.getTypeItem(item + 1, this@AddEditActivity)//нумерация списка с нуля,в базе с 1цы
            if (aircraftType != null) {
                airplane_type_id = aircraftType.typeId
            }
            tvAirplaneType!!.text = String.format("%s %s", getString(R.string.str_type), typeList[item])
        }
        typesBuilder.setNegativeButton(getString(R.string.str_cancel)) { dialog, which -> dialog.cancel() }
        val alert = typesBuilder.create()
        alert.show()
    }

    private fun getMotoTime(start: Float, finish: Float): Float {
        var mMoto = finish - start
        if (mMoto < 0) {
            return 0f
        }
        mMoto = java.lang.Float.parseFloat(roundUp(mMoto, 3).toString())
        return mMoto
    }

    private fun setLogTimefromMoto(motoTime: Float): Int {
        return (motoTime * 60).toInt()
    }

    fun roundUp(value: Float, digits: Int): BigDecimal {
        return BigDecimal("" + value).setScale(digits, BigDecimal.ROUND_HALF_UP)
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
        val tvMotoResult = xmlView.findViewById(R.id.tvMotoresult) as TextView
        edtMotoStart.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val startString = edtMotoStart.text.toString()
                val finishString = edtMotoFinish.text.toString()
                if (!startString.isEmpty() && !finishString.isEmpty()) {
                    try {
                        mMotoStart = java.lang.Float.parseFloat(startString)
                        mMotoFinish = java.lang.Float.parseFloat(finishString)
                        mMotoResult = getMotoTime(mMotoStart, mMotoFinish)
                        tvMotoResult.text = DateTimeUtils.strLogTime(setLogTimefromMoto(mMotoResult))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        edtMotoFinish.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    mMotoStart = java.lang.Float.parseFloat(edtMotoStart.text.toString())
                    mMotoFinish = java.lang.Float.parseFloat(edtMotoFinish.text.toString())
                    mMotoResult = getMotoTime(mMotoStart, mMotoFinish)
                    tvMotoResult.text = DateTimeUtils.strLogTime(setLogTimefromMoto(mMotoResult))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        alertDialog.setTitle(getString(R.string.str_moto))
        alertDialog.setCancelable(false).setPositiveButton(getString(R.string.str_ok)) { dialog, id ->
            logTime = setLogTimefromMoto(mMotoResult)
            logHours = logTime / 60
            logMinutes = logTime % 60
            edtTime?.setText(String.format("%s:%s", MathUtils.pad(logHours), MathUtils.pad(logMinutes)))
        }.setNegativeButton(getString(R.string.str_cancel)) { dialog, id -> dialog.cancel() }
        alertDialog.show()
    }

    @SuppressLint("DefaultLocale")
    @Throws(Exception::class)
    private fun correctLogTime() {
        val inputLogtime = edtTime!!.text.toString()
        if (inputLogtime.isEmpty()) {
            if (logTime != 0) {
                edtTime.setText(DateTimeUtils.strLogTime(logTime))
            } else {
                edtTime.setText("00:00")
                logTime = 0
            }
        } else if (inputLogtime.length == 1) {
            logTime = Integer.parseInt(edtTime!!.text.toString())
            edtTime.setText(String.format("00:0%d", logTime))
        } else if (inputLogtime.length == 2) {
            logMinutes = Integer.parseInt(edtTime!!.text.toString())
            logTime = Integer.parseInt(edtTime!!.text.toString())
            if (logMinutes > 59) {
                logHours = 1
                logMinutes -= 60
            }
            edtTime.setText(String.format("%s:%s", MathUtils.pad(logHours), MathUtils.pad(logMinutes)))
        } else if (inputLogtime.length > 2) {
            if (inputLogtime.contains(":")) {
                logMinutes = Integer.parseInt(edtTime!!.text.toString().substring(inputLogtime.length - 2, inputLogtime.length))
                logHours = Integer.parseInt(edtTime!!.text.toString().substring(0, inputLogtime.length - 3))
            } else {
                logMinutes = Integer.parseInt(edtTime!!.text.toString().substring(inputLogtime.length - 2, inputLogtime.length))
                logHours = Integer.parseInt(edtTime!!.text.toString().substring(0, inputLogtime.length - 2))
            }
            if (logMinutes > 59) {
                logHours = logHours + 1
                logMinutes = logMinutes - 60
            }
            logTime = logHours * 60 + logMinutes
            edtTime!!.setText(String.format("%s:%s", MathUtils.pad(logHours), MathUtils.pad(logMinutes)))
        }
    }

    private fun fillEdtTime(time: Int) {
        Log.d(AddEditActivity::class.java.simpleName, "fillEdtTime logTime = $time")
        try {
            if (time != 0) {
                edtTime!!.setText(DateTimeUtils.strLogTime(time))
            } else {
                edtTime!!.setText("00:00")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun AddAirplaneTypes() {
        DroidUtils.simpleInputDialog(this@AddEditActivity, getString(R.string.str_add_airplane_types), getString(R.string.str_ok), getString(R.string.str_cancel), InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
            override fun onConfirm(content: String) {
                airplane_type = content
                Local.addType(airplane_type, this@AddEditActivity)
                fillInputs()
            }

            override fun onError(error: String) {
                ToastMaker.toastError(this@AddEditActivity, error)
            }
        })
    }

    private fun fillInputs() {
        if (mRowId != 0) {
            disposable.add(Utility.mainThreadObservable<Flight>(Observable.fromCallable<Flight> { Local.getFlightItem(mRowId, this@AddEditActivity) }).subscribe({ flight ->
                Log.d(AddEditActivity::class.java.simpleName, "fillInputs: flight:" + flight!!)
                if (flight == null) {
                    initEmptyflight()
                    Observable.fromCallable { Local.getFlightItem(mRowId, this@AddEditActivity) }.subscribe()
                    return@subscribe
                }
                strDesc = flight.description
                edtDesc?.setText(strDesc)
                mDateTime = flight.datetime
                strDate = DateTimeUtils.getDateTime(flight.datetime, "dd.MM.yyyy")
                edtDate?.setText(strDate)
                logTime = flight!!.logtime
                reg_no = flight!!.reg_no
                edtRegNo!!.setText(reg_no)
                edtTime!!.setText(DateTimeUtils.strLogTime(logTime))
                airplane_type_id = flight!!.airplanetypeid
                var airplanetypetitle = flight!!.airplanetypetitle
                if (Utility.empty(airplanetypetitle)) {
                    val aircraftTypeItem = Local.getTypeItem(airplane_type_id, this@AddEditActivity)
                    val airplType = aircraftTypeItem?.typeName
                    airplanetypetitle = if (Utility.empty(airplType)) getString(R.string.str_type_empty) else getString(R.string.str_type) + " " + airplType
                }
                tvAirplaneType!!.setText(airplanetypetitle)
                day_night = flight!!.daynight
                spinDayNight!!.setSelection(day_night)
                ifr_vfr = flight!!.ifrvfr
                spinVfrIfr!!.setSelection(ifr_vfr)
                flight_type = flight!!.flighttype
                spinFlightType!!.setSelection(flight_type)
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
        airplane_type = ""
        mDateTime = DateTime.now().withTimeAtStartOfDay().millis
        logTime = 0
        day_night = 0
        ifr_vfr = 0
        flight_type = 0
        disposable.add(Utility.mainThreadObservable(Observable.fromCallable { Local.getTypeList(this@AddEditActivity) }).subscribe({ types ->
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
        spinDayNight!!.setSelection(day_night)
        spinVfrIfr!!.setSelection(ifr_vfr)
        spinFlightType!!.setSelection(flight_type)
    }

    @Throws(Exception::class)
    private fun saveState(strDesc: String, strTime: String, reg_no: String): Boolean {
        Log.d(AddEditActivity::class.java.simpleName, "saveState ")
        this.strDesc = strDesc
        this.strTime = strTime
        this.reg_no = reg_no
        day_night = spinDayNight!!.selectedItemId.toInt()
        ifr_vfr = spinVfrIfr!!.selectedItemId.toInt()
        flight_type = spinFlightType!!.selectedItemId.toInt()
        if (mRowId == 0) {
            val res = Local.addFlight(mDateTime, logTime, this.reg_no, airplane_type_id, day_night, ifr_vfr, flight_type, this.strDesc, this@AddEditActivity)
            return res > 0
        } else {
            return Local.updateFlight(mDateTime, logTime, this.reg_no, airplane_type_id, day_night, ifr_vfr, flight_type, this.strDesc, mRowId, this@AddEditActivity)//тут тоже делаем сужение типа,странно,что для вставки нужен int,а выдает long
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

    companion object {
        private val LOGTIME_STATE_KEY = "com.arny.flightlogbook.extra.instance.time"
        private val FRAG_TAG_DATE_PICKER = "fragment_date_picker_name"
    }
}