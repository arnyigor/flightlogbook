package com.arny.flightlogbook.presentation.viewflights

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
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.service.BackgroundIntentService
import com.arny.flightlogbook.presentation.addedit.AddEditActivity
import com.arny.flightlogbook.utils.Prefs
import com.arny.flightlogbook.utils.ToastMaker
import com.arny.flightlogbook.utils.Utility
import com.arny.flightlogbook.utils.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.utils.dialogs.ConfirmDialogListener
import com.arny.flightlogbook.utils.dialogs.ListDialogListener
import com.arny.flightlogbook.utils.dialogs.confirmDialog
import com.arny.flightlogbook.utils.dialogs.listDialog
import com.arny.flightlogbook.utils.observeOnMain
import io.reactivex.Observable
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
    lateinit var viewFlightsPresenter: ViewFlightsPresenterImpl

    @ProvidePresenter
    fun provideMainPresenter(): ViewFlightsPresenterImpl {
        return ViewFlightsPresenterImpl()
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context, msg)
    }

    companion object {
        @JvmStatic
        fun newInstance(): FlightListFragment {
            return FlightListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flight_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view1 ->
            val intent = Intent(context, AddEditActivity::class.java)
            startActivity(intent)
        }
        tvTotalTime.setText(R.string.loading_totals)
        mLayoutManager = LinearLayoutManager(context)
        rv_flights.layoutManager = mLayoutManager
        rv_flights.itemAnimator = DefaultItemAnimator()
        adapter = FlightsAdapter()
        adapter?.setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<Flight> {
            override fun onItemClick(position: Int, item: Flight) {
                showMenuDialog(item)
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
            Observable.fromCallable { Utility.isMyServiceRunning(BackgroundIntentService::class.java, ctx) }.observeOnMain().subscribe({ running ->
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

    override fun updateAdapter(flights: ArrayList<Flight>) {
        adapter?.clear()
        adapter?.addAll(flights)
    }

    private fun initFlights() {
        viewFlightsPresenter.loadFlights()
    }

    private fun restoreListPosition() {
        if (positionIndex != -1) {
            mLayoutManager?.scrollToPositionWithOffset(positionIndex, topView)
        }
    }

    private fun saveListPosition() {
        positionIndex = mLayoutManager?.findFirstVisibleItemPosition() ?: 0
        val startView = rv_flights.getChildAt(0)
        topView = if (startView == null) 0 else {
            val paddingTop = rv_flights.paddingTop
            startView.top - paddingTop
        }
    }

    private fun showMenuDialog(item: Flight) {
        context?.let { ctx ->
            listDialog(ctx, R.array.flights_edit_items, getString(R.string.choose_action), cancelable = true, dialogListener = ListDialogListener { selected ->
                when (selected) {
                    0 -> try {
                        val intent = Intent(context, AddEditActivity::class.java)
                        intent.putExtra(Consts.DB.COLUMN_ID, item.id)
                        startActivity(intent)
                        activity?.overridePendingTransition(0, 0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    1 -> confirmDialog(ctx, getString(R.string.str_delete), dialogListener = object : ConfirmDialogListener {
                        override fun onCancel() {

                        }

                        override fun onConfirm() {
                            viewFlightsPresenter.removeItem(item)
                        }
                    })
                    2 -> {
                        confirmDialog(ctx, getString(R.string.str_clearall), dialogListener = object : ConfirmDialogListener {
                            override fun onCancel() {

                            }

                            override fun onConfirm() {
                                viewFlightsPresenter.removeAllFlights()
                            }
                        })


                    }
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val filters = resources.getStringArray(R.array.flights_filers)
                val filterPos = Prefs.getInstance(activity as Context).get<Int>(Consts.PrefsConsts.CONFIG_USER_FILTER_FLIGHTS)?:0
                val filter = filters[filterPos]
                MaterialDialog.Builder(activity as Context)
                        .title(getString(R.string.str_sort_by) + " " + filter)
                        .items(R.array.flights_filers)
                        .autoDismiss(true)
                        .itemsCallback { _, _, which, _ ->
                            Prefs.getInstance(activity as Context).put(Consts.PrefsConsts.CONFIG_USER_FILTER_FLIGHTS, which)
                            initFlights()
                        }.show()
                return true
            }
        }
        return true
    }

    override fun displayTotalTime(time: String) {
        tvTotalTime?.text = time
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
