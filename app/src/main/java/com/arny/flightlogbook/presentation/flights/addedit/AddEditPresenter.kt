package com.arny.flightlogbook.presentation.flights.addedit

import android.util.Log
import com.arny.domain.common.PreferencesInteractor
import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Flight
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import moxy.InjectViewState
import moxy.MvpPresenter
import org.joda.time.DateTime
import javax.inject.Inject

@InjectViewState
class AddEditPresenter : MvpPresenter<AddEditView>(), CompositeDisposableComponent {
    @Inject
    lateinit var flightsInteractor: FlightsInteractor
    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor
    @Inject
    lateinit var preferencesInteractor: PreferencesInteractor
    private var id: Long? = null
    @Volatile
    private var intFlightTime: Int = 0
    @Volatile
    private var intNightTime: Int = 0
    @Volatile
    private var intGroundTime: Int = 0
    @Volatile
    private var intTotalTime: Int = 0
    private var flight: Flight? = null
    private var mDateTime: Long = 0
    private var mMotoStart: Float = 0.toFloat()
    private var mMotoFinish: Float = 0.toFloat()
    private var mMotoResult: Float = 0.toFloat()
    override val compositeDisposable = CompositeDisposable()

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: AddEditView?) {
        super.detachView(view)
        resetCompositeDisposable()
    }

    private fun initUI(flight: Flight) {
        viewState.setDescription(flight.description ?: "")
        viewState.setRegNo(flight.regNo)
        viewState.setTitle(flight.title)
        viewState.setToolbarTitle(resourcesInteractor.getString(R.string.str_edt_flight))
        loadDateTime(flight)
        loadTimes(flight)
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
            fromNullable { flightsInteractor.loadFlightType(flighttype.toLong()) }
                    .observeSubscribeAdd({
                        val title = "${resourcesInteractor.getString(R.string.str_flight_type_title)}${it.value?.typeTitle}"
                        viewState.setFligtTypeTitle(title)
                    })

        }
    }

    private fun loadDateTime(flight: Flight) {
        fromCallable { DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy") }
                .observeSubscribeAdd({ s ->
                    viewState.setDate(s)
                })
    }

    private fun loadTimes(flight: Flight) {
        intFlightTime = flight.flightTime
        val timeFormatted = flight.logtimeFormatted
        if (timeFormatted.isNullOrBlank()) {
            fromCallable { DateTimeUtils.strLogTime(intFlightTime) }
                    .observeSubscribeAdd({
                        viewState.setEdtTime(it)
                    })
        } else {
            viewState.setEdtTime(timeFormatted)
        }
        intGroundTime = flight.groundTime
        fromCallable { DateTimeUtils.strLogTime(intGroundTime) }
                .observeSubscribeAdd({ viewState.setEdtGroundTime(it) })
        intNightTime = flight.nightTime
        fromCallable { DateTimeUtils.strLogTime(intNightTime) }
                .observeSubscribeAdd({ viewState.setEdtNightTime(it) })
        timeSummChanged()
    }

    private fun timeSummChanged() {
        if (intNightTime > intFlightTime) {
            intFlightTime = intNightTime
            fromCallable { DateTimeUtils.strLogTime(intFlightTime) }
                    .observeSubscribeAdd({ viewState.setEdtTime(it) })
        }
        intTotalTime = intFlightTime + intGroundTime
        fromCallable { DateTimeUtils.strLogTime(intTotalTime) }
                .observeSubscribeAdd({ viewState.setTotalTime(it) })
    }

    fun correctFlightTime(stringTime: String) {
        correctTimeObs(stringTime, intFlightTime)
                .observeSubscribeAdd({
                    intFlightTime = it.intTime
                    viewState.setEdtTime(it.strTime)
                    timeSummChanged()
                })
    }

    fun correctNightTime(stringTime: String) {
        correctTimeObs(stringTime, intNightTime)
                .observeSubscribeAdd({
                    intNightTime = it.intTime
                    viewState.setEdtNightTime(it.strTime)
                    timeSummChanged()
                })
    }

    fun correctGroundTime(stringTime: String) {
        correctTimeObs(stringTime, intGroundTime)
                .observeSubscribeAdd({
                    intGroundTime = it.intTime
                    viewState.setEdtGroundTime(it.strTime)
                    timeSummChanged()
                })
    }

    private fun correctTimeObs(stringTime: String, initTime: Int) =
            fromCallable { getCorrectTime(stringTime, initTime) }

    private fun loadFlight(id: Long) {
        fromNullable { flightsInteractor.getFlight(id) }
                .observeSubscribeAdd({
                    this.flight = it.value
                    if (flight != null) {
                        initUI(flight!!)
                    } else {
                        viewState.toastError(resourcesInteractor.getString(R.string.record_not_found))
                        initEmptyUI()
                    }
                })
    }

    private fun initEmptyUI() {
        viewState.setDescription("")
        viewState.setToolbarTitle(resourcesInteractor.getString(R.string.str_add_flight))
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
            viewState.setMotoTimeResult(DateTimeUtils.strLogTime(motoTime))
        }
    }

    fun setMotoResult() {
        intFlightTime = setLogTimefromMoto(mMotoResult)
        val logHours = intFlightTime / 60
        val logMinutes = intFlightTime % 60
        val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
        viewState.setEdtTime(format)
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
        Log.i(AddEditPresenter::class.java.simpleName, "onDateSet: ");
        fromCallable {
            mDateTime = DateTimeUtils
                    .getJodaDateTime("$dayOfMonth.${(monthOfYear + 1)}.$year", "dd.MM.yyyy", true)
                    .withTimeAtStartOfDay().millis
            convertDateTime()
        }.observeSubscribeAdd({
            viewState.setDate(it)
        }, {
            viewState.toastError(resourcesInteractor.getString(R.string.error_enter_date))
        })
    }

    private fun setDayToday() {
        Log.i(AddEditPresenter::class.java.simpleName, "setDayToday: ");
        fromCallable {
            mDateTime = DateTime.now().withTimeAtStartOfDay().millis
            convertDateTime()
        }.observeSubscribeAdd({
            viewState.setDate(it)
        })
    }

    private fun convertDateTime() = DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy")

    fun initDateFromMask(extractedValue: String) {
        if (extractedValue.isNotBlank()) {
            fromCallable {
                val dateTime = DateTimeUtils.getJodaDateTime(extractedValue, "ddMMyyyy", true)
                mDateTime = dateTime.withTimeAtStartOfDay().millis
                convertDateTime()
            }.observeSubscribeAdd({
                viewState.setDate(it)
            }, {
                setDayToday()
                viewState.toastError(resourcesInteractor.getString(R.string.date_time_input_error))
            })
        }

    }

    fun setFlightPlaneType(planetypeId: Long?) {
        fromNullable {
            flightsInteractor.loadPlaneType(planetypeId)
        }.observeSubscribeAdd({
            val planeType = it.value
            this.flight?.planeId = planeType?.typeId
            val title = "${resourcesInteractor.getString(R.string.str_type)}${planeType?.typeName}"
            viewState.setPlaneTypeTitle(title)
        }, {
            viewState.toastError(resourcesInteractor.getString(R.string.err_plane_type_not_found))
        })
    }

    fun setFlightType(fightTypeId: Long?) {
        fromNullable { flightsInteractor.loadFlightType(fightTypeId) }
                .observeSubscribeAdd({
                    val flightType = it.value
                    this.flight?.flightTypeId = flightType?.id?.toInt()
                    val title = "${resourcesInteractor.getString(R.string.str_flight_type_title)}${flightType?.typeTitle}"
                    viewState.setFligtTypeTitle(title)
                }, {
                    viewState.toastError(resourcesInteractor.getString(R.string.err_flight_type_not_found))
                })
    }

    fun saveFlight(
            regNo: String,
            descr: String,
            sFlightTime: String,
            sGroundTime: String,
            sNightTime: String,
            titleText: String
    ) {
        Observables.zip(
                correctTimeObs(sFlightTime, intFlightTime),
                correctTimeObs(sGroundTime, intGroundTime),
                correctTimeObs(sNightTime, intNightTime)
        )
                .map { timeChanged(it) }
                .observeSubscribeAdd({
                    save(titleText, regNo, descr)
                }, {
                    viewState.toastError(it.message)
                })
    }

    private fun save(titleText: String, regNo: String, descr: String) {
        val flt = flight
        if (flt != null) {
            flt.title = titleText
            flt.datetime = mDateTime
            flt.flightTime = intFlightTime
            flt.nightTime = intNightTime
            flt.groundTime = intGroundTime
            flt.totalTime = intTotalTime
            flt.regNo = regNo
            flt.description = descr
            if (flt.id != null) {
                updateFlight(flt)
            } else {
                addNewFlight(flt)
            }
        } else {
            viewState.toastError(resourcesInteractor.getString(R.string.empty_flight))
        }
    }

    private fun timeChanged(it: Triple<CorrectedTimePair, CorrectedTimePair, CorrectedTimePair>): Boolean {
        val flightTime = it.first
        val groundTime = it.second
        val nightTime = it.third
        intFlightTime = flightTime.intTime
        intGroundTime = groundTime.intTime
        intNightTime = nightTime.intTime
        timeSummChanged()
        if (mDateTime == 0L) {
            mDateTime = System.currentTimeMillis()
        }
        return true
    }

    private fun addNewFlight(flt: Flight) {
        flightsInteractor.insertFlightAndGet(flt)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        viewState.toastSuccess(resourcesInteractor.getString(R.string.flight_save_success))
                        viewState.setResultOK()
                        viewState.onPressBack()
                    } else {
                        viewState.toastError(resourcesInteractor.getString(R.string.flight_not_save))
                    }
                }, {
                    it.printStackTrace()
                    viewState.toastError("${resourcesInteractor.getString(R.string.flight_save_error)}:${it.message}")
                })
                .addTo(compositeDisposable)
    }

    private fun updateFlight(flt: Flight) {
        flightsInteractor.updateFlight(flt)
                .observeSubscribeAdd({
                    if (it) {
                        viewState.toastSuccess(resourcesInteractor.getString(R.string.flight_save_success))
                        viewState.setResultOK()
                        viewState.onPressBack()
                    } else {
                        viewState.toastError(resourcesInteractor.getString(R.string.flight_not_save))
                    }
                }, {
                    it.printStackTrace()
                    viewState.toastError("${resourcesInteractor.getString(R.string.flight_save_error)}:${it.message}")
                })
    }

    fun removeFlight() {
        flightsInteractor.removeFlight(flight?.id)
                .observeSubscribeAdd({
                    if (it) {
                        viewState.toastSuccess(resourcesInteractor.getString(R.string.flight_removed))
                        viewState.onPressBack()
                    } else {
                        viewState.toastError(resourcesInteractor.getString(R.string.flight_not_removed))
                    }
                }, {
                    viewState.toastError(it.message)
                })
    }

    private fun getCorrectTime(stringTime: String, initTime: Int): CorrectedTimePair {
        var logMinutes: Int
        var logHours = 0
        var logTime = initTime
        return when {
            stringTime.isBlank() -> CorrectedTimePair(logTime, if (logTime != 0) DateTimeUtils.strLogTime(logTime) else "")
            stringTime.length == 1 -> {
                logTime = stringTime.parseInt(0)
                CorrectedTimePair(logTime, if (logTime != 0) String.format("00:0%d", logTime) else "")
            }
            stringTime.length == 2 -> {
                logMinutes = stringTime.parseInt(0)
                logTime = stringTime.parseInt(0)
                if (logMinutes > 59) {
                    logHours = 1
                    logMinutes -= 60
                }
                val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
                CorrectedTimePair(logTime, format)
            }
            stringTime.length > 2 -> {
                if (stringTime.contains(":")) {
                    logMinutes = stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(0, stringTime.length - 3).parseInt(0)
                } else {
                    logMinutes = stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(0, stringTime.length - 2).parseInt(0)
                }
                if (logMinutes > 59) {
                    logHours += 1
                    logMinutes -= 60
                }
                logTime = DateTimeUtils.logTimeMinutes(logHours, logMinutes)
                CorrectedTimePair(logTime, DateTimeUtils.strLogTime(logTime))
            }
            else -> CorrectedTimePair(logTime, if (logTime != 0) DateTimeUtils.strLogTime(logTime) else "")
        }
    }
}
