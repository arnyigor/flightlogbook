package com.arny.flightlogbook.presenter.addedit

import android.annotation.SuppressLint
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.utils.DateTimeUtils
import com.arny.flightlogbook.utils.MathUtils
import com.arny.flightlogbook.utils.empty
import com.arny.flightlogbook.utils.observeOnMain
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import javax.inject.Inject


@InjectViewState
class AddEditPresenterImpl : MvpPresenter<AddEditView>(), AddEditPresenter {
    @Inject
    lateinit var repository: MainRepositoryImpl
    private var id: Long? = null
    private var logTime: Int = 0
    private var aircraftType: AircraftType? = null
    private var flight: Flight? = null
    private var mDateTime: Long = 0
    private var logHours: Int = 0
    private var logMinutes: Int = 0
    private var mMotoStart: Float = 0.toFloat()
    private var mMotoFinish: Float = 0.toFloat()
    private var mMotoResult: Float = 0.toFloat()
    private val compositeDisposable = CompositeDisposable()

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: AddEditView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    override fun addAircraftType(name: String) {

    }

    override fun setExtractedDateTime(extractedValue: String) {
        try {
            mDateTime = DateTimeUtils.convertTimeStringToLong(extractedValue, "ddMMyyyy")
            val dateTime = convertDateTime()
            viewState?.setDate(dateTime)
        } catch (e: Exception) {
            val time = DateTimeUtils.getDateTime("ddMMyyyy")
            val long = DateTimeUtils.convertTimeStringToLong(time, "ddMMyyyy")
            mDateTime = long
            val dateTime = convertDateTime()
            viewState?.setDate(dateTime)
        }
    }

    override fun saveState(time: String, descr: String, regno: String) {
        val canEdit = try {
            if (!time.contains(":")) {
                correctLogTime(time)
            }
            true
        } catch (e: Exception) {
            viewState?.toastError(e.message)
            false
        }
        if (!canEdit) {
            return
        }
        if (flight != null) {
            flight?.logtime = logTime
            flight?.datetime = mDateTime
            repository.updateFlight(flight!!)
        } else {
            flight = Flight()
            flight?.logtime = logTime
            flight?.datetime = mDateTime
            compositeDisposable.add(repository.insertFlight(flight!!)
                    .observeOnMain()
                    .subscribe({

                    }, {

                    })
            )
        }
    }

