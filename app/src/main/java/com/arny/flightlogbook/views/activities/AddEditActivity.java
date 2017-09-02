package com.arny.flightlogbook.views.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.arny.arnylib.interfaces.InputDialogListener;
import com.arny.arnylib.utils.*;
import com.arny.flightlogbook.BuildConfig;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.common.Functions;
import com.arny.flightlogbook.common.Local;
import com.arny.flightlogbook.models.Flight;
import com.arny.flightlogbook.models.Type;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AddEditActivity extends AppCompatActivity implements CalendarDatePickerDialogFragment.OnDateSetListener {
    private static final String LOGTIME_STATE_KEY = "com.arny.flightlogbook.extra.instance.time";
    private static final String FRAG_TAG_DATE_PICKER = "fragment_date_picker_name";
    private LinearLayout motoCont;
    private String strDesc, strDate, strTime, airplane_type, reg_no;
    private int day_night, ifr_vfr, flight_type, logTime, logHours, logMinutes, airplane_type_id;
    private long mDateTime, mTypeID;
    private int mRowId;
    private float mMotoStart, mMotoFinish, mMotoResult;
    private TextInputLayout regNoLayout;
    private EditText edtDesc, edtTime, edtRegNo, edtMotoStart, edtMotoFinish;
    private Button btnAddEdtItem;
    private ImageButton btnAddAirplaneTypes;
    private Spinner spinDayNight, spinVfrIfr, spinFlightType;
    private TextView tvAirplaneType, tvMotoResult;
    private TextView edtDate;
	private List<String> typeList = new ArrayList<>();
	private boolean editable = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorText));
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.str_edt);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        edtDesc = (TextInputEditText) findViewById(R.id.edtDesc);
        motoCont = (LinearLayout) findViewById(R.id.motoContainer);
        ImageView dateTimeChoose = (ImageView) findViewById(R.id.iv_date);
        edtDate = (TextView) findViewById(R.id.edtDate);
        dateTimeChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(AddEditActivity.this);
                cdp.show(getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
            }
        });
        edtTime = (EditText) findViewById(R.id.edtTime);
        regNoLayout = (TextInputLayout) findViewById(R.id.edtRegNoLayout);
        edtRegNo = (EditText) regNoLayout.findViewById(R.id.edtRegNo);
        edtRegNo.requestFocus();
        btnAddEdtItem = (Button) findViewById(R.id.btnAddEdtItem);
        btnAddAirplaneTypes = (ImageButton) findViewById(R.id.btnAddAirplaneTypes);
        spinDayNight = (Spinner) findViewById(R.id.spinDayNight);
        spinVfrIfr = (Spinner) findViewById(R.id.spinVfrIfr);
        spinFlightType = (Spinner) findViewById(R.id.spinFlightType);
        tvAirplaneType = (TextView) findViewById(R.id.tvAirplaneType);
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mRowId = extras.getInt(Local.COLUMN_ID);
                editable = mRowId != 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (editable) {
            btnAddEdtItem.setText(getString(R.string.str_edt));
            setTitle(getString(R.string.str_edt));
        } else {
            btnAddEdtItem.setText(getString(R.string.str_add));
            setTitle(getString(R.string.str_add));
        }

        edtTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean inside) {
                if (inside) {
                    edtTime.setText("");
                }
                if (!inside) {
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
                if (typeList.size() > 0) {
                    showAirplaneTypes();
                } else {
                    Toast.makeText(AddEditActivity.this, R.string.str_no_types, Toast.LENGTH_SHORT).show();
                }

            }
        });

        if (Config.getBoolean("motoCheckPref",false,AddEditActivity.this)) {
            showMotoBtn();
        }
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        fillInputs();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logTime = savedInstanceState.getInt(LOGTIME_STATE_KEY);
        fillEdtTime(logTime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_type_edit:
                Intent AirplanesActivity = new Intent(AddEditActivity.this, AirplaneTypesActivity.class);
                startActivity(AirplanesActivity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filltypes() {
        try {
            List<Type> types = Local.getTypeList(AddEditActivity.this);
            for (Type type : types) {
                typeList.add(type.getTypeName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAirplaneTypes() {
        CharSequence[] cs = typeList.toArray(new CharSequence[typeList.size()]);
        AlertDialog.Builder typesBuilder = new AlertDialog.Builder(this);
        typesBuilder.setTitle(getString(R.string.str_type));
        typesBuilder.setItems(cs, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                airplane_type = typeList.get(item);
                Type type = Local.getTypeItem(item + 1, AddEditActivity.this);//нумерация списка с нуля,в базе с 1цы
                if (type != null) {
                    airplane_type_id = type.getTypeId();
                }
                tvAirplaneType.setText(String.format("%s %s", getString(R.string.str_type), typeList.get(item)));
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
                mMotoStart = Float.parseFloat(edtMotoStart.getText().toString());
                mMotoFinish = Float.parseFloat(edtMotoFinish.getText().toString());
                mMotoResult = getMotoTime(mMotoStart, mMotoFinish);
                tvMotoResult.setText(Functions.strLogTime(setLogTimefromMoto(mMotoResult)));
            } catch (Exception e) {
                e.printStackTrace();
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
                mMotoStart = Float.parseFloat(edtMotoStart.getText().toString());
                mMotoFinish = Float.parseFloat(edtMotoFinish.getText().toString());
                mMotoResult = getMotoTime(mMotoStart, mMotoFinish);
                tvMotoResult.setText(Functions.strLogTime(setLogTimefromMoto(mMotoResult)));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private float getMotoTime(float start, float finish) {
        float mMoto = finish - start;
        if (mMoto < 0) {
            return 0;
        }
        mMoto = Float.parseFloat(String.valueOf(roundUp(mMoto, 3)));
        return mMoto;
    }

    private int setLogTimefromMoto(float motoTime) {
        return (int) (motoTime * 60);
    }

    public BigDecimal roundUp(float value, int digits) {
        return new BigDecimal("" + value).setScale(digits, BigDecimal.ROUND_HALF_UP);
    }

    private void showMotoBtn() {
        LinearLayout.LayoutParams lButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Button btn = new Button(this);
        btn.setLayoutParams(lButtonParams);
        btn.setId(R.id.motoBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoto();
            }
        });
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
        alertDialog.setCancelable(false).setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                logTime = setLogTimefromMoto(mMotoResult);
                logHours = logTime / 60;
                logMinutes = logTime % 60;
                edtTime.setText(String.format("%s:%s", Functions.pad(logHours), Functions.pad(logMinutes)));
            }
        }).setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


    @SuppressLint("DefaultLocale")
    private void correctLogTime() {
        try {
            String inputLogtime = edtTime.getText().toString();
            if (inputLogtime.length() == 0) {
                if (logTime != 0) {
                    edtTime.setText(Functions.strLogTime(logTime));
                } else {
                    edtTime.setText("00:00");
                    logTime = 0;
                }
            } else if (inputLogtime.length() == 1) {
                logTime = Integer.parseInt(edtTime.getText().toString());
                edtTime.setText(String.format("00:0%d", logTime));
            } else if (inputLogtime.length() == 2) {
                logMinutes = Integer.parseInt(edtTime.getText().toString());
                logTime = Integer.parseInt(edtTime.getText().toString());
                if (logMinutes > 59) {
                    logHours = 1;
                    logMinutes = logMinutes - 60;
                }
                edtTime.setText(String.format("%s:%s", Functions.pad(logHours), Functions.pad(logMinutes)));
            } else if (inputLogtime.length() > 2) {
                if (inputLogtime.contains(":")) {
                    logMinutes = Integer.parseInt(edtTime.getText().toString().substring(inputLogtime.length() - 2, inputLogtime.length()));
                    logHours = Integer.parseInt(edtTime.getText().toString().substring(0, inputLogtime.length() - 3));
                } else {
                    logMinutes = Integer.parseInt(edtTime.getText().toString().substring(inputLogtime.length() - 2, inputLogtime.length()));
                    logHours = Integer.parseInt(edtTime.getText().toString().substring(0, inputLogtime.length() - 2));
                }
                if (logMinutes > 59) {
                    logHours = logHours + 1;
                    logMinutes = logMinutes - 60;
                }
                logTime = logHours * 60 + logMinutes;
                edtTime.setText(String.format("%s:%s", Functions.pad(logHours), Functions.pad(logMinutes)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillEdtTime(int time) {
        Log.d(AddEditActivity.class.getSimpleName(), "fillEdtTime logTime = " + time);
        try {
            if (time != 0) {
                edtTime.setText(Functions.strLogTime(time));
            } else {
                edtTime.setText("00:00");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void AddAirplaneTypes() {
        DroidUtils.simpleInputDialog(AddEditActivity.this, getString(R.string.str_add_airplane_types), getString(R.string.str_ok), getString(R.string.str_cancel), InputType.TYPE_CLASS_TEXT, new InputDialogListener() {
            @Override
            public void onConfirm(String content) {
                airplane_type = content;
                mTypeID = Local.addType(airplane_type, AddEditActivity.this);
                fillInputs();
            }

            @Override
            public void onError(String error) {
                ToastMaker.toastError(AddEditActivity.this,error);
            }
        });
    }

    private void fillInputs() {
        if (mRowId != 0) {
            Flight flight = Local.getFlightItem(mRowId, AddEditActivity.this);
            strDesc = flight.getDescription();
            edtDesc.setText(strDesc);
            mDateTime = flight.getDatetime();
            strDate = Functions.getDateTime(flight.getDatetime(), "dd MMM yyyy");
            edtDate.setText(strDate);
            logTime = flight.getLogtime();
            strTime = Functions.getDateTime(flight.getDatetime(), "hh:mm");
            edtTime.setText(strTime);
            reg_no = flight.getReg_no();
            edtRegNo.setText(reg_no);
            edtTime.setText(Functions.strLogTime(logTime));
            airplane_type_id = flight.getAirplanetypeid();
            Type typeItem = Local.getTypeItem(airplane_type_id, AddEditActivity.this);
            String airplType = typeItem != null ? typeItem.getTypeName() : null;
            String airTypesText = Utility.empty(airplType) ? getString(R.string.str_type_empty) : getString(R.string.str_type) + " " + airplType;
            tvAirplaneType.setText(airTypesText);
            day_night = flight.getDaynight();
            spinDayNight.setSelection(day_night);
            ifr_vfr = flight.getIfrvfr();
            spinVfrIfr.setSelection(ifr_vfr);
            flight_type = flight.getFlighttype();
            spinFlightType.setSelection(flight_type);
        } else {
            edtDesc.setText("");
            edtDate.setText("");
            strDesc = "";
            strDate = "";
            reg_no = "";
            airplane_type = "";
            mDateTime = Functions.convertTimeStringToLong(DateTimeUtils.getDateTime( "dd MM yyyy"), "dd MM yyyy");
            logTime = 0;
            day_night = 0;
            ifr_vfr = 0;
            flight_type = 0;
            filltypes();
            if (typeList.size() > 0) {
                airplane_type_id = 1;
                tvAirplaneType.setText(String.format("%s %s", getString(R.string.str_type), typeList.get(0)));
            } else {
                airplane_type_id = 0;
                tvAirplaneType.setText(getString(R.string.str_no_types));
            }
            spinDayNight.setSelection(day_night);
            spinVfrIfr.setSelection(ifr_vfr);
            spinFlightType.setSelection(flight_type);
        }// row!=0
        //filltypes();
        Log.d(AddEditActivity.class.getSimpleName(), "mRowId: " + String.valueOf(mRowId));
        Log.d(AddEditActivity.class.getSimpleName(), "strDesc: " + String.valueOf(strDesc));
        Log.d(AddEditActivity.class.getSimpleName(), "strDate: " + String.valueOf(strDate));
        Log.d(AddEditActivity.class.getSimpleName(), "reg_no: " + String.valueOf(reg_no));
        Log.d(AddEditActivity.class.getSimpleName(), "airplane_type: " + String.valueOf(airplane_type));
        Log.d(AddEditActivity.class.getSimpleName(), "airplane_type_id: " + String.valueOf(airplane_type_id));
        Log.d(AddEditActivity.class.getSimpleName(), "mDateTime: " + String.valueOf(mDateTime));
        Log.d(AddEditActivity.class.getSimpleName(), "logTime: " + String.valueOf(logTime));
        Log.d(AddEditActivity.class.getSimpleName(), "day_night: " + String.valueOf(day_night));
        Log.d(AddEditActivity.class.getSimpleName(), "ifr_vfr: " + String.valueOf(ifr_vfr));
        Log.d(AddEditActivity.class.getSimpleName(), "flight_type: " + String.valueOf(flight_type));
        Log.d(AddEditActivity.class.getSimpleName(), "-------------fillInputs end---------");
    }// fillinputs

    private boolean saveState() {
        String strlogTime = edtTime.getText().toString();
        Log.d(AddEditActivity.class.getSimpleName(), "saveState ");
        if (!strlogTime.contains(":")) {
            correctLogTime();
        }
        strDesc = edtDesc.getText().toString();
        strDate = edtDate.getText().toString();
        if (strDate.equals("")) {
            strDate = DateTimeUtils.getDateTime( "dd MM yyyy");
        }
        strTime = edtTime.getText().toString();
        reg_no = edtRegNo.getText().toString();
        day_night = (int) spinDayNight.getSelectedItemId();
        ifr_vfr = (int) spinVfrIfr.getSelectedItemId();
        flight_type = (int) spinFlightType.getSelectedItemId();
        if (mRowId == 0) {
            long res = Local.addFlight(mDateTime, logTime, reg_no, airplane_type_id, day_night, ifr_vfr, flight_type, strDesc, AddEditActivity.this);
            return res > 0;
        } else {
            return Local.updateFlight(mDateTime, logTime, reg_no, airplane_type_id, day_night, ifr_vfr, flight_type, strDesc, (int) mRowId, AddEditActivity.this);//тут тоже делаем сужение типа,странно,что для вставки нужен int,а выдает long
        }
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        strDate = dayOfMonth + " " + (monthOfYear+1) + " " + year;
        mDateTime = Functions.convertTimeStringToLong(strDate, "dd MM yyyy");
        edtDate.setText(DateTimeUtils.getDateTime(mDateTime,"dd MMM yyyy"));
    }
}