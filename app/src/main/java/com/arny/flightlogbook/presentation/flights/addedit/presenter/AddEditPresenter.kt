package com.arny.flightlogbook.presentation.flights.addedit.presenter

import com.arny.constants.CONSTS.STRINGS.PARAM_COLOR
import com.arny.domain.common.PreferencesInteractor
import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Flight
import com.arny.domain.models.Params
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.flights.addedit.models.CorrectedTimePair
import com.arny.flightlogbook.presentation.flights.addedit.view.AddEditView
import com.arny.helpers.utils.*
import io.reactivex.rxkotlin.Observables
import moxy.InjectViewState
import org.joda.time.DateTime
import javax.inject.Inject

@InjectViewState
class AddEditPresenter : BaseMvpPresenter<AddEditView>() {

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
    private var airportCodes = mutableListOf<String>()

    init {
        FlightApp.appComponent.inject(this)
    }

    private fun initUI(flight: Flight) {
        viewState.setDescription(flight.description ?: "")
        viewState.setRegNo(flight.regNo)
        viewState.setToolbarTitle(resourcesInteractor.getString(R.string.str_edt_flight))
        loadColor(flight)
        loadDateTime(flight)
        loadTimes(flight)
        loadIfrVfr(flight)
        loadFlightType()
        loadPlaneTypes()
    }

    private fun loadIfrVfr(flight: Flight) {
        viewState.setIfrSelected(flight.ifrvfr == 1)
    }

    private fun loadColor(flight: Flight) {
        flight.colorInt?.let {
            viewState.setViewColor(it)
            viewState.setRemoveColorVisible(true)
        }?.run {
            fromNullable { flight.params?.getParam(PARAM_COLOR, "") }
                    .map {
                        val hexColor = it.value
                        if (!hexColor.isNullOrBlank()) {
                            hexColor.toIntColor()
                        } else {
                            -1
                        }
                    }
                    .subscribeFromPresenter({
                        val hasColor = it != -1
                        viewState.setRemoveColorVisible(hasColor)
                        if (hasColor) {
                            viewState.setViewColor(it)
                        }
                    })
        }
    }

    private fun loadPlaneTypes() {
        val planeId = flight?.planeId
        if (planeId != null) {
            setFlightPlaneType(planeId)
        }
    }

    private fun loadFlightType() {
        flight?.flightTypeId?.let {
            fromNullable { flightsInteractor.loadFlightType(it.toLong()) }
                    .subscribeFromPresenter({
                        val title =
                                "${resourcesInteractor.getString(R.string.str_flight_type_title)}:${it.value?.typeTitle ?: "-"}"
                        viewState.setFligtTypeTitle(title)
                    })
        }
    }

