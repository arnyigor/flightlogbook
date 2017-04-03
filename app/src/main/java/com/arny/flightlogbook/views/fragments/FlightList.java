package com.arny.flightlogbook.views.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.BackgroundIntentService;
import com.arny.flightlogbook.models.Functions;
import com.arny.flightlogbook.views.activities.AddEditActivity;
import com.arny.flightlogbook.models.DataList;
import com.arny.flightlogbook.models.DatabaseHandler;

import java.util.List;

public class FlightList extends Fragment {
    private DatabaseHandler db;
    private boolean finishOperation = true;
    private ListView listView;
    private List<DataList> FlightData;
    private List<DataList> TypeData;
    private String airplane_type;
    private int airplane_type_id, ctxPos;
    private Context context;
    private TextView tvTotalTime;

    public FlightList() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.flight_list, container, false);
        context = container.getContext();
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new
                        Intent(context, AddEditActivity.class);
                startActivity(intent);
            }
        });
        tvTotalTime = (TextView) view.findViewById(R.id.tvTotalTime);
        listView = (ListView) view.findViewById(R.id.listView);
        db = new DatabaseHandler(context);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BackgroundIntentService.ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter);
        if (!Functions.isMyServiceRunning(BackgroundIntentService.class,context)) {
            initFlights();
        }
    }

    private void initFlights() {
        LoadFlights lf = new LoadFlights();
        lf.execute();
        displayTotalTime();
    }

   private class LoadFlights extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                FlightData = db.getFlightListByDate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            listView.setAdapter(new ViewAdapter());
            displayTotalTime();
        }
    }

    public class ViewAdapter extends BaseAdapter {

        LayoutInflater mInflater;

        public ViewAdapter() {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return FlightData.size();
//            return db.getFlightCount();
        }

        @Override
        public Object getItem(int position) {
            return FlightData.get(position);
//            return db.getFlightItem(FlightData.get(position).getId());
        }

        @Override
        public long getItemId(int position) {
            return FlightData.get(position).getId();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item, null);
                viewHolder = new ViewHolder();
                viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
                viewHolder.tvLogTime = (TextView) convertView.findViewById(R.id.tvLogTime);
                viewHolder.tvType = (TextView) convertView.findViewById(R.id.tvType);
                viewHolder.tvRegNo = (TextView) convertView.findViewById(R.id.tvRegNo);
                viewHolder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            airplane_type_id = FlightData.get(position).getAirplanetypeid();
            TypeData = db.getTypeItem(airplane_type_id);
            for (DataList type : TypeData) {
                airplane_type = type.getAirplanetypetitle();
            }
            viewHolder.tvDate.setText(Functions.getDateTime(FlightData.get(position).getDatetime(),"dd MMM yyyy"));
            viewHolder.tvLogTime.setText(Functions.strLogTime(FlightData.get(position).getLogtime()));
            viewHolder.tvType.setText(airplane_type);
            viewHolder.tvRegNo.setText(String.valueOf(FlightData.get(position).getReg_no()));
            viewHolder.tvDesc.setText(FlightData.get(position).getDescription());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMenuDialog(position);
                }
            });
//
//            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.item, null);
//            }
//            ((TextView) convertView.findViewById(R.id.tvDate)).setText(Functions.getDateTime(FlightData.get(position).getDatetime(),"ddMMMyyyy"));
//            ((TextView) convertView.findViewById(R.id.tvLogTime)).setText(Functions.strLogTime(FlightData.get(position).getLogtime()));
//
//            airplane_type_id = FlightData.get(position).getAirplanetypeid();
//            TypeData = db.getTypeItem(airplane_type_id);
//            for (DataList type : TypeData) {
//                airplane_type = type.getAirplanetypetitle();
//            }
//            ((TextView) convertView.findViewById(R.id.tvType)).setText(airplane_type);
//            ((TextView) convertView.findViewById(R.id.tvRegNo)).setText(String.valueOf(FlightData.get(position).getReg_no()));
//            ((TextView) convertView.findViewById(R.id.tvDesc)).setText(FlightData.get(position).getDescription());
//
//            convertView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showMenuDialog(position);
//                }
//            });
            return convertView;
        }
        private class ViewHolder {
            TextView tvDate,tvLogTime,tvType,tvRegNo,tvDesc;
        }
    }

    private void showMenuDialog(int pos) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        String contextMenuText[] = {getString(R.string.str_edt), getString(R.string.str_delete), getString(R.string.str_clearall)};
        ctxPos = pos;//кидаем в глобальную переменную чтобы все видели
        alert.setItems(contextMenuText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        try {
                            Intent intent = new Intent(getActivity().getApplicationContext(), AddEditActivity.class);
                            intent.putExtra(DatabaseHandler.COLUMN_ID, FlightData.get(ctxPos).getId());
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        AlertDialog.Builder delDialog = new AlertDialog.Builder(context);
                        delDialog.setTitle(getString(R.string.str_delete) + "?");
                        delDialog
                                .setNegativeButton(getString(R.string.str_cancel), null);
                        delDialog.setPositiveButton(getString(R.string.str_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        db.removeFlight(FlightData.get(ctxPos).getId());
                                        LoadFlights lf = new LoadFlights();
                                        lf.execute();
//                                        displayTotalTime();
                                    }
                                });
                        AlertDialog alert = delDialog.create();
                        alert.show();
                        break;
                    case 2:
                        AlertDialog.Builder delallDialog = new AlertDialog.Builder(context);
                        delallDialog.setTitle(getString(R.string.str_clearall) + "?");
                        delallDialog
                                .setNegativeButton(getString(R.string.str_cancel), null);
                        delallDialog.setPositiveButton(getString(R.string.str_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        db.removeAllFlights();
                                        LoadFlights lf = new LoadFlights();
                                        lf.execute();
//                                        displayTotalTime();
                                    }
                                });
                        AlertDialog alertAll = delallDialog.create();
                        alertAll.show();
                        break;
                }
            }
        });
        alert.show();
    }

    private void displayTotalTime() {
        tvTotalTime.setText(context.getResources().getString(R.string.str_totaltime) + " " + Functions.strLogTime(db.getFlightsTime()));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("FlightList", "onPause: ");
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
                initFlights();
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}