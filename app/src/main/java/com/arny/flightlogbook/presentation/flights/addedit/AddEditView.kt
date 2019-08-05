package com.arny.flightlogbook.presentation.flights.addedit

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.data.db.intities.TimeToFlightEntity

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface AddEditView : MvpView {
    fun setDescription(desc: String)
    fun setDate(date: String)
    fun setLogTime(strLogTime: String?)
    fun setRegNo(regNo: String?)
    fun setSpinDayNight(daynight: Int)
    fun setSpinIfrVfr(ifrvfr: Int)
    fun setFlightType(flighttype: Int)
    fun setEdtTimeText(strLogTime: String?)
    fun setMotoTimeResult(motoTime: String?)
    fun setToolbarTitle(string: String)
    fun toastError(msg: String?)
    fun setPlaneTypeTitle(title: String?)
    fun addFlightTimeToAdapter(timeFlightEntity: TimeToFlightEntity)
    fun notifyAddTimeItemChanged(position: Int)
    fun setTotalTime(total: String)
    fun timeSummChange()
    fun setTotalFlightTime(flightTime: String)
    fun setFligtTypeTitle(title: String)
    fun updateFlightTimesAdapter(items: List<TimeToFlightEntity>)
    fun toastSuccess(msg: String?)
    fun onPressBack()
}