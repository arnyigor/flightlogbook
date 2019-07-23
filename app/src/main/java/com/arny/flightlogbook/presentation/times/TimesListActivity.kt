package com.arny.flightlogbook.presentation.times

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.constants.CONSTS
import com.arny.data.db.intities.TimeTypeEntity
import com.arny.domain.correctLogTime
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_times_list.*
import kotlinx.android.synthetic.main.time_input_dialog_layout.view.*

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
        timesListPresenter.loadTimes()
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
                timesListPresenter.onItemClick(item)
            }

        })
        rv_time_types.layoutManager = LinearLayoutManager(this)
        rv_time_types.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    override fun showDialogSetTime(item: TimeTypeEntity) {
        var cDialog: AlertDialog? = null
        var value = ""
        cDialog = createCustomLayoutDialog(R.layout.time_input_dialog_layout, {
            var logTime = 0
            MaskedTextChangedListener.installOn(edt_time, "[00]:[00]", object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                    Log.i(TimesListActivity::class.java.simpleName, "onTextChanged: extractedValue:$extractedValue,formattedValue:$formattedValue");
                    value = extractedValue
                }
            })

            tv_dlg_title.text = getString(R.string.enter_time) + " \"${item.title}\""
            edt_time.hint = "чч:мм"
            edt_time.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    correctLogTime(value, logTime) { time, timeText ->
                        logTime = time
                        edt_time.setText(timeText)
                    }
                }
            }
            iv_close.setOnClickListener {
                cDialog?.dismiss()
            }
            btn_time_dlg_ok.setOnClickListener {
                correctLogTime(value, logTime) { time, timeText ->
                    logTime = time
                    edt_time.setText(timeText)
                }
                timesListPresenter.addFlightTime(item.id, item.title, logTime, chbox_add_flight_time.isChecked)
                cDialog?.dismiss()
            }
        }, false)
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

    override fun confirmSelectedTimeFlight(id: Long?, title: String?, totalTime: Int, addToFlight: Boolean) {
        putExtras(Activity.RESULT_OK) {
            putExtra(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT_ID, id)
            putExtra(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT_TITLE, title)
            putExtra(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT, totalTime)
            putExtra(CONSTS.EXTRAS.EXTRA_TIME_FLIGHT_ADD, addToFlight)
        }
        onBackPressed()
    }
}
