package com.arny.flightlogbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
	private static final String LOGTIME_STATE_KEY = "LOGTIME_STATE_KEY";
	private static final int CODE_READ = 42;
	private static final int CM_DELETE_ID = 102;
	private static final int CM_EDIT_ID = 103;
	private static final String TAG = "LOG_TAG";
	private static final String EXEL_FILE_NAME = "PilotLogBook.xls";
	private static final String LOG_SHEET_MAIN = "Timelog";
	private static final int PICKFILE_RESULT_CODE = 1;
	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 110;
	private static final int SAVE_FILE_RESULT_CODE = 111;
	private static String[] PERMISSIONS_STORAGE = {
			android.Manifest.permission.READ_EXTERNAL_STORAGE,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE
	};
	// =============Variables start================
	Context context = this;
	DatabaseHandler db;
	ListView listView;
	List<DataList> FlightData;
	List<DataList> TypeData;
	List<DataList> ExportData;
	LinearLayout linL;
	TextView tvTotalTime;
	String tvTotalString, strDesc, strDate, strTOTime, strLandTime, strTime, reg_no, airplane_type;
	long mDateTime;
	int totalH, totalMin, day_night, ifr_vfr, flight_type, logTime, TOTime, LandTime, LandHour, LandMinute, btngravity, airplane_type_id, ctxPos;
	boolean autoExportXLSPref, metarPref;
	MenuItem metarAction, exelOpenAction, exelImportAction;

	// ====================CustomCode_start=====================================
	Intent fileintent;

	public static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	public static boolean verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}

		return permission == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		Log.i(TAG, "onRequestPermissionsResult requestCode = " + requestCode);
		switch (requestCode) {
			case REQUEST_EXTERNAL_STORAGE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Log.i(TAG, "onRequestPermissionsResult PERMISSION_GRANTED");
					fileintent = new Intent();
					fileintent.setAction(Intent.ACTION_GET_CONTENT);
					fileintent.addCategory(Intent.CATEGORY_OPENABLE);
					fileintent.setType("*/*");
					startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
				} else {
					Toast.makeText(MainActivity.this, R.string.str_storage_permission_denied, Toast.LENGTH_SHORT).show();
					// Log.i(TAG, "onRequestPermissionsResult PERMISSION_DENIED");
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	public static boolean isExternalStorageReadOnly() {
		String extStorageState = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
	}

	public static boolean isExternalStorageAvailable() {
		String extStorageState = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(extStorageState);
	}

	public static int convertStringToTime(String time) {
		int posDelim = time.indexOf(":");
		int hours = Integer.parseInt(time.substring(0, posDelim));
		int mins = Integer.parseInt(time.substring(posDelim + 1, time.length()));
		return mins + (hours * 60);
	}

	// =============Variables end================
	// ====================onCreate start=========================
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getPrefs();
		tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
		linL = (LinearLayout) findViewById(R.id.listlayout);
		listView = (ListView) findViewById(R.id.listView);
		registerForContextMenu(listView);
		db = new DatabaseHandler(this);
		// ==================onCreateCode start=========================

		FloatingActionButton fabButton = new
				FloatingActionButton.Builder(this).withDrawable(
				getResources().getDrawable(R.drawable.ic_add_white_24dp))
				.withButtonColor(getResources().getColor(R.color.colorFab)).withGravity(Gravity.TOP | Gravity.RIGHT).withMargins(0, 0, 0, 0).create();
		fabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new
						Intent(MainActivity.this, AddEditActivity.class);
				startActivity(intent);
			}
		});
		// ==================onCreateCode end=========================
		displayTotalTime();

	}// ============onCreate end====================

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			savedInstanceState.putInt(LOGTIME_STATE_KEY, logTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// всегда вызывайте суперкласс для сохранения состояний видов
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);
		try {
			logTime = savedInstanceState.getInt(LOGTIME_STATE_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i(TAG, "onPrepareOptionsMenu ");
		Log.i(TAG, "onPrepareOptionsMenu isFileExist" + isFileExist());
		metarAction = menu.findItem(R.id.action_metar);
		exelOpenAction = menu.findItem(R.id.action_open_file);
		exelImportAction = menu.findItem(R.id.action_import_excel);
		metarAction.setVisible(metarPref);
		exelOpenAction.setVisible(isFileExist());
		exelImportAction.setVisible(isFileExist());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_about:
				Intent intent = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(intent);
				break;
			case R.id.action_settings:
				Intent intentSettings = new Intent(MainActivity.this, Preferences.class);
				startActivity(intentSettings);
				break;
			case R.id.action_metar:
				Intent metarActivity = new Intent(MainActivity.this, MetarActivity.class);
				startActivity(metarActivity);
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
				Intent AirplanesActivity = new Intent(MainActivity.this, AirplaneTypesActivity.class);
				startActivity(AirplanesActivity);
				break;
			case R.id.action_import_from_file:
				showImportDialogSD();
				break;
					/*case R.id.action_dropbox_sync:
						Intent intentDropbox = new Intent(MainActivity.this, DropboxActivity.class);
						startActivity(intentDropbox);
						break;*/
		}
		return super.onOptionsItemSelected(item);
	}

	private void showImportDialogSD() {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(getString(R.string.str_import_attention));
		alert.setMessage(getString(R.string.str_import_massage));
		alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					boolean mlolipop = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
					boolean permissionGranded = false;
					if (mlolipop){
						permissionGranded = verifyStoragePermissions(MainActivity.this);// Do something for lollipop and above versions
					}
					if (permissionGranded || !mlolipop){
						fileintent = new Intent();
						fileintent.setAction(Intent.ACTION_GET_CONTENT);
						fileintent.addCategory(Intent.CATEGORY_OPENABLE);
						fileintent.setType("*/*");
						startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, getString(R.string.str_error_import), Toast.LENGTH_SHORT).show();
				}

			}
		});
		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case PICKFILE_RESULT_CODE:
				if (resultCode == RESULT_OK) {
					String FilePath = data.getData().getPath();
					readExcelFile(getBaseContext(), EXEL_FILE_NAME, true, FilePath);
					FlightData = db.getFlightListByDate();
					listView.setAdapter(new ViewAdapter());
					displayTotalTime();
					Toast.makeText(MainActivity.this, getString(R.string.str_import_success), Toast.LENGTH_SHORT).show();
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

	private void displayTotalTime() {
		int tmpLogTime = 0;
		tmpLogTime = db.getFlightsTime();
		totalH = tmpLogTime / 60;
		totalMin = tmpLogTime % 60;
		tvTotalString = getString(R.string.str_totaltime) + " " + pad(totalH) + ":" + pad(totalMin);
		tvTotalTime.setText(tvTotalString);
	}

	public String strLogTime(int logtime) {
		int h = logtime / 60;
		int m = logtime % 60;
		return pad(h) + ":" + pad(m);
	}

	public void getPrefs() {
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
//		btnPosPref = prefs.getString("btnPosPref", "br");
		autoExportXLSPref = prefs.getBoolean("autoExportXLSPref", false);
		metarPref = prefs.getBoolean("metarCheckPref", false);
		Log.i(TAG, "getPrefs autoExportXLSPref = " + autoExportXLSPref);
	}

	public boolean isFileExist() {
		if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
			Log.e(TAG, "Storage not available or read only");
			return false;
		}
		File sdPath = Environment.getExternalStorageDirectory();
		File file = new File(sdPath + "/Android/data/com.arny.flightlogbook/files", EXEL_FILE_NAME);
		Log.i(TAG, "FilePath file:" + file);
		Log.i(TAG, "isFileExist " + String.valueOf(file.exists() && file.isFile()));
		return file.exists() && file.isFile();
	}

	public void openFileWith() {
		try {
			Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
			File sdPath = Environment.getExternalStorageDirectory();
			File file = new File(sdPath + "/Android/data/com.arny.flightlogbook/files", EXEL_FILE_NAME);
			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			myIntent.setDataAndType(Uri.fromFile(file), mimetype);
			startActivity(myIntent);
		} catch (Exception e) {
			Log.i(TAG, "openFileWith Exception" + e.toString());
		}
	}

	void saveToFile(File aFile) {
		Uri theUri = Uri.fromFile(aFile).buildUpon().scheme("file.new").build();
		Intent theIntent = new Intent(Intent.ACTION_PICK);
		theIntent.setData(theUri);
		theIntent.putExtra(Intent.EXTRA_TITLE, "A Custom Title"); //optional
		theIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); //optional
		try {
			startActivityForResult(theIntent, SAVE_FILE_RESULT_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean saveExcelFile(Context context, String fileName) {
		Log.i(TAG, "saveExcelFile ");
		Row row;
		if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
			Log.e(TAG, "Storage not available or read only");
			return false;
		}
		boolean success = false;

		//New Workbook
		Workbook wb = new HSSFWorkbook();

		//Cell style for header row
		CellStyle cs = wb.createCellStyle();
		cs.setFillForegroundColor(HSSFColor.LIME.index);
		cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		Cell c;
		//New Sheet
		Sheet sheet_main = wb.createSheet(LOG_SHEET_MAIN);
		//create base row
		row = sheet_main.createRow(0);
		c = row.createCell(0);
		c.setCellValue(getString(R.string.str_date));
		c = row.createCell(1);
		c.setCellValue(getString(R.string.str_itemlogtime));
		c = row.createCell(2);
		c.setCellValue(getString(R.string.str_type_null));
		c = row.createCell(3);
		c.setCellValue(getString(R.string.str_regnum));
		c = row.createCell(4);
		c.setCellValue(getString(R.string.str_day_night));
		c = row.createCell(5);
		c.setCellValue(getString(R.string.str_vfr_ifr));
		c = row.createCell(6);
		c.setCellValue(getString(R.string.str_flight_type));
		c = row.createCell(7);
		c.setCellValue(getString(R.string.str_desc));

		ExportData = db.getFlightListByDate();
		int rows = 1;
		for (DataList export : ExportData) {
			Log.i(TAG, "ID: " + export.getId());
			Log.i(TAG, "DATE: " + getDate(export.getDatetime()));
			Log.i(TAG, "STR_TIME: " + getTime(export.getDatetime()));
			Log.i(TAG, "DATETIME: " + export.getDatetime());
			Log.i(TAG, "LOG_TIME: " + strLogTime(export.getLogtime()));
			Log.i(TAG, "REG_NO: " + export.getReg_no());
			airplane_type_id = export.getAirplanetypeid();
			TypeData = db.getTypeItem(airplane_type_id);
			for (DataList type : TypeData) {
				airplane_type = type.getAirplanetypetitle();
			}
			Log.i(TAG, "AIRPLANE_TYPE: " + airplane_type);
			Log.i(TAG, "DAY_NIGHT: " + export.getDaynight());
			Log.i(TAG, "IFR_VFR: " + export.getIfrvfr());
			Log.i(TAG, "FLIGHT_TYPE: " + export.getFlighttype());
			Log.i(TAG, "DESCRIPTION: " + export.getDescription());

			row = sheet_main.createRow(rows);
			c = row.createCell(0);
			c.setCellValue(getDate(export.getDatetime()));
			c = row.createCell(1);
			c.setCellValue(strLogTime(export.getLogtime()));
			c = row.createCell(2);
			c.setCellValue(airplane_type);
			c = row.createCell(3);
			c.setCellValue(export.getReg_no());
			c = row.createCell(4);
			c.setCellValue(export.getDaynight());
			c = row.createCell(5);
			c.setCellValue(export.getIfrvfr());
			c = row.createCell(6);
			c.setCellValue(export.getFlighttype());
			c = row.createCell(7);
			c.setCellValue(export.getDescription());
			rows++;
		}

		sheet_main.setColumnWidth(0, (15 * 200));
		sheet_main.setColumnWidth(1, (15 * 150));
		sheet_main.setColumnWidth(2, (15 * 150));
		sheet_main.setColumnWidth(3, (15 * 150));
		sheet_main.setColumnWidth(4, (15 * 250));
		sheet_main.setColumnWidth(5, (15 * 300));
		sheet_main.setColumnWidth(6, (15 * 200));
		sheet_main.setColumnWidth(7, (15 * 500));

		// Create a path where we will place our List of objects on external storage
		File file = new File(context.getExternalFilesDir(null), fileName);
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(file);
			wb.write(os);
			Log.i(TAG, "Writing file" + file);
			Toast.makeText(MainActivity.this, getString(R.string.str_file_saved) + " " + file, Toast.LENGTH_SHORT).show();
			success = true;
		} catch (IOException e) {
			Log.i(TAG, "Error writing " + file, e);
		} catch (Exception e) {
			Log.i(TAG, "Failed to save file", e);
		} finally {
			try {
				if (null != os)
					os.close();
			} catch (Exception ex) {
				Log.i(TAG, "Exception " + ex.toString());
			}
		}
		return success;
	}

	private void readExcelFile(Context context, String filename, boolean fromSystem, String systemPath) {
		boolean hasType = false;
		boolean checked = false;
		File xlsfile = null;
		if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
			Log.i(TAG, "Storage not available or read only");
			return;
		}

		try {
			if (fromSystem) {
				xlsfile = new File("", systemPath);
			} else {
				xlsfile = new File(context.getExternalFilesDir(null), filename);
			}
			Log.i(TAG, "readExcelFile xlsfile = " + xlsfile);
			InputStream myInput = null;
			HSSFWorkbook myWorkBook = null;
			try {
				myInput = new FileInputStream(xlsfile);
				myWorkBook = new HSSFWorkbook(myInput);
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(MainActivity.this, "Ошибка чтения файла", Toast.LENGTH_SHORT).show();
				Log.i(TAG, "getLocalizedMessage= " + e.getMessage());
				return;
			}
			// Creating Input Stream

			/*FileInputStream myInput = new FileInputStream(file);

			// Create a POIFSFileSystem object
			POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

			// Create a workbook using the File System
			HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);*/

			// Get the first sheet from workbook
			HSSFSheet mySheet = myWorkBook.getSheetAt(0);
			/** We now need something to iterate through the cells.**/
			Iterator rowIter = mySheet.rowIterator();
			int rowCnt = 0;
			//databaseHandler.deleteItems();
			db.removeAllFlights();
			while (rowIter.hasNext()) {
				HSSFRow myRow = (HSSFRow) rowIter.next();
				Iterator cellIter = myRow.cellIterator();
				Log.i(TAG, "rowIter " + rowCnt);
				int cellCnt = 0;
				while (cellIter.hasNext()) {
					HSSFCell myCell = (HSSFCell) cellIter.next();
					Log.i(TAG, "Cell: " + cellCnt);
					Log.i(TAG, "Cell Value: " + myCell.toString());
					if (rowCnt > 0) {
						Log.i(TAG, "Cell: " + cellCnt);
						switch (cellCnt) {
							case 0:
								try {
									strDate = myCell.toString();
								} catch (Exception e) {
									strDate = getCurrentDate();
									Log.i(TAG, "readExcelFile Exception strDate " + e.toString());
								}
								Log.i(TAG, "strDate " + strDate);
								break;
							case 1:
								try {
									strTime = myCell.toString();
								} catch (Exception e) {
									strTime = "00:00";
									Log.i(TAG, "readExcelFile Exception strTime" + e.toString());
								}
								Log.i(TAG, "strTime " + strTime);
								break;
							case 2:
								try {
									airplane_type = myCell.toString();
									TypeData = db.getTypeList();
									for (DataList types : TypeData) {
										hasType = airplane_type.equals(types.getAirplanetypetitle());
										if (hasType) {
											airplane_type_id = types.getAirplanetypeid();
										}
									}
									Log.i(TAG, "hasType " + hasType);
									if (hasType) {
										checked = true;
									} else {
										Log.i(TAG, "checked " + checked);
										if (!checked) {
											db.addType(airplane_type);
										}
									}
								} catch (Exception e) {
									airplane_type = "";
									Log.i(TAG, "readExcelFile Exception airplane_type" + e.toString());
								}
								Log.i(TAG, "airplane_type " + airplane_type);
								break;
							case 3:
								try {
									reg_no = myCell.toString();
								} catch (Exception e) {
									reg_no = "";
									Log.i(TAG, "readExcelFile Exception reg_no" + e.toString());
								}
								Log.i(TAG, "reg_no " + reg_no);
								break;
							case 4:
								try {
									day_night = Integer.parseInt(myCell.toString());
								} catch (Exception e) {
									day_night = 0;
									Log.i(TAG, "readExcelFile Exception day_night " + e.toString());
								}
								Log.i(TAG, "day_night " + day_night);
								break;
							case 5:
								try {
									ifr_vfr = Integer.parseInt(myCell.toString());
								} catch (Exception e) {
									ifr_vfr = 0;
									Log.i(TAG, "readExcelFile Exception ifr_vfr " + e.toString());
								}
								Log.i(TAG, "ifr_vfr " + ifr_vfr);
								break;
							case 6:
								try {
									flight_type = Integer.parseInt(myCell.toString());
								} catch (Exception e) {
									flight_type = 0;
									Log.i(TAG, "readExcelFile Exception flight_type " + e.toString());
								}
								Log.i(TAG, "flight_type " + flight_type);
								break;
							case 7:
								try {
									strDesc = myCell.toString();
								} catch (Exception e) {
									strDesc = "";
									Log.i(TAG, "readExcelFile Exception strDesc" + e.toString());
								}
								Log.i(TAG, "strDesc " + strDesc);
								try {
									logTime = convertStringToTime(strTime);
									mDateTime = convertTimeStringToLong(strDate);
									Log.i(TAG, "strDesc: " + strDesc);
									Log.i(TAG, "strDate: " + strDate);
									Log.i(TAG, "mDateTime: " + mDateTime);
									Log.i(TAG, "strTime: " + strTime);
									Log.i(TAG, "logTime: " + logTime);
									Log.i(TAG, "reg_no: " + reg_no);
									Log.i(TAG, "airplane_type_id: " + airplane_type_id);
									Log.i(TAG, "airplane_type: " + airplane_type);
									Log.i(TAG, "day_night: " + day_night);
									Log.i(TAG, "ifr_vfr: " + ifr_vfr);
									Log.i(TAG, "flight_type: " + flight_type);
									db.addFlight(mDateTime, logTime, reg_no, airplane_type_id, day_night, ifr_vfr, flight_type, strDesc);
								} catch (Exception e) {
									Log.i(TAG, "readExcelFile Exception" + e.toString());
								}
								break;
						}//switch (cellCnt)
					}//if (rowCnt>0)
					cellCnt++;
				}//cellIter.hasNext()
				rowCnt++;
			}//while (rowIter.hasNext())
			//cursorExport.requery();
		} catch (Exception e) {
			Log.i(TAG, "readExcelFile Exception = " + e.toString());
		}
	}//readFile

	private void showExportAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.str_export_attention));
		alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveExcelFile(getBaseContext(), EXEL_FILE_NAME);
				Toast.makeText(MainActivity.this, getString(R.string.str_export_success), Toast.LENGTH_SHORT).show();
			}
		});
		alert.show();
	}

	private void showImportAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.str_import_attention));
		alert.setMessage(getString(R.string.str_import_massage));
		alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				readExcelFile(getBaseContext(), EXEL_FILE_NAME, false, null);
				FlightData = db.getFlightListByDate();
				listView.setAdapter(new ViewAdapter());
				displayTotalTime();
				Toast.makeText(MainActivity.this, getString(R.string.str_import_success), Toast.LENGTH_SHORT).show();
			}
		});
		alert.show();
	}

	private String getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);
		Log.i(TAG, "getCurrentDate y " + y);
		int m = cal.get(Calendar.MONTH);
		Log.i(TAG, "getCurrentDate m " + m);
		int d = cal.get(Calendar.DAY_OF_MONTH);
		Log.i(TAG, "getCurrentDate d " + d);
		String strDateFormat = "MMM";
		String strM = new DateFormatSymbols().getMonths()[m];
		Date dat = null;
		try {
			dat = new SimpleDateFormat(strDateFormat).parse(strM);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		String fDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(dat);
		return d + " " + fDate + " " + y;
	}

	private String getDate(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH);
		int d = cal.get(Calendar.DAY_OF_MONTH);
		String strDateFormat = "MMM";
		String strM = new DateFormatSymbols().getMonths()[m];
		Date dat = null;
		try {
			dat = new SimpleDateFormat(strDateFormat).parse(strM);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		String fDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(dat);
		return d + " " + fDate + " " + y;
	}

	private String getTime(long timestamp) {
		Date d = new Date(timestamp);
		SimpleDateFormat format = new SimpleDateFormat("hh:mm");
		return format.format(d);
	}

	public long convertTimeStringToLong(String myTimestamp) {
		Calendar mCalendar = Calendar.getInstance();
		SimpleDateFormat curFormater = new SimpleDateFormat("dd MMM yyyy");
		Date dateObj = null;
		try {
			dateObj = curFormater.parse(myTimestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		mCalendar.setTime(dateObj);
		return mCalendar.getTimeInMillis();
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CM_EDIT_ID, 0, R.string.str_edt);
		menu.add(0, CM_DELETE_ID, 0, R.string.str_delete);
	}

	public boolean onContextItemSelected(MenuItem item) {
		Log.i(TAG, "onContextItemSelected ");
		final AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int id = item.getItemId();
		switch (id) {
			case CM_DELETE_ID:
				AlertDialog.Builder delDialog = new AlertDialog.Builder(
						MainActivity.this);
				delDialog.setTitle(getString(R.string.str_delete) + "?");
				delDialog
						.setNegativeButton(getString(R.string.str_cancel), null);
				delDialog.setPositiveButton(getString(R.string.str_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								db.removeFlight(FlightData.get(acmi.position).getId());
								FlightData = db.getFlightListByDate();
								listView.setAdapter(new ViewAdapter());
								displayTotalTime();
							}
						});
				AlertDialog alert = delDialog.create();
				alert.show();
				break;
			case CM_EDIT_ID:
				try {
					Intent intent = new Intent(this, AddEditActivity.class);
					intent.putExtra(DatabaseHandler.COLUMN_ID, FlightData.get(acmi.position).getId());
					startActivity(intent);
				} catch (Exception e) {
					Log.i("LOG_TAG error EDIT", e.toString());
				}
				break;
		}
		return super.onContextItemSelected(item);
	}

	private void showMenuDialog(int pos) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		String contextMenuText[] = {getString(R.string.str_edt), getString(R.string.str_delete),getString(R.string.str_clearall)};
		ctxPos = pos;//кидаем в глобальную переменную чтобы все видели
		alert.setItems(contextMenuText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						try {
							Intent intent = new Intent(getApplicationContext(), AddEditActivity.class);
							intent.putExtra(DatabaseHandler.COLUMN_ID, FlightData.get(ctxPos).getId());
							startActivity(intent);
						} catch (Exception e) {
							Log.i("LOG_TAG error EDIT", e.toString());
						}
						break;
					case 1:
						AlertDialog.Builder delDialog = new AlertDialog.Builder(
								MainActivity.this);
						delDialog.setTitle(getString(R.string.str_delete) + "?");
						delDialog
								.setNegativeButton(getString(R.string.str_cancel), null);
						delDialog.setPositiveButton(getString(R.string.str_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										db.removeFlight(FlightData.get(ctxPos).getId());
										FlightData = db.getFlightListByDate();
										listView.setAdapter(new ViewAdapter());
										displayTotalTime();
									}
								});
						AlertDialog alert = delDialog.create();
						alert.show();
						break;
					case 2:
						AlertDialog.Builder delallDialog = new AlertDialog.Builder(
								MainActivity.this);
						delallDialog.setTitle(getString(R.string.str_clearall) + "?");
						delallDialog
								.setNegativeButton(getString(R.string.str_cancel), null);
						delallDialog.setPositiveButton(getString(R.string.str_ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										db.removeAllFlights();
										FlightData = db.getFlightListByDate();
										listView.setAdapter(new ViewAdapter());
										displayTotalTime();
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

	protected void onResume() {
		super.onResume();
		Log.i("LOG_TAG", "----------onResume-------------");
		FlightData = db.getFlightListByDate();
		listView.setAdapter(new ViewAdapter());
		displayTotalTime();
	}

	protected void onPause() {
		super.onPause();
		Log.i("LOG_TAG", "----------onPause-------------");
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.i("LOG_TAG", "----------onDestroy-------------");
	}

	@Override
	public void onBackPressed() {
		openQuitDialog();
	}

	private void openQuitDialog() {
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(
				MainActivity.this);
		quitDialog.setTitle(getString(R.string.str_exit));
		quitDialog.setNegativeButton(getString(R.string.str_cancel), null);
		quitDialog.setPositiveButton(getString(R.string.str_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						getPrefs();
						if (autoExportXLSPref) {
							saveExcelFile(getBaseContext(), EXEL_FILE_NAME);
						}
						finish();
					}
				});
		AlertDialog alert = quitDialog.create();
		alert.show();
	}

	public class ViewAdapter extends BaseAdapter {

		LayoutInflater mInflater;

		public ViewAdapter() {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return db.getFlightCount();
		}

		@Override
		public Object getItem(int position) {
			return db.getFlightItem(FlightData.get(position).getId());
		}

		@Override
		public long getItemId(int position) {
			return FlightData.get(position).getId();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item, null);
			}
			((TextView) convertView.findViewById(R.id.tvDate)).setText(getDate(FlightData.get(position).getDatetime()));
			((TextView) convertView.findViewById(R.id.tvLogTime)).setText(strLogTime(FlightData.get(position).getLogtime()));

			airplane_type_id = FlightData.get(position).getAirplanetypeid();
			TypeData = db.getTypeItem(airplane_type_id);
			for (DataList type : TypeData) {
				airplane_type = type.getAirplanetypetitle();
			}
			((TextView) convertView.findViewById(R.id.tvType)).setText(airplane_type);
			((TextView) convertView.findViewById(R.id.tvRegNo)).setText(String.valueOf(FlightData.get(position).getReg_no()));
			((TextView) convertView.findViewById(R.id.tvDesc)).setText(FlightData.get(position).getDescription());

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showMenuDialog(position);
				}
			});
			/*convertView.setOnClickListener(new View.OnClickListener() {
		        @Override
                public void onClick(View view) {
					final Dialog dialog = new Dialog(context);
					dialog.setContentView(R.layout.add_item);
					dialog.setTitle("Update Data in Database");
					final EditText flightTime = (EditText) dialog.findViewById(R.id.edtTime);
					List<DataList> dbl =(db.getFlightItem(FlightData.get(position).getId()));
					for (DataList item : dbl) {
						flightTime.setText(strLogTime(item.getLogtime()));
					}
					Button Add = (Button) dialog.findViewById(R.id.btnAddEdtItem);
					Add.setText("Update");
					Add.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							db.updateType(flightTime.getText().toString(),FlightData.get(position).getId());
							FlightData = db.getTypeList();
							listView.setAdapter(new ViewAdapter());
							dialog.dismiss();
						}
					});
					dialog.show();

                }
            });*/
			return convertView;
		}
	}

}
