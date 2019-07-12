package com.arny.flightlogbook.presentation.types

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.flightlogbook.data.models.PlaneType

/**
 *Created by Sedoy on 09.07.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface PlaneTypesView : MvpView {
    fun updateAdapter(list: List<PlaneType>)
    fun showEditDialog(type: PlaneType, position: Int)
    fun toastSuccess(string: String)
    fun showRemoveDialog(item: PlaneType, position: Int)
    fun toastError(msg: String?)
    fun notifyItemChanged(position: Int)
    fun setAdapterVisible(vis: Boolean)
    fun setEmptyViewVisible(vis: Boolean)
    fun setBtnRemoveAllVisible(vis: Boolean)
    fun itemRemoved(position: Int)
    fun clearAdapter()
}