    override fun setUIFromFlight(flight: Flight) {
        viewState?.setDescription(flight.description ?: "")
        viewState?.setDate(DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy"))
        logTime = flight.logtime ?: 0
        viewState?.setLogTime(DateTimeUtils.strLogTime(logTime))
        viewState?.setRegNo(flight.reg_no)
        viewState?.setSpinDayNight(flight.daynight ?: 0)
        viewState?.setSpinIfrVfr(flight.ifrvfr ?: 0)
        viewState?.setFlightType(flight.flighttype ?: 0)
        viewState?.setToolbarTitle(repository.getString(R.string.str_edt))
    }

    override fun correctLogTime(stringTime: String) {
        val inputLogtime = stringTime
        if (inputLogtime.isBlank()) {
            if (logTime != 0) {
                val strLogTime = DateTimeUtils.strLogTime(logTime)
                viewState?.setEdtTimeText(strLogTime)
            } else {
                viewState?.setEdtTimeText("00:00")
            }
        } else if (inputLogtime.length == 1) {
            logTime = Integer.parseInt(inputLogtime)
            val format = String.format("00:0%d", logTime)
            viewState?.setEdtTimeText(format)
        } else if (inputLogtime.length == 2) {
            logMinutes = Integer.parseInt(inputLogtime)
            logTime = Integer.parseInt(inputLogtime)
            if (logMinutes > 59) {
                logHours = 1
                logMinutes -= 60
            }
            val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
            viewState?.setEdtTimeText(format)
        } else if (inputLogtime.length > 2) {
            if (inputLogtime.contains(":")) {
                logMinutes = Integer.parseInt(inputLogtime.substring(inputLogtime.length - 2, inputLogtime.length))
                logHours = Integer.parseInt(inputLogtime.substring(0, inputLogtime.length - 3))
            } else {
                logMinutes = Integer.parseInt(inputLogtime.substring(inputLogtime.length - 2, inputLogtime.length))
                logHours = Integer.parseInt(inputLogtime.substring(0, inputLogtime.length - 2))
            }
            if (logMinutes > 59) {
                logHours += 1
                logMinutes -= 60
            }
            logTime = logHours * 60 + logMinutes
            val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
            viewState?.setEdtTimeText(format)
        }
    }

    override fun setAircraftType(aircraftType: AircraftType?) {
        this.aircraftType = aircraftType
        this.flight?.aircraft_id = aircraftType?.typeId
    }

    @SuppressLint("CheckResult")
    override fun initUIFromId(id: Long?) {
        repository.getFlight(id ?: 0).observeOnMain()
                .subscribe({ nulable ->
                    val flight = nulable.value
                    this.flight = flight
                    if (flight != null) {
                        setUIFromFlight(flight)
                    } else {
                        viewState?.toastError(repository.getString(R.string.record_not_found))
                        initEmptyUI()
                    }
                }) {
                    it.printStackTrace()
                    initEmptyUI()
                    viewState?.toastError(repository.getString(R.string.record_not_found) + ":" + it.message)
                }
    }

    override fun initEmptyUI() {
        viewState?.setDescription("")
        viewState?.setDate("")
        viewState?.setToolbarTitle(repository.getString(R.string.str_add))
    }

    override fun loadPlaneTypes() {
        repository.loadTypes({
            if (!it.empty()) {
                viewState?.updateAircaftTypes(it)
            } else {
                viewState?.updateAircaftTypes(arrayListOf())
            }
        }, {
            viewState?.updateAircaftTypes(arrayListOf())
            it.printStackTrace()
        })
    }

    override fun initState(id: Long?) {
        this.id = id
        loadPlaneTypes()
        if (id != null && id != 0L) {
            initUIFromId(id)
        } else {
            initEmptyUI()
        }
        val showMoto = repository.getPrefBoolean(Consts.PrefsConsts.PREF_MOTO_TIME, false)
        if (showMoto) {
            viewState?.showMotoBtn()
        }
    }

    override fun onMotoTimeChange(startTime: String, finishTime: String) {
        if (startTime.isNotBlank() && finishTime.isNotBlank()) {
            mMotoStart = java.lang.Float.parseFloat(startTime)
            mMotoFinish = java.lang.Float.parseFloat(finishTime)
            mMotoResult = getMotoTime(mMotoStart, mMotoFinish)
            val motoTime = setLogTimefromMoto(mMotoResult)
            viewState?.setMotoTimeResult(DateTimeUtils.strLogTime(motoTime))
        }
    }

    override fun setMotoResult() {
        logTime = setLogTimefromMoto(mMotoResult)
        logHours = logTime / 60
        logMinutes = logTime % 60
        val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
        viewState?.setEdtTimeText(format)
    }

    private fun setLogTimefromMoto(motoTime: Float): Int {
        return (motoTime * 60).toInt()
    }

    private fun getMotoTime(start: Float, finish: Float): Float {
        var mMoto = finish - start
        if (mMoto < 0) {
            return 0f
        }
        mMoto = java.lang.Float.parseFloat(MathUtils.round(mMoto.toDouble(), 3).toString())
        return mMoto
    }

    override fun onDateSet(dayOfMonth: Int, monthOfYear: Int, year: Int) {
        val date = dayOfMonth.toString() + " " + (monthOfYear + 1) + " " + year
        try {
            mDateTime = DateTimeUtils.getDateTime(date, "dd MM yyyy").withTimeAtStartOfDay().millis
        } catch (e: Exception) {
            e.printStackTrace()
            viewState?.toastError("Ошибка ввода даты")
        }
        val dateTime = convertDateTime()
        viewState?.setDate(dateTime)
    }

    private fun setDayToday() {
        mDateTime = DateTime.now().withTimeAtStartOfDay().millis
        val dateTime = convertDateTime()
        viewState?.setDate(dateTime)
    }

    private fun convertDateTime() = DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy")

    override fun initDateFromMask(maskFilled: Boolean, extractedValue: String) {
        try {
            mDateTime = DateTimeUtils.getDateTime(extractedValue, "ddMMyyyy").withTimeAtStartOfDay().millis
        } catch (e: Exception) {
            setDayToday()
            viewState?.toastError("Ошибка ввода даты")
        }
    }
}