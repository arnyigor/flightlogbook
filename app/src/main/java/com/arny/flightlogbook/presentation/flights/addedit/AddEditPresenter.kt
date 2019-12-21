package com.arny.flightlogbook.presentation.flights.addedit

import com.arny.domain.common.CommonUseCase
import com.arny.domain.common.PrefsUseCase
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Flight
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.helpers.coroutins.*
import com.arny.helpers.utils.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import moxy.MvpPresenter
import org.joda.time.DateTime
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@InjectViewState
class AddEditPresenter : MvpPresenter<AddEditView>(), CompositeDisposableComponent, CoroutineScope {
    @Inject
    lateinit var flightsInteractor: FlightsInteractor
    @Inject
    lateinit var commonUseCase: CommonUseCase
    @Inject
    lateinit var prefsUseCase: PrefsUseCase
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
    override val coroutineContext: CoroutineContext = getMainCoroutinContext()
    private val compositeJob = CompositeJob()

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: AddEditView?) {
        super.detachView(view)
        resetCompositeDisposable()
        compositeJob.clear()

    }

    private fun initUI(flight: Flight) {
        launchSafe({
            viewState?.setDescription(flight.description ?: "")
            viewState?.setRegNo(flight.regNo)
            viewState?.setToolbarTitle(commonUseCase.getString(R.string.str_edt_flight))
            loadDateTime(flight)
            loadTimes(flight)
            loadFlightType()
            loadPlaneTypes()
        }, {
            viewState?.toastError(it.message)
        }).addTo(compositeJob)
    }

    private fun loadPlaneTypes() {
        val planeId = flight?.planeId
        if (planeId != null) {
            setFlightPlaneType(planeId)
        }
    }

    private suspend fun loadFlightType() {
        val flighttype = flight?.flightTypeId
        if (flighttype != null) {
            ioThread { flightsInteractor.loadFlightType(flighttype.toLong()) }
                    ?.let {
                        val title = "${commonUseCase.getString(R.string.str_flight_type_title)}${it.typeTitle}"
                        viewState?.setFligtTypeTitle(title)
                    }
        }
    }

    private suspend fun loadDateTime(flight: Flight) {
        ioThread { DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy") }
                .let { viewState?.setDate(it) }
    }


    private suspend fun loadTimes(flight: Flight) {
        intFlightTime = flight.flightTime
        flowIO { DateTimeUtils.strLogTime(intFlightTime) }
                .flowOn(getMainCoroutinContext())
                .handleErrors()
                .collect {
                    viewState?.setDate(it)
                }
    }


    private suspend fun timeSummChanged() {
        if (intNightTime > intFlightTime) {
            intFlightTime = intNightTime
            ioThread { DateTimeUtils.strLogTime(intFlightTime) }
                    .let { viewState?.setEdtFlightTimeText(it) }
        }
        intTotalTime = intFlightTime + intGroundTime
        ioThread { DateTimeUtils.strLogTime(intTotalTime) }
                .let { viewState?.setTotalTime(it) }
    }

    fun correctFlightTime(stringTime: String) {
        launch {
            val pair = withContext(Dispatchers.IO) { getCorrectTime(stringTime, intFlightTime) }
            intFlightTime = pair.first
            viewState?.setEdtFlightTimeText(pair.second)
            timeSummChanged()
        }.addTo(compositeJob)
    }

    fun correctNightTime(stringTime: String) {
        launch {
            val pair = withContext(Dispatchers.IO) { getCorrectTime(stringTime, intNightTime) }
            intNightTime = pair.first
            viewState?.setEdtNightTimeText(pair.second)
            timeSummChanged()
        }.addTo(compositeJob)
    }

    fun correctGroundTime(stringTime: String) {
        launch {
            val pair = ioThread { getCorrectTime(stringTime, intGroundTime) }
            intGroundTime = pair.first
            viewState?.setEdtGroundTimeText(pair.second)
            timeSummChanged()
        }.addTo(compositeJob)
    }

    private fun loadFlight(id: Long) {
        launchSafe({
            ioThread { flightsInteractor.getFlight(id) }.let {
                this.flight = it
                if (flight != null) {
                    initUI(flight!!)
                } else {
                    viewState?.toastError(commonUseCase.getString(R.string.record_not_found))
                    initEmptyUI()
                }
            }
        }, {
            initEmptyUI()
            viewState?.toastError(commonUseCase.getString(R.string.record_not_found) + ":" + it.message)
        }).addTo(compositeJob)
    }

    private fun initEmptyUI() {
        viewState?.setDescription("")
        viewState?.setDate("")
        viewState?.setToolbarTitle(commonUseCase.getString(R.string.str_add_flight))
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
        intFlightTime = setLogTimefromMoto(mMotoResult)
        val logHours = intFlightTime / 60
        val logMinutes = intFlightTime % 60
        val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
        viewState?.setEdtFlightTimeText(format)
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
        launchAsync({
            mDateTime = DateTimeUtils.getJodaDateTime("$dayOfMonth.${(monthOfYear + 1)}.$year", "dd.MM.yyyy", true).withTimeAtStartOfDay().millis
            convertDateTime()
        }, {
            viewState?.setDate(it)
        }, {
            viewState?.toastError("Ошибка ввода даты")
        })
    }

    private fun setDayToday() {
        launchAsync({
            mDateTime = DateTime.now().withTimeAtStartOfDay().millis
            convertDateTime()
        }, {
            viewState?.setDate(it)
        }, {
            it.printStackTrace()
        })
    }

    private fun convertDateTime() = DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy")

    fun initDateFromMask(extractedValue: String) {
        launchAsync({
            mDateTime = DateTimeUtils.getJodaDateTime(extractedValue, "ddMMyyyy", true).withTimeAtStartOfDay().millis
        }, {

        }, {
            setDayToday()
            viewState?.toastError("Ошибка ввода даты")
        })
    }

    fun setFlightPlaneType(planetypeId: Long?) {
        launchSafe {
            ioThread { flightsInteractor.loadPlaneTypeObs(planetypeId) }
                    ?.let { planeType ->
                        this.flight?.planeId = planeType.typeId
                        val title = "${commonUseCase.getString(R.string.str_type)}${planeType.typeName}"
                        viewState?.setPlaneTypeTitle(title)
                    }
                    ?: viewState?.toastError(commonUseCase.getString(R.string.err_plane_type_not_found))
        }.addTo(compositeJob)
    }

    fun setFlightType(fightTypeId: Long?) {
        launchSafe {
            ioThread { flightsInteractor.loadFlightType(fightTypeId) }
                    ?.let { flightType ->
                        this.flight?.flightTypeId = flightType.id?.toInt()
                        val title = "${commonUseCase.getString(R.string.str_flight_type_title)}${flightType.typeTitle}"
                        viewState?.setFligtTypeTitle(title)
                    }
                    ?: viewState?.toastError(commonUseCase.getString(R.string.err_flight_type_not_found))
        }.addTo(compositeJob)
    }

    fun saveFlight(regNo: String, descr: String, sFlightTime: String, sGroundTime: String, sNightTime: String) {
        launchSafe({
            val pairFlight = ioThread { getCorrectTime(sFlightTime, intFlightTime) }
            val pairGround = ioThread { getCorrectTime(sGroundTime, intGroundTime) }
            val pairNight = ioThread { getCorrectTime(sNightTime, intNightTime) }
            intFlightTime = pairFlight.first
            intGroundTime = pairGround.first
            intNightTime = pairNight.first
            timeSummChanged()
            viewState?.setEdtFlightTimeText(pairFlight.second)
            viewState?.setEdtGroundTimeText(pairGround.second)
            if (mDateTime == 0L) {
                mDateTime = System.currentTimeMillis()
            }
            val flt = flight
            if (flt != null) {
                flt.datetime = mDateTime
                flt.flightTime = intFlightTime
                flt.totalTime = intFlightTime
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
                viewState?.toastError(commonUseCase.getString(R.string.empty_flight))
            }
        }, {
            viewState?.toastError(it.message)
        }).addTo(compositeJob)
    }

    private fun addNewFlight(flt: Flight) {
        flightsInteractor.insertFlightAndGet(flt)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        viewState?.toastSuccess(commonUseCase.getString(R.string.flight_save_success))
                        viewState?.setResultOK()
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

    private fun updateFlight(flt: Flight) {
        flightsInteractor.updateFlight(flt)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        viewState?.toastSuccess(commonUseCase.getString(R.string.flight_save_success))
                        viewState?.setResultOK()
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

    fun removeFlight() {
        flightsInteractor.removeFlight(flight?.id)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        viewState?.toastSuccess(commonUseCase.getString(R.string.flight_removed))
                        viewState?.onPressBack()
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.flight_not_removed))
                    }
                }, {
                    viewState?.toastError(it.message)
                }).addTo(compositeDisposable)
    }


    fun getCorrectTime(stringTime: String, initTime: Int): Pair<Int, String> {
        var logMinutes: Int
        var logHours = 0
        var logTime = initTime
        return when {
            stringTime.isBlank() -> Pair(logTime, if (logTime != 0) DateTimeUtils.strLogTime(logTime) else "")
            stringTime.length == 1 -> {
                logTime = stringTime.parseInt(0)
                Pair(logTime, if (logTime != 0) String.format("00:0%d", logTime) else "")
            }
            stringTime.length == 2 -> {
                logMinutes = stringTime.parseInt(0)
                logTime = stringTime.parseInt(0)
                if (logMinutes > 59) {
                    logHours = 1
                    logMinutes -= 60
                }
                val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
                Pair(logTime, format)
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
                Pair(logTime, DateTimeUtils.strLogTime(logTime))
            }
            else -> Pair(logTime, if (logTime != 0) DateTimeUtils.strLogTime(logTime) else "")
        }
    }
}