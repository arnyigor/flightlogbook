package com.arny.flightlogbook.views.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arny.flightlogbook.BuildConfig;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.BackgroundIntentService;
import com.arny.flightlogbook.models.DropboxClientFactory;
import com.arny.flightlogbook.models.Functions;
import com.arny.flightlogbook.models.GetCurrentAccountTask;
import com.arny.flightlogbook.views.activities.HomeActivity;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class DropboxSyncFragment extends Fragment {
    private static final String DROPBOX_STR_TOKEN = "access-token";
    private static final String PREF_DBX_EMAIL = "dbx_email";
    private static final String PREF_DBX_NAME = "dbx_name";
    private static final String PREF_DBX_LOCAL_DATETIME = "dbx_local_datetime";
    private static final String PREF_DBX_REMOTE_DATETIME = "dbx_remote_datetime";
    private static final String PREF_DBX_SYNC_DATETIME = "dbx_sync_datetime";
    private Context context;
    private Button login_button,btnSync,btnSyncDown,btnSyncUp;
    private TextView tvDbxEmail, tvDbxName,tvDpxData;
    private String accessToken,mOperationResult,dbxEmail,dbxName,notif,localfileDate,remoteFileDate,syncDateTime;
    private ProgressDialog pDialog;
    private boolean finishOperation,operationSuccess;
    private DbxClientV2 client;
    private Intent mMyServiceIntent;
    private int mOperation;
    private HashMap<String, String> hashMap;

    public DropboxSyncFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dropbox_sync, container, false);
        context = container.getContext();
        mMyServiceIntent = new Intent(context, BackgroundIntentService.class);
        initUI(rootView);
        initListeners();
        initState();
        return rootView;
    }

    private void initState() {
        localfileDate = Functions.getPrefString(context,PREF_DBX_LOCAL_DATETIME, "");
        remoteFileDate = Functions.getPrefString(context,PREF_DBX_REMOTE_DATETIME, "");
        syncDateTime = Functions.getPrefString(context,PREF_DBX_SYNC_DATETIME, "");
        dbxEmail = Functions.getPrefString(context,PREF_DBX_EMAIL, "");
        dbxName = Functions.getPrefString(context,PREF_DBX_NAME, "");
        tvDbxEmail.setText(String.format(getString(R.string.dropbox_email),dbxEmail));
        tvDbxName.setText(String.format(getString(R.string.dropbox_name),dbxName));
        setSyncDataFileDateTime();
    }

    private void initListeners() {
        login_button.setOnClickListener(onClickListenerAuth);
        btnSync.setOnClickListener(onClickListenerSync);
        btnSyncUp.setOnClickListener(onClickListenerSyncUpload);
        btnSyncDown.setOnClickListener(onClickListenerSyncDownload);
    }

    private void initUI(View rootView) {
        login_button = (Button) rootView.findViewById(R.id.btnDpxLogin);
        btnSync = (Button) rootView.findViewById(R.id.btnSync);
        btnSyncDown = (Button) rootView.findViewById(R.id.btnSyncDown);
        btnSyncUp = (Button) rootView.findViewById(R.id.btnSyncUp);
        tvDbxEmail = (TextView) rootView.findViewById(R.id.tvDpxEmail);
        tvDbxName = (TextView) rootView.findViewById(R.id.tvDpxName);
        tvDpxData = (TextView) rootView.findViewById(R.id.tvDpxData);
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
    }

    private void setSyncDataFileDateTime() {
        StringBuilder dpxData = new StringBuilder();
        int downVis = !Functions.empty(remoteFileDate) ? View.VISIBLE : View.GONE;
        int upVis = !Functions.empty(localfileDate) ? View.VISIBLE : View.GONE;
        dpxData.append(getString(R.string.dropbox_sync_files)).append(!Functions.empty(syncDateTime) ? syncDateTime : getString(R.string.dropbox_sync_files_no_data)).append("\n");
        dpxData.append(getString(R.string.dropbox_sync_files_local)).append(!Functions.empty(localfileDate) ? localfileDate : getString(R.string.dropbox_sync_files_no_data)).append("\n");
        dpxData.append(getString(R.string.dropbox_sync_files_remote)).append(!Functions.empty(remoteFileDate) ? remoteFileDate : getString(R.string.dropbox_sync_files_no_data)).append("\n");
        btnSyncDown.setVisibility(downVis);
        btnSyncUp.setVisibility(upVis);
        tvDpxData.setText(dpxData.toString());
    }

    View.OnClickListener onClickListenerAuth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Auth.startOAuth2Authentication(context, getString(R.string.dropbox_app_key));
        }
    };

    View.OnClickListener onClickListenerSyncUpload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (Functions.checkSDRRWPermessions(context,getActivity(),Functions.REQUEST_DBX_EXTERNAL_STORAGE)){
                uploadFile();
            }
        }
    };

    View.OnClickListener onClickListenerSyncDownload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (Functions.checkSDRRWPermessions(context,getActivity(),Functions.REQUEST_DBX_EXTERNAL_STORAGE)){
                downLoadFile();
            }
        }
    };

    View.OnClickListener onClickListenerSync = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Functions.checkSDRRWPermessions(context,getActivity(),Functions.REQUEST_DBX_EXTERNAL_STORAGE)){
                getFileData();
            }
        }
    };

    public void downLoadFile() {
        try {
            if (DropboxClientFactory.getClient() !=null){
                showProgress(getString(R.string.dropbox_sync_downloading));
                if (mMyServiceIntent == null){
                    mMyServiceIntent = new Intent(context, BackgroundIntentService.class);
                }
                mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_DBX_DOWNLOAD);
                context.startService(mMyServiceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, getString(R.string.dropbox_auth_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadFile() {
        try {
            if (DropboxClientFactory.getClient() !=null){
                showProgress(getString(R.string.dropbox_sync_uploading));
                if (mMyServiceIntent == null){
                    mMyServiceIntent = new Intent(context, BackgroundIntentService.class);
                }
                mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_DBX_UPLOAD);
                context.startService(mMyServiceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, getString(R.string.dropbox_auth_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void getFileData() {
        try {
            if (DropboxClientFactory.getClient() !=null){
                showProgress(getString(R.string.dropbox_sync_files));
                if (mMyServiceIntent == null){
                    mMyServiceIntent = new Intent(context, BackgroundIntentService.class);
                }
                mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_DBX_SYNC);
                context.startService(mMyServiceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, getString(R.string.dropbox_auth_error), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BackgroundIntentService.ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter);

        if (Functions.isMyServiceRunning(BackgroundIntentService.class, context)) {
            getOperationNotif(context);
            showProgress(notif);
        }else{
            hideProgress();
            AsyncAuth asyncAuth = new AsyncAuth();
            asyncAuth.execute();
        }
    }

    private void getOperationNotif(Context context) {
        switch (BackgroundIntentService.getOperation()) {
            case BackgroundIntentService.OPERATION_DBX_SYNC:
                notif = context.getResources().getString(R.string.dropbox_sync_files);
                break;
            case BackgroundIntentService.OPERATION_EXPORT:
                notif = context.getResources().getString(R.string.str_export_excel);
                break;
            case BackgroundIntentService.OPERATION_IMPORT_SD:
                notif = context.getResources().getString(R.string.str_import_excel);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgress();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    private void hideProgress() {
        if (pDialog !=null){
            if (BuildConfig.DEBUG) Log.d(DropboxSyncFragment.class.getSimpleName(), "hideProgress: pDialog.isShowing() = " + pDialog.isShowing());
            if (pDialog.isShowing()){
                pDialog.dismiss();
            }
        }
    }

    private void showProgress(String notif) {
        if (pDialog !=null){
            pDialog.setMessage(notif);
            if (BuildConfig.DEBUG) Log.d(DropboxSyncFragment.class.getSimpleName(), "showProgress: pDialog.isShowing() = " + pDialog.isShowing());
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
        }
    }

    private void setUIVisibility() {
        if (hasToken()) {
            login_button.setVisibility(View.GONE);
            tvDbxEmail.setVisibility(View.VISIBLE);
            tvDbxName.setVisibility(View.VISIBLE);
            btnSync.setVisibility(View.VISIBLE);
        } else {
            login_button.setVisibility(View.VISIBLE);
            tvDbxEmail.setVisibility(View.GONE);
            tvDbxName.setVisibility(View.GONE);
            btnSync.setVisibility(View.GONE);
        }
    }

    private void getAccessToken() {
        accessToken = Functions.getPrefString(context,DROPBOX_STR_TOKEN, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                Functions.getPrefs(context).edit().putString(DROPBOX_STR_TOKEN, accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }
    }

    private class AsyncAuth extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setUIVisibility();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getAccessToken();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            setUIVisibility();
        }
    }

    private boolean hasToken() {
        if (accessToken==null){
            accessToken = Functions.getPrefString(context,DROPBOX_STR_TOKEN, null);
        }
        return accessToken != null;
    }

    private void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                Functions.getPrefs(context).edit().putString(PREF_DBX_EMAIL, result.getEmail()).apply();
                Functions.getPrefs(context).edit().putString(PREF_DBX_NAME, result.getName().getDisplayName()).apply();
                if (BuildConfig.DEBUG) Log.d(DropboxSyncFragment.class.getSimpleName(), "onComplete: getView() = " + getView());
                if (getView() != null){
                    tvDbxEmail.setText(String.format(getString(R.string.dropbox_email),result.getEmail()));
                    tvDbxName.setText(String.format(getString(R.string.dropbox_name),result.getName().getDisplayName()));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        loadData();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false);
                mOperation = intent.getIntExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD);
                mOperationResult = intent.getStringExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_RESULT);
                operationSuccess = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH_SUCCESS, false);
                hashMap = (HashMap<String, String>) intent.getSerializableExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_DATA);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (finishOperation){
                hideProgress();
                if (operationSuccess){
                    operationResult();
                    Toasty.success(context, mOperationResult, Toast.LENGTH_SHORT).show();
                }else{
                    Toasty.error(context, mOperationResult, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void operationResult() {
        switch (mOperation){
            case BackgroundIntentService.OPERATION_DBX_SYNC:
                localfileDate = hashMap.get(BackgroundIntentService.EXTRA_KEY_OPERATION_DATA_LOCAL_DATE);
                remoteFileDate = hashMap.get(BackgroundIntentService.EXTRA_KEY_OPERATION_DATA_REMOTE_DATE);
                syncDateTime = Functions.getDateTime(0,"dd MMM yyyy HH:mm:ss");
                Functions.setPrefString(context,PREF_DBX_LOCAL_DATETIME, localfileDate);
                Functions.setPrefString(context,PREF_DBX_REMOTE_DATETIME, remoteFileDate);
                Functions.setPrefString(context,PREF_DBX_SYNC_DATETIME, syncDateTime);
                setSyncDataFileDateTime();
                break;
        }
    }

    private void showDialog(String result) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setTitle(result);
        dlg.setNegativeButton(getString(R.string.str_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = dlg.create();
        alert.show();
    }


}
