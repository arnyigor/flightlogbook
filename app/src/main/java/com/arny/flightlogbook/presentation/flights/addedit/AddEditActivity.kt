package com.arny.flightlogbook.presentation.flights.addedit

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.adapters.CustomRVLayoutManager
import com.arny.constants.CONSTS
import com.arny.data.db.intities.TimeToFlightEntity
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.flightlogbook.presentation.times.TimesListActivity
import com.arny.flightlogbook.presentation.types.PlaneTypesActivity
import com.arny.helpers.utils.*
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_addedit.*
import kotlinx.android.synthetic.main.time_input_dialog_layout.view.*
import java.util.*

class AddEditActivity : MvpAppCompatActivity(), AddEditView, CalendarDatePickerDialogFragment.OnDateSetListener {
    private var timesAdapter: FlightTimesAdapter? = null
    private var tvMotoResult: TextView? = null
    private var imm: InputMethodManager? = null
    private var aAdapter: AircraftSpinnerAdapter? = null
    private var needRealodTypes = false

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
            title = getString(R.string.str_edt)
            this?.setDisplayHomeAsUpEnabled(true)
        }
        val flightId = getExtra<Long>(CONSTS.DB.COLUMN_ID)
        val customRVLayoutManager = CustomRVLayoutManager(this)
        customRVLayoutManager.setScrollEnabled(false)
        timesAdapter = FlightTimesAdapter()
        timesAdapter?.setViewHolderListener(object : FlightTimesAdapter.FlightTimesClickListener {
            override fun onTimeIncludeToflight(position: Int, item: TimeToFlightEntity) {
//                addEditPresenter.onTimeItemAddToFlightTime(position, item)
            }

            override fun onTimeExcludeflight(position: Int, item: TimeToFlightEntity) {
//                addEditPresenter.onTimeExcludeFromFlightTime(position, item)
            }

            override fun onItemClick(position: Int, item: TimeToFlightEntity) {
                createCustomLayoutDialog(R.layout.time_input_dialog_layout, {
                    val listener = MaskedTextChangedListener.installOn(edt_time, "[00]:[00]", object : MaskedTextChangedListener.ValueListener {
                        override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                            Log.d("TAG", extractedValue)
                            Log.d("TAG", maskFilled.toString())
                        }
                    })
                    edt_time.setHint(listener.placeholder())
                    edt_time.text.toString()
                })
                //addEditPresenter.onAddTimeChange(position, item)
            }

        })
        tv_add_time.setOnClickListener {
            launchActivity<TimesListActivity>(CONSTS.REQUESTS.REQUEST_ADD_TIME) {
                putExtra(CONSTS.DB.COLUMN_ID, flightId)
            }
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        }
        rv_time_types.layoutManager = customRVLayoutManager
        rv_time_types.adapter = timesAdapter
        initUI()
        initTypes()
        addEditPresenter.initState(flightId)
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(this, msg)
    }

    override fun onResume() {
        super.onResume()
        if (needRealodTypes) {
            needRealodTypes = false
            addEditPresenter.loadPlaneTypes()
        }
    }


    private fun initTypes() {
        aAdapter = AircraftSpinnerAdapter(this)
        spin_aircraft_types.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                addEditPresenter.setAircraftType(aAdapter?.items?.getOrNull(position))
            }
        }
        spin_aircraft_types.adapter = aAdapter
    }

    private fun initUI() {
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        edtDate?.addTextChangedListener(MaskedTextChangedListener("[00].[00].[0000]", ArrayList(), false, edtDate,
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable) {
                        if (Utility.empty(edtDate.text.toString())) {
                            tilDate?.hint = getString(R.string.str_date)
                            edtDate?.hint = null
                        }
                    }
                }, object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                if (maskFilled) {
                    addEditPresenter.initDateFromMask(maskFilled, extractedValue)
                }
            }
        }))
        iv_date.setOnClickListener { v ->
            val cdp = CalendarDatePickerDialogFragment()
                    .setOnDateSetListener(this@AddEditActivity)
            cdp.show(supportFragmentManager, "fragment_date_picker_name")
        }
        edtRegNo.requestFocus()
        edtTime.setOnFocusChangeListener { _, inside ->
            if (inside) {
                edtTime?.setText("")
            }
            if (!inside) {
                addEditPresenter.correctLogTime(edtTime.text.toString())
            }
        }
        edtTime.setOnClickListener { edtTime.setText("") }
        edtTime.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                addEditPresenter.correctLogTime(edtTime.text.toString())

                return@setOnKeyListener true
            }
            false
        }
        add_type.setOnClickListener {
            launchActivity<FragmentContainerActivity>(CONSTS.REQUESTS.REQUEST_ADD_TYPE) {
                putExtra(CONSTS.FRAGMENTS.FRAGMENT_TAG, CONSTS.FRAGMENTS.FRAGMENT_TAG_TYPE_LIST)
            }
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(AddEditActivity::class.java.simpleName, "onActivityResult: requestCode:$requestCode;resultCode:$resultCode;data:" + Utility.dumpIntent(data) )
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_ADD_TYPE -> {
                    val id = data.getExtra<Long>("id")
                    needRealodTypes = true
                }
                CONSTS.REQUESTS.REQUEST_ADD_TIME -> {
                }
            }
        }
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

    override fun updateAircaftTypes(types: List<PlaneType>) {
        aAdapter?.clear()
        aAdapter?.addAll(types)
    }

    override fun setLogTime(strLogTime: String?) {
        edtTime.setText(strLogTime)
        addEditPresenter.correctLogTime(edtTime.text.toString())
    }

    override fun setRegNo(regNo: String?) {
    }

    override fun setSpinDayNight(daynight: Int) {
    }

    override fun setSpinIfrVfr(ifrvfr: Int) {
    }

    override fun setFlightType(flighttype: Int) {
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
                super.onBackPressed()
                return true
            }
            R.id.action_type_edit -> {
                launchActivity<PlaneTypesActivity> {}
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setMotoTimeResult(motoTime: String?) {
        tvMotoResult?.text = motoTime
    }

    override fun showMotoBtn() {
        val lButtonParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val btn = Button(this)
        btn.background = ContextCompat.getDrawable(this, R.drawable.btn_bg_accent)
        btn.setTextColor(ContextCompat.getColor(this, R.color.bpWhite))
        btn.layoutParams = lButtonParams
        btn.setOnClickListener { showMoto() }
        btn.text = getString(R.string.str_moto_btn)
        motoContainer.addView(btn)
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


    /*private fun fillEdtTime(time: Int) {
        if (time != 0) {
            edtTime.setText(DateTimeUtils.strLogTime(time))
        } else {
            edtTime.setText("00:00")
        }
    }*/

    /*private fun addAirplaneTypes() {
        dialog = inputDialog(this, getString(R.string.str_add_airplane_types), inputType = InputType.TYPE_CLASS_TEXT, dialogListener = object : InputDialogListener {
            override fun onCancel() {

            }

            override fun onConfirm(content: String?) {
                if (content == null || content.isBlank()) {
                    toastError(getString(R.string.aircraft_type_not_correct))
                    return
                }
                addEditPresenter.addAircraftType(content)
                dialog?.dismiss()
            }
        })
    }*/


    /* private fun fillInputs() {
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
     }*/

    override fun onDateSet(dialog: CalendarDatePickerDialogFragment, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        addEditPresenter.onDateSet(dayOfMonth, monthOfYear, year)
    }
}