package com.arny.flightlogbook.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;
import com.arny.flightlogbook.utils.DateTimeUtils;
import com.arny.flightlogbook.utils.Utility;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.data.Local;
import com.arny.flightlogbook.data.models.Flight;
import com.arny.flightlogbook.data.models.Statistic;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticFragment extends Fragment {
	private List<Flight> FlightData;
	private List<Statistic> statistics;
	private Context ctx;
	private TextView tvDateFrom, tvDateTo;
	private Calendar dateAndTimeStart = Calendar.getInstance();
	private Calendar dateAndTimeEnd = Calendar.getInstance();
	private long startdatetime, enddatetime;
	private ListView lvStatResult;
	private ProgressBar progressStat;
	private StatisticAdapter statAdapter;
	private final CompositeDisposable disposable = new CompositeDisposable();

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		ctx = context;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.statistic_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		statAdapter = new StatisticAdapter();
		initUI(view);
		startInitDateTime();
		if (statistics != null) {
			refreshAdapter();
		} else {
			getStatistic();
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disposable.clear();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void refreshAdapter() {
		statAdapter.notifyDataSetChanged();
		progressStat.setVisibility(View.GONE);
		lvStatResult.setAdapter(statAdapter);
	}

	//инициализация view
	private void initUI(View view) {
		tvDateFrom = view.findViewById(R.id.tvDateFrom);
		tvDateTo = view.findViewById(R.id.tvDateTo);
		lvStatResult = view.findViewById(R.id.lvStatResult);
		tvDateFrom.setOnClickListener(v -> setDateFrom());
		tvDateTo.setOnClickListener(v -> setDateTo());
		progressStat = view.findViewById(R.id.progressStat);
		progressStat.setVisibility(View.VISIBLE);
	}

	private String getFilterQuery() {
		return "";
	}

	//функция статистики
	private void getStatistic() {
		disposable.add(Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getStatistic(getFilterQuery(), ctx)))
				.subscribe(result -> {
					statistics = result;
					refreshAdapter();
				}));
	}

	//начальные данные времени
	private void startInitDateTime() {
		disposable.add(Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getFlightListByDate(ctx))).subscribe(flights -> {
			FlightData = flights;
			if (FlightData.size() > 0) {
				startdatetime = FlightData.get(0).getDatetime();
				enddatetime = FlightData.get(FlightData.size() - 1).getDatetime();
			} else {
				startdatetime = Calendar.getInstance().getTimeInMillis();
				enddatetime = Calendar.getInstance().getTimeInMillis();
			}
			setDateTimeToTextView();
		}));
	}

	//устанавливаем дату в textView
	private void setDateTimeToTextView() {
		dateAndTimeStart.setTimeInMillis(startdatetime);
		tvDateFrom.setText(DateTimeUtils.getDateTime(startdatetime, "ddMMMyyyy"));
		dateAndTimeEnd.setTimeInMillis(enddatetime);
		tvDateTo.setText(DateTimeUtils.getDateTime(enddatetime, "ddMMMyyyy"));
	}

	// отображаем диалоговое окно для выбора даты
	public void setDateFrom() {
		dateAndTimeStart.setTimeInMillis(startdatetime);
		new DatePickerDialog(ctx, onDateStartSetListener,
				dateAndTimeStart.get(Calendar.YEAR),
				dateAndTimeStart.get(Calendar.MONTH),
				dateAndTimeStart.get(Calendar.DAY_OF_MONTH))
				.show();
	}

	// отображаем диалоговое окно для выбора даты
	public void setDateTo() {
		dateAndTimeEnd.setTimeInMillis(enddatetime);
		new DatePickerDialog(ctx, onDateEndSetListener,
				dateAndTimeEnd.get(Calendar.YEAR),
				dateAndTimeEnd.get(Calendar.MONTH),
				dateAndTimeEnd.get(Calendar.DAY_OF_MONTH))
				.show();
	}

	// установка обработчика выбора даты start
	DatePickerDialog.OnDateSetListener onDateStartSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			dateAndTimeStart.set(Calendar.YEAR, year);
			dateAndTimeStart.set(Calendar.MONTH, monthOfYear);
			dateAndTimeStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			startdatetime = dateAndTimeStart.getTimeInMillis();
			checkStartEndDateTime();
			tvDateFrom.setText(DateTimeUtils.getDateTime(startdatetime, "ddMMMyyyy"));
		}
	};

	// установка обработчика выбора end
	DatePickerDialog.OnDateSetListener onDateEndSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			dateAndTimeEnd.set(Calendar.YEAR, year);
			dateAndTimeEnd.set(Calendar.MONTH, monthOfYear);
			dateAndTimeEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			enddatetime = dateAndTimeEnd.getTimeInMillis();
			checkStartEndDateTime();
			tvDateTo.setText(DateTimeUtils.getDateTime(enddatetime, "ddMMMyyyy"));
		}
	};

	//проверяем конечную дату больше начальной
	private void checkStartEndDateTime() {
		if (startdatetime > enddatetime) {
			Toast.makeText(ctx, getString(R.string.stat_error_end_less_start), Toast.LENGTH_SHORT).show();
			startInitDateTime();
		}
	}

	public class StatisticAdapter extends BaseAdapter {
		TextView tvMonths, tvCnt, tvTotalMonth, tvDayNight, tvVfrIfr, tvCZM;
		LayoutInflater mInflater;

		public StatisticAdapter() {
			mInflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			return statistics.size();
		}

		public List<Statistic> getList() {
			return new ArrayList<>(statistics);
		}

		@Override
		public Object getItem(int position) {
			return statistics.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.stat_list_item, null);
			}
			tvMonths = convertView.findViewById(R.id.tvMonths);
			tvCnt = convertView.findViewById(R.id.tvCnt);
			tvTotalMonth = convertView.findViewById(R.id.tvTotalMonth);
			tvDayNight = convertView.findViewById(R.id.tvDayNight);
			tvVfrIfr = convertView.findViewById(R.id.tvVfrIfr);
			tvCZM = convertView.findViewById(R.id.tvCZM);

			tvMonths.setText(String.valueOf(statistics.get(position).getStrMoths()));
			tvCnt.setText(String.valueOf(statistics.get(position).getCnt()));
			tvTotalMonth.setText(String.valueOf(statistics.get(position).getStrTotalByMonths()));
			tvDayNight.setText(String.valueOf(statistics.get(position).getDnTime()));
			tvVfrIfr.setText(String.valueOf(statistics.get(position).getIvTime()));
			tvCZM.setText(String.valueOf(statistics.get(position).getCzmTime()));

			return convertView;
		}
	}

}
