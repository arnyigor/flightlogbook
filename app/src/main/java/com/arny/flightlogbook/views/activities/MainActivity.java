//package com.arny.flightlogbook.views.activities;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.content.res.Configuration;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.preference.PreferenceManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.content.LocalBroadcastManager;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.ContextMenu;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.arny.flightlogbook.R;
//import com.arny.flightlogbook.models.BackgroundIntentService;
//import com.arny.flightlogbook.models.DataList;
//import com.arny.flightlogbook.models.DatabaseHandler;
//import com.arny.flightlogbook.models.Funct;
//import com.arny.flightlogbook.models.Functions;
//import com.arny.flightlogbook.models.Preferences;
//import com.arny.flightlogbook.views.fragments.FlightList;
//import com.arny.flightlogbook.views.fragments.StatisticFragment;
//
//import org.apache.poi.hssf.usermodel.HSSFCellStyle;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hssf.util.HSSFColor;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.DateFormatSymbols;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class MainActivity extends AppCompatActivity {
//
//	private static final String LOGTIME_STATE_KEY = "LOGTIME_STATE_KEY";
//	private static final int CM_DELETE_ID = 102;
//	private static final int CM_EDIT_ID = 103;
//	private static final String TAG = "LOG_TAG";
//
//	private static final int PICKFILE_RESULT_CODE = 1;
//	// Storage Permissions
//	private static final int REQUEST_EXTERNAL_STORAGE_XLS = 110;
//	private static final int SAVE_FILE_RESULT_CODE = 111;
//	private static String[] PERMISSIONS_STORAGE = {
//			android.Manifest.permission.READ_EXTERNAL_STORAGE,
//			android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//	};
//	DatabaseHandler db;
//	ListView listView;
//	List<DataList> FlightData;
//	List<DataList> TypeData;
//	List<DataList> ExportData;
//	TextView tvTotalTime;
//	String tvTotalString, airplane_type;
//	int totalH, totalMin, logTime, airplane_type_id, ctxPos;
//	private boolean autoExportXLSPref, metarPref;
//	MenuItem metarAction, exelOpenAction, exelImportAction;
//	private String[] mScreenTitles;
//	private DrawerLayout mDrawerLayout;
//	private ListView mDrawerList;
//	private ActionBarDrawerToggle mDrawerToggle;
//	private CharSequence mDrawerTitle;
//	private CharSequence mTitle;
//	private Context context = this;
//	private Intent mMyServiceIntent;
//	// ====================CustomCode_start=====================================
//	Intent fileintent;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.start_activity);
//		getPrefs();
//		db = new DatabaseHandler(this);
//		mMyServiceIntent = new Intent(MainActivity.this, BackgroundIntentService.class);
//		mScreenTitles = getResources().getStringArray(R.array.drawer_items);
//		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//		mDrawerList = (ListView) findViewById(R.id.left_drawer);
//		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mScreenTitles));
//		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
//
//		try {
//			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//			getSupportActionBar().setHomeButtonEnabled(true);
//			mTitle = mDrawerTitle = getTitle();
//			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START); // установка тени к NavDrawer
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		mDrawerToggle = new ActionBarDrawerToggle(
//				this, /* host Activity */
//				mDrawerLayout, /* DrawerLayout object */
//				R.string.drawer_open, /* "open drawer" description */
//				R.string.drawer_close /* "close drawer" description */
//		) {
//
//			/** Called when a drawer has settled in a completely closed state. */
//			public void onDrawerClosed(View view) {
//				getSupportActionBar().setTitle(mTitle);
//				supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//			}
//
//			/** Called when a drawer has settled in a completely open state. */
//			public void onDrawerOpened(View drawerView) {
//				getSupportActionBar().setTitle(mDrawerTitle);
//				supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//			}
//		};
//
//		// Set the drawer toggle as the DrawerListener
//		mDrawerLayout.addDrawerListener(mDrawerToggle);
//
//		// Initialize the first fragment when the application first loads.
//		if (savedInstanceState == null) {
//			selectItem(0);
//		}
//
//	}
//
//	public static boolean verifyStoragePermissions(Activity activity) {
//		// Check if we have write permission
//		int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//		if (permission != PackageManager.PERMISSION_GRANTED) {
//			// We don't have permission so prompt the user
//			ActivityCompat.requestPermissions(
//					activity,
//					PERMISSIONS_STORAGE,
//					REQUEST_EXTERNAL_STORAGE_XLS
//			);
//		}
//
//		return permission == PackageManager.PERMISSION_GRANTED;
//	}
//
//	@Override
//	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//		Log.i(TAG, "onRequestPermissionsResult requestCode = " + requestCode);
//		switch (requestCode) {
//			case REQUEST_EXTERNAL_STORAGE_XLS: {
//				// If request is cancelled, the result arrays are empty.
//				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					// Log.i(TAG, "onRequestPermissionsResult PERMISSION_GRANTED");
//					fileintent = new Intent();
//					fileintent.setAction(Intent.ACTION_GET_CONTENT);
//					fileintent.addCategory(Intent.CATEGORY_OPENABLE);
//					fileintent.setType("*/*");
//					startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
//				} else {
//					Toast.makeText(MainActivity.this, R.string.str_storage_permission_denied, Toast.LENGTH_SHORT).show();
//					// Log.i(TAG, "onRequestPermissionsResult PERMISSION_DENIED");
//					// permission denied, boo! Disable the
//					// functionality that depends on this permission.
//				}
//				return;
//			}
//
//			// other 'case' lines to check for other
//			// permissions this app might request
//		}
//	}
//
//	@Override
//	public void setTitle(CharSequence title) {
//		mTitle = title;
//		try {
//			getSupportActionBar().setTitle(mTitle);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//		// Sync the toggle state after onRestoreInstanceState has occurred.
//		mDrawerToggle.syncState();
//	}
//
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		// Pass any configuration change to the drawer toggles
//		mDrawerToggle.onConfigurationChanged(newConfig);
//	}
//
//	@Override
//	public void onSaveInstanceState(Bundle savedInstanceState) {
//		try {
//			savedInstanceState.putInt(LOGTIME_STATE_KEY, logTime);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		// всегда вызывайте суперкласс для сохранения состояний видов
//		super.onSaveInstanceState(savedInstanceState);
//	}
//
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//		try {
//			logTime = savedInstanceState.getInt(LOGTIME_STATE_KEY);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main_menu, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		Log.i(TAG, "onPrepareOptionsMenu ");
//		metarAction = menu.findItem(R.id.action_metar);
//		exelOpenAction = menu.findItem(R.id.action_open_file);
//		exelImportAction = menu.findItem(R.id.action_import_excel);
//		metarAction.setVisible(metarPref);
//		exelOpenAction.setVisible(isFileExist());
//		exelImportAction.setVisible(isFileExist());
//		return super.onPrepareOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//
//		if (mDrawerToggle.onOptionsItemSelected(item)) {
//			return true;
//		}
//		int id = item.getItemId();
//		switch (id) {
//			case R.id.action_about:
//				Intent intent = new Intent(MainActivity.this, AboutActivity.class);
//				startActivity(intent);
//				break;
//			case R.id.action_settings:
//				Intent intentSettings = new Intent(MainActivity.this, Preferences.class);
//				startActivity(intentSettings);
//				break;
//			case R.id.action_metar:
//				Intent metarActivity = new Intent(MainActivity.this, MetarActivity.class);
//				startActivity(metarActivity);
//				break;
//			case R.id.action_import_excel:
//				showImportAlert();
//				break;
//			case R.id.action_export_excel:
//				showExportAlert();
//				break;
//			case R.id.action_open_file:
//				openFileWith();
//				break;
//			case R.id.action_type_edit:
//				Intent mAirplanesActivity = new Intent(MainActivity.this, AirplaneTypesActivity.class);
//				startActivity(mAirplanesActivity);
//				break;
//			case R.id.action_import_from_file:
//				showImportDialogSD();
//				break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (requestCode) {
//			case PICKFILE_RESULT_CODE:
//				if (resultCode == RESULT_OK) {
//					String FilePath = data.getData().getPath();
//					if (mMyServiceIntent == null){
//						mMyServiceIntent = new Intent(MainActivity.this, BackgroundIntentService.class);
//					}
//					mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD);
//					mMyServiceIntent.putExtra(BackgroundIntentService.EXTRA_KEY_IMPORT_SD_FILENAME, FilePath);
//					startService(mMyServiceIntent);
//				} else {
//					Toast.makeText(MainActivity.this, getString(R.string.str_error_import), Toast.LENGTH_SHORT).show();
//				}
//				break;
//			case SAVE_FILE_RESULT_CODE:
//				if (resultCode == RESULT_OK && data != null && data.getData() != null) {
//					String theFilePath = data.getData().getPath();
//				}
//				break;
//		}
//	}
//
//	@Override
//	public void onBackPressed() {
//		openQuitDialog();
//	}
//
//	private class DrawerItemClickListener implements ListView.OnItemClickListener {
//		@Override
//		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//			selectItem(position);
//		}
//	}
//
//	private void selectItem(int position) {
//		Fragment fragment = getFragment(position);
//		if (fragment != null) {
//			FragmentManager fragmentManager = getSupportFragmentManager();
//			fragmentManager.beginTransaction()
//					.replace(R.id.container, fragment).commit();
//
//			mDrawerList.setItemChecked(position, true);
//			setTitle(mScreenTitles[position]);
//			mDrawerLayout.closeDrawer(mDrawerList);
//			mDrawerToggle.setDrawerIndicatorEnabled(true);
//		} else {
//			// Error
//			Log.e(this.getClass().getName(), "Error. Fragment is not created");
//		}
//	}
//
//
//}
