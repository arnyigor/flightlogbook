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

import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.BackgroundIntentService;
import com.arny.flightlogbook.models.DownloadFileTask;
import com.arny.flightlogbook.models.DropboxClientFactory;
import com.arny.flightlogbook.models.Functions;
import com.arny.flightlogbook.models.GetCurrentAccountTask;
import com.arny.flightlogbook.models.UploadFileTask;
import com.arny.flightlogbook.views.activities.HomeActivity;
import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class DropboxSyncFragment extends Fragment {
    private static final String DROPBOX_STR_TOKEN = "access-token";
    private static final String DROPBOX_EMAIL = "dbx_email";
    private static final String DROPBOX_NAME = "dbx_name";
    private Context context;
    private Button login_button,btnSync;
    private TextView tvDbxEmail, tvDbxName;
    private String accessToken,mOperationResult,dbxEmail,dbxName;
    private ProgressDialog pDialog;
    private boolean finishOperation,operationSuccess;
    private DbxClientV2 client;
    private Intent mMyServiceIntent;
    private int mOperation;

    public DropboxSyncFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dropbox_sync, container, false);
        context = container.getContext();
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
        mMyServiceIntent = new Intent(context, BackgroundIntentService.class);
        login_button = (Button) rootView.findViewById(R.id.btnDpxLogin);
        btnSync = (Button) rootView.findViewById(R.id.btnSync);
        tvDbxEmail = (TextView) rootView.findViewById(R.id.tvDpxEmail);
        tvDbxName = (TextView) rootView.findViewById(R.id.tvDpxName);
        dbxEmail = Functions.getPrefs(context).getString(DROPBOX_EMAIL, "");
        dbxName = Functions.getPrefs(context).getString(DROPBOX_NAME, "");
        tvDbxEmail.setText(String.format(getString(R.string.dropbox_email),dbxEmail));
        tvDbxName.setText(String.format(getString(R.string.dropbox_name),dbxName));
        login_button.setOnClickListener(onClickListenerAuth);
        btnSync.setOnClickListener(onClickListenerSync);
        return rootView;
    }

    View.OnClickListener onClickListenerAuth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Auth.startOAuth2Authentication(context, getString(R.string.dropbox_app_key));
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

    public void getFileData() {
        try {
                if (DropboxClientFactory.getClient() !=null){
                    pDialog.setMessage(getString(R.string.dropbox_sync_files));
                    if (!pDialog.isShowing()){
                        pDialog.show();
                    }
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
        if (!Functions.isMyServiceRunning(BackgroundIntentService.class, context)) {
            AsyncAuth auth = new AsyncAuth();
            auth.execute();
        } else {
            String notif;
            switch (mOperation) {
                case BackgroundIntentService.OPERATION_DBX_SYNC:
                    notif = context.getResources().getString(R.string.dropbox_sync_files);
                    break;
                default:
                    notif = context.getResources().getString(R.string.str_import_excel);
                    break;
            }
            pDialog.setMessage(notif);
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (pDialog!=null && pDialog.isShowing() ){
            pDialog.dismiss();
            pDialog.cancel();
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
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
        accessToken = Functions.getPrefs(context).getString(DROPBOX_STR_TOKEN, null);
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
            accessToken = Functions.getPrefs(context).getString(DROPBOX_STR_TOKEN, null);
        }
        return accessToken != null;
    }

    private void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                Functions.getPrefs(context).edit().putString(DROPBOX_EMAIL, result.getEmail()).apply();
                Functions.getPrefs(context).edit().putString(DROPBOX_NAME, result.getName().getDisplayName()).apply();
                Log.i(DropboxSyncFragment.class.getSimpleName(), "onComplete: getView() = " + getView());
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
                if (!finishOperation){
                    finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false);
                    mOperation = intent.getIntExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD);
                    mOperationResult = intent.getStringExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_RESULT);
                    operationSuccess = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH_SUCCESS, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(DropboxSyncFragment.class.getSimpleName(), "onReceive: finishOperation = " + finishOperation);
            Log.i(DropboxSyncFragment.class.getSimpleName(), "onReceive: operationSuccess = " + operationSuccess);
            Log.i(DropboxSyncFragment.class.getSimpleName(), "onReceive: mOperation = " + mOperation);
            if (finishOperation){
                Log.i(DropboxSyncFragment.class.getSimpleName(), "onReceive: pDialog = " + pDialog);
                if (pDialog !=null){
                    pDialog.dismiss();
                }
                Toast.makeText(context, mOperationResult, Toast.LENGTH_SHORT).show();
//                showDialog(mOperationResult);
            }else{
                pDialog.setMessage(getString(R.string.dropbox_sync_files));
                if (!pDialog.isShowing()){
                    pDialog.show();
                }
            }
        }
    };

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
