package com.arny.flightlogbook.presentation.flights.addedit.presenter

import com.arny.core.CONSTS
import com.arny.core.CONSTS.STRINGS.PARAM_COLOR
import com.arny.core.utils.*
import com.arny.core.utils.DateTimeUtils.strLogTime
import com.arny.domain.airports.IAirportsInteractor
import com.arny.domain.common.PreferencesInteractor
import com.arny.domain.common.ResourcesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Airport
import com.arny.domain.models.Flight
import com.arny.domain.models.Params
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.customfields.domain.CustomFieldInteractor
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.flights.addedit.models.getCorrectDayTime
import com.arny.flightlogbook.presentation.flights.addedit.models.getCorrectTime
import com.arny.flightlogbook.presentation.flights.addedit.view.AddEditView
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import moxy.InjectViewState
import org.joda.time.DateTime
import javax.inject.Inject

@InjectViewState
class AddEditPresenter : BaseMvpPresenter<AddEditView>() {

    private var customFieldsValues = mutableListOf<CustomFieldValue>()

    @Inject
    lateinit var flightsInteractor: FlightsInteractor

    @Inject
    lateinit var airportsInteractor: IAirportsInteractor

    @Inject
    lateinit var customFieldInteractor: CustomFieldInteractor

    @Inject
    lateinit var resourcesInteractor: ResourcesInteractor

    @Inject
    lateinit var prefsInteractor: PreferencesInteractor
    internal var flightId: Long? = null

    @Volatile
    var intFlightTime: Int = 0

    @Volatile
    var intNightTime: Int = 0

    @Volatile
    var intDepTime: Int = 0

    @Volatile
    var intArrivalTime: Int = 0

    @Volatile
    var intGroundTime: Int = 0

    @Volatile
    private var intTotalTime: Int = 0

