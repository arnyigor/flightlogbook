package com.arny.flightlogbook.presentation.flights.addedit

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.arny.domain.models.TimeToFlight

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface AddEditView : MvpView {
    fun setDescription(desc: String)
    fun setDate(date: String)
    fun setRegNo(regNo: String?)
    fun setEdtFlightTimeText(strLogTime: String?)
    fun setMotoTimeResult(motoTime: String?)
    fun setToolbarTitle(string: String)
    fun toastError(msg: String?)
    fun setPlaneTypeTitle(title: String?)
    fun addFlightTimeToAdapter(timeFlightEntity: TimeToFlight)
    fun notifyAddTimeItemChanged(position: Int)
    fun setTotalTime(total: String)
    fun setTotalFlightTime(flightTime: String)
    fun setFligtTypeTitle(title: String)
    fun updateFlightTimesAdapter(items: List<TimeToFlight>)
    fun toastSuccess(msg: String?)
    fun onPressBack()
    fun setResultOK()
    fun setEdtNightTimeText(nightTimeText: String)
    fun setEdtGroundTimeText(groundTimeText: String)
}