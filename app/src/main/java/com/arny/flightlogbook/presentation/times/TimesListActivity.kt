package com.arny.flightlogbook.presentation.times

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.db.intities.TimeTypeEntity
import com.arny.flightlogbook.data.utils.*
import com.arny.flightlogbook.data.utils.dialogs.ConfirmDialogListener
import com.arny.flightlogbook.data.utils.dialogs.InputDialogListener
import com.arny.flightlogbook.data.utils.dialogs.confirmDialog
import com.arny.flightlogbook.data.utils.dialogs.inputDialog
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
        timesListPresenter.loadTimes()
        btn_confirm_selected.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val callingClass = callingActivity?.toString()
        Log.i(TimesListActivity::class.java.simpleName, "onBackPressed: $callingClass");
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun notifyItemChanged(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_add_time_type -> {
                inputDialog(this, getString(R.string.str_add_time_type), dialogListener = object : InputDialogListener {
                    override fun onConfirm(title: String?) {
                        timesListPresenter.addTimeType(title)
                    }

                    override fun onCancel() {

                    }
                })
            }
            R.id.btn_confirm_selected -> {
                val items = adapter?.getItems()
                timesListPresenter.onConfirmSelected(items)
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
                timesListPresenter.onItemSelect(item, position, adapter?.getItems())
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

    override fun updateAdapter(timeTypes: List<TimeTypeEntity>) {
        adapter?.addAll(timeTypes)
    }

    override fun setBtnConfirmSelectVisible(vis: Boolean) {
        btn_confirm_selected.setVisible(vis)
    }

    override fun onConfirmSelectedTimes(selected: String?) {
        putExtras(Activity.RESULT_OK) {
            putExtra(CONSTS.EXTRAS.EXTRA_ADD_TIME_IDS,selected)
        }
        onBackPressed()
    }
}
