package com.arny.flightlogbook.presenter.addedit

import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.presenter.base.BaseMvpPresenter
import com.arny.flightlogbook.presenter.base.BaseMvpView

object AddEditContract {
    interface View : BaseMvpView {
        fun setDescription(desc: String)
        fun setDate(date: String)
        fun updateAircaftTypes(types: List<AircraftType>)
        fun setLogTime(strLogTime: String?)
        fun setRegNo(regNo: String?)
        fun setSpinDayNight(daynight: Int)
        fun setSpinIfrVfr(ifrvfr: Int)
        fun setFlightType(flighttype: Int)
        fun setEdtTimeText(strLogTime: String?)
        fun setMotoTimeResult(motoTime: String?)
    }

    interface Presenter : BaseMvpPresenter<View> {
        fun initState(id: Long?)
        fun initEmptyUI()
        fun initUIFromId(id: Long?)
        fun setUIFromFlight(flight: Flight)
        fun setAircraftType(aircraftType: AircraftType?)
        fun correctLogTime(stringTime: String)
        fun onMotoTimeChange(startTime: String, finishTime: String)
        fun setMotoResult()
        fun addAircraftType(name: String)
    }
}