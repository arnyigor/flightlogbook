package com.arny.flightlogbook.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.data.Consts;
import com.arny.flightlogbook.data.service.BackgroundIntentService;
import com.arny.flightlogbook.data.sync.dropbox.DropboxClientFactory;
import com.arny.flightlogbook.data.sync.dropbox.GetCurrentAccountTask;
import com.arny.flightlogbook.utils.DateTimeUtils;
import com.arny.flightlogbook.utils.Prefs;
import com.arny.flightlogbook.utils.Utility;
import com.dropbox.core.android.Auth;
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
	private Button login_button, btnSync, btnSyncDown, btnSyncUp;
	private TextView tvDbxEmail, tvDbxName, tvDpxData;
	private String accessToken, mOperationResult, dbxEmail, dbxName, notif, localfileDate, remoteFileDate, syncDateTime;
	private ProgressDialog pDialog;
	private boolean finishOperation, operationSuccess;
	private Intent mMyServiceIntent;
	private int mOperation;
	private HashMap<String, String> hashMap;
	private CheckBox checkBoxAutoImport;

	public DropboxSyncFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_dropbox_sync, container, false);
		context = container.getContext();
		mMyServiceIntent = new Intent(context, BackgroundIntentService.class);
		return rootView;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		login_button = view.findViewById(R.id.btnDpxLogin);
		btnSync = view.findViewById(R.id.btnSync);
		btnSyncDown = view.findViewById(R.id.btnSyncDown);
		btnSyncUp = view.findViewById(R.id.btnSyncUp);
		tvDbxEmail = view.findViewById(R.id.tvDpxEmail);
		tvDbxName = view.findViewById(R.id.tvDpxName);
		tvDpxData = view.findViewById(R.id.tvDpxData);
		checkBoxAutoImport = view.findViewById(R.id.checkBoxAutoImport);
		pDialog = new ProgressDialog(context);
		pDialog.setCancelable(false);
		initListeners();
		initState();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		login_button = null;
		btnSync = null;
		btnSyncDown = null;
		btnSyncUp = null;
		tvDbxEmail = null;
		tvDbxName = null;
		tvDpxData = null;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}

	private void initState() {
		localfileDate = Prefs.getString(PREF_DBX_LOCAL_DATETIME, context, "");
		remoteFileDate = Prefs.getString(PREF_DBX_REMOTE_DATETIME, context, "");
		syncDateTime = Prefs.getString(PREF_DBX_SYNC_DATETIME, context, "");
		dbxEmail = Prefs.getString(PREF_DBX_EMAIL, context, "");
		dbxName = Prefs.getString(PREF_DBX_NAME, context, "");
		tvDbxEmail.setText(String.format(getString(R.string.dropbox_email), dbxEmail));
		tvDbxName.setText(String.format(getString(R.string.dropbox_name), dbxName));
		boolean autoImport = Prefs.getBoolean(Consts.PrefsConsts.DROPBOX_AUTOIMPORT_TO_DB, false, context);
		checkBoxAutoImport.setChecked(autoImport);
		setSyncDataFileDateTime();
	}

	private void initListeners() {
		login_button.setOnClickListener(onClickListenerAuth);
		btnSync.setOnClickListener(onClickListenerSync);
		btnSyncUp.setOnClickListener(onClickListenerSyncUpload);
		btnSyncDown.setOnClickListener(onClickListenerSyncDownload);
		checkBoxAutoImport.setOnCheckedChangeListener((compoundButton, b) -> Prefs.setBoolean(Consts.PrefsConsts.DROPBOX_AUTOIMPORT_TO_DB, b, context));
	}

	private void setSyncDataFileDateTime() {
		StringBuilder dpxData = new StringBuilder();
		int downVis = !Utility.empty(remoteFileDate) ? View.VISIBLE : View.GONE;
		int upVis = !Utility.empty(localfileDate) ? View.VISIBLE : View.GONE;
		dpxData.append(getString(R.string.dropbox_sync_files)).append(!Utility.empty(syncDateTime) ? syncDateTime : getString(R.string.dropbox_sync_files_no_data)).append("\n");
		dpxData.append(getString(R.string.dropbox_sync_files_local)).append(!Utility.empty(localfileDate) ? localfileDate : getString(R.string.dropbox_sync_files_no_data)).append("\n");
		dpxData.append(getString(R.string.dropbox_sync_files_remote)).append(!Utility.empty(remoteFileDate) ? remoteFileDate : getString(R.string.dropbox_sync_files_no_data)).append("\n");
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
				new MaterialDialog.Builder(context)
						.title(R.string.warning)
						.content(getString(R.string.dropbox_sync_upload) +"?")
						.positiveText(android.R.string.ok)
						.negativeText(android.R.string.cancel)
						.onPositive((dialog, which) -> uploadFile())
						.show();
		}
	};

	View.OnClickListener onClickListenerSyncDownload = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (Prefs.getBoolean(Consts.PrefsConsts.DROPBOX_AUTOIMPORT_TO_DB, false, context)) {
					new MaterialDialog.Builder(context)
							.title(R.string.warning)
							.content(R.string.dropbox_sync_warning_content)
							.positiveText(android.R.string.ok)
							.negativeText(android.R.string.cancel)
							.onPositive((dialog, which) -> downLoadFile())
							.show();
				}else{
					new MaterialDialog.Builder(context)
							.title(R.string.warning)
							.content(getString(R.string.dropbox_sync_download) +"?")
							.positiveText(android.R.string.ok)
							.negativeText(android.R.string.cancel)
							.onPositive((dialog, which) -> downLoadFile())
							.show();
				}
		}
	};

	View.OnClickListener onClickListenerSync = v -> getFileData();

	public void downLoadFile() {
		try {
			if (DropboxClientFactory.getClient() != null) {
				showProgress(getString(R.string.dropbox_sync_downloading));
				if (mMyServiceIntent == null) {
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
			if (DropboxClientFactory.getClient() != null) {
				showProgress(getString(R.string.dropbox_sync_uploading));
				if (mMyServiceIntent == null) {
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
			if (DropboxClientFactory.getClient() != null) {
				showProgress(getString(R.string.dropbox_sync_files));
				if (mMyServiceIntent == null) {
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

		if (Utility.isMyServiceRunning(BackgroundIntentService.class, context)) {
			getOperationNotif(context);
			showProgress(notif);
		} else {
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
		if (pDialog != null) {
			Log.d(DropboxSyncFragment.class.getSimpleName(), "hideProgress: pDialog.isShowing() = " + pDialog.isShowing());
			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}
		}
	}

	private void showProgress(String notif) {
		if (pDialog != null) {
			pDialog.setMessage(notif);
				Log.d(DropboxSyncFragment.class.getSimpleName(), "showProgress: pDialog.isShowing() = " + pDialog.isShowing());
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
		accessToken = Prefs.getString(DROPBOX_STR_TOKEN, context, null);
		if (accessToken == null) {
			accessToken = Auth.getOAuth2Token();
			if (accessToken != null) {
				Prefs.setString(DROPBOX_STR_TOKEN, accessToken, context);
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
		if (accessToken == null) {
			accessToken = Prefs.getString(DROPBOX_STR_TOKEN, context, null);
		}
		return accessToken != null;
	}

	private void loadData() {
		new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
			@Override
			public void onComplete(FullAccount result) {
				Prefs.setString(PREF_DBX_EMAIL, result.getEmail(), context);
				Prefs.setString(PREF_DBX_NAME, result.getName().getDisplayName(), context);
					Log.d(DropboxSyncFragment.class.getSimpleName(), "onComplete: getView() = " + getView());
				if (getView() != null) {
					tvDbxEmail.setText(String.format(getString(R.string.dropbox_email), result.getEmail()));
					tvDbxName.setText(String.format(getString(R.string.dropbox_name), result.getName().getDisplayName()));
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
			if (finishOperation) {
				hideProgress();
				if (operationSuccess) {
					operationResult();
					Toasty.success(context, mOperationResult, Toast.LENGTH_SHORT).show();
				} else {
					Toasty.error(context, mOperationResult, Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	private void operationResult() {
		switch (mOperation) {
			case BackgroundIntentService.OPERATION_DBX_SYNC:
				localfileDate = hashMap.get(BackgroundIntentService.EXTRA_KEY_OPERATION_DATA_LOCAL_DATE);
				remoteFileDate = hashMap.get(BackgroundIntentService.EXTRA_KEY_OPERATION_DATA_REMOTE_DATE);
				syncDateTime = DateTimeUtils.getDateTime((long) 0, "dd MMM yyyy HH:mm:ss");
				Prefs.setString(PREF_DBX_LOCAL_DATETIME, localfileDate, context);
				Prefs.setString(PREF_DBX_REMOTE_DATETIME, remoteFileDate, context);
				Prefs.setString(PREF_DBX_SYNC_DATETIME, syncDateTime, context);
				setSyncDataFileDateTime();
				break;
		}
	}

}
