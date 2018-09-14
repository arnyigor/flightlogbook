package com.arny.flightlogbook.presenter.viewflights

import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.presenter.base.BaseMvpPresenter
import com.arny.flightlogbook.presenter.base.BaseMvpView

object ViewFlightsContract {
    interface View : BaseMvpView {
        fun updateAdapter(flights: ArrayList<Flight>)
        fun displayTotalTime(time: String)
    }

    interface Presenter : BaseMvpPresenter<View> {
        fun loadFlights()
        fun removeAllFlights()
        fun removeItem(item: Flight?)
    }
}