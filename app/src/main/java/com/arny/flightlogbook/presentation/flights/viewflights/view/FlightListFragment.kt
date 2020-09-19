package com.arny.flightlogbook.presentation.flights.viewflights.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arny.domain.models.Flight
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.flights.addedit.view.AddEditActivity
import com.arny.flightlogbook.presentation.flights.viewflights.presenter.ViewFlightsPresenter
import com.arny.flightlogbook.presentation.main.MainFragment
import com.arny.helpers.utils.*
import kotlinx.android.synthetic.main.fragment_flight_list.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class FlightListFragment : MvpAppCompatFragment(), ViewFlightsView, MainFragment {
    private var adapter: FlightsAdapter? = null
    private var positionIndex: Int = 0
    private var mLayoutManager: LinearLayoutManager? = null
    private var topView: Int = 0
    private var hasSelectedItems: Boolean = false

    @InjectPresenter
    lateinit var presenter: ViewFlightsPresenter

    @ProvidePresenter
    fun provideMainPresenter(): ViewFlightsPresenter {
        return ViewFlightsPresenter()
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    companion object {

        @JvmStatic
        fun getInstance(): FlightListFragment {
            return FlightListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flight_list, container, false)
    }

    override fun showError(message: String?) {
        requireView().showSnackBar(message)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab_add_flight.setOnClickListener {
            launchActivity<AddEditActivity>(
                    CONSTS.REQUESTS.REQUEST_ADD_EDIT_FLIGHT,
                    enterAnim = R.anim.anim_slide_in_left,
                    exitAnim = R.anim.anim_slide_out_left
            )
        }
        requireActivity().title = getString(R.string.fragment_logbook)
        mLayoutManager = LinearLayoutManager(context)
        rvflights.layoutManager = mLayoutManager
        rvflights.itemAnimator = DefaultItemAnimator()
        adapter = FlightsAdapter(object : FlightsAdapter.OnFlightsListListener {
            override fun onFlightSelect(position: Int, item: Flight) {
                presenter.onFlightSelect(position, item)
            }

            override fun onFlightRemove(position: Int, item: Flight) {
                alertDialog(
                        requireContext(),
                        getString(R.string.remove_item_question),
                        btnCancelText = getString(R.string.str_cancel),
                        onConfirm = {
                            presenter.removeItem(item)
                        })
            }
        }).apply {
            setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<Flight> {
                override fun onItemClick(position: Int, item: Flight) {
                    when {
                        hasSelectedItems -> presenter.onFlightSelect(position, item)
                        else -> {
                            launchActivity<AddEditActivity>(
                                    CONSTS.REQUESTS.REQUEST_ADD_EDIT_FLIGHT,
                                    enterAnim = R.anim.anim_slide_in_left,
                                    exitAnim = R.anim.anim_slide_out_left
                            ) {
                                putExtra(CONSTS.DB.COLUMN_ID, item.id)
                            }
                        }
                    }
                }
            })
        }
        rvflights.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0 && !fab_add_flight.isShown)
                    fab_add_flight.show()
                else if (dy > 0 && fab_add_flight.isShown)
                    fab_add_flight.hide()
            }

        })
        rvflights.adapter = adapter
        presenter.loadFlights()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_ADD_EDIT_FLIGHT -> {
                    presenter.loadFlights(true)
                }
            }
        }
    }

    override fun viewLoadProgress(vis: Boolean) {
        progress_flights.setVisible(vis)
    }

    override fun onPause() {
        saveListPosition()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        fab_add_flight.show()
        restoreListPosition()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.flights_menu, menu)
        menu.findItem(R.id.action_remove_items)?.isVisible = hasSelectedItems
    }

    override fun invalidateMenuSelected(hasSelectedItems: Boolean) {
        this.hasSelectedItems = hasSelectedItems
        activity?.invalidateOptionsMenu()
    }

    override fun updateAdapter(flights: List<Flight>) {
        adapter?.addAll(flights)
        restoreListPosition()
    }

    override fun invalidateAdapter(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    override fun showEmptyView(vis: Boolean) {
        tv_empty_view.setVisible(vis)
    }

    private fun restoreListPosition() {
        if (positionIndex != -1) {
            mLayoutManager?.scrollToPositionWithOffset(positionIndex, topView)
        }
    }

    override fun showTotalsInfo(content: String?) {
        tvTotalTime.isVisible = !content.isNullOrBlank()
        tvTotalTime.text = content
    }

    private fun saveListPosition() {
        positionIndex = mLayoutManager?.findFirstVisibleItemPosition() ?: 0
        val startView = rvflights.getChildAt(0)
        topView = if (startView == null) 0 else {
            val paddingTop = rvflights.paddingTop
            startView.top - paddingTop
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val filters = resources.getStringArray(R.array.flights_filers)
                val filterPos = Prefs.getInstance(activity as Context).get<Int>(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
                        ?: 0
                val filter = filters[filterPos]
                listDialog(
                        context = requireActivity(),
                        title = getString(R.string.str_sort_by) + " " + filter,
                        items = resources.getStringArray(R.array.flights_filers).map { it },
                        onSelect = { index, _ ->
                            presenter.changeOrder(index)
                        }
                )
                return true
            }
            R.id.action_remove_items -> {
                alertDialog(
                        requireContext(),
                        getString(R.string.remove_selected_items_question),
                        btnCancelText = getString(R.string.str_cancel),
                        onConfirm = {
                            presenter.removeSelectedItems()
                        })
            }
        }
        return true
    }

    override fun clearAdaper() {
        adapter?.clear()
    }
}
