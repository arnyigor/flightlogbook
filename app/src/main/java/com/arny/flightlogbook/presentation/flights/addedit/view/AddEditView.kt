package com.arny.flightlogbook.presentation.flights.addedit.view

import androidx.annotation.StringRes
import com.arny.domain.models.Airport
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface AddEditView : MvpView {
    fun setDescription(desc: String)
    fun setDate(date: String)
    fun setEdtFlightTimeText(strLogTime: String?)
    fun setMotoTimeResult(motoTime: String?)
    fun setToolbarTitle(@StringRes title: Int)

    @OneExecution
    fun toastError(msg: String?)
    fun setPlaneTypeTitle(title: String?)
    fun setTotalTime(total: String)
    fun setTotalFlightTime(flightTime: String)
    fun setFligtTypeTitle(title: String)
    fun toastSuccess(msg: String?)

    @OneExecution
    fun setResultOK()
    fun setEdtNightTimeText(nightTimeText: String)
    fun setEdtGroundTimeText(groundTimeText: String)
    @OneExecution
    fun onColorSelect(colors: IntArray)
    fun setViewColor(color: Int)
    fun setRemoveColorVisible(visible: Boolean)
    fun setIfrSelected(selected: Boolean)
    fun requestStorageAndSave()
    fun saveFlight()
    fun setFieldsList(
        list: List<CustomFieldValue>,
        requestLayout: Boolean = false,
    )

    fun setCustomFieldsVisible(visible: Boolean)
    fun setDeparture(departure: Airport?)
    fun setArrival(arrival: Airport?)
    fun setEdtDepUtcTime(depTime: Int)
    fun setEdtArrUtcTimeText(arrTime: Int)
    fun removeItemFromAdapter(position: Int)
}