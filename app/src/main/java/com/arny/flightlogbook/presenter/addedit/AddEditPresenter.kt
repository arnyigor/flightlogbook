package com.arny.flightlogbook.presenter.addedit

import android.annotation.SuppressLint
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.presenter.base.BaseMvpPresenterImpl
import com.arny.flightlogbook.utils.DateTimeUtils
import com.arny.flightlogbook.utils.MathUtils
import com.arny.flightlogbook.utils.observeOnMain


class AddEditPresenter : BaseMvpPresenterImpl<AddEditContract.View>(), AddEditContract.Presenter {
    private var logTime: Int = 0
    private val repository = MainRepositoryImpl.instance
    private var aircraftType: AircraftType? = null
    private var flight: Flight? = null
    private var logHours: Int = 0
    private var logMinutes: Int = 0
    private var mMotoStart: Float = 0.toFloat()
    private var mMotoFinish: Float = 0.toFloat()
    private var mMotoResult: Float = 0.toFloat()
    override fun setUIFromFlight(flight: Flight) {
        mView?.setDescription(flight.description ?: "")
        mView?.setDate(DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy"))
        logTime = flight.logtime ?: 0
        mView?.setLogTime(DateTimeUtils.strLogTime(logTime))
        mView?.setRegNo(flight.reg_no)
        mView?.setSpinDayNight(flight.daynight ?: 0)
        mView?.setSpinIfrVfr(flight.ifrvfr ?: 0)
        mView?.setFlightType(flight.flighttype ?: 0)
    }

    override fun correctLogTime(stringTime: String) {
        val inputLogtime = stringTime
        if (inputLogtime.isBlank()) {
            if (logTime != 0) {
                val strLogTime = DateTimeUtils.strLogTime(logTime)
                mView?.setEdtTimeText(strLogTime)
            } else {
                mView?.setEdtTimeText("00:00")
            }
        } else if (inputLogtime.length == 1) {
            logTime = Integer.parseInt(inputLogtime)
            val format = String.format("00:0%d", logTime)
            mView?.setEdtTimeText(format)
        } else if (inputLogtime.length == 2) {
            logMinutes = Integer.parseInt(inputLogtime)
            logTime = Integer.parseInt(inputLogtime)
            if (logMinutes > 59) {
                logHours = 1
                logMinutes -= 60
            }
            val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
            mView?.setEdtTimeText(format)
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
            mView?.setEdtTimeText(format)
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
                        mView?.toastError(repository.getString(R.string.record_not_found))
                        initEmptyUI()
                    }
                }) {
                    it.printStackTrace()
                    initEmptyUI()
                    mView?.toastError(repository.getString(R.string.record_not_found) + ":" + it.message)
                }
    }

    override fun initEmptyUI() {
        mView?.setDescription("")
        mView?.setDate("")
    }

    @SuppressLint("CheckResult")
    private fun loadPLaneTypes() {
        val observable = repository.getAircraftTypes()
        observable.observeOnMain()
                .subscribe({
                    if (it != null) {
                        mView?.updateAircaftTypes(it)
                    } else {
                        mView?.updateAircaftTypes(arrayListOf())
                    }
                }, {
                    mView?.updateAircaftTypes(arrayListOf())
                    it.printStackTrace()
                })
    }

    override fun initState(id: Long?) {
        loadPLaneTypes()
        if (id != null && id != 0L) {
            initUIFromId(id)
        } else {
            initEmptyUI()
        }
    }

    override fun onMotoTimeChange(startTime: String, finishTime: String) {
        if (startTime.isNotBlank() && finishTime.isNotBlank()) {
            mMotoStart = java.lang.Float.parseFloat(startTime)
            mMotoFinish = java.lang.Float.parseFloat(finishTime)
            mMotoResult = getMotoTime(mMotoStart, mMotoFinish)
            val motoTime = setLogTimefromMoto(mMotoResult)
            mView?.setMotoTimeResult(DateTimeUtils.strLogTime(motoTime))
        }
    }

    override fun setMotoResult() {
        logTime = setLogTimefromMoto(mMotoResult)
        logHours = logTime / 60
        logMinutes = logTime % 60
        val format = String.format("%s:%s", DateTimeUtils.pad(logHours), DateTimeUtils.pad(logMinutes))
        mView?.setEdtTimeText(format)
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
}