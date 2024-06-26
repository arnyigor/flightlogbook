package com.arny.flightlogbook.presentation.flights.addedit.presenter

import androidx.annotation.DrawableRes
import com.arny.core.utils.OptionalNull
import com.arny.core.utils.toOptionalNull
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.CONSTS.STRINGS.PARAM_COLOR
import com.arny.flightlogbook.data.models.Airport
import com.arny.flightlogbook.data.models.CustomFieldType
import com.arny.flightlogbook.data.models.CustomFieldValue
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.data.utils.DateTimeUtils
import com.arny.flightlogbook.data.utils.DateTimeUtils.strLogTime
import com.arny.flightlogbook.data.utils.DateTimeUtils.strLogTimeZero
import com.arny.flightlogbook.data.utils.MathUtils
import com.arny.flightlogbook.data.utils.getColorsIntArray
import com.arny.flightlogbook.data.utils.toHexColor
import com.arny.flightlogbook.data.utils.toIntColor
import com.arny.flightlogbook.domain.airports.IAirportsInteractor
import com.arny.flightlogbook.domain.common.PreferencesInteractor
import com.arny.flightlogbook.domain.common.ResourcesInteractor
import com.arny.flightlogbook.domain.customfields.CustomFieldInteractor
import com.arny.flightlogbook.domain.flights.FlightsInteractor
import com.arny.flightlogbook.domain.models.getCorrectDayTime
import com.arny.flightlogbook.presentation.flights.addedit.view.AddEditView
import com.arny.flightlogbook.presentation.mvp.BaseMvpPresenter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import org.joda.time.DateTime
import javax.inject.Inject
import kotlin.properties.Delegates

