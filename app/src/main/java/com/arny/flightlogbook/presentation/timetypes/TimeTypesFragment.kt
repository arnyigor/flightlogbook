package com.arny.flightlogbook.presentation.timetypes


import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.domain.models.TimeType
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import kotlinx.android.synthetic.main.time_types_list_layout.*

class TimeTypesFragment : MvpAppCompatFragment(),TimesListView, View.OnClickListener {
     private var timeTypesAdapter: TimeTypesAdapter? = null
     @InjectPresenter
     lateinit var timesListPresenter: TimesListPresenter

     @ProvidePresenter
     fun provideTimesListPresenter(): TimesListPresenter {
          return TimesListPresenter()
     }

     companion object {
          @JvmStatic
          fun getInstance(): TimeTypesFragment {
               return TimeTypesFragment()
          }
     }

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
          return inflater.inflate(R.layout.time_types_list_layout, container, false)
     }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
          super.onViewCreated(view, savedInstanceState)
          timeTypesAdapter = TimeTypesAdapter(object : TimeTypesAdapter.TimeTypesListener {
               override fun onEditTimeType(position: Int, item: TimeType) {
                    showEditDialog(item, position)
               }

               override fun onDeleteTimeType(item: TimeType) {
                    showConfirmDeleteDialog(item)
               }

               override fun onItemClick(position: Int, item: TimeType) {
                    timesListPresenter.onItemClick(item)
               }

          },false)
          rv_time_types.layoutManager = LinearLayoutManager(activity as Context)
          rv_time_types.adapter = timeTypesAdapter
          fab_add_time_type.setOnClickListener(this)
          timesListPresenter.loadTimes()
     }

     override fun onClick(v: View?) {
          when (v?.id) {
               R.id.fab_add_time_type -> {
                    inputDialog(activity as Context, getString(R.string.str_add_time_type), dialogListener = object : InputDialogListener {
                         override fun onConfirm(title: String?) {
                              timesListPresenter.addTimeType(title)
                         }

                         override fun onCancel() {

                         }
                    })
               }
          }
     }

     private fun showConfirmDeleteDialog(item: TimeType) {
          confirmDialog(activity as Context, getString(R.string.confirm_delete_time_type), null, getString(android.R.string.ok), getString(android.R.string.cancel), true, object : ConfirmDialogListener {
               override fun onConfirm() {
                    timesListPresenter.removeTimeType(item)
               }

               override fun onCancel() {

               }
          })
     }



     private fun showEditDialog(item: TimeType, position: Int) {
          inputDialog(activity as Context, getString(R.string.str_edt_time_type), "", item.title, getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
               override fun onConfirm(newName: String) {
                    timesListPresenter.editTimeTypeTitle(item, newName, position)
               }

               override fun onCancel() {

               }
          })
     }

     override fun setEmptyView(vis: Boolean) {

     }

     override fun setListVisible(vis: Boolean) {
          rv_time_types.setVisible(vis)
     }

     override fun toastError(msg: String?) {
          ToastMaker.toastError(activity as Context, msg)
     }

     override fun updateAdapter(timeTypes: List<TimeType>) {
          timeTypesAdapter?.addAll(timeTypes)
     }

     override fun notifyItemChanged(position: Int) {
          timeTypesAdapter?.notifyItemChanged(position)
     }

     override fun showDialogSetTime(item: TimeType) {

     }

     override fun confirmSelectedTimeFlight(id: Long?, title: String?, totalTime: Int, addToFlight: Boolean) {

     }
}
