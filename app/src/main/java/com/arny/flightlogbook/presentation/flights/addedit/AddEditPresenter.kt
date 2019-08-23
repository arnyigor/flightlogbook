package com.arny.flightlogbook.presentation.flights.addedit

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.common.PrefsUseCase
import com.arny.domain.correctLogTime
import com.arny.domain.flights.FlightsUseCase
import com.arny.domain.models.Flight
import com.arny.domain.models.TimeToFlight
import com.arny.domain.models.TimeType
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
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
    @Volatile
    private var logTime: Int = 0
    @Volatile
    private var sumlogTime: Int = 0
    @Volatile
    private var sumFlightTime: Int = 0
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

    private fun initUI(flight: Flight) {
        viewState?.setDescription(flight.description ?: "")
        viewState?.setRegNo(flight.reg_no)
        viewState?.setToolbarTitle(commonUseCase.getString(R.string.str_edt_flight))
        loadDateTime(flight)
        loadFlightTimes()
        loadFlightType()
        loadPlaneTypes()
    }

    private fun loadPlaneTypes() {
        val planeId = flight?.planeId
        if (planeId != null) {
            setFlightPlaneType(planeId)
        }
    }

    private fun loadFlightType() {
        val flighttype = flight?.flightTypeId
        if (flighttype != null) {
            flightsUseCase.loadFlightType(flighttype.toLong())
                    .observeOnMain()
                    .subscribe({
                        val flightType = it.value
                        if (flightType != null) {
                            val title = "${commonUseCase.getString(R.string.str_flight_type_title)}${flightType.typeTitle}"
                            viewState?.setFligtTypeTitle(title)
                        }
                    },{
                        it.printStackTrace()
                    })
                    .addTo(compositeDisposable)
        }
    }

    private fun loadDateTime(flight: Flight) {
        fromCallable { DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy") }
                .observeOnMain()
                .subscribe({date->
                    viewState?.setDate(date)
                },{
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    private fun loadFlightTimes() {
        logTime = flight?.logtime ?: 0
        flightsUseCase.loadDBFlightToTimes(id)
                .zipWith(fromCallable { DateTimeUtils.strLogTime(logTime) })
                .observeOnMain()
                .subscribe({ pair->
                    flight?.times = pair.first
                    viewState?.setLogTime(pair.second)
                    if (!flight?.times.isNullOrEmpty()) {
                        viewState?.updateFlightTimesAdapter(flight?.times!!)
                    }
                    viewState?.timeSummChange()
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun correctingLogTime(stringTime: String) {
        correctLogTime(stringTime,logTime) { time, timeText ->
            logTime = time
            viewState?.setEdtTimeText(timeText)
            viewState?.timeSummChange()
        }
    }

    private fun loadFlight(id: Long?) {
        flightsUseCase.getFlight(id ?: 0)
                .observeOnMain()
                .subscribe({ nulable ->
                    this.flight = nulable.value
                    if (flight != null) {
                        initUI(flight!!)
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.record_not_found))
                        initEmptyUI()
                    }
                }) {
                    it.printStackTrace()
                    initEmptyUI()
                    viewState?.toastError(commonUseCase.getString(R.string.record_not_found) + ":" + it.message)
                }.addTo(compositeDisposable)
    }

    private fun initEmptyUI() {
        viewState?.setDescription("")
        viewState?.setDate("")
        viewState?.setToolbarTitle(commonUseCase.getString(R.string.str_add_flight))
        viewState?.timeSummChange()
        flight = Flight()
    }

    fun initState(id: Long?) {
        this.id = id
        if (id != null && id != 0L) {
            loadFlight(id)
        } else {
            initEmptyUI()
        }
    }

    fun onMotoTimeChange(startTime: String, finishTime: String) {
        if (startTime.isNotBlank() && finishTime.isNotBlank()) {
            mMotoStart = startTime.toFloat()
            mMotoFinish = finishTime.toFloat()
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
         flightsUseCase.loadPlaneTypeObs(planetypeId)
                 .observeOnMain()
                 .subscribe({
                     val planeType = it.value
                     if (planeType != null) {
                         this.flight?.planeId = planeType.typeId
                         val title = "${commonUseCase.getString(R.string.str_type)}${planeType.typeName}"
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
                        this.flight?.flightTypeId = flightType.id?.toInt()
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
            val timeType = TimeType(timeId, timeTitle)
            val timeToFlight = TimeToFlight(null, id, timeId, timeType, time
                    ?: 0, addToFlight ?: false)
            timeToFlight.flight = flight?.id
            viewState?.addFlightTimeToAdapter(timeToFlight)
            viewState?.timeSummChange()
        }
    }

    fun onAddTimeChanged(timeValue: String, addTiFlight: Boolean, item: TimeToFlight, position: Int) {
        val splittedTime = timeValue.split(":")
        val hh = splittedTime.getOrNull(0)?.parseInt() ?: 0
        val mm = splittedTime.getOrNull(1).parseInt() ?: 0
        item.time = (hh * 60) + mm
        item.addToFlightTime = addTiFlight
        viewState?.notifyAddTimeItemChanged(position)
    }

    fun onTimeSummChange(items: ArrayList<TimeToFlight>?) {
        if (items != null) {
            calcTotaltimes(items)
        }
    }

    private fun calcTotaltimes(items: ArrayList<TimeToFlight>) {
        fromCallable { items }
                .map { times ->
                    sumlogTime = logTime + times.sumBy { it.time }
                    sumFlightTime = logTime + times.filter { it.addToFlightTime }.sumBy { it.time }
                    val strLogTime = DateTimeUtils.strLogTime(sumlogTime)
                    val strFlightTime = DateTimeUtils.strLogTime(sumFlightTime)
                    Pair(strLogTime, strFlightTime)
                }
                .observeOnMain()
                .subscribe({
                    viewState?.setTotalTime("${commonUseCase.getString(R.string.str_total_time)} ${it.first}")
                    viewState?.setTotalFlightTime("${commonUseCase.getString(R.string.str_total_item_flight_time)} ${it.second}")
                }, {
                    it.printStackTrace()
                })
                .addTo(compositeDisposable)
    }

    fun saveFlight(regNo: String, descr: String, flightTimes: ArrayList<TimeToFlight>?) {
        flight?.datetime = mDateTime
        flight?.logtime = logTime
        flight?.sumlogTime = sumlogTime
        flight?.sumFlightTime = sumFlightTime
        flight?.reg_no = regNo
        flight?.description = descr
        flight?.let { flt ->
            if (flt.id != null) {
                flightsUseCase.updateFlight(flt,flightTimes)
                        .observeOnMain()
                        .subscribe({
                            if (it) {
                                viewState?.toastSuccess(commonUseCase.getString(R.string.flight_save_success))
                                viewState?.onPressBack()
                            } else {
                                viewState?.toastError(commonUseCase.getString(R.string.flight_not_save))
                            }
                        }, {
                            it.printStackTrace()
                            viewState?.toastError("${commonUseCase.getString(R.string.flight_save_error)}:${it.message}")
                        })
                        .addTo(compositeDisposable)
            }else{
                flightsUseCase.insertFlightAndGet(flt,flightTimes)
                        .observeOnMain()
                        .subscribe({
                            if (it) {
                                viewState?.toastSuccess(commonUseCase.getString(R.string.flight_save_success))
                                viewState?.onPressBack()
                            } else {
                                viewState?.toastError(commonUseCase.getString(R.string.flight_not_save))
                            }
                        }, {
                            it.printStackTrace()
                            viewState?.toastError("${commonUseCase.getString(R.string.flight_save_error)}:${it.message}")
                        })
                        .addTo(compositeDisposable)
            }
        } ?: viewState?.toastError(commonUseCase.getString(R.string.empty_flight))
    }
}