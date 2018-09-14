package com.arny.flightlogbook.presenter.addedit

import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import com.arny.flightlogbook.presenter.base.BaseMvpPresenterImpl
import com.arny.flightlogbook.utils.DateTimeUtils
import com.arny.flightlogbook.utils.Utility


class AddEditPresenter : BaseMvpPresenterImpl<AddEditContract.View>(), AddEditContract.Presenter {
    private var logTime: Int = 0
    private val repository = MainRepositoryImpl.instance
    override fun setUIFromFlight(flight: Flight) {
        mView?.setDescription(flight.description ?: "")
        mView?.setDateTime(DateTimeUtils.getDateTime(flight.datetime ?: 0, "dd.MM.yyyy"))
        logTime = flight.logtime ?: 0
        val strLogTime = DateTimeUtils.strLogTime(logTime)
        mView?.setLogTime(strLogTime)
        mView?.setRegNo(flight.reg_no)
        val airplType = flight.airplanetypetitle
        val airplanetypetitle = if (Utility.empty(airplType)) repository.getString(R.string.str_type_empty) else repository.getString(R.string.str_type) + " " + airplType
        mView?.setPlaneType(airplanetypetitle)
        mView?.setSpinDayNight(flight.daynight ?: 0)
        mView?.setSpinIfrVfr(flight.ifrvfr ?: 0)
        mView?.setFlightType(flight.flighttype ?: 0)
    }

    override fun initUIFromId(id: Long?) {
        Utility.mainThreadObservable(repository.getFlight(id ?: 0))
                .subscribe({
                    if (it != null) {
                        setUIFromFlight(it)
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

    override fun initState(id: Long?) {
        loadPLaneTypes()
        if (id != null) {
            initUIFromId(id)
        } else {
            initEmptyUI()
        }
    }
}