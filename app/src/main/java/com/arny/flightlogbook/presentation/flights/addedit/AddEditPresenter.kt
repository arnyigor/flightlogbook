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
import com.arny.helpers.coroutins.launchAsync
import com.arny.helpers.utils.*
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import javax.inject.Inject


@InjectViewState
class AddEditPresenter : MvpPresenter<AddEditView>(),CompositeDisposableComponent {
    @Inject
    lateinit var flightsUseCase: FlightsUseCase
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
    private var sumlogTime: Int = 0
    @Volatile
    private var sumFlightTime: Int = 0
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
        viewState?.setDescription(flight.description ?: "")
        viewState?.setRegNo(flight.regNo)
        viewState?.setToolbarTitle(commonUseCase.getString(R.string.str_edt_flight))
        viewState?.toastError("ГЛОБАЛЬНАЯ ПЕРЕДЕЛКА СТУКТУРЫ ДАННЫХ")
        loadDateTime(flight)
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
                    .observeSubscribeAdd({
                        val flightType = it.value
                        if (flightType != null) {
                            val title = "${commonUseCase.getString(R.string.str_flight_type_title)}${flightType.typeTitle}"
                            viewState?.setFligtTypeTitle(title)
                        }
                    })
        }
    }

    private fun loadDateTime(flight: Flight) {
        fromCallable { DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy") }
                .observeSubscribeAdd({ viewState?.setDate(it) })
    }

    fun correctFlightTime(stringTime: String) {
        correctLogTime(stringTime,intFlightTime) { time, timeText ->
            intFlightTime = time
            viewState?.setEdtFlightTimeText(timeText)
            viewState?.timeSummChange()
        }
    }

    fun correctNightTime(stringTime: String) {
        correctLogTime(stringTime,intNightTime) { time, timeText ->
            intNightTime = time
            viewState?.setEdtNightTimeText(timeText)
            viewState?.timeSummChange()
        }
    }

    fun correctGroundTime(stringTime: String) {
        correctLogTime(stringTime,intGroundTime) { time, timeText ->
            intGroundTime = time
            viewState?.setEdtGroundTimeText(timeText)
            viewState?.timeSummChange()
        }
    }

    private fun loadFlight(id: Long?) {
        flightsUseCase.getFlight(id ?: 0)
                .observeSubscribeAdd({
                    this.flight = it.value
                    if (flight != null) {
                        initUI(flight!!)
                    } else {
                        viewState?.toastError(commonUseCase.getString(R.string.record_not_found))
                        initEmptyUI()
                    }
                }, {
                    it.printStackTrace()
                    initEmptyUI()
                    viewState?.toastError(commonUseCase.getString(R.string.record_not_found) + ":" + it.message)
                })
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
        intFlightTime = setLogTimefromMoto(mMotoResult)
        val logHours = intFlightTime / 60
        val logMinutes = intFlightTime % 60
        val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
        viewState?.setEdtFlightTimeText(format)
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
        },{
            viewState?.setDate(it)
        },{
            it.printStackTrace()
        })
    }

    private fun convertDateTime() = DateTimeUtils.getDateTime(mDateTime, "dd.MM.yyyy")

    fun initDateFromMask(extractedValue: String) {
        launchAsync({
            mDateTime = DateTimeUtils.getJodaDateTime(extractedValue, "ddMMyyyy",true).withTimeAtStartOfDay().millis
        },{

        },{
            setDayToday()
            viewState?.toastError("Ошибка ввода даты")
        })
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
                    sumlogTime = intFlightTime + times.sumBy { it.time }
                    sumFlightTime = intFlightTime + times.filter { it.addToFlightTime }.sumBy { it.time }
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

    fun saveFlight(regNo: String, descr: String,  timeStr: String) {
        correctLogTime(timeStr,intFlightTime) { time, timeText ->
            intFlightTime = time
            viewState?.setEdtFlightTimeText(timeText)
            viewState?.timeSummChange()
            if (mDateTime == 0L) {
                mDateTime = System.currentTimeMillis()
            }
            flight?.datetime = mDateTime
            flight?.flightTime = intFlightTime
            flight?.sumFlightTime = sumFlightTime
            flight?.regNo = regNo
            flight?.description = descr
            flight?.let { flt ->
                if (flt.id != null) {
                    flightsUseCase.updateFlight(flt)
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
                }else{
                    flightsUseCase.insertFlightAndGet(flt)
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
            } ?: viewState?.toastError(commonUseCase.getString(R.string.empty_flight))
        }
    }

    fun removeFlight() {
        flightsUseCase.removeFlight(flight?.id)
                .observeOnMain()
                .subscribe({
                    if (it) {
                        viewState?.toastSuccess(commonUseCase.getString(R.string.flight_removed))
                        viewState?.onPressBack()
                    }else{
                        viewState?.toastError(commonUseCase.getString(R.string.flight_not_removed))
                    }
                }, {
                    viewState?.toastError(it.message)
                }).addTo(compositeDisposable)
    }
}