package com.arny.flightlogbook.presenter.addedit

import com.arny.arnylib.presenter.base.BaseMvpPresenterImpl
import com.arny.arnylib.utils.DateTimeUtils
import com.arny.arnylib.utils.Utility
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepository


class AddEditPresenter(private val repository: MainRepository) : BaseMvpPresenterImpl<AddEditContract.View>(), AddEditContract.Presenter {
    override fun setUIFromFlight(flight: Flight) {
        mView?.setDescription(flight.description ?: "")
        mView?.setDateTime(DateTimeUtils.getDateTime(flight.datetime, "dd.MM.yyyy"))
        logTime = flight.logtime
        val strLogTime = DateTimeUtils.strLogTime(logTime)
        mView?.setLogTime(strLogTime)
        mView?.setRegNo(flight.reg_no)
        val airplType = flight.airplanetypetitle
        val airplanetypetitle = if (Utility.empty(airplType)) repository.getString(R.string.str_type_empty) else repository.getString(R.string.str_type) + " " + airplType
        mView?.setPlaneType(airplanetypetitle)
        mView?.setSpinDayNight(flight.daynight)
        mView?.setSpinIfrVfr(flight.ifrvfr)
        mView?.setFlightType(flight.flighttype)
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
    }

    private var logTime: Int = 0
    override fun initEmptyUI() {
        mView?.setDescription("")
        mView?.setDate("")
    }

    private fun loadPLaneTypes() {
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
        loadPLaneTypes()
        if (id != null) {
            initUIFromId(id)
        } else {
            initEmptyUI()
        }
    }
}