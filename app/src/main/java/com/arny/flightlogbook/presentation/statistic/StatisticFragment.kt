package com.arny.flightlogbook.presentation.statistic

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.flightlogbook.R
import com.arny.helpers.utils.DateTimeUtils
import kotlinx.android.synthetic.main.statistic_fragment.*
import java.util.*

class StatisticFragment : MvpAppCompatFragment(),StatisticsView {
    private val dateAndTimeStart = Calendar.getInstance()
    private val dateAndTimeEnd = Calendar.getInstance()
    private var startdatetime: Long = 0
    private var enddatetime: Long = 0
    private var statAdapter: StatisticAdapter? = null

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
        statAdapter = StatisticAdapter()
        tv_start_date.setOnClickListener { setDateFrom() }
        tv_end_date.setOnClickListener { setDateTo() }
        rv_statistic.layoutManager = LinearLayoutManager(context)
        rv_statistic.adapter = statAdapter
        startInitDateTime()
        statisticsPresenter.loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu!!.clear()
    }



    // установка обработчика выбора даты start
    internal var onDateStartSetListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        dateAndTimeStart.set(Calendar.YEAR, year)
        dateAndTimeStart.set(Calendar.MONTH, monthOfYear)
        dateAndTimeStart.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        startdatetime = dateAndTimeStart.timeInMillis
        checkStartEndDateTime()
        tv_start_date.text = DateTimeUtils.getDateTime(startdatetime, "dd.MM.yyyy")
    }

    // установка обработчика выбора end
    private var onDateEndSetListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        dateAndTimeEnd.set(Calendar.YEAR, year)
        dateAndTimeEnd.set(Calendar.MONTH, monthOfYear)
        dateAndTimeEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        enddatetime = dateAndTimeEnd.timeInMillis
        checkStartEndDateTime()
        tv_end_date.text = DateTimeUtils.getDateTime(enddatetime, "dd.MM.yyyy")
    }

    //начальные данные времени
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

    //устанавливаем дату в textView
    private fun setDateTimeToTextView() {
        dateAndTimeStart.timeInMillis = startdatetime
        tv_start_date.text = DateTimeUtils.getDateTime(startdatetime, "ddMMMyyyy")
        dateAndTimeEnd.timeInMillis = enddatetime
        tv_end_date.text = DateTimeUtils.getDateTime(enddatetime, "ddMMMyyyy")
    }

    // отображаем диалоговое окно для выбора даты
    fun setDateFrom() {
        dateAndTimeStart.timeInMillis = startdatetime
        DatePickerDialog(activity as Context, onDateStartSetListener,
                dateAndTimeStart.get(Calendar.YEAR),
                dateAndTimeStart.get(Calendar.MONTH),
                dateAndTimeStart.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    // отображаем диалоговое окно для выбора даты
    fun setDateTo() {
        dateAndTimeEnd.timeInMillis = enddatetime
        DatePickerDialog(activity as Context, onDateEndSetListener,
                dateAndTimeEnd.get(Calendar.YEAR),
                dateAndTimeEnd.get(Calendar.MONTH),
                dateAndTimeEnd.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    //проверяем конечную дату больше начальной
    private fun checkStartEndDateTime() {
        if (startdatetime > enddatetime) {
            Toast.makeText(activity as Context, getString(R.string.stat_error_end_less_start), Toast.LENGTH_SHORT).show()
            startInitDateTime()
        }
    }

}
