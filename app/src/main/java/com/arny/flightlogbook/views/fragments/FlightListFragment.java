package com.arny.flightlogbook.views.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arny.arnylib.adapters.SimpleBindableAdapter;
import com.arny.arnylib.utils.*;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.adapter.FlightListHolder;
import com.arny.flightlogbook.common.BackgroundIntentService;
import com.arny.flightlogbook.common.FuncsKt;
import com.arny.flightlogbook.common.Functions;
import com.arny.flightlogbook.common.Local;
import com.arny.flightlogbook.models.Flight;
import com.arny.flightlogbook.views.activities.AddEditActivity;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

import java.util.ArrayList;
import java.util.List;

public class FlightListFragment extends Fragment {
	private SimpleBindableAdapter<Flight, FlightListHolder> flightListAdapter;
	private boolean finishOperation = true;
	private List<Flight> flights = new ArrayList<>();
	private int ctxPos;
	private Context context;
	private TextView tvTotalTime;
	private MaterialDialog itemSelectorDialog;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = container.getContext();
		return inflater.inflate(R.layout.flight_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		FloatingActionButton fab = view.findViewById(R.id.fab);
		fab.setOnClickListener(view1 -> {
			Intent intent = new Intent(context, AddEditActivity.class);
			startActivity(intent);
		});
		tvTotalTime = view.findViewById(R.id.tvTotalTime);
		tvTotalTime.setText(R.string.loading_totals);
		RecyclerView recyclerView = view.findViewById(R.id.listView);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		flightListAdapter = new SimpleBindableAdapter<>(context, R.layout.flight_list_item, FlightListHolder.class);
		flightListAdapter.setActionListener((FlightListHolder.SimpleActionListener) (position, Item) -> showMenuDialog(position));
		recyclerView.setAdapter(flightListAdapter);
		DroidUtils.runLayoutAnimation(recyclerView, R.anim.layout_animation_fall_down);
		itemSelectorDialog = new MaterialDialog.Builder(context)
				.items(R.array.flights_edit_items)
				.itemsCallback((dialog, view1, which, text) -> {
					switch (which) {
						case 0:
							try {
								Intent intent = new Intent(context, AddEditActivity.class);
								intent.putExtra(Local.COLUMN_ID, flights.get(ctxPos).getId());
								startActivity(intent);
							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						case 1:
							DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete), () -> {
								Utility.mainThreadObservable(Observable.just(1).doOnNext(o -> {
									Local.removeFlight(flights.get(ctxPos).getId(), context);
								})).subscribe(o -> {
									flightListAdapter.removeChild(ctxPos);
									displayTotalTime();
								}, throwable -> ToastMaker.toastError(context, throwable.getMessage()));
							});
							break;
						case 2:
							DroidUtils.alertConfirmDialog(context, getString(R.string.str_clearall), () ->
									Utility.mainThreadObservable(Observable.just(1)
											.doOnNext(o -> Local.removeAllFlights(context)))
											.subscribe(o -> {
												initFlights(FuncsKt.getFilterflights(Config.getInt(Local.CONFIG_USER_FILTER_FLIGHTS, context)));
											}, throwable -> ToastMaker.toastError(context, throwable.getMessage())));
							break;
					}
				})
				.build();
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(BackgroundIntentService.ACTION);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter);
		if (!Functions.isMyServiceRunning(BackgroundIntentService.class, context)) {
			initFlights(FuncsKt.getFilterflights(Config.getInt(Local.CONFIG_USER_FILTER_FLIGHTS, context)));
		}
	}

	private void initFlights(String orderby) {
		Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getFlightListByDate(context, orderby))
		).subscribe(flights -> {
			this.flights = flights;
			flightListAdapter.clear();
			flightListAdapter.addAll(flights);
			displayTotalTime();
		}, throwable -> {
			ToastMaker.toastError(context, throwable.getMessage());
			displayTotalTime();
		});
	}

	private void showMenuDialog(int pos) {
//		AlertDialog.Builder alert = new AlertDialog.Builder(context);
//		String contextMenuText[] = {getString(R.string.str_edt), getString(R.string.str_delete), getString(R.string.str_clearall)};
		ctxPos = pos;//кидаем в глобальную переменную чтобы все видели
		itemSelectorDialog.show();
//		alert.setItems(contextMenuText, (dialog, which) -> {
//			switch (which) {
//				case 0:
//					try {
//						Intent intent = new Intent(context, AddEditActivity.class);
//						intent.putExtra(Local.COLUMN_ID, flights.get(ctxPos).getId());
//						startActivity(intent);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					break;
//				case 1:
//					DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete), () -> {
//						Utility.mainThreadObservable(Observable.just(1).doOnNext(o -> {
//							Local.removeFlight(flights.get(ctxPos).getId(), context);
//						})).subscribe(o -> {
//							flightListAdapter.removeChild(ctxPos);
//							displayTotalTime();
//						}, throwable -> ToastMaker.toastError(context, throwable.getMessage()));
//					});
//					break;
//				case 2:
//					DroidUtils.alertConfirmDialog(context, getString(R.string.str_clearall), () ->
//							Utility.mainThreadObservable(Observable.just(1)
//									.doOnNext(o -> Local.removeAllFlights(context)))
//									.subscribe(o -> {
//										initFlights(FuncsKt.getFilterflights(Config.getInt(Local.CONFIG_USER_FILTER_FLIGHTS, context)));
//									}, throwable -> ToastMaker.toastError(context, throwable.getMessage())));
//					break;
//			}
//		});
//		alert.show();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.flights_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_filter:
				String[] filters = getResources().getStringArray(R.array.flights_filers);
				int filterPos = Config.getInt(Local.CONFIG_USER_FILTER_FLIGHTS, context);
				String filter = filters[filterPos];
				new MaterialDialog.Builder(context)
						.title(getString(R.string.str_sort_by) + " " + filter)
						.items(R.array.flights_filers)
						.autoDismiss(true)
						.itemsCallback((dialog, view, which, text) -> {
							Config.setInt(Local.CONFIG_USER_FILTER_FLIGHTS, which, context);
							initFlights(FuncsKt.getFilterflights(which));
						})
						.show();
				return true;
		}
		return true;
	}

	@SuppressLint("DefaultLocale")
	private void displayTotalTime() {
		Observable<Integer> flightsTimeObs = Observable.fromCallable(() -> Local.getFlightsTime(context));
		Observable<Integer> flightsTotalObs = Observable.fromCallable(() -> Local.getFlightsTotal(context));
		Utility.mainThreadObservable(Observable.zip(flightsTimeObs, flightsTotalObs, (time, cnt) ->
				String.format("%s %s %s %d", context.getResources().getString(R.string.str_totaltime), DateTimeUtils.strLogTime(time), " Всего записей:", cnt))).subscribe(s -> {
			tvTotalTime.setText(s);
		});

	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (finishOperation) {
				initFlights(FuncsKt.getFilterflights(Config.getInt(Local.CONFIG_USER_FILTER_FLIGHTS, context)));
			}
		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}