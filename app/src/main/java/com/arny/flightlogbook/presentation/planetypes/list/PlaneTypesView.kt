package com.arny.flightlogbook.presentation.planetypes.list

import com.arny.flightlogbook.data.models.PlaneType
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface PlaneTypesView : MvpView {
    fun updateAdapter(list: List<PlaneType>)
    fun toastSuccess(string: String)
    fun toastError(msg: String?)
    fun notifyItemChanged(position: Int)
    fun setEmptyViewVisible(vis: Boolean)
}