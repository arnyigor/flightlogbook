package com.arny.flightlogbook.presentation.flights.addedit.view

import com.arny.flightlogbook.customfields.models.CustomFieldValue
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface AddEditView : MvpView {
    fun setDescription(desc: String)
    fun setDate(date: String)
    fun setRegNo(regNo: String?)
    fun setEdtFlightTimeText(strLogTime: String?)
    fun setMotoTimeResult(motoTime: String?)
    fun setToolbarTitle(string: String)
    fun toastError(msg: String?)
    fun setPlaneTypeTitle(title: String?)
    fun setTotalTime(total: String)
    fun setTotalFlightTime(flightTime: String)
    fun setFligtTypeTitle(title: String)
    fun toastSuccess(msg: String?)
    @StateStrategyType(value = OneExecutionStateStrategy::class)
    fun onPressBack()
    fun setResultOK()
    fun setEdtNightTimeText(nightTimeText: String)
    fun setEdtGroundTimeText(groundTimeText: String)
    fun onColorSelect(colors: IntArray)
    fun setViewColor(color: Int)
    fun setRemoveColorVisible(visible: Boolean)
    fun setIfrSelected(selected: Boolean)
    fun requestStorageAndSave()
    fun saveFlight()
    fun setFieldsList(list: List<CustomFieldValue>)
    @StateStrategyType(value = OneExecutionStateStrategy::class)
    fun notifyCustomFieldUpdate(item: CustomFieldValue)
}