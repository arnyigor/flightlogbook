package com.arny.flightlogbook.presentation.statistic

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import com.arny.domain.Local
import com.arny.domain.models.Flight
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.mainThreadObservable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.statistic_fragment.*
import java.util.*

class StatisticFragment : Fragment() {
    private var FlightData: List<Flight>? = null
    private var statistics: List<Statistic>? = null
    private val dateAndTimeStart = Calendar.getInstance()
    private val dateAndTimeEnd = Calendar.getInstance()
    private var startdatetime: Long = 0
    private var enddatetime: Long = 0
    private var statAdapter: StatisticAdapter? = null
    private val disposable = CompositeDisposable()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.statistic_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statAdapter = StatisticAdapter()
        initUI()
        startInitDateTime()
        if (statistics != null) {
            refreshAdapter()
        } else {
            getStatistic()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu!!.clear()
    }

    private fun refreshAdapter() {
        statAdapter?.notifyDataSetChanged()
        rv_statistic.adapter = statAdapter
    }

    //инициализация view
    private fun initUI() {
        tv_start_date.setOnClickListener {  setDateFrom() }
        tv_end_date.setOnClickListener { setDateTo() }
    }

    //функция статистики
    private fun getStatistic() {
        disposable.add(mainThreadObservable(Observable.fromCallable {
            Local.getStatistic("", activity as Context)
        })
                .subscribe({result->
                    statistics = result
                    refreshAdapter()
                },{
                    it.printStackTrace()
                }))
    }

    //начальные данные времени
    private fun startInitDateTime() {
        disposable.add(mainThreadObservable(Observable.fromCallable { Local.getFlightListByDate(activity as Context, "") })
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
                }))
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
