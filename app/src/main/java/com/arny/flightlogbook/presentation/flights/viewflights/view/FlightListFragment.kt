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
import com.arny.core.CONSTS
import com.arny.core.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FragmentFlightListBinding
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.flightlogbook.presentation.flights.viewflights.presenter.ViewFlightsPresenter
import com.arny.flightlogbook.presentation.main.MainFirstFragment
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class FlightListFragment : BaseMvpFragment(), ViewFlightsView, MainFirstFragment {
    companion object {
        fun getInstance(): FlightListFragment {
            return FlightListFragment()
        }
    }

    private lateinit var binding: FragmentFlightListBinding
    private var adapter: FlightsAdapter? = null
    private var positionIndex: Int = 0
    private var mLayoutManager: LinearLayoutManager? = null
    private var topView: Int = 0
    private var hasSelectedItems: Boolean = false

    @Inject
    lateinit var presenterProvider: Provider<ViewFlightsPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    override fun getTitle(): String = getString(R.string.fragment_logbook)

    override fun showError(message: String?) {
        requireView().showSnackBar(message)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFlightListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            fabAddFlight.setOnClickListener {
                launchActivity<FragmentContainerActivity>(CONSTS.REQUESTS.REQUEST_ADD_EDIT_FLIGHT) {
                    action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_FLIGHT
                }
            }
            mLayoutManager = LinearLayoutManager(context)
            rvFlights.layoutManager = mLayoutManager
            rvFlights.itemAnimator = DefaultItemAnimator()
            adapter = FlightsAdapter(object : FlightsAdapter.OnFlightsListListener {
                override fun onItemClick(position: Int, item: Flight) {
                    if (hasSelectedItems) {
                        presenter.onFlightSelect(position, item)
                    } else {
                        launchActivity<FragmentContainerActivity>(CONSTS.REQUESTS.REQUEST_ADD_EDIT_FLIGHT) {
                            action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_FLIGHT
                            putExtra(CONSTS.DB.COLUMN_ID, item.id)
                        }
                    }
                }

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
            })
            rvFlights.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    when {
                        dy < 0 && !fabAddFlight.isShown -> fabAddFlight.show()
                        dy > 0 && fabAddFlight.isShown -> fabAddFlight.hide()
                    }
                }

            })
            rvFlights.adapter = adapter
        }
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
        binding.progressFlights.isVisible = vis
    }

    override fun onPause() {
        saveListPosition()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.fabAddFlight.show()
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
        adapter?.submitList(flights)
        restoreListPosition()
    }

    override fun invalidateAdapter(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    override fun showEmptyView(vis: Boolean) {
        binding.tvEmptyView.isVisible = vis
    }

    private fun restoreListPosition() {
        if (positionIndex != -1) {
            mLayoutManager?.scrollToPositionWithOffset(positionIndex, topView)
        }
    }

    override fun showTotalsInfo(content: String?) {
        binding.tvTotalTime.isVisible = !content.isNullOrBlank()
        binding.tvTotalTime.text = content
    }

    private fun saveListPosition() {
        positionIndex = mLayoutManager?.findFirstVisibleItemPosition() ?: 0
        val startView = binding.rvFlights.getChildAt(0)
        topView = if (startView == null) 0 else startView.top - binding.rvFlights.paddingTop
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val filters = resources.getStringArray(R.array.flights_filers)
                // FIXME заменить
                val filterPos = Prefs.getInstance(activity as Context)
                    .get<Int>(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
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
                    context = requireContext(),
                    title = getString(R.string.remove_selected_items_question),
                    btnCancelText = getString(R.string.str_cancel),
                    onConfirm = {
                        presenter.removeSelectedItems()
                    })
            }
        }
        return true
    }
}
