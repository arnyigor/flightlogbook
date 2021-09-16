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
import com.arny.core.utils.ToastMaker
import com.arny.domain.models.Statistic
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.MultiSelectionSpinner
import com.arny.flightlogbook.databinding.StatisticFragmentBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.statistic.presenter.StatisticsPresenter
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import moxy.ktx.moxyPresenter
import java.util.*

class StatisticFragment : BaseMvpFragment(), StatisticsView, View.OnClickListener {
    companion object {
        fun getInstance(): StatisticFragment = StatisticFragment()
    }

    private lateinit var binding: StatisticFragmentBinding
    private val statisticsPresenter by moxyPresenter { StatisticsPresenter() }

    private var statAdapter: StatisticAdapter? = null

    override fun getTitle(): String = getString(R.string.fragment_stats)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatisticFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        with(binding) {
            tvStartDate.setOnClickListener(this@StatisticFragment)
            tvEndDate.setOnClickListener(this@StatisticFragment)
            tvPeriodType.setOnClickListener(this@StatisticFragment)
            ivPeriodLeft.setOnClickListener(this@StatisticFragment)
            ivPeriodRight.setOnClickListener(this@StatisticFragment)
            vColor.setOnClickListener(this@StatisticFragment)
            statAdapter = StatisticAdapter()
            rvStatistic.layoutManager = LinearLayoutManager(context)
            rvStatistic.adapter = statAdapter
            spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
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

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    statisticsPresenter.onFilterSelected(position)
                }
            }
            mssFilterType.setOnSelectionListener(object :
                MultiSelectionSpinner.OnMultiSelectionChooseListener {
                override fun onSelected(mSelection: List<Int>, items: Array<String>?) {
                    statisticsPresenter.onFilterTypeSelected(
                        spinStatFilter.selectedItemPosition,
                        mSelection
                    )
                }
            })
        }
    }

    override fun setFilterStatisticVisible(vis: Boolean) {
        binding.tvFilterStatBy.isVisible = vis
        binding.spinStatFilter.isVisible = vis
        binding.mssFilterType.isVisible = vis
        binding.vColor.isVisible = false
        if (vis) {
            statisticsPresenter.onFilterSelected(binding.spinStatFilter.selectedItemPosition)
        }
    }


    override fun setViewColorVisible(visible: Boolean) {
        binding.vColor.isVisible = visible
    }

    override fun setFilterTypeVisible(visible: Boolean) {
        binding.mssFilterType.isVisible = visible
    }

    override fun setFilterSpinnerItems(items: List<String>) {
        binding.mssFilterType.setItems(items)
        binding.mssFilterType.setSelection(0)
    }

    override fun showEmptyView(showEmpty: Boolean) {
        binding.tvEmptyStatView.isVisible = showEmpty
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvStartDate -> statisticsPresenter.initDateStart()
            R.id.tvEndDate -> statisticsPresenter.initDateEnd()
            R.id.tvPeriodType -> statisticsPresenter.onPeriodTypeClick()
            R.id.ivPeriodLeft -> statisticsPresenter.decreasePeriod()
            R.id.ivPeriodRight -> statisticsPresenter.increasePeriod()
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
        binding.tvPeriodType.isVisible = vis
        binding.ivPeriodLeft.isVisible = vis
        binding.ivPeriodRight.isVisible = vis
    }

    override fun setCustomPeriodVisible(vis: Boolean) {
        binding.tvStartDate.isVisible = vis
        binding.tvEndDate.isVisible = vis
    }

    override fun setPeriodItemText(periodItem: String?) {
        binding.tvPeriodType.text = periodItem
    }

    override fun setStartDateText(date: String?) {
        binding.tvStartDate.text = date
    }

    override fun setEndDateText(date: String?) {
        binding.tvEndDate.text = date
    }

    override fun setViewColor(color: Int) {
        binding.vColor.setBackgroundColor(color)
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
        CalendarDatePickerDialogFragment()
            .setPreselectedDate(year, month, day)
            .setOnDateSetListener { dialog, y, monthOfYear, dayOfMonth ->
                dialog.dismiss()
                statisticsPresenter.onDateStartSet(y, monthOfYear, dayOfMonth)
            }.show(childFragmentManager, "fragment_date_start_picker_name")
    }

    override fun toastError(string: String?) {
        ToastMaker.toastError(context, string)
    }

    override fun showDateDialogEnd(year: Int, month: Int, day: Int) {
        CalendarDatePickerDialogFragment()
            .setPreselectedDate(year, month, day)
            .setOnDateSetListener { dialog, y, monthOfYear, dayOfMonth ->
                dialog.dismiss()
                statisticsPresenter.onDateEndSet(y, monthOfYear, dayOfMonth)
            }.show(childFragmentManager, "fragment_date_end_picker_name")
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
