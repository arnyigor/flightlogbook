package com.arny.flightlogbook.presentation.flights.viewflights

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.adapters.SimpleAbstractAdapter
import com.arny.constants.CONSTS
import com.arny.domain.models.Flight
import com.arny.domain.service.BackgroundIntentService
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.flights.addedit.AddEditActivity
import com.arny.helpers.utils.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_flight_list.*

class FlightListFragment : MvpAppCompatFragment(), ViewFlightsView {
    private var adapter: FlightsAdapter? = null
    private var finishOperation = true
    private val disposable = CompositeDisposable()
    private var positionIndex: Int = 0
    private var mLayoutManager: LinearLayoutManager? = null
    private var topView: Int = 0

    @InjectPresenter
    lateinit var viewFlightsPresenter: ViewFlightsPresenter

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            launchActivity<AddEditActivity> { }
            activity?.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        }
        mLayoutManager = LinearLayoutManager(context)
        rv_flights.layoutManager = mLayoutManager
        rv_flights.itemAnimator = DefaultItemAnimator()
        adapter = FlightsAdapter(object : FlightsAdapter.FlightsAdapterListener {
            override fun onEditDelete(position: Int, item: Flight) {
                confirmDialog(activity as Context, getString(R.string.str_delete), dialogListener = object : ConfirmDialogListener {
                    override fun onCancel() {

                    }

                    override fun onConfirm() {
                        viewFlightsPresenter.removeItem(item)
                    }
                })
            }
        })
        adapter?.setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<Flight> {
            override fun onItemClick(position: Int, item: Flight) {
                launchActivity<AddEditActivity> {
                    putExtra(CONSTS.DB.COLUMN_ID, item.id)
                }
                activity?.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
            }
        })
        rv_flights.adapter = adapter
        restoreListPosition()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onPause() {
        saveListPosition()
        super.onPause()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver) }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BackgroundIntentService.ACTION)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        context?.let { ctx ->
            LocalBroadcastManager.getInstance(ctx).registerReceiver(broadcastReceiver, filter)
            fromCallable { Utility.isMyServiceRunning(BackgroundIntentService::class.java, ctx) }
                    .observeOnMain()
                    .subscribe({ running ->
                        if (!running) {
                            initFlights()
                        }
                    }, {
                        it.printStackTrace()
                    })

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.flights_menu, menu)
    }

    override fun updateAdapter(flights: List<Flight>) {
        adapter?.addAll(flights)
    }

    override fun showEmptyView(vis: Boolean) {
         tv_empty_view.setVisible(vis)
    }

    private fun initFlights() {
        viewFlightsPresenter.loadFlights()
    }

    private fun restoreListPosition() {
        if (positionIndex != -1) {
            mLayoutManager?.scrollToPositionWithOffset(positionIndex, topView)
        }
    }

    override fun showTotalsInfo(content: String?) {
//        tv_totals.setText(content)
    }

    private fun saveListPosition() {
        positionIndex = mLayoutManager?.findFirstVisibleItemPosition() ?: 0
        val startView = rv_flights.getChildAt(0)
        topView = if (startView == null) 0 else {
            val paddingTop = rv_flights.paddingTop
            startView.top - paddingTop
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val filters = resources.getStringArray(R.array.flights_filers)
                val filterPos = Prefs.getInstance(activity as Context).get<Int>(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)?:0
                val filter = filters[filterPos]
                MaterialDialog.Builder(activity as Context)
                        .title(getString(R.string.str_sort_by) + " " + filter)
                        .items(R.array.flights_filers)
                        .autoDismiss(true)
                        .itemsCallback { _, _, which, _ ->
                            viewFlightsPresenter.changeOrder(which)
                        }.show()
                return true
            }
        }
        return true
    }

    override fun clearAdaper() {
         adapter?.clear()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (finishOperation) {
                initFlights()
            }
        }
    }
}
