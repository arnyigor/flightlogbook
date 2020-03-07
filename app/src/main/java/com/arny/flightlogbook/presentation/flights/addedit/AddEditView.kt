package com.arny.flightlogbook.presentation.flights.addedit

import com.arny.domain.models.TimeToFlight
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface AddEditView : MvpView {
    fun setDescription(desc: String)
    fun setDate(date: String)
    fun setRegNo(regNo: String?)
    fun setEdtTime(strLogTime: String?)
    fun setMotoTimeResult(motoTime: String?)
    fun setToolbarTitle(string: String)
    fun toastError(msg: String?)
    fun setPlaneTypeTitle(title: String?)
    fun addFlightTimeToAdapter(timeFlightEntity: TimeToFlight)
    fun setTotalTime(total: String)
    fun setTotalFlightTime(flightTime: String)
    fun setFligtTypeTitle(title: String)
    fun toastSuccess(msg: String?)
    fun onPressBack()
    fun setResultOK()
    fun setEdtNightTime(nightTimeText: String)
    fun setEdtGroundTime(groundTimeText: String)
    fun setTitle(title: String?)
    fun onColorSelect(colors: IntArray)
}