@InjectViewState
class AddEditPresenter @Inject constructor(
    private val flightsInteractor: FlightsInteractor,
    private val airportsInteractor: IAirportsInteractor,
    private val customFieldInteractor: CustomFieldInteractor,
    private val resourcesInteractor: ResourcesInteractor,
    private val prefsInteractor: PreferencesInteractor,
) : BaseMvpPresenter<AddEditView>() {
    @get:Synchronized
    @set:Synchronized
    private var customFieldsValues = mutableListOf<CustomFieldValue>()
    private var updateDisposable: Disposable? = null
    internal var flightId: Long? = null
        set(value) {
            field = if (value != -1L) value else null
        }

    @Volatile
    var intFlightTime: Int = 0

    @Volatile
    var intNightTime: Int = 0

    @Volatile
    var intDepartureUtcTime: Int = 0

    @Volatile
    var intArrivalUtcTime: Int = 0

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
    private var description: String = ""
    private var saveInProgress = false
    private var customFieldTimeHasFocus by Delegates.observable(false) { _, old, hasFocus ->
        if (old != hasFocus && !hasFocus && saveInProgress) {
            saveFlight(description)
        }
    }
    private var lockDepartureTime by Delegates.observable(false) { _, old, lock ->
        if (old != lock) {
            viewState.setIvLockDepartureTimeIcon(getLockIcon(lock))
        }
    }
    private var lockArrivalTime by Delegates.observable(false) { _, old, lock ->
        if (old != lock) {
            viewState.setIvLockArrivalTimeIcon(getLockIcon(lock))
        }
    }
    private var lockFlightTime by Delegates.observable(false) { _, old, lock ->
        if (old != lock) {
            viewState.setIvLockFlightTimeIcon(getLockIcon(lock))
        }
    }
    private val customFieldEnabled = CONSTS.COMMON.ENABLE_CUSTOM_FIELDS

    private data class TimesResult(
        val intFlightTime: String,
        val intNightTime: String,
        val intGroundTime: String,
        val intTotalTime: String,
    )

    override fun onFirstViewAttach() {
        initState()
    }

    private fun loadFlight(id: Long) {
        Single.fromCallable { flightsInteractor.getFlight(id).toOptionalNull() }
            .subscribeFromPresenter({ optionalNull ->
                val value = optionalNull.value
                if (value != null) {
                    flight = value
                    initUI(value)
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
        flight?.customParams = hashMapOf()
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
        flightId?.let {
            viewState.setToolbarTitle(R.string.str_edt_flight)
            loadFlight(it)
        } ?: kotlin.run {
            initEmptyUI()
        }
    }

    private fun initUI(flight: Flight) {
        viewState.setDescription(flight.description ?: "")
        loadColor(flight)
        loadDateTime(flight)
        loadDepArrivalTime(flight)
        loadTimes(flight)
        loadIfrVfr(flight)
        loadFlightType(flight.flightTypeId)
        loadPlaneTypes(flight)
        loadCustomFields()
        loadDepArrival(flight)
    }

    private fun loadDepArrivalTime(flight: Flight) {
        flight.departureUtcTime?.let {
            intDepartureUtcTime = it
            viewState.setEdtDepUtcTime(it)
        }
        flight.arrivalUtcTime?.let {
            intArrivalUtcTime = it
            viewState.setEdtArrUtcTime(it)
        }
        if (intDepartureUtcTime == 0 && intArrivalUtcTime == 0) {
            intArrivalUtcTime = flight.flightTime
            viewState.setEdtArrUtcTime(intArrivalUtcTime)
        }
    }

    private fun loadDepArrival(flight: Flight) {
        Single.fromCallable { airportsInteractor.getAirport(flight.departureId) }
            .subscribeFromPresenter({ optionalNull ->
                optionalNull.value?.let {
                    viewState.setDeparture(it)
                }
            })
        Single.fromCallable { airportsInteractor.getAirport(flight.arrivalId) }
            .subscribeFromPresenter({ optionalNull ->
                optionalNull.value?.let {
                    viewState.setArrival(it)
                }
            })
    }

    private fun loadCustomFields() {
        viewState.setCustomFieldsVisible(customFieldEnabled)
        if (customFieldEnabled) {
            Single.fromCallable { customFieldInteractor.getCustomFieldsWithValues(flightId) }
                .map { list ->
                    sortedByDefault(list.onEach(::correctCustomFieldTime))
                }
                .subscribeFromPresenter({ values ->
                    customFieldsValues = values
                    viewState.setFieldsList(customFieldsValues)
                    updateTimes()
                }, {
                    it.printStackTrace()
                })
        }
    }

    private fun correctCustomFieldTime(fieldValue: CustomFieldValue) {
        if (fieldValue.field?.type is CustomFieldType.Time) {
            val (_, strVal) = getCorrectDayTime(
                fieldValue.value.toString(),
                DateTimeUtils.convertStringToTime(fieldValue.value.toString())
            )
            fieldValue.value = strVal
        }
    }

    private fun sortedByDefault(list: List<CustomFieldValue>) =
        list.sortedBy { it.field?.showByDefault == false }.toMutableList()

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
        Single.fromCallable { flight.customParams?.get(PARAM_COLOR).toOptionalNull() }
            .map {
                val hexColor = it.value.toString()
                if (hexColor.isNotBlank() && hexColor.startsWith("#")) {
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
                Single.fromCallable {
                    flightsInteractor.loadPlaneTypeByRegNo(flight.regNo).toOptionalNull()
                }
            )
        }
    }

    private fun loadFlightType(typeId: Long?) {
        typeId?.let {
            Single.fromCallable { flightsInteractor.loadFlightType(it).toOptionalNull() }
                .subscribeFromPresenter({
                    val title =
                        "${getString(R.string.str_flight_type_title)}:${it.value?.typeTitle ?: "-"}"
                    viewState.setFligtTypeTitle(title)
                })
        }
    }

    private fun loadDateTime(flight: Flight) {
        mDateTime = flight.datetime ?: 0
        Single.fromCallable { DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy") }
            .subscribeFromPresenter({ s ->
                viewState.setDate(s)
            })
    }

    private fun loadTimes(flight: Flight) {
        intFlightTime = flight.flightTime
        intNightTime = flight.nightTime
        intGroundTime = flight.groundTime
        updateTimes()
    }

    private fun updateUI() {
        val disposable = updateDisposable
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        }
        updateDisposable = Single.fromCallable {
            TimesResult(
                strLogTimeZero(intFlightTime),
                strLogTimeZero(intNightTime),
                strLogTimeZero(intGroundTime),
                strLogTime(intTotalTime)
            )
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (flight, night, ground, total) ->
                viewState.setEdtFlightTimeText(flight)
                viewState.setEdtGroundTimeText(ground)
                viewState.setEdtNightTimeText(night)
                viewState.setTotalTime(total)
            }, {
                it.printStackTrace()
            })
    }

    private fun getCustomTimes(): Int = if (customFieldEnabled)
        flightsInteractor.getAddTimeSum(customFieldsValues) else 0

    fun setDepartureTime(utcTime: Int) {
        intDepartureUtcTime = utcTime
        updateTimes()
    }

    fun setArrivalTime(utcTime: Int) {
        intArrivalUtcTime = utcTime
        intFlightTime = 0
        updateTimes()
    }

    fun setFlightTime(utcTime: Int) {
        intFlightTime = utcTime
        correctByFlightTime()
        updateTimes()
    }

    private fun correctByFlightTime() {
        intArrivalUtcTime = intFlightTime + intDepartureUtcTime
        if (intNightTime > intFlightTime) {
            intNightTime = 0
        }
        if (intFlightTime == 0) {
            intArrivalUtcTime = 0
            intDepartureUtcTime = 0
            viewState.setEdtArrUtcTime(intArrivalUtcTime)
            viewState.setEdtDepUtcTime(intDepartureUtcTime)
        } else {
            viewState.setEdtArrUtcTime(intArrivalUtcTime)
        }
    }

    fun setNightTime(time: Int) {
        intNightTime = time
        updateTimes()
    }

    fun setGroundTime(time: Int) {
        intGroundTime = time
        updateTimes()
    }

    private fun updateTimes() {
        correctTimes()
        updateUI()
    }

    private fun correctTimes() {
        val arrUtcTime = intArrivalUtcTime
        val depUtcTime = intDepartureUtcTime
        intFlightTime = calcFlightTime(arrUtcTime, depUtcTime)
        if (intNightTime > intFlightTime) {
            setFlightTime(intNightTime)
        }
        intTotalTime = intFlightTime + intGroundTime + getCustomTimes()
    }

    private fun calcFlightTime(arrUtcTime: Int, depUtcTime: Int) =
        if (arrUtcTime >= depUtcTime) {
            arrUtcTime - depUtcTime
        } else {
            (24 * 60 - depUtcTime) + arrUtcTime
        }

    private fun correctTimeObs(stringTime: String, initTime: Int) =
        Single.fromCallable { getCorrectDayTime(stringTime, initTime) }

    fun onMotoTimeChange(startTime: String, finishTime: String) {
        if (startTime.isNotBlank() && finishTime.isNotBlank()) {
            mMotoStart = startTime.toFloat()
            mMotoFinish = finishTime.toFloat()
            mMotoResult = getMotoTime(mMotoStart, mMotoFinish)
            viewState.setMotoTimeResult(strLogTime(setLogTimeFromMoto(mMotoResult)))
        }
    }

    fun setMotoResult() {
        intFlightTime = setLogTimeFromMoto(mMotoResult)
        setFlightTime(intFlightTime)
    }

    private fun setLogTimeFromMoto(motoTime: Float): Int {
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
        Single.fromCallable {
            mDateTime = DateTimeUtils.getJodaDateTime(
                "$dayOfMonth.${(monthOfYear + 1)}.$year",
                "dd.MM.yyyy",
                true
            ).withTimeAtStartOfDay().millis
            convertDateTime()
        }
            .subscribeFromPresenter({
                viewState.setDate(it)
            }, {
                viewState.toastError(getString(R.string.error_enter_date))
            })
    }

    private fun setDayToday() {
        Single.fromCallable {
            mDateTime = DateTime.now().withTimeAtStartOfDay().millis
            convertDateTime()
        }.subscribeFromPresenter({
            viewState.setDate(it)
        })
    }

    private fun convertDateTime() = DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy")

    fun initDateFromMask(extractedValue: String) {
        Single.fromCallable {
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

      fun setFlightPlaneType(planeTypeId: Long?) {
        val planeTypeNullable = Single.fromCallable {
            flightsInteractor.loadPlaneType(planeTypeId).toOptionalNull()
        }
        loadPlaneType(planeTypeNullable)
    }

    private fun loadPlaneType(planeTypeNullable: Single<OptionalNull<PlaneType?>>) {
        planeTypeNullable.subscribeFromPresenter({
            it.value?.let { planeType ->
                flight?.planeId = planeType.typeId
                flight?.planeType = planeType
                flight?.regNo = planeType.regNo
                viewState.setPlaneTypeTitle(planeType)
            } ?: run {
                viewState.setPlaneTypeTitle()
            }
        }, {
            viewState.toastError(getString(R.string.err_plane_type_not_found))
        })
    }

    fun setFlightType(fightTypeId: Long?) {
        Single.fromCallable { flightsInteractor.loadFlightType(fightTypeId).toOptionalNull() }
            .subscribeFromPresenter({
                val flightType = it.value
                this.flight?.flightTypeId = flightType?.id
                viewState.setFligtTypeTitle("${getString(R.string.str_flight_type_title)}:${flightType?.typeTitle}")
            }, {
                viewState.toastError(getString(R.string.err_flight_type_not_found))
            })
    }

    fun saveFlight(description: String) {
        saveInProgress = true
        this.description = description
        if (customFieldTimeHasFocus) {
            return
        }
        updateTimes()
        Single.fromCallable {
            if (mDateTime == 0L) {
                mDateTime = System.currentTimeMillis()
            }
            mDateTime
        }
            .subscribeFromPresenter({ time ->
                val flt = flight
                if (flt != null) {
                    flt.datetime = time
                    flt.flightTime = intFlightTime
                    flt.nightTime = intNightTime
                    flt.groundTime = intGroundTime
                    flt.totalTime = intTotalTime
                    flt.description = description
                    flt.departureUtcTime = intDepartureUtcTime
                    flt.arrivalUtcTime = intArrivalUtcTime
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
                saveInProgress = false
            }, {
                saveInProgress = false
                viewState.toastError(it.message)
            })
    }

    private fun addNewFlight(flt: Flight) {
        Single.fromCallable { flightsInteractor.insertFlightAndGet(flt) }
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
        customFieldsValues = customFieldsValues.filter {
            val value = it.value.toString()
            value != "null" && value.isNotBlank()
        }.toMutableList()
        customFieldsValues.forEach { it.externalId = id }
    }

    private fun updateFlight(flt: Flight) {
        Single.fromCallable { flightsInteractor.updateFlight(flt) }
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
        Single.fromCallable { getColorsIntArray() }
            .subscribeFromPresenter({ colors ->
                viewState.onColorSelect(colors)
            })
    }

    fun onColorSelected(color: Int) {
        flight?.customParams?.set(PARAM_COLOR, color.toHexColor())
        viewState.setViewColor(color)
        viewState.setRemoveColorVisible(true)
    }

    fun removeColor() {
        flight?.customParams?.remove(PARAM_COLOR)
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
        value: String,
        position: Int
    ) {
        if (item.field?.type is CustomFieldType.Time) {
            var timeValue = value
            if (timeValue.isBlank()) {
                timeValue = "0"
            }
            correctTimeObs(timeValue, DateTimeUtils.convertStringToTime(item.value.toString()))
                .subscribeFromPresenter({ pair ->
                    item.value = strLogTime(pair.intTime)
                    customFieldsValues.find { it.fieldId == item.fieldId }?.let {
                        it.value = item.value
                    }
                    viewState.setFieldsList(customFieldsValues)
                    viewState.notifyItemChanged(position)
                    updateTimes()
                    onCustomFieldTimeInChanges(false)
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
                Single.fromCallable { customFieldInteractor.getCustomField(id) }
                    .map { optionalNull ->
                        val field = optionalNull.value
                        var added = false
                        var fields = customFieldsValues
                        if (field != null) {
                            added = fields.add(
                                CustomFieldValue(
                                    field = field,
                                    externalId = this.flightId,
                                    fieldId = id
                                )
                            )
                            fields = sortedByDefault(fields)
                        }
                        fields to added
                    }
                    .subscribeFromPresenter({ (values, added) ->
                        if (added) {
                            customFieldsValues = values
                            viewState.setFieldsList(customFieldsValues)
                            updateTimes()
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

    fun onCustomFieldValueDelete(item: CustomFieldValue, position: Int) {
        Single.fromCallable {
            customFieldsValues.find { it.fieldId == item.fieldId }.toOptionalNull()
        }
            .map { fieldNullable ->
                val fields = customFieldsValues
                val fieldValue = fieldNullable.value
                val removed: Boolean = fieldValue != null && fields.remove(fieldValue)
                sortedByDefault(fields) to removed
            }
            .subscribeFromPresenter({ (values, removed) ->
                if (removed) {
                    customFieldsValues = values
                    viewState.setFieldsList(customFieldsValues)
                    viewState.notifyItemRemoved(position)
                    updateTimes()
                }
            }, {
                viewState.toastError(it.message)
            })
    }

    fun revokeSafeFile() {
        prefsInteractor.setAutoExportXLS(false)
    }

    fun onCustomFieldTimeInChanges(hasFocus: Boolean) {
        this.customFieldTimeHasFocus = hasFocus
    }

    fun onToggleLockDepartureTime() {
        // TODO добавить функционал
        // lockDepartureTime = !lockDepartureTime
        // updateTimeLocks()
    }

    // TODO добавить функционал
    fun onToggleLockArrivalTime() {
        // TODO добавить функционал
        // lockArrivalTime = !lockArrivalTime
        // updateTimeLocks()
    }

    fun onToggleLockFlightTime() {
        // TODO добавить функционал
        // lockFlightTime = !lockFlightTime
        // updateTimeLocks()
    }

    private fun updateTimeLocks() {
        // TODO добавить функционал
    }

    @DrawableRes
    private fun getLockIcon(lock: Boolean) = if (lock) {
        R.drawable.ic_lock
    } else {
        R.drawable.ic_lock_open
    }
}
