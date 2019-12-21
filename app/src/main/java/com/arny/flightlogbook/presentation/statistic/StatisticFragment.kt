package com.arny.flightlogbook.presentation.statistic

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.*
import android.widget.Adapter
import android.widget.AdapterView
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.arny.adapters.MultiSelectionSpinner
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R
import com.arny.helpers.utils.ToastMaker
import com.arny.helpers.utils.setVisible
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import kotlinx.android.synthetic.main.statistic_fragment.*
import java.util.*

class StatisticFragment : MvpAppCompatFragment(), StatisticsView, View.OnClickListener {
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
        tv_start_date.setOnClickListener(this)
        tv_end_date.setOnClickListener(this)
        tv_pediod_type.setOnClickListener(this)
        iv_period_left.setOnClickListener(this)
        iv_period_right.setOnClickListener(this)
        statAdapter = StatisticAdapter()
        rv_statistic.layoutManager = LinearLayoutManager(context)
        rv_statistic.adapter = statAdapter
        spin_period.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                statisticsPresenter.onPeriodChanged(position)
            }
        }
        chbox_extended_stat.setOnCheckedChangeListener { _, isChecked ->
            statisticsPresenter.onExtendedStatisticChanged(isChecked)
        }

        chbox_filter.setOnCheckedChangeListener { _, isChecked ->
            statisticsPresenter.onFilterChanged(isChecked)
        }

        spin_stat_filter.setSelection(Adapter.NO_SELECTION, true)
        spin_stat_filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                statisticsPresenter.onFilterTypeSelect(position)
            }
        }
        spin_stat_filter_type.setOnSelectionListener(object : MultiSelectionSpinner.OnMultiSelectionChooseListener {
            override fun onSelected(mSelection: List<Int>, items: Array<String>?) {
                statisticsPresenter.onFilter(spin_stat_filter.selectedItemPosition, mSelection)
            }
        })
    }

    override fun setFilterStatisticVisible(vis: Boolean) {
        tv_filter_stat_by.setVisible(vis)
        spin_stat_filter.setVisible(vis)
        spin_stat_filter_type.setVisible(vis)
        if (vis) {
            statisticsPresenter.onFilterTypeSelect(spin_stat_filter.selectedItemPosition)
        }
    }

    override fun setFilterSpinnerItems(items: List<String>) {
        spin_stat_filter_type.setItems(items)
        spin_stat_filter_type.setSelection(0)
    }

    override fun showEmptyView(showEmpty: Boolean) {
        tv_empty_stat_view.setVisible(showEmpty)
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

    override fun updateAdapter(stats: ArrayList<Statistic>) {
        statAdapter?.addAll(stats)
    }

    override fun clearAdapter() {
        statAdapter?.clear()
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

    override fun showDateDialogStart(year: Int, month: Int, day: Int) {
        CalendarDatePickerDialogFragment()
                .setPreselectedDate(year, month, day)
                .setOnDateSetListener { dialog, y, monthOfYear, dayOfMonth ->
                    dialog.dismiss()
                    statisticsPresenter.onDateStartSet(y, monthOfYear, dayOfMonth)
                }.show(fragmentManager, "fragment_date_start_picker_name")
    }

    override fun toastError(string: String?) {
         ToastMaker.toastError(context,string)
    }

    override fun showDateDialogEnd(year: Int, month: Int, day: Int) {
        CalendarDatePickerDialogFragment()
                .setPreselectedDate(year, month, day)
                .setOnDateSetListener { dialog, y, monthOfYear, dayOfMonth ->
                    dialog.dismiss()
                    statisticsPresenter.onDateEndSet(y, monthOfYear, dayOfMonth)
                }.show(fragmentManager, "fragment_date_end_picker_name")
    }
}
