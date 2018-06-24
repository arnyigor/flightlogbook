package com.arny.flightlogbook.presenter.viewflights

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.arny.arnylib.adapters.SimpleBindableAdapter
import com.arny.arnylib.interfaces.ListDialogListener
import com.arny.arnylib.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapter.FlightListHolder
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.data.*
import com.arny.flightlogbook.data.service.BackgroundIntentService
import com.arny.flightlogbook.data.Local
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.presenter.addedit.AddEditActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

import java.util.ArrayList

class FlightListFragment : Fragment() {
    private var flightListAdapter: SimpleBindableAdapter<Flight, FlightListHolder>? = null
    private var finishOperation = true
    private var flights: List<Flight> = ArrayList()
    private var ctxPos: Int = 0
    private var tvTotalTime: TextView? = null
    private var itemSelectorDialog: MaterialDialog? = null
    private val disposable = CompositeDisposable()
    private var positionIndex: Int = 0
    private var mLayoutManager: LinearLayoutManager? = null
    private var topView: Int = 0
    private var recyclerView: RecyclerView? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (finishOperation) {
                initFlights(getFilterflights(Config.getInt(Consts.Prefs.CONFIG_USER_FILTER_FLIGHTS, context)))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
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
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        tvTotalTime?.setText(R.string.loading_totals)
        recyclerView = view.findViewById(R.id.listView)
        mLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        flightListAdapter = SimpleBindableAdapter(context, R.layout.flight_list_item, FlightListHolder::class.java)
        flightListAdapter?.setActionListener(object : FlightListHolder.SimpleActionListener {
            override fun onItemClick(position: Int, Item: Any?) {
                showMenuDialog(position)
            }
        })
        recyclerView?.adapter = flightListAdapter
        restoreListPosition()
        DroidUtils.runLayoutAnimation(recyclerView, R.anim.layout_animation_fall_down)
        val flights_edit_items = R.array.flights_edit_items
        context?.let {
            listDialog(it, flights_edit_items, dialogListener = ListDialogListener { which ->
                when (which) {
                    0 -> try {
                        val intent = Intent(it, AddEditActivity::class.java)
                        intent.putExtra(Consts.DB.COLUMN_ID, flights[ctxPos].id)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    1 -> DroidUtils.alertConfirmDialog(it, getString(R.string.str_delete)) {
                        disposable.add(Utility.mainThreadObservable(Observable.just(1).doOnNext { o -> Local.removeFlight(flights[ctxPos].id.toInt(), it) })
                                .doOnSubscribe { disposable1 -> tvTotalTime?.setText(R.string.loading_totals) }
                                .subscribe({ o ->
                                    flightListAdapter?.removeChild(ctxPos)
                                    displayTotalTime()
                                }) { throwable -> ToastMaker.toastError(it, throwable.message) })
                    }
                    2 -> DroidUtils.alertConfirmDialog(it, getString(R.string.str_clearall)) {
                        disposable.add(Utility.mainThreadObservable(Observable.just(1)
                                .doOnNext { o -> Local.removeAllFlights(it) })
                                .subscribe({ o -> initFlights(getFilterflights(Config.getInt(Consts.Prefs.CONFIG_USER_FILTER_FLIGHTS, it))) }
                                ) { throwable -> ToastMaker.toastError(it, throwable.message) })
                    }
                }
            })
        }
        itemSelectorDialog = context?.let {
            MaterialDialog.Builder(it)
                    .items(flights_edit_items)
                    .itemsCallback { dialog, view1, which, text ->
                        when (which) {
                            0 -> try {
                                val intent = Intent(context, AddEditActivity::class.java)
                                intent.putExtra(Consts.DB.COLUMN_ID, flights[ctxPos].id)
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            1 -> DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete)) {
                                disposable.add(Utility.mainThreadObservable(Observable.just(1).doOnNext { o -> Local.removeFlight(flights[ctxPos].id.toInt(), context) })
                                        .doOnSubscribe { disposable1 -> tvTotalTime?.setText(R.string.loading_totals) }
                                        .subscribe({ o ->
                                            flightListAdapter?.removeChild(ctxPos)
                                            displayTotalTime()
                                        }) { throwable -> ToastMaker.toastError(context, throwable.message) })
                            }
                            2 -> DroidUtils.alertConfirmDialog(context, getString(R.string.str_clearall)) {
                                disposable.add(Utility.mainThreadObservable(Observable.just(1)
                                        .doOnNext { o -> Local.removeAllFlights(context) })
                                        .subscribe({ o -> initFlights(getFilterflights(Config.getInt(Consts.Prefs.CONFIG_USER_FILTER_FLIGHTS, context!!))) }
                                        ) { throwable -> ToastMaker.toastError(context, throwable.message) })
                            }
                        }
                    }
                    .build()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.flights_menu, menu)
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
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(broadcastReceiver, filter)
            if (!DroidUtils.isMyServiceRunning(BackgroundIntentService::class.java, context)) {
                initFlights(getFilterflights(Config.getInt(Consts.Prefs.CONFIG_USER_FILTER_FLIGHTS, it)))
            }
        }
    }

    private fun initFlights(orderby: String) {
        disposable.add(Utility.mainThreadObservable(Observable.fromCallable { Local.getFlightListByDate(context, orderby) }
        ).doOnSubscribe { disposable1 -> tvTotalTime?.setText(R.string.loading_totals) }.subscribe({ flights ->
            this.flights = flights
            flightListAdapter?.clear()
            flightListAdapter?.addAll(flights)
            displayTotalTime()
            restoreListPosition()
        }) { throwable ->
            ToastMaker.toastError(context, throwable.message)
            displayTotalTime()
        })
    }

    private fun restoreListPosition() {
        if (positionIndex != -1) {
            mLayoutManager?.scrollToPositionWithOffset(positionIndex, topView)
        }
    }

    private fun saveListPosition() {
        positionIndex = mLayoutManager?.findFirstVisibleItemPosition() ?: 0
        val startView = recyclerView?.getChildAt(0)
        topView = if (startView == null) 0 else {
            val paddingTop = recyclerView?.paddingTop ?: 0
            startView.top - paddingTop
        }
    }

    private fun showMenuDialog(pos: Int) {
        ctxPos = pos//кидаем в глобальную переменную чтобы все видели
        itemSelectorDialog?.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val filters = resources.getStringArray(R.array.flights_filers)
                context?.let {
                    val filterPos = Config.getInt(Consts.Prefs.CONFIG_USER_FILTER_FLIGHTS, it)
                    val filter = filters[filterPos]
                    MaterialDialog.Builder(it)
                            .title(getString(R.string.str_sort_by) + " " + filter)
                            .items(R.array.flights_filers)
                            .autoDismiss(true)
                            .itemsCallback { dialog, view, which, text ->
                                Config.setInt(Consts.Prefs.CONFIG_USER_FILTER_FLIGHTS, which, it)
                                initFlights(getFilterflights(which))
                            }.show()
                }
                return true
            }
        }
        return true
    }

    @SuppressLint("DefaultLocale")
    private fun displayTotalTime() {
        val flightsTimeObs = Observable.fromCallable { Local.getFlightsTime(context) }
        val flightsTotalObs = Observable.fromCallable { Local.getFlightsTotal(context) }
        disposable.add(Utility.mainThreadObservable(Observable.zip<Int, Int, String>(flightsTimeObs, flightsTotalObs, BiFunction { time: Int, cnt: Int ->
            String.format("%s %s\n%s %d", context?.resources?.getString(R.string.str_totaltime),
                    DateTimeUtils.strLogTime(time),
                    getString(R.string.total_records),
                    cnt)
        })).subscribe { s -> tvTotalTime?.text = s })
    }
}