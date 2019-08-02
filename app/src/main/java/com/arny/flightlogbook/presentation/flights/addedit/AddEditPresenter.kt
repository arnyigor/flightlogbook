package com.arny.flightlogbook.presentation.flights.addedit

import android.annotation.SuppressLint
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.data.db.intities.TimeToFlightEntity
import com.arny.data.db.intities.TimeTypeEntity
import com.arny.domain.common.CommonUseCase
import com.arny.domain.common.PrefsUseCase
import com.arny.domain.correctLogTime
import com.arny.domain.flights.FlightsUseCase
import com.arny.domain.models.Flight
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import javax.inject.Inject


@InjectViewState
class AddEditPresenter : MvpPresenter<AddEditView>() {
    @Inject
    lateinit var flightsUseCase: FlightsUseCase
    @Inject
    lateinit var commonUseCase: CommonUseCase
    @Inject
    lateinit var prefsUseCase: PrefsUseCase
    private var id: Long? = null
    private var logTime: Int = 0
    private var aircraftType: PlaneType? = null
    private var flight: Flight? = null
    private var mDateTime: Long = 0
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

    fun saveState(time: String, descr: String, regno: String) {
        val canEdit = try {
            if (!time.contains(":")) {
                correctingLogTime(time)
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
            flightsUseCase.updateFlight(flight!!)
                    .observeOnMain()
                    .subscribe({

                    },{

                    })
                    .addTo(compositeDisposable)
        } else {
            flight = Flight()
            flight?.logtime = logTime
            flight?.datetime = mDateTime
            compositeDisposable.add(flightsUseCase.insertFlight(flight!!)
                    .observeOnMain()
                    .subscribe({

                    }, {

                    })
            )
        }
    }

    private fun setUIFromFlight(flight: Flight) {
        viewState?.setDescription(flight.description ?: "")
        viewState?.setDate(DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy"))
        logTime = flight.logtime ?: 0
        viewState?.setLogTime(DateTimeUtils.strLogTime(logTime))
        viewState?.setRegNo(flight.reg_no)
        viewState?.setSpinDayNight(flight.daynight ?: 0)
        viewState?.setSpinIfrVfr(flight.ifrvfr ?: 0)
        viewState?.setFlightType(flight.flighttype ?: 0)
        viewState?.setToolbarTitle(commonUseCase.getString(R.string.str_edt_flight))
        viewState?.timeSummChange()
    }

    fun correctingLogTime(stringTime: String) {
        correctLogTime(stringTime,logTime) { time, timeText ->
            logTime = time
            viewState?.setEdtTimeText(timeText)
            viewState?.timeSummChange()
        }
    }

    @SuppressLint("CheckResult")
    fun initUIFromId(id: Long?) {
        flightsUseCase.getFlight(id ?: 0)
                .observeOnMain()
                .subscribe({ nulable ->
                    val flight = nulable.value
                    this.flight = flight
                    if (flight != null) {
                        setUIFromFlight(flight)
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.record_not_found))
                        initEmptyUI()
                    }
                }) {
                    it.printStackTrace()
                    initEmptyUI()
                    viewState?.toastError(commonUseCase.getString(R.string.record_not_found) + ":" + it.message)
                }
    }

    private fun initEmptyUI() {
        viewState?.setDescription("")
        viewState?.setDate("")
        viewState?.setToolbarTitle(commonUseCase.getString(R.string.str_add_flight))
        viewState?.timeSummChange()
    }

