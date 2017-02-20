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
    private Context context;
    private Button login_button,btnSync;
    private TextView tvDbxEmail, tvDbxName;
    private String accessToken;
    private ProgressDialog pDialog;
    private boolean finishOperation;
    private File syncFolder;
    private DbxClientV2 client;

    public DropboxSyncFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dropbox_sync, container, false);
        context = container.getContext();
        login_button = (Button) rootView.findViewById(R.id.btnDpxLogin);
        btnSync = (Button) rootView.findViewById(R.id.btnSync);
        tvDbxEmail = (TextView) rootView.findViewById(R.id.tvDpxEmail);
        tvDbxName = (TextView) rootView.findViewById(R.id.tvDpxName);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Auth.startOAuth2Authentication(context, getString(R.string.dropbox_app_key));
            }
        });
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean permissionGranded = Functions.checkSDRRWPermessions(context,getActivity(),Functions.REQUEST_DBX_EXTERNAL_STORAGE);
                if (permissionGranded){
                    getFileData();
                }
            }
        });

        return rootView;
    }

    public void getFileData() {
        try {
            client = DropboxClientFactory.getClient();
            pDialog = new ProgressDialog(context);
            pDialog.setCancelable(false);
            pDialog.setMessage(getString(R.string.dropbox_sync_files));
            pDialog.show();
            AsyncFileData asyncFileData = new AsyncFileData();
            asyncFileData.execute();
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
        if (!Functions.isMyServiceRunning(BackgroundIntentService.class,context)) {
            AsyncAuth auth = new AsyncAuth();
            auth.execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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

    private class AsyncFileData extends AsyncTask<Void, Void, Void> {
        private FileMetadata remoteMetadata;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Get files and folder metadata from Dropbox root directory
                ListFolderResult result = client.files().listFolder("");
                Log.i(AsyncFileData.class.getSimpleName(), "doInBackground: result = " + String.valueOf(result));
                while (true) {

                    for (Metadata metadata : result.getEntries()) {
                        Log.i(AsyncFileData.class.getSimpleName(), "doInBackground: hasname = " + metadata.getName().compareToIgnoreCase(Functions.EXEL_FILE_NAME));
                        if (metadata.getName().compareToIgnoreCase(Functions.EXEL_FILE_NAME)==0){
                            if (metadata instanceof  FileMetadata){
                                remoteMetadata = (FileMetadata)metadata;
                                break;
                            }
                        }
                        Log.i(AsyncFileData.class.getSimpleName(), "doInBackground: metadata getName= " + metadata.getName());
                        Log.i(AsyncFileData.class.getSimpleName(), "doInBackground: metadata getPathLower= " + metadata.getPathLower());
                    }

                    if (!result.getHasMore()) {
                        break;
                    }
                    result = client.files().listFolderContinue(result.getCursor());
                }
                syncFile(remoteMetadata);
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            btnSync.setVisibility(View.VISIBLE);
            if (pDialog !=null){
                pDialog.dismiss();
            }
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
                tvDbxEmail.setText(String.format(getString(R.string.dropbox_email),result.getEmail()));
                tvDbxName.setText(String.format(getString(R.string.dropbox_name),result.getName().getDisplayName()));
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (finishOperation) {
                Log.i(DropboxSyncFragment.class.getSimpleName(), "onReceive: finishOperation = " + finishOperation);
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

    private void downloadFile(FileMetadata file) {

        new DownloadFileTask(context, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                pDialog.dismiss();

                if (result != null) {
                    showDialog("Синхронизация завершена");
                // viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                pDialog.dismiss();
                e.printStackTrace();
                Toast.makeText(context, getString(R.string.dropbox_sync_error), Toast.LENGTH_SHORT).show();
            }
        }, syncFolder).execute(file);

    }

    private void uploadFile() {

        new UploadFileTask(context, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                pDialog.dismiss();
//                String message = result.getName() + " size " + result.getSize() + " modified " +
//                        DateFormat.getDateTimeInstance().format(result.getClientModified());
//                Toast.makeText(context, message, Toast.LENGTH_SHORT) .show();
                showDialog("Синхронизация завершена");
            }

            @Override
            public void onError(Exception e) {
                pDialog.dismiss();
                Log.e(DropboxSyncFragment.class.getSimpleName(), "onError: Failed to upload file " + e.getMessage());
                Toast.makeText(context, "An error has occurred", Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute();
    }

    public void syncFile(final FileMetadata remoteFile) {
        File localFile = new File(context.getExternalFilesDir(null), Functions.EXEL_FILE_NAME);
        syncFolder = context.getExternalFilesDir(null);
        Date localLastModified, remoteLastModified;

        try {
            if (remoteFile ==null){
                uploadFile();
                return;
            }
            remoteLastModified = remoteFile.getClientModified();
            localLastModified = new Date(localFile.lastModified());

            Log.i(DropboxSyncFragment.class.getSimpleName(), "syncFolder: remoteLastModified = " + Functions.getDateTime(remoteLastModified,null));
            Log.i(DropboxSyncFragment.class.getSimpleName(), "syncFolder: localLastModified = " + Functions.getDateTime(localLastModified,null));

            if (remoteLastModified.after(localLastModified) || !localFile.isFile() || localFile.length() == 0) {
                downloadFile(remoteFile);
            } else if (remoteLastModified.before(localLastModified) || remoteFile.getSize()==0) {
                uploadFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
