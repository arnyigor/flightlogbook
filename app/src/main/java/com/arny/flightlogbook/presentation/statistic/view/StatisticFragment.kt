package com.arny.flightlogbook.presentation.statistic.view

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Adapter
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.MultiSelectionSpinner
import com.arny.flightlogbook.presentation.statistic.presenter.StatisticsPresenter
import com.arny.helpers.utils.ToastMaker
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import kotlinx.android.synthetic.main.statistic_fragment.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
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
        requireActivity().title = getString(R.string.fragment_stats)
        tv_start_date.setOnClickListener(this)
        tv_end_date.setOnClickListener(this)
        tv_pediod_type.setOnClickListener(this)
        iv_period_left.setOnClickListener(this)
        iv_period_right.setOnClickListener(this)
        vColor.setOnClickListener(this)
        statAdapter = StatisticAdapter()
        rv_statistic.layoutManager = LinearLayoutManager(context)
        rv_statistic.adapter = statAdapter
        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                statisticsPresenter.onPeriodChanged(position)
            }
        }
        chboxExtendedStat.setOnCheckedChangeListener { _, isChecked ->
            statisticsPresenter.onExtendedStatisticChanged(isChecked)
        }

        chboxFilter.setOnCheckedChangeListener { _, isChecked ->
            statisticsPresenter.onFilterChanged(isChecked)
        }

        spinStatFilter.setSelection(Adapter.NO_SELECTION, true)
        spinStatFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                statisticsPresenter.onFilterSelected(position)
            }
        }
        mssFilterType.setOnSelectionListener(object : MultiSelectionSpinner.OnMultiSelectionChooseListener {
            override fun onSelected(mSelection: List<Int>, items: Array<String>?) {
                statisticsPresenter.onFilterTypeSelected(spinStatFilter.selectedItemPosition, mSelection)
            }
        })
    }

    override fun setFilterStatisticVisible(vis: Boolean) {
        tv_filter_stat_by.isVisible = vis
        spinStatFilter.isVisible = vis
        mssFilterType.isVisible = vis
        vColor.isVisible = false
        if (vis) {
            statisticsPresenter.onFilterSelected(spinStatFilter.selectedItemPosition)
        }
    }


    override fun setViewColorVisible(visible: Boolean) {
        vColor.isVisible = visible
    }

    override fun setFilterTypeVisible(visible: Boolean) {
        mssFilterType.isVisible = visible
    }

    override fun setFilterSpinnerItems(items: List<String>) {
        mssFilterType.setItems(items)
        mssFilterType.setSelection(0)
    }

    override fun showEmptyView(showEmpty: Boolean) {
        tv_empty_stat_view.isVisible = showEmpty
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_start_date -> statisticsPresenter.initDateStart()
            R.id.tv_end_date -> statisticsPresenter.initDateEnd()
            R.id.tv_pediod_type -> statisticsPresenter.onPeriodTypeClick()
            R.id.iv_period_left -> statisticsPresenter.decreasePeriod()
            R.id.iv_period_right -> statisticsPresenter.increasePeriod()
            R.id.vColor -> statisticsPresenter.colorClick()
        }
    }

    override fun updateAdapter(stats: ArrayList<Statistic>) {
        statAdapter?.addAll(stats)
    }

    override fun clearAdapter() {
        statAdapter?.clear()
    }

    override fun setPeriodTypeVisible(vis: Boolean) {
        tv_pediod_type.isVisible = vis
        iv_period_left.isVisible = vis
        iv_period_right.isVisible = vis
    }

    override fun setCustomPeriodVisible(vis: Boolean) {
        tv_start_date.isVisible = vis
        tv_end_date.isVisible = vis
    }

    override fun setPeriodItemText(periodItem: String?) {
        tv_pediod_type.text = periodItem
    }

    override fun setStartDateText(date: String?) {
        tv_start_date.text = date
    }

    override fun setEndDateText(date: String?) {
        tv_end_date.text = date
    }

    override fun setViewColor(color: Int) {
        vColor.setBackgroundColor(color)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun showDateDialogStart(year: Int, month: Int, day: Int) {
        fragmentManager?.let {
            CalendarDatePickerDialogFragment()
                    .setPreselectedDate(year, month, day)
                    .setOnDateSetListener { dialog, y, monthOfYear, dayOfMonth ->
                        dialog.dismiss()
                        statisticsPresenter.onDateStartSet(y, monthOfYear, dayOfMonth)
                    }.show(it, "fragment_date_start_picker_name")
        }
    }

    override fun toastError(string: String?) {
        ToastMaker.toastError(context, string)
    }

    override fun showDateDialogEnd(year: Int, month: Int, day: Int) {
        fragmentManager?.let {
            CalendarDatePickerDialogFragment()
                    .setPreselectedDate(year, month, day)
                    .setOnDateSetListener { dialog, y, monthOfYear, dayOfMonth ->
                        dialog.dismiss()
                        statisticsPresenter.onDateEndSet(y, monthOfYear, dayOfMonth)
                    }.show(it, "fragment_date_end_picker_name")
        }
    }

    override fun onColorSelect(colors: IntArray) {
        MaterialDialog(requireContext()).show {
            title(R.string.select_color)
            colorChooser(colors, initialSelection = Color.BLUE) { _, color ->
                statisticsPresenter.onColorSelected(color)
            }
            positiveButton(R.string.select)
        }
    }
}
