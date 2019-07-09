package com.arny.flightlogbook.presenter.addedit

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.flightlogbook.data.models.AircraftType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface AddEditView : MvpView {
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
    fun showMotoBtn()
    fun setToolbarTitle(string: String)
    fun toastError(msg: String?)
}