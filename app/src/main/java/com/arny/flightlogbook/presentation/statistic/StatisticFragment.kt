package com.arny.flightlogbook.presentation.statistic

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.flightlogbook.R
import com.arny.helpers.utils.ToastMaker
import com.arny.helpers.utils.setVisible
import kotlinx.android.synthetic.main.statistic_fragment.*

class StatisticFragment : MvpAppCompatFragment(), StatisticsView, View.OnClickListener {

    @InjectPresenter
    lateinit var statisticsPresenter: StatisticsPresenter

    @ProvidePresenter
    fun provideStatisticsPresenter(): StatisticsPresenter {
        return StatisticsPresenter()
    }

    companion object {
        @JvmStatic
        fun getInstance(): StatisticFragment {
            return StatisticFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.statistic_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_start_date.setOnClickListener(this)
        tv_end_date.setOnClickListener(this)
        tv_pediod_type.setOnClickListener(this)
        iv_period_left.setOnClickListener(this)
        iv_period_right.setOnClickListener(this)
        spin_period.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                statisticsPresenter.onPeriodChanged(position)
            }
        }
        startInitDateTime()
        statisticsPresenter.loadData()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_start_date -> statisticsPresenter.initDateStart()
            R.id.tv_end_date -> statisticsPresenter.initDateEnd()
            R.id.tv_pediod_type -> statisticsPresenter.onPeriodTypeClick()
            R.id.iv_period_left -> statisticsPresenter.decreasePeriod()
            R.id.iv_period_right -> statisticsPresenter.increasePeriod()
        }
    }

    override fun setPeriodTypeVisible(vis: Boolean) {
        tv_pediod_type.setVisible(vis)
        iv_period_left.setVisible(vis)
        iv_period_right.setVisible(vis)
    }

    override fun setCustomPeriodVisible(vis: Boolean) {
        tv_start_date.setVisible(vis)
        tv_end_date.setVisible(vis)
    }

    override fun setPeriodItemText(periodItem: String?) {
        tv_pediod_type.setText(periodItem)
    }

    override fun setStartDateText(date: String?) {
        tv_start_date.setText(date)
    }

    override fun setEndDateText(date: String?) {
        tv_end_date.setText(date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }


    private fun startInitDateTime() {
        /* disposable.add(mainThreadObservable(Observable.fromCallable { Local.getFlightListByDate(activity as Context, "") })
                 .subscribe({ flights ->
                     FlightData = flights
                     if (FlightData!!.isNotEmpty()) {
                         startdatetime = FlightData!![0].datetime!!
                         enddatetime = FlightData!![FlightData!!.size - 1].datetime!!
                     } else {
                         startdatetime = Calendar.getInstance().timeInMillis
                         enddatetime = Calendar.getInstance().timeInMillis
                     }
                     setDateTimeToTextView()
                 },{
                     it.printStackTrace()
                 }))*/
    }

    override fun showDateDialogStart(year: Int, month: Int, day: Int) {
        DatePickerDialog(activity as Context, DatePickerDialog.OnDateSetListener { _, y, monthOfYear, dayOfMonth ->
            statisticsPresenter.onDateStartSet(y, monthOfYear, dayOfMonth)
        }, year, month, day)
                .show()
    }

    override fun toastError(string: String?) {
         ToastMaker.toastError(context,string)
    }

    override fun showDateDialogEnd(year: Int, month: Int, day: Int) {
        DatePickerDialog(activity as Context, DatePickerDialog.OnDateSetListener { _, y, monthOfYear, dayOfMonth ->
            statisticsPresenter.onDateEndSet(y, monthOfYear, dayOfMonth)
        },year,month,day)
                .show()
    }
}