    private fun loadDateTime(flight: Flight) {
        mDateTime = flight.datetime ?: 0
        fromCallable { DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy") }
                .subscribeFromPresenter({ s ->
                    viewState.setDate(s)
                })
    }

    private fun loadTimes(flight: Flight) {
        intFlightTime = flight.flightTime
        intNightTime = flight.nightTime
        intGroundTime = flight.groundTime
        fromCallable { DateTimeUtils.strLogTime(intFlightTime) }
                .subscribeFromPresenter({ viewState.setEdtFlightTimeText(it) })
        fromCallable { DateTimeUtils.strLogTime(intNightTime) }
                .subscribeFromPresenter({ viewState.setEdtNightTimeText(it) })
        fromCallable { DateTimeUtils.strLogTime(intGroundTime) }
                .subscribeFromPresenter({ viewState.setEdtGroundTimeText(it) })
        timeSummChanged()
    }

    private fun timeSummChanged() {
        if (intNightTime > intFlightTime) {
            intFlightTime = intNightTime
            fromCallable { DateTimeUtils.strLogTime(intFlightTime) }
                    .subscribeFromPresenter({ viewState.setEdtFlightTimeText(it) })
        }
        intTotalTime = intFlightTime + intGroundTime
        fromCallable { DateTimeUtils.strLogTime(intTotalTime) }
                .subscribeFromPresenter({ viewState.setTotalTime(it) })
    }

    fun correctFlightTime(stringTime: String) {
        correctTimeObs(stringTime, intFlightTime)
                .subscribeFromPresenter({
                    intFlightTime = it.intTime
                    viewState.setEdtFlightTimeText(it.strTime)
                    timeSummChanged()
                })
    }

    fun correctNightTime(stringTime: String) {
        correctTimeObs(stringTime, intNightTime)
                .subscribeFromPresenter({
                    intNightTime = it.intTime
                    viewState.setEdtNightTimeText(it.strTime)
                    timeSummChanged()
                })
    }

    fun correctGroundTime(stringTime: String) {
        correctTimeObs(stringTime, intGroundTime)
                .subscribeFromPresenter({
                    intGroundTime = it.intTime
                    viewState.setEdtGroundTimeText(it.strTime)
                    timeSummChanged()
                })
    }

    private fun correctTimeObs(stringTime: String, initTime: Int) =
            fromCallable { getCorrectTime(stringTime, initTime) }

    private fun loadFlight(id: Long) {
        fromNullable { flightsInteractor.getFlight(id) }
                .subscribeFromPresenter({
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
        viewState.setDate("")
        viewState.setToolbarTitle(resourcesInteractor.getString(R.string.str_add_flight))
        flight = Flight()
        flight?.params = Params()
    }

    fun initState(id: Long?) {
        this.id = id
        if (id != null && id != 0L) {
            loadFlight(id)
        } else {
            initEmptyUI()
        }
        requestCodes()
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
        val format =
                String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
        viewState.setEdtFlightTimeText(format)
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
        fromCallable {
            mDateTime = DateTimeUtils.getJodaDateTime(
                    "$dayOfMonth.${(monthOfYear + 1)}.$year",
                    "dd.MM.yyyy",
                    true
            ).withTimeAtStartOfDay().millis
            convertDateTime()
        }.subscribeFromPresenter({
            viewState.setDate(it)
        }, {
            viewState.toastError(resourcesInteractor.getString(R.string.error_enter_date))
        })
    }

    private fun setDayToday() {
        fromCallable {
            mDateTime = DateTime.now().withTimeAtStartOfDay().millis
            convertDateTime()
        }.subscribeFromPresenter({
            viewState.setDate(it)
        })
    }

    private fun convertDateTime() = DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy")

    fun initDateFromMask(extractedValue: String) {
        fromCallable {
            val dateTime = DateTimeUtils.getJodaDateTime(extractedValue, "ddMMyyyy", true)
            mDateTime = dateTime.withTimeAtStartOfDay().millis
            convertDateTime()
        }.subscribeFromPresenter({
            viewState.setDate(it)
        }, {
            setDayToday()
            viewState.toastError(resourcesInteractor.getString(R.string.date_time_input_error))
        })

    }

    fun setFlightPlaneType(planetypeId: Long?) {
        fromNullable {
            flightsInteractor.loadPlaneType(planetypeId)
        }.subscribeFromPresenter({
            val planeType = it.value
            this.flight?.planeId = planeType?.typeId
            val title = "${resourcesInteractor.getString(R.string.str_type)}:${planeType?.typeName}"
            viewState.setPlaneTypeTitle(title)
        }, {
            viewState.toastError(resourcesInteractor.getString(R.string.err_plane_type_not_found))
        })
    }

    fun setFlightType(fightTypeId: Long?) {
        fromNullable { flightsInteractor.loadFlightType(fightTypeId) }
                .subscribeFromPresenter({
                    val flightType = it.value
                    this.flight?.flightTypeId = flightType?.id?.toInt()
                    val title =
                            "${resourcesInteractor.getString(R.string.str_flight_type_title)}:${flightType?.typeTitle}"
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
            sNightTime: String
    ) {
        val flightTimeObs = correctTimeObs(sFlightTime, intFlightTime)
        val groundTimeObs = correctTimeObs(sGroundTime, intGroundTime)
        val nightTimeObs = correctTimeObs(sNightTime, intNightTime)
        Observables.zip(flightTimeObs, groundTimeObs, nightTimeObs)
                .map {
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
                    true
                }
                .subscribeFromPresenter({
                    val flt = flight
                    if (flt != null) {
                        flt.datetime = mDateTime
                        flt.flightTime = intFlightTime
                        flt.totalTime = intFlightTime
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
                }, {
                    viewState.toastError(it.message)
                })
    }

    private fun addNewFlight(flt: Flight) {
        flightsInteractor.insertFlightAndGet(flt)
                .subscribeFromPresenter({
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

    private fun updateFlight(flt: Flight) {
        flightsInteractor.updateFlight(flt)
                .subscribeFromPresenter({
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
                .observeOnMain()
                .subscribeFromPresenter({
                    if (it) {
                        viewState.setResultOK()
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
            stringTime.isBlank() -> CorrectedTimePair(
                    logTime,
                    if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
            )
            stringTime.length == 1 -> {
                logTime = stringTime.parseInt(0)
                CorrectedTimePair(
                        logTime,
                        if (logTime != 0) String.format("00:0%d", logTime) else ""
                )
            }
            stringTime.length == 2 -> {
                logMinutes = stringTime.parseInt(0)
                logTime = stringTime.parseInt(0)
                if (logMinutes > 59) {
                    logHours = 1
                    logMinutes -= 60
                }
                val format = String.format(
                        "%s:%s",
                        DateTimeUtils.pad(logHours),
                        DateTimeUtils.pad(logMinutes)
                )
                CorrectedTimePair(
                        logTime,
                        format
                )
            }
            stringTime.length > 2 -> {
                if (stringTime.contains(":")) {
                    logMinutes =
                            stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(0, stringTime.length - 3).parseInt(0)
                } else {
                    logMinutes =
                            stringTime.substring(stringTime.length - 2, stringTime.length).parseInt(0)
                    logHours = stringTime.substring(0, stringTime.length - 2).parseInt(0)
                }
                if (logMinutes > 59) {
                    logHours += 1
                    logMinutes -= 60
                }
                logTime = DateTimeUtils.logTimeMinutes(logHours, logMinutes)
                CorrectedTimePair(
                        logTime,
                        DateTimeUtils.strLogTime(logTime)
                )
            }
            else -> CorrectedTimePair(
                    logTime,
                    if (logTime != 0) DateTimeUtils.strLogTime(logTime) else ""
            )
        }
    }

    fun colorClick() {
        fromCallable { getColorsIntArray() }
                .subscribeFromPresenter({ colors ->
                    viewState.onColorSelect(colors)
                })
    }

    fun onColorSelected(color: Int) {
        flight?.params?.setParam(PARAM_COLOR, color.toHexColor())
        viewState.setViewColor(color)
        viewState.setRemoveColorVisible(true)
    }

    fun removeColor() {
        flight?.params?.removeParam(PARAM_COLOR)
        viewState.setViewColor(android.R.color.transparent)
        viewState.setRemoveColorVisible(false)
    }

    fun setVfrIfr(vfrIfrId: Int) {
        flight?.ifrvfr = vfrIfrId
    }

    fun checkAutoExportFile() {
        val autoExportXLS = preferencesInteractor.isAutoExportXLS()
        if (autoExportXLS) {
            viewState.requestStorageAndSave()
        } else {
            viewState.saveFlight()
        }
    }

    private fun requestCodes() {
        fromCallable { resourcesInteractor.getAssetFileString("codes/Codes.dat") }
                .map { it.split(",") }
                .subscribeFromPresenter({
                    airportCodes = it.toMutableList()
                    viewState.updateMultiCompleteCodes(airportCodes.toTypedArray())
                })

    }

    private fun getNewCodes(sequence: Sequence<String>, baseList: List<String>): List<String> {
        return sequence
                .filter { !baseList.contains(it) }
                .distinct()
                .filter { it.isNotBlank() }
                .toList()
    }

    private fun getInputCodes(text: String): Sequence<String> {
        return text.split("-", " ", ",", " ")
                .asSequence()
                .filter { it.length == 4 }
                .filter { item ->
                    item.toCharArray().any {
                        (it.isUpperCase() && (it in 'A'..'Z')) or it.isDigit()
                    }
                }
    }

    fun updateCodes(input: String) {
        val inputCodesSeq = getInputCodes(input)
        val newCodes = getNewCodes(inputCodesSeq, airportCodes)
        if (newCodes.isNotEmpty()) {
            airportCodes.addAll(newCodes)
            viewState.updateMultiCompleteCodes(airportCodes.toTypedArray())
        }
        val inputCodes = inputCodesSeq.toList()
        if (inputCodes.size >= 2) {
            val from = inputCodes.first()
            val to = inputCodes[1]
            viewState.setRoute(from, to)
        } else {
            viewState.setRoute(null, null)
        }
    }
}