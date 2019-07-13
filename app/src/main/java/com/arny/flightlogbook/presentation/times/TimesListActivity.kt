package com.arny.flightlogbook.presentation.times

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.View
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.db.intities.TimeToFlightEntity
import com.arny.flightlogbook.data.db.intities.TimeTypeEntity
import com.arny.flightlogbook.data.utils.ToastMaker
import com.arny.flightlogbook.data.utils.dialogs.ConfirmDialogListener
import com.arny.flightlogbook.data.utils.dialogs.InputDialogListener
import com.arny.flightlogbook.data.utils.dialogs.confirmDialog
import com.arny.flightlogbook.data.utils.dialogs.inputDialog
import com.arny.flightlogbook.data.utils.getExtra
import com.arny.flightlogbook.data.utils.setVisible
import com.arny.flightlogbook.data.utils.setupActionBar
import kotlinx.android.synthetic.main.activity_times_list.*

class TimesListActivity : MvpAppCompatActivity(), TimesListView, View.OnClickListener {
    private var adapter: TimeTypesAdapter? = null
    @InjectPresenter
    lateinit var timesListPresenter: TimesListPresenter

    @ProvidePresenter
    fun provideTimesListPresenter(): TimesListPresenter {
        return TimesListPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_times_list)
        setupActionBar(R.id.tool_bar) {
            title = "Типы времени"
            this?.setDisplayHomeAsUpEnabled(true)
        }
        initAdapter()
        fab_add_time_type.setOnClickListener(this)
        val flightId = getExtra<Long>(CONSTS.DB.COLUMN_ID)
        timesListPresenter.loadTimes(flightId)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_add_time_type -> {
                inputDialog(this, getString(R.string.str_add_time_type), dialogListener =  object: InputDialogListener {
                    override fun onConfirm(title: String?) {
                         timesListPresenter.addTimeType(title)
                    }

                    override fun onCancel() {

                    }
                })
            }
            else -> {
            }
        }
    }

    private fun initAdapter() {
        adapter = TimeTypesAdapter(object : TimeTypesAdapter.TimeTypesListener {
            override fun onEditTimeType(position: Int, item: TimeTypeEntity) {
                showEditDialog(item, position)
            }

            override fun onDeleteTimeType(item: TimeTypeEntity) {
                showConfirmDeleteDialog(item)
            }

            override fun onItemClick(position: Int, item: TimeTypeEntity) {

            }

        })
        rv_time_types.layoutManager = LinearLayoutManager(this)
        rv_time_types.adapter = adapter
    }

    private fun showConfirmDeleteDialog(item: TimeTypeEntity) {
        confirmDialog(this, getString(R.string.confirm_delete_time_type), null, getString(android.R.string.ok), getString(android.R.string.cancel), true, object : ConfirmDialogListener {
            override fun onConfirm() {
                timesListPresenter.removeTimeType(item)
            }

            override fun onCancel() {

            }
        })
    }

    private fun showEditDialog(item: TimeTypeEntity, position: Int) {
        inputDialog(this, getString(R.string.str_edt_time_type), "", item.title, getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
            override fun onConfirm(newName: String) {
                timesListPresenter.editTimeTypeTitle(item, newName, position)
            }

            override fun onCancel() {

            }
        })
    }

    override fun setEmptyView(vis: Boolean) {
        tv_empty_time_types.setVisible(vis)
    }

    override fun setListVisible(vis: Boolean) {
        rv_time_types.setVisible(vis)
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(this, msg)
    }

    override fun updateAdapter(timeTypes: List<TimeToFlightEntity>) {

    }


}
