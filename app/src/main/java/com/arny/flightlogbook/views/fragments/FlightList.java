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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.arny.arnylib.adapters.SimpleBindableAdapter;
import com.arny.arnylib.interfaces.AlertDialogListener;
import com.arny.arnylib.utils.DroidUtils;
import com.arny.flightlogbook.adapter.FlightListHolder;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.BackgroundIntentService;
import com.arny.flightlogbook.models.Functions;
import com.arny.flightlogbook.views.activities.AddEditActivity;
import com.arny.flightlogbook.models.DataList;
import com.arny.flightlogbook.models.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

public class FlightList extends Fragment {
	private SimpleBindableAdapter<DataList, FlightListHolder> flightListAdapter;
    private DatabaseHandler db;
    private boolean finishOperation = true;
	private List<DataList> FlightData = new ArrayList<>();
	private int ctxPos;
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
                Intent intent = new Intent(context, AddEditActivity.class);
                startActivity(intent);
            }
        });
        tvTotalTime = (TextView) view.findViewById(R.id.tvTotalTime);
	    RecyclerView listView = (RecyclerView) view.findViewById(R.id.listView);
	    listView.setLayoutManager( new LinearLayoutManager(context));
	    listView.setItemAnimator(new DefaultItemAnimator());
	    flightListAdapter = new SimpleBindableAdapter<>(context,R.layout.flight_list_item, FlightListHolder.class);
	    flightListAdapter.setActionListener(new FlightListHolder.SimpleActionListener() {

		    @Override
		    public void OnItemClickListener(int position, Object Item) {
			    Log.i(FlightList.class.getSimpleName(), "OnItemClickListener: position = " + position);
			    Log.i(FlightList.class.getSimpleName(), "OnItemClickListener: Item = " + Item);
			    showMenuDialog(position);
		    }
	    });
	    listView.setAdapter(flightListAdapter);
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
                FlightData = db.getFlightListByDate(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
	        flightListAdapter.clear();
	        flightListAdapter.addAll(FlightData);
            displayTotalTime();
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
                            Intent intent = new Intent(context, AddEditActivity.class);
                            intent.putExtra(DatabaseHandler.COLUMN_ID, FlightData.get(ctxPos).getId());
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
	                    DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete), new AlertDialogListener() {
		                    @Override
		                    public void onConfirm() {
			                    db.removeFlight(FlightData.get(ctxPos).getId());
			                    LoadFlights lf = new LoadFlights();
			                    lf.execute();
		                    }
	                    });
                        break;
                    case 2:
	                    DroidUtils.alertConfirmDialog(context, getString(R.string.str_clearall), new AlertDialogListener() {
		                    @Override
		                    public void onConfirm() {
			                    db.removeAllFlights();
			                    LoadFlights lf = new LoadFlights();
			                    lf.execute();
		                    }
	                    });
                        break;
                }
            }
        });
        alert.show();
    }

    private void displayTotalTime() {
        tvTotalTime.setText(String.format("%s %s", context.getResources().getString(R.string.str_totaltime), Functions.strLogTime(db.getFlightsTime())));
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
                initFlights();
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}