    fun loadPlaneTypes() {
        flightsUseCase.loadPlaneTypes()
                .observeOnMain()
                .subscribe({
                    if (it.isNotEmpty()) {
                        viewState?.updateAircaftTypes(it)
                    } else {
                        viewState?.updateAircaftTypes(arrayListOf())
                    }
                }, {
                    viewState?.updateAircaftTypes(arrayListOf())
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun initState(id: Long?) {
        this.id = id
        loadPlaneTypes()
        if (id != null && id != 0L) {
            initUIFromId(id)
        } else {
            initEmptyUI()
        }
    }

    fun onMotoTimeChange(startTime: String, finishTime: String) {
        if (startTime.isNotBlank() && finishTime.isNotBlank()) {
            mMotoStart = java.lang.Float.parseFloat(startTime)
            mMotoFinish = java.lang.Float.parseFloat(finishTime)
            mMotoResult = getMotoTime(mMotoStart, mMotoFinish)
            val motoTime = setLogTimefromMoto(mMotoResult)
            viewState?.setMotoTimeResult(DateTimeUtils.strLogTime(motoTime))
        }
    }

    fun setMotoResult() {
        logTime = setLogTimefromMoto(mMotoResult)
        val logHours = logTime / 60
        val logMinutes = logTime % 60
        val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
        viewState?.setEdtTimeText(format)
        viewState?.timeSummChange()
    }

    private fun setLogTimefromMoto(motoTime: Float): Int {
        return (motoTime * 60).toInt()
    }

    private fun getMotoTime(start: Float, finish: Float): Float {
        var mMoto = finish - start
        if (mMoto < 0) {
            return 0f
        }
        mMoto = MathUtils.round(mMoto.toDouble(), 3).toFloat()
        return mMoto
    }

    fun onDateSet(dayOfMonth: Int, monthOfYear: Int, year: Int) {
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

    fun initDateFromMask(extractedValue: String) {
        try {
            mDateTime = DateTimeUtils.getDateTime(extractedValue, "ddMMyyyy").withTimeAtStartOfDay().millis
        } catch (e: Exception) {
            setDayToday()
            viewState?.toastError("Ошибка ввода даты")
        }
    }

    fun setFlightPlaneType(planetypeId: Long?) {
         flightsUseCase.loadPlaneType(planetypeId)
                 .observeOnMain()
                 .subscribe({
                     val planeType = it.value
                     if (planeType != null) {
                         this.aircraftType = planeType
                         val title = "${commonUseCase.getString(R.string.str_type)}${aircraftType?.typeName}"
                         viewState?.setPlaneTypeTitle(title)
                     }else{
                         viewState?.toastError(commonUseCase.getString(R.string.err_plane_type_not_found))
                     }
                 },{
                     it.printStackTrace()
                 })
                 .addTo(compositeDisposable)
    }

    fun setFlightType(fightTypeId: Long?) {
        flightsUseCase.loadFlightType(fightTypeId)
                .observeOnMain()
                .subscribe({
                    val flightType = it.value
                    if (flightType != null) {
                        this.flight?.flighttype = flightType.id?.toInt()
                        val title = "${commonUseCase.getString(R.string.str_flight_type_title)}${flightType.typeTitle}"
                        viewState?.setFligtTypeTitle(title)
                    }else{
                        viewState?.toastError(commonUseCase.getString(R.string.err_flight_type_not_found))
                    }
                },{
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun addFlightTime(timeId: Long?, timeTitle: String?, time: Int?, addToFlight: Boolean?) {
        if (timeId != null) {
            val timeTypeEntity = TimeTypeEntity(timeId, timeTitle)
            val timeFlightEntity = TimeToFlightEntity(null, id, timeId, timeTypeEntity, time
                    ?: 0, addToFlight ?: false)
            viewState?.addFlightTimeToAdapter(timeFlightEntity)
            viewState?.timeSummChange()
        }
    }

    fun onAddTimeChanged(timeValue: String, addTiFlight: Boolean, item: TimeToFlightEntity, position: Int) {
        val splittedTime = timeValue.split(":")
        val hh = splittedTime.getOrNull(0)?.parseInt() ?: 0
        val mm = splittedTime.getOrNull(1).parseInt() ?: 0
        val totalTime = (hh * 60) + mm
        item.time = totalTime
        item.addToFlightTime = addTiFlight
        viewState?.notifyAddTimeItemChanged(position)
    }

    fun onTimeSummChange(items: ArrayList<TimeToFlightEntity>?) {
        if (items != null) {
            val totalItemsTime = items.sumBy { it.time }
            val flightItemsTime = items.filter { it.addToFlightTime }.sumBy { it.time }
            val totalTime = logTime + totalItemsTime
            val totalFlightTime = logTime + flightItemsTime
            val strLogTime = DateTimeUtils.strLogTime(totalTime)
            val strFlightTime = DateTimeUtils.strLogTime(totalFlightTime)
            viewState?.setTotalTime("${commonUseCase.getString(R.string.str_totaltime)} $strLogTime")
            viewState?.setTotalFlightTime("${commonUseCase.getString(R.string.str_total_item_flight_time)} $strFlightTime")
        }
    }
}