    @Volatile
    private var flight: Flight? = null
    private var mDateTime: Long = 0
    private var mMotoStart: Float = 0.toFloat()
    private var mMotoFinish: Float = 0.toFloat()
    private var mMotoResult: Float = 0.toFloat()
    private val customFieldEnabled = CONSTS.COMMON.ENABLE_CUSTOM_FIELDS

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        initState()
    }

    private fun loadFlight(id: Long) {
        fromNullable { flightsInteractor.getFlight(id) }
            .subscribeFromPresenter({ optionalNull ->
                val value = optionalNull.value
                if (value != null) {
                    flight = value
                    initUI(flight!!)
                } else {
                    viewState.toastError(getString(R.string.record_not_found))
                    initEmptyUI()
                }
            })
    }

    private fun initEmptyUI() {
        viewState.setDescription("")
        viewState.setDate("")
        viewState.setToolbarTitle(R.string.str_add_flight)
        flight = Flight()
        flight?.params = Params()
        loadCustomFields()
        loadLastSavedData()
    }

    private fun loadLastSavedData() {
        prefsInteractor.getSavedFlightTypeId()?.let {
            flight?.flightTypeId = it
            loadFlightType(flight?.flightTypeId)
        }
        prefsInteractor.getSavedAircraftId()?.let {
            setFlightPlaneType(it)
        }
    }

    private fun initState() {
        if (flightId != null) {
            viewState.setToolbarTitle(R.string.str_edt_flight)
            loadFlight(flightId!!)
        } else {
            initEmptyUI()
        }
    }


    private fun initUI(flight: Flight) {
        viewState.setDescription(flight.description ?: "")
        loadColor(flight)
        loadDateTime(flight)
        loadTimes(flight)
        loadIfrVfr(flight)
        loadFlightType(flight.flightTypeId)
        loadPlaneTypes(flight)
        loadCustomFields()
        loadDepArrival(flight)
        loadDepArrivalTime(flight)
    }

    private fun loadDepArrivalTime(flight: Flight) {
        flight.departureUtcTime?.let {
            intDepTime = it
            viewState.setEdtDepTime(it)
        }
        flight.arrivalUtcTime?.let {
            intArrivalTime = it
            viewState.setEdtArrTimeText(strLogTime(it))
        }
    }

    private fun loadDepArrival(flight: Flight) {
        fromSingle { airportsInteractor.getAirport(flight.departureId) }
            .subscribeFromPresenter({ optionalNull ->
                optionalNull.value?.let {
                    viewState.setDeparture(it)
                }
            })
        fromSingle { airportsInteractor.getAirport(flight.arrivalId) }
            .subscribeFromPresenter({ optionalNull ->
                optionalNull.value?.let {
                    viewState.setArrival(it)
                }
            })
    }

    private fun loadCustomFields() {
        viewState.setCustomFieldsVisible(customFieldEnabled)
        if (customFieldEnabled) {
            fromSingle { customFieldInteractor.getCustomFieldsWithValues(flightId) }
                .map { list ->
                    list.forEach { fieldValue ->
                        if (fieldValue.type is CustomFieldType.Time) {
                            val (_, strVal) = getCorrectTime(
                                fieldValue.value.toString(),
                                DateTimeUtils.convertStringToTime(fieldValue.value.toString())
                            )
                            fieldValue.value = strVal
                        }
                    }
                    list
                }
                .subscribeFromPresenter({
                    customFieldsValues = it.toMutableList()
                    viewState.setFieldsList(customFieldsValues, true)
                    timeSummChanged()
                }, {
                    it.printStackTrace()
                })
        }
    }

    private fun loadIfrVfr(flight: Flight) {
        viewState.setIfrSelected(flight.ifrvfr == 1)
    }

    private fun loadColor(flight: Flight) {
        if (flight.colorInt != null) {
            viewState.setViewColor(flight.colorInt!!)
            viewState.setRemoveColorVisible(true)
        } else {
            loadColorParam(flight)
        }
    }

    private fun loadColorParam(flight: Flight) {
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

    private fun loadPlaneTypes(flight: Flight) {
        when {
            flight.planeId != null -> setFlightPlaneType(flight.planeId)
            !flight.regNo.isNullOrBlank() -> loadPlaneType(
                fromNullable { flightsInteractor.loadPlaneTypeByRegNo(flight.regNo) }
            )
        }
    }

    private fun loadFlightType(typeId: Int?) {
        typeId?.let {
            fromNullable { flightsInteractor.loadFlightType(it.toLong()) }
                .subscribeFromPresenter({
                    val title =
                        "${getString(R.string.str_flight_type_title)}:${it.value?.typeTitle ?: "-"}"
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
        fromCallable { strLogTime(intFlightTime) }
            .subscribeFromPresenter({ viewState.setEdtFlightTimeText(it) })
        fromCallable { strLogTime(intNightTime) }
            .subscribeFromPresenter({ viewState.setEdtNightTimeText(it) })
        fromCallable { strLogTime(intGroundTime) }
            .subscribeFromPresenter({ viewState.setEdtGroundTimeText(it) })
        timeSummChanged()
    }

    private fun timeSummChanged() {
        if (intNightTime > intFlightTime) {
            intFlightTime = intNightTime
        }
        fromCallable { strLogTime(intFlightTime) }
            .subscribeFromPresenter({ viewState.setEdtFlightTimeText(it) })
        val customTimes =
            if (customFieldEnabled) flightsInteractor.getAddTimeSum(customFieldsValues) else 0
        intTotalTime = intFlightTime + intGroundTime + customTimes
        fromCallable { strLogTime(intTotalTime) }
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

    fun setDepartureTime(time: Int) {
        intDepTime = time
        flight?.departureUtcTime = intDepTime
        correctFlightTimeByDepArr()
        timeSummChanged()
    }

    fun setArrivalTime(time: Int) {
        intArrivalTime = time
        flight?.arrivalUtcTime = intArrivalTime
        correctFlightTimeByDepArr()
        timeSummChanged()
    }

    fun correctArrivalTime(stringTime: String) {
        correctDayTimeObs(stringTime, intDepTime)
            .doOnNext {
                intArrivalTime = it.intTime
                flight?.arrivalUtcTime = intArrivalTime
                correctFlightTimeByDepArr()
            }
            .subscribeFromPresenter({
                viewState.setEdtArrTimeText(it.strTime)
                timeSummChanged()
            })
    }

    private fun correctFlightTimeByDepArr() {
        intFlightTime = if (intArrivalTime >= intDepTime) {
            intArrivalTime - intDepTime
        } else {
            (24 * 60 - intDepTime) + intArrivalTime
        }
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

    private fun correctDayTimeObs(stringTime: String, initTime: Int) =
        fromCallable { getCorrectDayTime(stringTime, initTime) }

    fun onMotoTimeChange(startTime: String, finishTime: String) {
        if (startTime.isNotBlank() && finishTime.isNotBlank()) {
            mMotoStart = startTime.toFloat()
            mMotoFinish = finishTime.toFloat()
            mMotoResult = getMotoTime(mMotoStart, mMotoFinish)
            viewState.setMotoTimeResult(strLogTime(setLogTimefromMoto(mMotoResult)))
        }
    }

    fun setMotoResult() {
        intFlightTime = setLogTimefromMoto(mMotoResult)
        viewState.setEdtFlightTimeText(strLogTime(intFlightTime))
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
            viewState.toastError(getString(R.string.error_enter_date))
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
            viewState.toastError(getString(R.string.date_time_input_error))
        })

    }

    fun setFlightPlaneType(planetypeId: Long?) {
        loadPlaneType(fromNullable { flightsInteractor.loadPlaneType(planetypeId) })
    }

    private fun loadPlaneType(planeTypeNullable: Observable<OptionalNull<PlaneType?>>) {
        planeTypeNullable.subscribeFromPresenter({
            it.value?.let { planeType ->
                flight?.planeId = planeType.typeId
                flight?.planeType = planeType
                val title =
                    "${getString(R.string.str_type)}\n${getString(planeType.mainType?.nameRes)} " +
                            "${planeType.typeName} ${getString(R.string.str_regnum)}:${planeType.regNo}"
                viewState.setPlaneTypeTitle(title)
            } ?: run {
                viewState.setPlaneTypeTitle("${getString(R.string.str_type)}:${getString(R.string.no_type)}")
            }
        }, {
            viewState.toastError(getString(R.string.err_plane_type_not_found))
        })
    }

    fun setFlightType(fightTypeId: Long?) {
        fromNullable { flightsInteractor.loadFlightType(fightTypeId) }
            .subscribeFromPresenter({
                val flightType = it.value
                this.flight?.flightTypeId = flightType?.id?.toInt()
                viewState.setFligtTypeTitle("${getString(R.string.str_flight_type_title)}:${flightType?.typeTitle}")
            }, {
                viewState.toastError(getString(R.string.err_flight_type_not_found))
            })
    }

    fun saveFlight(
        descr: String,
        sFlightTime: String,
        sGroundTime: String,
        sNightTime: String
    ) {
        val flightTimeObs = correctTimeObs(sFlightTime, intFlightTime)
        val groundTimeObs = correctTimeObs(sGroundTime, intGroundTime)
        val nightTimeObs = correctTimeObs(sNightTime, intNightTime)
        Observables.zip(flightTimeObs, groundTimeObs, nightTimeObs)
            .map { (flightTime, groundTime, nightTime) ->
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
                    flt.description = descr
                    val id = flt.id
                    if (id != null) {
                        updateFieldsExternalId(id)
                        updateFlight(flt)
                    } else {
                        addNewFlight(flt)
                    }
                } else {
                    viewState.toastError(getString(R.string.empty_flight))
                }
            }, {
                viewState.toastError(it.message)
            })
    }

    private fun addNewFlight(flt: Flight) {
        fromSingle { flightsInteractor.insertFlightAndGet(flt) }
            .map { saveId ->
                val success = saveId != 0L
                if (success) {
                    updateFieldsExternalId(saveId)
                }
                saveCustomFieldsValues(success)
            }
            .doOnSuccess {
                if (it) {
                    prefsInteractor.setSavedAircraftId(flt.planeId)
                    prefsInteractor.setSavedFlightTypeId(flt.flightTypeId)
                }
            }
            .subscribeFromPresenter({ success ->
                if (success) {
                    viewState.toastSuccess(getString(R.string.flight_save_success))
                    viewState.setResultOK()
                } else {
                    viewState.toastError(getString(R.string.flight_not_save))
                }
            }, {
                viewState.toastError("${getString(R.string.flight_save_error)}:${it.message}")
            })
    }

    private fun updateFieldsExternalId(id: Long) {
        customFieldsValues.forEach { it.externalId = id }
    }

    private fun updateFlight(flt: Flight) {
        fromSingle { flightsInteractor.updateFlight(flt) }
            .map(::saveCustomFieldsValues)
            .doOnSuccess {
                if (it) {
                    prefsInteractor.setSavedAircraftId(flt.planeId)
                    prefsInteractor.setSavedFlightTypeId(flt.flightTypeId)
                }
            }
            .subscribeFromPresenter({
                if (it) {
                    viewState.toastSuccess(getString(R.string.flight_save_success))
                    viewState.setResultOK()
                } else {
                    viewState.toastError(getString(R.string.flight_not_save))
                }
            }, {
                it.printStackTrace()
                viewState.toastError("${getString(R.string.flight_save_error)}:${it.message}")
            })
    }

    private fun saveCustomFieldsValues(success: Boolean): Boolean =
        if (success) {
            customFieldInteractor.saveValues(customFieldsValues, flightId)
        } else {
            false
        }

    fun removeFlight() {
        if (flight?.id != null) {
            flightsInteractor.removeFlight(flight?.id)
                .subscribeFromPresenter({
                    if (it) {
                        viewState.toastSuccess(getString(R.string.flight_removed))
                        viewState.setResultOK()
                    } else {
                        viewState.toastError(getString(R.string.flight_not_removed))
                    }
                }, {
                    viewState.toastError(it.message)
                })
        } else {
            viewState.setResultOK()
        }
    }

    private fun getString(res: Int?) = resourcesInteractor.getString(res)

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
        val autoExportXLS = prefsInteractor.isAutoExportXLS()
        if (autoExportXLS) {
            viewState.requestStorageAndSave()
        } else {
            viewState.saveFlight()
        }
    }

    fun onCustomFieldValueChange(
        item: CustomFieldValue,
        value: String
    ) {
        if (item.type is CustomFieldType.Time) {
            correctTimeObs(value, DateTimeUtils.convertStringToTime(item.value.toString()))
                .subscribeFromPresenter({ pair ->
                    item.value = strLogTime(pair.intTime)
                    customFieldsValues.find { it.fieldId == item.fieldId }?.let {
                        it.value = item.value
                    }
                    timeSummChanged()
                })
        } else {
            item.value = value
            customFieldsValues.find { it.fieldId == item.fieldId }?.let {
                it.value = item.value
            }
        }
    }

    fun addCustomField(customFieldId: Long?) {
        if (customFieldsValues.find { it.fieldId == customFieldId } == null) {
            customFieldId?.let { id ->
                fromSingle { customFieldInteractor.getCustomField(id) }
                    .subscribeFromPresenter({
                        val field = it.value
                        if (field != null) {
                            customFieldsValues.add(
                                CustomFieldValue(
                                    field = field,
                                    externalId = this.flightId,
                                    type = field.type,
                                    fieldId = id
                                )
                            )
                            viewState.setFieldsList(customFieldsValues, true)
                        }
                    }, {
                        viewState.toastError(it.message)
                    })
            }
        }
    }

    fun setDeparture(departure: Airport?) {
        flight?.departureId = departure?.id
        viewState.setDeparture(departure)
    }

    fun setArrival(arrival: Airport?) {
        flight?.arrivalId = arrival?.id
        viewState.setArrival(arrival)
    }

    fun onCustomFieldValueDelete(position: Int) {
        customFieldsValues.getOrNull(position)?.let {
            customFieldsValues.removeAt(position)
            viewState.removeItemFromAdapter(position)
            timeSummChanged()
        }
    }
}
