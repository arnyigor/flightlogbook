package com.arny.flightlogbook;

//imports start==========

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//imports end==========

//==============Activity start=========================
public class AddEditActivity extends Activity {
	static final int DIALOG_DATE = 101;
	private static final String TAG = "LOG_TAG";
	// =============Variables start================
	List<DataList> FlightData;
	List<DataList> ListType;
	Context context = this;
	DatabaseHandler db;
	Calendar mCalendar;
	LinearLayout motoCont;
	View.OnClickListener editMotoBtn;
	String strDesc, strDate, strTime, strMonth,airplane_type,reg_no;
	int mYear, mMonth, mDay, day_night, ifr_vfr, flight_type, logTime, logHours, logMinutes,airplane_type_id;
	long mDateTime, mTypeID;
	int mRowId;
	Float mMotoStart,mMotoFinish,mMotoResult;
	EditText edtDesc, edtDate, edtTime, edtRegNo,edtMotoStart,edtMotoFinish;
	Button btnAddEdtItem,btnAddAirplaneTypes;
	Spinner spinDayNight, spinVfrIfr, spinFlightType;
	TextView tvAirplaneType,tvMotoResult;
	Boolean motoCheckPref;
	List<String> typeList;
	boolean editable = false;

	// ==============Forms variables end==============
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_item);
		// ================Forms Ids start=========================
		getPrefs();
		db = new DatabaseHandler(this);
		edtDesc = (EditText) findViewById(R.id.edtDesc);
		motoCont = (LinearLayout) findViewById(R.id.motoContainer);
		edtDate = (EditText) findViewById(R.id.edtDate);
		edtTime = (EditText) findViewById(R.id.edtTime);
		edtRegNo = (EditText) findViewById(R.id.edtRegNo);
		btnAddEdtItem = (Button) findViewById(R.id.btnAddEdtItem);
		btnAddAirplaneTypes = (Button) findViewById(R.id.btnAddAirplaneTypes);
		spinDayNight = (Spinner) findViewById(R.id.spinDayNight);
		spinVfrIfr = (Spinner) findViewById(R.id.spinVfrIfr);
		spinFlightType = (Spinner) findViewById(R.id.spinFlightType);
		tvAirplaneType = (TextView) findViewById(R.id.tvAirplaneType);
		// ================Forms Ids end=========================
		// ==================onCreateCode start=========================
		mCalendar = Calendar.getInstance();
		mYear = mCalendar.get(Calendar.YEAR);
		mMonth = mCalendar.get(Calendar.MONTH);
		mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

		db = new DatabaseHandler(this);
		Bundle extras = getIntent().getExtras();
		try {
			Log.i(TAG, "onCreate getIntExtra = " + extras.getInt(DatabaseHandler.COLUMN_ID));
			mRowId = extras.getInt(DatabaseHandler.COLUMN_ID);
			editable = mRowId != 0;
		} catch (Exception e) {
			Log.i(TAG, "onCreate Exception getStringExtra " + e.toString());
		}

		if (editable) {
			btnAddEdtItem.setText(getString(R.string.str_edt));
			setTitle(getString(R.string.str_edt));
		}else{
			btnAddEdtItem.setText(getString(R.string.str_add));
			setTitle(getString(R.string.str_add));
		}

		edtTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (b) {
					edtTime.setText("");
				}
				if (!b){
					correctLogTime();
				}
			}
		});

		edtTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				edtTime.setText("");
			}
		});

		edtTime.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int i, KeyEvent keyEvent) {
				if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
					correctLogTime();
					return true;
				}
				return false;
			}
		});

		btnAddEdtItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (saveState()) {
					finish();
				}
			}
		});

		edtDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(DIALOG_DATE);
			}
		});

		spinDayNight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent,
			                           View itemSelected, int selectedItemPosition, long selectedId) {
				day_night = selectedItemPosition;
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		spinVfrIfr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent,
			                           View itemSelected, int selectedItemPosition, long selectedId) {
				ifr_vfr = selectedItemPosition;
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		btnAddAirplaneTypes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AddAirplaneTypes();
			}
		});

		tvAirplaneType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				filltypes();
				showAirplaneTypes();
			}
		});

		editMotoBtn = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoto();
			}
		};

		if (motoCheckPref) {
			showMotoBtn();
		}
		// ==================onCreateCode end=========================
	}// ============onCreate end====================

	private void filltypes() {
		Log.i(TAG, "--------------------filltypes_start-------------------");
		try {
			typeList = new ArrayList<String>();
			ListType = db.getTypeList();
			for (DataList type : ListType) {
				Log.i(TAG, "idColType: " + type.getAirplanetypetitle());
				typeList.add(type.getAirplanetypetitle());
			}
		} catch (SQLException e) {
			Log.i(TAG, "SQLException: " + e.toString());
		}
		Log.i(TAG, "--------------------filltypes_end-------------------");
	}

	private void showAirplaneTypes() {
		CharSequence[] cs = typeList.toArray(new CharSequence[typeList.size()]);
		Log.i(TAG, "cs " + Arrays.toString(cs));
		AlertDialog.Builder typesBuilder = new AlertDialog.Builder(this);
		typesBuilder.setTitle(getString(R.string.str_type));
		typesBuilder.setItems(cs, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				airplane_type = typeList.get(item);
				Log.i(TAG, "airplane_type:" + airplane_type);
				ListType = db.getTypeItem(item + 1);//нумерация списка с нуля,в базе с 1цы
				for (DataList type : ListType) {
					Log.i(TAG, "getAirplanetypeid: " + type.getAirplanetypeid());
					airplane_type_id = type.getAirplanetypeid();
				}
				tvAirplaneType.setText(getString(R.string.str_type) + " " + typeList.get(item));
			}
		});
		typesBuilder.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog alert = typesBuilder.create();
		alert.show();
	}

	TextWatcher motoStart = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			try {
				mMotoStart= Float.parseFloat(edtMotoStart.getText().toString());
				mMotoFinish= Float.parseFloat(edtMotoFinish.getText().toString());
				mMotoResult = getMotoTime(mMotoStart,mMotoFinish);
				tvMotoResult.setText(String.valueOf(mMotoResult));
			} catch (Exception e) {
				Log.i(TAG, "onTextChanged Exception = "+e.toString());
			}
		}
	};

	TextWatcher motoFinish = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			try {
				mMotoStart= Float.parseFloat(edtMotoStart.getText().toString());
				mMotoFinish= Float.parseFloat(edtMotoFinish.getText().toString());
				mMotoResult = getMotoTime(mMotoStart,mMotoFinish);
				tvMotoResult.setText(String.valueOf(mMotoResult));
			} catch (Exception e) {
				Log.i(TAG, "onTextChanged Exception = "+e.toString());
			}

		}
	};

	public void getPrefs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		motoCheckPref = prefs.getBoolean("motoCheckPref", false);
	}

	private float getMotoTime(float start, float finish) {
		float mMoto = finish - start;
		if (mMoto < 0) {
			return 0;
		}
		mMoto = Float.parseFloat(String.valueOf(roundUp(mMoto,3)));
		return mMoto;
	}

	private int setLogTimefromMoto(float motoTime) {
		return (int) (motoTime * 60);
	}

	public BigDecimal roundUp(float value, int digits){
		return new BigDecimal(""+value).setScale(digits, BigDecimal.ROUND_HALF_UP);
	}

	private void showMotoBtn() {
		LinearLayout.LayoutParams lButtonParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,  LinearLayout.LayoutParams.WRAP_CONTENT);
		Button btn =  new Button(this);
		btn.setLayoutParams(lButtonParams);
		btn.setId(R.id.motoBtn);
		btn.setOnClickListener(editMotoBtn);
		btn.setText(getString(R.string.str_moto_btn));
		motoCont.addView(btn);
	}

	private void showMoto() {
		LayoutInflater li = LayoutInflater.from(AddEditActivity.this);
		View xmlView = li.inflate(R.layout.moto, null);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddEditActivity.this);
		alertDialog.setView(xmlView);
		edtMotoStart = (EditText) xmlView.findViewById(R.id.edtStartMoto);
		edtMotoFinish = (EditText) xmlView.findViewById(R.id.edtFinishMoto);
		tvMotoResult = (TextView) xmlView.findViewById(R.id.tvMotoresult);
		edtMotoStart.addTextChangedListener(motoStart);
		edtMotoFinish.addTextChangedListener(motoFinish);
		alertDialog.setTitle(getString(R.string.str_moto));
		alertDialog.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				logTime = setLogTimefromMoto(mMotoResult);
				logHours = logTime / 60;
				logMinutes = logTime % 60;
				Log.i(TAG, "onClick logtime = "+logTime);
				Log.i(TAG, "onClick logHours = "+logHours);
				Log.i(TAG, "onClick logMinutes = "+logMinutes);
				edtTime.setText(pad(logHours)+":"+pad(logMinutes));
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int Year, int monthOfYear,
							  int dayOfMonth) {
			mYear = Year;
			String strDateFormat = "MMM";
			strMonth = new DateFormatSymbols().getMonths()[monthOfYear];
			Date date = null;
			try {
				date = new SimpleDateFormat(strDateFormat).parse(strMonth);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			String formDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(date);
			mDay = dayOfMonth;
			strDate = mDay + " " + formDate + " " + mYear;
			edtDate.setText(strDate);
			mDateTime = convertTimeStringToLong(strDate);
		}
	};

	public String strLogTime(int logtime){
		int totalH = logtime / 60;
		int totalMin = logtime % 60;
		return pad(totalH) + ":" + pad(totalMin);
	}

	private void correctLogTime(){
		try {
			String inputLogtime = edtTime.getText().toString();
			Log.i(TAG, "correctLogTime inputLogtime.length = "+inputLogtime.length());
			if (inputLogtime.length() == 0){
				edtTime.setText("00:00");
				logTime = 0;
			}else if (inputLogtime.length() == 1){
				logTime = Integer.parseInt(edtTime.getText().toString());
				edtTime.setText("00:0"+logTime);
			} else if (inputLogtime.length() == 2) {
				logMinutes = Integer.parseInt(edtTime.getText().toString());
				logTime = Integer.parseInt(edtTime.getText().toString());
				if (logMinutes > 59) {
					logHours = 1;
					logMinutes = logMinutes-60;
				}
				edtTime.setText(pad(logHours)+":"+pad(logMinutes));
			} else if (inputLogtime.length() > 2) {
				logMinutes = Integer.parseInt(edtTime.getText().toString().substring(inputLogtime.length()-2,inputLogtime.length()));
				logHours = Integer.parseInt(edtTime.getText().toString().substring(0,inputLogtime.length()-2));
				Log.i(TAG, "correctLogTime logMinutes = "+logMinutes);
				Log.i(TAG, "correctLogTime logHours = "+logHours);
				if (logMinutes > 59) {
					logHours = logHours+1;
					logMinutes = logMinutes-60;
				}
				logTime = logHours * 60 + logMinutes;
				edtTime.setText(pad(logHours)+":"+pad(logMinutes));
			}
		} catch (Exception e) {
			Log.i(TAG, "correctLogTime Exception = "+e.toString());
		}
	}

	private void AddAirplaneTypes() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		LinearLayout layout = new LinearLayout(this);
		final EditText edtTypeInput = new EditText(this);
		final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		edtTypeInput.setLayoutParams(lparams);
		alert.setTitle(getString(R.string.str_add_airplane_types));
		layout.addView(edtTypeInput);
		alert.setView(layout);
		alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				airplane_type = edtTypeInput.getText().toString();
				if (airplane_type.equals("")) {
					Toast.makeText(getBaseContext(), getString(R.string.str_error_add_airplane_types), Toast.LENGTH_SHORT).show();
				} else {
					mTypeID = db.addType(airplane_type);
				}
			}
		});
		alert.show();
	}

	private void fillInputs() {
		Log.i("LOG_TAG", "-------------fillInputs---------");
		if (mRowId != 0) {
			try {
				FlightData = db.getFlightItem(mRowId);
				for (DataList aFlightData : FlightData) {
					strDesc = aFlightData.getDescription();
					edtDesc.setText(strDesc);
					mDateTime = aFlightData.getDatetime();
					strDate = getDate(aFlightData.getDatetime());
					edtDate.setText(strDate);
					logTime = aFlightData.getLogtime();
					strTime = getTime(aFlightData.getDatetime());
					edtTime.setText(strTime);
					reg_no = aFlightData.getReg_no();
					edtRegNo.setText(reg_no);
					Log.i(TAG, "fillInputs  aFlightData.getAirplanetypeid() = " +  aFlightData.getAirplanetypeid());
					Log.i(TAG, "fillInputs  aFlightData.getAirplanetype() = " +  aFlightData.getAirplanetype());
					Log.i(TAG, "fillInputs  aFlightData.getAirplanetypetitle() = " +  aFlightData.getAirplanetypetitle());
					airplane_type_id = aFlightData.getAirplanetypeid();
					ListType = db.getTypeItem(airplane_type_id);
					for (DataList type : ListType) {
						airplane_type = type.getAirplanetypetitle();
					}
					edtTime.setText(strLogTime(logTime));
					tvAirplaneType.setText(getString(R.string.str_type) + airplane_type);
					day_night = aFlightData.getDaynight();
					spinDayNight.setSelection(day_night);
					ifr_vfr = aFlightData.getIfrvfr();
					spinVfrIfr.setSelection(ifr_vfr);
					flight_type = aFlightData.getFlighttype();
					spinFlightType.setSelection(flight_type);
				}
			} catch (Exception e) {
				Log.i("LOG_TAG", e.toString());
			}
		} else {
			airplane_type_id = 0;
			edtDesc.setText("");
			edtDate.setText("");
			strDesc = "";
			strDate = "";
			reg_no = "";
			airplane_type = "";
			mDateTime = convertTimeStringToLong(getCurrentDate());
			logTime = 0;
			day_night = 0;
			ifr_vfr = 0;
			flight_type = 0;
			tvAirplaneType.setText(getString(R.string.str_type_empty));
			spinDayNight.setSelection(day_night);
			spinVfrIfr.setSelection(ifr_vfr);
			spinFlightType.setSelection(flight_type);
		}// row!=0
		//filltypes();
		Log.i("LOG_TAG", "mRowId: " + String.valueOf(mRowId));
		Log.i("LOG_TAG", "strDesc: " + String.valueOf(strDesc));
		Log.i("LOG_TAG", "strDate: " + String.valueOf(strDate));
		Log.i("LOG_TAG", "reg_no: " + String.valueOf(reg_no));
		Log.i("LOG_TAG", "airplane_type: " + String.valueOf(airplane_type));
		Log.i("LOG_TAG", "airplane_type_id: " + String.valueOf(airplane_type_id));
		Log.i("LOG_TAG", "mDateTime: " + String.valueOf(mDateTime));
		Log.i("LOG_TAG", "logTime: " + String.valueOf(logTime));
		Log.i("LOG_TAG", "day_night: " + String.valueOf(day_night));
		Log.i("LOG_TAG", "ifr_vfr: " + String.valueOf(ifr_vfr));
		Log.i("LOG_TAG", "flight_type: " + String.valueOf(flight_type));
		Log.i("LOG_TAG", "-------------fillInputs end---------");
	}// fillinputs

	private boolean saveState() {
		String strlogTime = edtTime.getText().toString();
		Log.i(TAG, "saveState " );
		if (!strlogTime.contains(":")) {
			correctLogTime();
		}
		if (logTime != 0) {
				strDesc = edtDesc.getText().toString();
				if (strDesc.equals("")) {
					strDesc = getString(R.string.str_empty_title);
				}
				strDate = edtDate.getText().toString();
				if (strDate.equals("")) {
					strDate = getCurrentDate();
				}
				strTime = edtTime.getText().toString();
				reg_no = edtRegNo.getText().toString();
				day_night = (int) spinDayNight.getSelectedItemId();
				ifr_vfr = (int) spinVfrIfr.getSelectedItemId();
				flight_type = (int) spinFlightType.getSelectedItemId();
				if (mRowId == 0) {
					long res = db.addFlight(mDateTime,logTime,reg_no,airplane_type_id,day_night,ifr_vfr,flight_type,strDesc);
					return res>0;
				} else {
					return db.updateFlight(mDateTime,logTime,reg_no,airplane_type_id,day_night,ifr_vfr,flight_type,strDesc,(int)mRowId);//тут тоже делаем сужение типа,странно,что для вставки нужен int,а выдает long
				}
		} else {
			edtTime.setText("");
			Toast.makeText(AddEditActivity.this, R.string.str_enter_logtime, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private String getCurrentDate() {
		Calendar cal = Calendar.getInstance();
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

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_DATE:
				return new DatePickerDialog(this, datePickerListener, mYear,
						mMonth, mDay);
		}
		return null;
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

	public static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("LOG_TAG", "onResume");
		Log.i("LOG_TAG", "mRowId: " + mRowId);
		fillInputs();
	}
	// ====================OnClicks end======================================
}// ===================Activity end==================================
// ===================SimpleActivity==================================