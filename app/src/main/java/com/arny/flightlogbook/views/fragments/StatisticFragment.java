package com.arny.flightlogbook.views.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.DataList;
import com.arny.flightlogbook.models.DatabaseHandler;
import com.arny.flightlogbook.models.Functions;
import com.arny.flightlogbook.models.Statistic;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.R.id.list;

public class StatisticFragment extends Fragment {
    private static final String LIST_STATE = "listState";
    private Parcelable mListState = null;
    private DatabaseHandler db;
    private List<DataList> FlightData;
    private List<Statistic> statistics;
    private Context ctx;
    private TextView tvDateFrom,tvDateTo;
    private Calendar dateAndTimeStart = Calendar.getInstance();
    private Calendar dateAndTimeEnd = Calendar.getInstance();
    private long startdatetime,enddatetime;
    private ListView lvStatResult;
    private ProgressBar progressStat;
    private StatisticAdapter statAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.statistic_fragment, container, false);
        ctx = container.getContext();
        db = new DatabaseHandler(ctx);
        statAdapter = new StatisticAdapter();
        initUI(view);
        startInitDateTime();
        if(statistics != null) {
            refreshAdapter();
        }else{
            getStatistic();
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelable(LIST_STATE, lvStatResult.onSaveInstanceState());
    }

    private void refreshAdapter() {
        statAdapter.notifyDataSetChanged();
        progressStat.setVisibility(View.GONE);
        lvStatResult.setAdapter(statAdapter);
    }

    //инициализация view
    private void initUI(View view) {
        tvDateFrom = (TextView) view.findViewById(R.id.tvDateFrom);
        tvDateTo = (TextView) view.findViewById(R.id.tvDateTo);
        lvStatResult = (ListView) view.findViewById(R.id.lvStatResult);
        tvDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFrom();
            }
        });
        tvDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateTo();
            }
        });
        progressStat = (ProgressBar) view.findViewById(R.id.progressStat);
        progressStat.setVisibility(View.VISIBLE);
    }

    private String getFilterQuery(){
      String query = "";
      return query;
    }


    //функция статистики
    private void getStatistic(){
        LoadStatistic ls = new LoadStatistic();
        ls.execute();
    }


    private class LoadStatistic extends AsyncTask<Void, Void, List<Statistic>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Statistic> doInBackground(Void... params) {
            try {
                return db.getStatistic(getFilterQuery());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Statistic> result) {
            super.onPostExecute(result);
            statistics = result;
            refreshAdapter();
        }
    }

    //начальные данные времени
    private void startInitDateTime() {
        FlightData = db.getFlightListByDate();
        if (FlightData.size()>0){
            startdatetime= FlightData.get(0).getDatetime();
            enddatetime = FlightData.get(FlightData.size()-1).getDatetime();
        }else{
            startdatetime = Calendar.getInstance().getTimeInMillis();
            enddatetime = Calendar.getInstance().getTimeInMillis();
        }
        setDateTimeToTextView();
    }

    //устанавливаем дату в textView
    private void setDateTimeToTextView() {
        dateAndTimeStart.setTimeInMillis(startdatetime);
        tvDateFrom.setText(Functions.getDateTime(startdatetime,"ddMMMyyyy"));
        dateAndTimeEnd.setTimeInMillis(enddatetime);
        tvDateTo.setText(Functions.getDateTime(enddatetime,"ddMMMyyyy"));
    }

    // отображаем диалоговое окно для выбора даты
    public void setDateFrom() {
        dateAndTimeStart.setTimeInMillis(startdatetime);
        new DatePickerDialog(ctx, onDateStartSetListener,
                dateAndTimeStart.get(dateAndTimeStart.YEAR),
                dateAndTimeStart.get(dateAndTimeStart.MONTH),
                dateAndTimeStart.get(dateAndTimeStart.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора даты
    public void setDateTo() {
        dateAndTimeEnd.setTimeInMillis(enddatetime);
        new DatePickerDialog(ctx, onDateEndSetListener,
                dateAndTimeEnd.get(dateAndTimeEnd.YEAR),
                dateAndTimeEnd.get(dateAndTimeEnd.MONTH),
                dateAndTimeEnd.get(dateAndTimeEnd.DAY_OF_MONTH))
                .show();
    }

    // установка обработчика выбора даты start
    DatePickerDialog.OnDateSetListener onDateStartSetListener =new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTimeStart.set(Calendar.YEAR, year);
            dateAndTimeStart.set(Calendar.MONTH, monthOfYear);
            dateAndTimeStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            startdatetime = dateAndTimeStart.getTimeInMillis();
            checkStartEndDateTime();
            tvDateFrom.setText(Functions.getDateTime(startdatetime,"ddMMMyyyy"));
        }
    };

    // установка обработчика выбора end
    DatePickerDialog.OnDateSetListener onDateEndSetListener =new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTimeEnd.set(Calendar.YEAR, year);
            dateAndTimeEnd.set(Calendar.MONTH, monthOfYear);
            dateAndTimeEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            enddatetime = dateAndTimeEnd.getTimeInMillis();
            checkStartEndDateTime();
            tvDateTo.setText(Functions.getDateTime(enddatetime,"ddMMMyyyy"));
        }
    };

    //проверяем конечную дату больше начальной
    private void checkStartEndDateTime() {
        if (startdatetime>enddatetime){
            Toast.makeText(ctx, "Конечная дата меньше начальной", Toast.LENGTH_SHORT).show();
            startInitDateTime();
        }
    }

    public class StatisticAdapter extends BaseAdapter {
        TextView tvMonths,tvCnt,tvTotalMonth,tvDayNight,tvVfrIfr,tvCZM;
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
            tvMonths = (TextView) convertView.findViewById(R.id.tvMonths);
            tvCnt = (TextView) convertView.findViewById(R.id.tvCnt);
            tvTotalMonth = (TextView) convertView.findViewById(R.id.tvTotalMonth);
            tvDayNight = (TextView) convertView.findViewById(R.id.tvDayNight);
            tvVfrIfr = (TextView) convertView.findViewById(R.id.tvVfrIfr);
            tvCZM = (TextView) convertView.findViewById(R.id.tvCZM);

            tvMonths.setText(String.valueOf(statistics.get(position).getStrMoths()));
            tvCnt.setText(String.valueOf(statistics.get(position).getCnt()));
            tvTotalMonth.setText(String.valueOf(statistics.get(position).getStrTotalByMonths()));
            tvDayNight.setText(String.valueOf(statistics.get(position).getDnTime()));
            tvVfrIfr.setText(String.valueOf(statistics.get(position).getIVTime()));
            tvCZM.setText(String.valueOf(statistics.get(position).getCzmTime()));

            return convertView;
        }
    }

}
