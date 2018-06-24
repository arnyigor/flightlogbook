package com.arny.flightlogbook.presenter.addedit

import com.arny.arnylib.presenter.base.BaseMvpPresenter
import com.arny.arnylib.presenter.base.BaseMvpView
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.models.Flight

object AddEditContract {
    interface View : BaseMvpView {
        fun setDescription(desc: String)
        fun setDate(date: String)
        fun updateAircaftTypes(types: List<AircraftType>)
        fun setDateTime(mDateTime: String)
        fun setLogTime(strLogTime: String?)

    }

    interface Presenter : BaseMvpPresenter<View> {
        fun initState(id: Int?)
        fun initEmptyUI()
        fun initUIFromId(id: Int)
        fun setUIFromFlight(flight: Flight)
    }
}