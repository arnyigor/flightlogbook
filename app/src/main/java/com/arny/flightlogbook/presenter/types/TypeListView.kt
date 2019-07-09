package com.arny.flightlogbook.presenter.types

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.flightlogbook.data.models.AircraftType

/**
 *Created by Sedoy on 09.07.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface TypeListView : MvpView {
    fun updateAdapter(list: ArrayList<AircraftType>)
    fun showEditDialog(type: AircraftType)
    fun toastSuccess(string: String)
    fun showRemoveDialog(item: AircraftType)
    fun toastError(msg: String?)
}