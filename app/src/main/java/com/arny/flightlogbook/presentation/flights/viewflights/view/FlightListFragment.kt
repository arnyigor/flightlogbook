package com.arny.flightlogbook.presentation.flights.viewflights.view

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arny.core.CONSTS
import com.arny.core.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FragmentFlightListBinding
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.flights.viewflights.presenter.ViewFlightsPresenter
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class FlightListFragment : BaseMvpFragment(), ViewFlightsView {
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

    override fun showError(message: String?) {
        requireView().showSnackBar(message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        title = getString(R.string.fragment_logbook)
        setFragmentResultListener(CONSTS.EXTRAS.EXTRA_ACTION_EDIT_FLIGHT) { _, _ ->
            presenter.loadFlights(checkAutoExport = true, restoreScroll = true)
        }
        initUI(view)
        presenter.loadFlights(restoreScroll = true)
    }

    private fun initUI(view: View) {
        with(binding) {
            fabAddFlight.setOnClickListener {
                view.findNavController().navigate(R.id.addEditFragment)
            }
            mLayoutManager = LinearLayoutManager(context)
            rvFlights.layoutManager = mLayoutManager
            rvFlights.itemAnimator = DefaultItemAnimator()
            adapter = FlightsAdapter(
                onItemClick = { position, item ->
                    onItemClick(position, item)
                },
                onFlightSelect = { position, item ->
                    presenter.onFlightSelect(position, item)
                },
                onFlightRemove = (::showRemoveDialog)
            )
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
    }

    private fun onItemClick(position: Int, item: Flight) {
        if (hasSelectedItems) {
            presenter.onFlightSelect(position, item)
        } else {
            val id = item.id
            id?.let {
                findNavController().navigate(
                    FlightListFragmentDirections.actionNavFlightsToAddEditFragment(id)
                )
            }
        }
    }

    private fun showRemoveDialog(item: Flight) {
        alertDialog(
            requireContext(),
            getString(R.string.remove_item_question),
            btnCancelText = getString(R.string.str_cancel),
            onConfirm = {
                presenter.removeItem(item)
            })
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
        restoreListPosition()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.flights_menu, menu)
        menu.findItem(R.id.action_remove_items)?.isVisible = hasSelectedItems
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun invalidateMenuSelected(hasSelectedItems: Boolean) {
        this.hasSelectedItems = hasSelectedItems
        activity?.invalidateOptionsMenu()
    }

    override fun updateAdapter(flights: List<Flight>, restoreScroll: Boolean) {
        adapter?.submitList(flights)
        if (restoreScroll) {
            restoreListPosition()
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
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
                true
            }
            R.id.action_remove_items -> {
                alertDialog(
                    context = requireContext(),
                    title = getString(R.string.remove_selected_items_question),
                    btnCancelText = getString(R.string.str_cancel),
                    onConfirm = {
                        presenter.removeSelectedItems()
                    })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
