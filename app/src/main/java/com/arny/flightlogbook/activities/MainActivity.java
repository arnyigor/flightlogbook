package com.arny.flightlogbook.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.arny.arnylib.utils.*;
import com.arny.flightlogbook.BuildConfig;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.data.Consts;
import com.arny.flightlogbook.data.service.BackgroundIntentService;
import com.arny.flightlogbook.data.Local;
import com.arny.flightlogbook.fragments.DropboxSyncFragment;
import com.arny.flightlogbook.fragments.FlightListFragment;
import com.arny.flightlogbook.fragments.StatisticFragment;
import com.arny.flightlogbook.fragments.TypeListFragment;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import java.io.File;
import java.util.List;

import com.tbruyelle.rxpermissions2.RxPermissions;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerListener {

	private static final int SAVE_FILE_RESULT_CODE = 111;
	private static final int MENU_DROPBOX_SYNC = 112;
	private static final int PICKFILE_RESULT_CODE = 1;
	private static final int MENU_FLIGHTS = 0;
	private static final int MENU_TYPES = 1;
	private static final int MENU_STATS = 2;
	private static final String DRAWER_SELECTION = "drawer_selection";
	private int mOperation;
	private String mOperationResult, notif;
	private Drawer drawer = null;
	private Toolbar toolbar;
	private Intent fileintent, mMyServiceIntent;
	private Context context;
	private boolean operationSuccess, finishOperation = true;
	private ProgressDialog bgProgress;
	private static final int TIME_DELAY = 2000;
	private static long back_pressed;
	private final CompositeDisposable disposable = new CompositeDisposable();
	private RxPermissions rxPermissions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_home);
		bgProgress = new ProgressDialog(context);
		bgProgress.setCancelable(false);
		initBgService();
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle(getString(R.string.fragment_logbook));
		rxPermissions = new RxPermissions(this);
		drawer = new DrawerBuilder()
				.withActivity(this)
				.withOnDrawerListener(this)
				.withRootView(R.id.drawer_container)
				.withToolbar(toolbar)
				.withActionBarDrawerToggle(true)
				.withActionBarDrawerToggleAnimated(true)
				.addDrawerItems(
						new PrimaryDrawerItem().withIdentifier(MENU_FLIGHTS).withName(R.string.fragment_logbook).withIcon(GoogleMaterial.Icon.gmd_flight_takeoff),
						new PrimaryDrawerItem().withIdentifier(MENU_TYPES).withName(R.string.fragment_types).withIcon(GoogleMaterial.Icon.gmd_flight),
						new PrimaryDrawerItem().withIdentifier(MENU_STATS).withName(R.string.fragment_stats).withIcon(GoogleMaterial.Icon.gmd_equalizer),
						new PrimaryDrawerItem().withIdentifier(MENU_DROPBOX_SYNC).withName(R.string.fragment_dropbox_sync).withIcon(R.drawable.ic_dropbox_sync)
				)
				.withOnDrawerItemClickListener((view, position, drawerItem) -> {
					selectItem((int) drawerItem.getIdentifier());
					return true;
				})
				.build();
		if (savedInstanceState == null) {
			selectItem(MENU_FLIGHTS);
		} else {
			try {
				drawer.setSelection(Long.parseLong(savedInstanceState.getString(DRAWER_SELECTION)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disposable.clear();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//add the values which need to be saved from the drawer to the bundle
		outState = drawer.saveInstanceState(outState);
		outState.putString(DRAWER_SELECTION, String.valueOf(drawer.getCurrentSelection()));
		//add the values which need to be saved from the accountHeader to the bundle
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.d(MainActivity.class.getSimpleName(), "onPrepareOptionsMenu:  menu.hasVisibleItems() = " + menu.hasVisibleItems());
		boolean fileExist = Local.isAppFileExist(context);
		MenuItem exelOpenAction = menu.findItem(R.id.action_open_file);
		MenuItem exelImportAction = menu.findItem(R.id.action_import_excel);
		if (exelOpenAction != null) {
			exelOpenAction.setVisible(fileExist);
		}
		if (exelImportAction != null) {
			exelImportAction.setVisible(fileExist);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.action_about:
				Intent intent = new Intent(context, AboutActivity.class);
				startActivity(intent);
				break;
			case R.id.action_settings:
				Intent intentSettings = new Intent(context, Preferences.class);
				startActivity(intentSettings);
				break;
			case R.id.action_import_excel:
				showImportAlert();
				break;
			case R.id.action_export_excel:
				showExportAlert();
				break;
			case R.id.action_open_file:
				openFileWith();
				break;
			case R.id.action_type_edit:
				Intent mAirplanesActivity = new Intent(context, AirplaneTypesActivity.class);
				startActivity(mAirplanesActivity);
				break;
			case R.id.action_import_from_file:
				showImportDialogSD();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void selectItem(int position) {
		Fragment fragment = null;
		switch (position) {
			case MENU_FLIGHTS:
				fragment = new FlightListFragment();
				toolbar.setTitle(getString(R.string.fragment_logbook));
				break;
			case MENU_TYPES:
				fragment = new TypeListFragment();
				toolbar.setTitle(getString(R.string.str_airplane_types));
				break;
			case MENU_STATS:
				fragment = new StatisticFragment();
				toolbar.setTitle(getString(R.string.fragment_stats));
				break;
			case MENU_DROPBOX_SYNC:
				fragment = new DropboxSyncFragment();
				toolbar.setTitle(getString(R.string.fragment_dropbox_sync));
				break;
		}
		if (fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
			drawer.closeDrawer();
		}
	}

	@SuppressLint("RestrictedApi")
	@Override
	public void onBackPressed() {

		if (drawer.isDrawerOpen()) {
			drawer.closeDrawer();
		} else {
			List<Fragment> fragments = getSupportFragmentManager().getFragments();
			boolean isMain = false;
			for (Fragment curFrag : fragments) {
				if (curFrag != null && curFrag.isVisible() && curFrag instanceof FlightListFragment) {
					isMain = true;
				}
			}
			if (!isMain) {
				selectItem(MENU_FLIGHTS);
			} else {
				if (back_pressed + TIME_DELAY > System.currentTimeMillis()) {
					if (Config.getBoolean("autoExportXLSPref", false, context)) {
						initBgService();
						mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_EXPORT);
						startService(mMyServiceIntent);
					}
					super.onBackPressed();
				} else {
					Toast.makeText(getBaseContext(), R.string.press_back_again_to_exit,
							Toast.LENGTH_SHORT).show();
				}
				back_pressed = System.currentTimeMillis();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!BasePermissions.canAccessStorage(this, 777)) {
			return;
		}
		IntentFilter filter = new IntentFilter(BackgroundIntentService.ACTION);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter);
		if (DroidUtils.isMyServiceRunning(BackgroundIntentService.class, context)) {
			getOperationNotif(context);
			showProgress(notif);
		} else {
			hideProgress();
		}
	}

	private void hideProgress() {
		try {
			if (bgProgress != null) {
				bgProgress.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showProgress(String notif) {
		try {
			if (bgProgress != null) {
				Log.d(MainActivity.class.getSimpleName(), "hideProgress: bgProgress.isShowing() = " + bgProgress.isShowing());
				bgProgress.setMessage(notif);
				if (!bgProgress.isShowing()) {
					bgProgress.show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		hideProgress();
		LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (BuildConfig.DEBUG) {
			Log.d(MainActivity.class.getSimpleName(), "onRequestPermissionsResult: requestCode = " + requestCode);
		}
		switch (requestCode) {
			case Consts.RequestCodes.REQUEST_EXTERNAL_STORAGE_XLS:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					fileintent = new Intent();
					fileintent.setAction(Intent.ACTION_GET_CONTENT);
					fileintent.addCategory(Intent.CATEGORY_OPENABLE);
					fileintent.setType("*/*");
					startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
				} else {
					Toast.makeText(context, R.string.str_storage_permission_denied, Toast.LENGTH_SHORT).show();
				}
				break;
			case Consts.RequestCodes.REQUEST_DBX_EXTERNAL_STORAGE:
				if (BasePermissions.permissionGranted(grantResults)) {
					Fragment dropboxSyncFragment = getSupportFragmentManager().findFragmentById(R.id.container);
					if (dropboxSyncFragment instanceof DropboxSyncFragment) {
						((DropboxSyncFragment) dropboxSyncFragment).getFileData();
					}
				}
				break;
			case 777:
				if (BasePermissions.permissionGranted(grantResults)) {
					onResume();
				}
				break;
		}
	}

	@SuppressLint("CheckResult")
	private void showImportDialogSD() {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(getString(R.string.str_import_attention));
		alert.setMessage(getString(R.string.str_import_massage));
		alert.setNegativeButton(getString(R.string.str_cancel), (dialog, which) -> dialog.cancel());
		alert.setPositiveButton(getString(R.string.str_ok), (dialog, which) -> {
			rxPermissions
					.request(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(permissionGranded -> {
						if (permissionGranded) {
							fileintent = new Intent();
							fileintent.setAction(Intent.ACTION_GET_CONTENT);
							fileintent.addCategory(Intent.CATEGORY_OPENABLE);
							fileintent.setType("*/*");
							startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
						}
					}, throwable -> {
						ToastMaker.toastError(this, getString(R.string.str_error_import) + ":" + throwable.getMessage());
					}
			);

		});
		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case PICKFILE_RESULT_CODE:
				if (resultCode == RESULT_OK) {
					String FilePath = data.getData().getPath();
					if (BuildConfig.DEBUG)
						Log.d(MainActivity.class.getSimpleName(), "onActivityResult: FilePath = " + FilePath);
					initBgService();
					mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD);
					mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_IMPORT_SD_FILENAME, FilePath);
					startService(mMyServiceIntent);
					showProgress(getString(R.string.str_import_excel));
				} else {
					Toast.makeText(MainActivity.this, getString(R.string.str_error_import), Toast.LENGTH_SHORT).show();
				}
				break;
			case SAVE_FILE_RESULT_CODE:
				if (resultCode == RESULT_OK && data != null && data.getData() != null) {
					String theFilePath = data.getData().getPath();
				}
				break;
		}
	}

	private void openFileWith() {
		try {
			Intent myIntent = new Intent(Intent.ACTION_VIEW);
			File sdPath = Environment.getExternalStorageDirectory();
			File file = new File(sdPath + "/Android/data/com.arny.flightlogbook/files", Consts.Files.EXEL_FILE_NAME);
			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			myIntent.setDataAndType(Uri.fromFile(file), mimetype);
			startActivity(myIntent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BuildConfig.DEBUG)
				Log.d(MainActivity.class.getSimpleName(), "onReceive: service runing = " + DroidUtils.isMyServiceRunning(BackgroundIntentService.class, context));
			try {
				finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false);
				mOperation = intent.getIntExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD);
				mOperationResult = intent.getStringExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_RESULT);
				operationSuccess = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH_SUCCESS, false);
			} catch (Exception e) {
				e.printStackTrace();
				hideProgress();
			}
			Log.d(MainActivity.class.getSimpleName(), "onReceive: finishOperation = " + finishOperation);
			Log.d(MainActivity.class.getSimpleName(), "onReceive: operationSuccess = " + operationSuccess);
			if (finishOperation) {
				hideProgress();
				if (operationSuccess) {
					Toasty.success(context, mOperationResult, Toast.LENGTH_SHORT).show();
				} else {
					Toasty.error(context, mOperationResult, Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

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
			default:
				notif = context.getResources().getString(R.string.str_import_excel);
				break;
		}
	}

	private void showExportAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.str_export_attention));
		alert.setNegativeButton(getString(R.string.str_cancel), (dialog, which) -> dialog.cancel());
		alert.setPositiveButton(getString(R.string.str_ok), (dialog, which) -> {
			initBgService();
			mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_EXPORT);
			startService(mMyServiceIntent);
			showProgress(getString(R.string.str_export_excel));
		});
		alert.show();
	}

	private void showImportAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.str_import_attention));
		alert.setMessage(getString(R.string.str_import_massage));
		alert.setNegativeButton(getString(R.string.str_cancel), (dialog, which) -> dialog.cancel());
		alert.setPositiveButton(getString(R.string.str_ok), (dialog, which) -> {
			initBgService();
			mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD);
			mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_IMPORT_SD_FILENAME, "");
			startService(mMyServiceIntent);
			ToastMaker.toastSuccess(this, "Импорт данных из файла стартовал");
		});
		alert.show();
	}

	private void initBgService() {
		if (mMyServiceIntent == null) {
			mMyServiceIntent = new Intent(MainActivity.this, BackgroundIntentService.class);
		}
	}

	public void updateNavDrawerCounts() {
		PrimaryDrawerItem itemNewTask = (PrimaryDrawerItem) drawer.getDrawerItem(MENU_FLIGHTS);
		PrimaryDrawerItem itemCompleteTask = (PrimaryDrawerItem) drawer.getDrawerItem(MENU_TYPES);
		disposable.add(Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getFlightListByDate(MainActivity.this).size())).subscribe(flights -> {
			if (itemNewTask != null && drawer != null) {
				itemNewTask.withBadge(String.valueOf(flights)).withBadgeStyle(new BadgeStyle().withTextColorRes(R.color.colorAccent));
				drawer.updateItem(itemNewTask);
			}
		}));
		disposable.add(Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getTypeList(MainActivity.this).size())).subscribe(types -> {
			if (itemCompleteTask != null && drawer != null) {
				itemCompleteTask.withBadge(String.valueOf(types)).withBadgeStyle(new BadgeStyle().withTextColorRes(R.color.colorAccent));
				drawer.updateItem(itemCompleteTask);
			}
		}));
	}

	@Override
	public void onDrawerOpened(View drawerView) {
		updateNavDrawerCounts();
	}

	@Override
	public void onDrawerClosed(View drawerView) {

	}

	@Override
	public void onDrawerSlide(View drawerView, float slideOffset) {

	}
}
