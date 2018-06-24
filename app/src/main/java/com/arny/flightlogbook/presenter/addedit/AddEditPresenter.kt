package com.arny.flightlogbook.presenter.addedit

import com.arny.arnylib.presenter.base.BaseMvpPresenterImpl
import com.arny.arnylib.utils.DateTimeUtils
import com.arny.arnylib.utils.Utility
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepository
import org.joda.time.DateTime


class AddEditPresenter(private val repository: MainRepository) : BaseMvpPresenterImpl<AddEditContract.View>(), AddEditContract.Presenter {
    override fun setUIFromFlight(flight: Flight) {
        mView?.setDescription(flight.description ?: "")
        mView?.setDateTime(DateTimeUtils.getDateTime(flight.datetime, "dd.MM.yyyy"))
        logTime = flight.logtime
        val strLogTime = DateTimeUtils.strLogTime(logTime)
        mView?.setLogTime(strLogTime)
    }

    override fun initUIFromId(id: Int) {
        Utility.mainThreadObservable(repository.getFlight(id.toLong()))
                .subscribe({
                    if (it != null) {
                        setUIFromFlight(it)
                    } else {
                        mView?.toastError(repository.getString(R.string.record_not_found))
                        initEmptyUI()
                    }
                }) {
                    initEmptyUI()
                    mView?.toastError(repository.getString(R.string.record_not_found) + ":" + it.message)
                }
//        disposable.add(Utility.mainThreadObservable<Flight>(Observable.fromCallable<Flight> { Local.getFlightItem(mRowId, this@AddEditActivity) }).subscribe({ flight ->
//            logTime = flight!!.logtime
//            reg_no = flight!!.reg_no
//            edtRegNo!!.setText(reg_no)
//            edtTime!!.setText(DateTimeUtils.strLogTime(logTime))
//            airplane_type_id = flight!!.airplanetypeid
//            var airplanetypetitle = flight!!.airplanetypetitle
//            if (Utility.empty(airplanetypetitle)) {
//                val aircraftTypeItem = Local.getTypeItem(airplane_type_id, this@AddEditActivity)
//                val airplType = aircraftTypeItem?.typeName
//                airplanetypetitle = if (Utility.empty(airplType)) getString(R.string.str_type_empty) else getString(R.string.str_type) + " " + airplType
//            }
//            tvAirplaneType!!.setText(airplanetypetitle)
//            day_night = flight!!.daynight
//            spinDayNight!!.setSelection(day_night)
//            ifr_vfr = flight!!.ifrvfr
//            spinVfrIfr!!.setSelection(ifr_vfr)
//            flight_type = flight!!.flighttype
//            spinFlightType!!.setSelection(flight_type)
//        }, { throwable -> ToastMaker.toastError(this, throwable.message) }))
    }

    private var logTime: Int = 0
    override fun initEmptyUI() {
        mView?.setDescription("")
        mView?.setDate("")
        Utility.mainThreadObservable(repository.getDbTypeList())
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

    override fun initState(id: Int?) {
        if (id != null) {
            initUIFromId(id)
        } else {
            initEmptyUI()
        }
    }
}