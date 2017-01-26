package com.arny.flightlogbook.models;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.arny.flightlogbook.views.activities.MainActivity;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class BackgroundLoader extends AsyncTaskLoader<String> {
    private Context context;
    private DatabaseHandler db;
    private List<DataList> TypeData;
    private Bundle mBundle;


    private String  strDesc, strDate, strTime, reg_no, airplane_type,action;
    private long mDateTime;
    private int day_night, ifr_vfr, flight_type, logTime, airplane_type_id ;

    public static final String EXTRA_LOADER_RESULT_SUCCESS = "com.arny.flightlogbook.models.backgroundloader.success";
    public static final String EXTRA_LOADER_INSIDE_ACTION = "com.arny.flightlogbook.models.backgroundloader.action";
    public static final String EXTRA_LOADER_ACTION_SD_IMPORT = "com.arny.flightlogbook.models.backgroundloader.action.sd.import";
    public static final String EXTRA_LOADER_INSIDE_SD_FILENAME = "com.arny.flightlogbook.models.backgroundloader.action.sd.filename";
    private static final String TAG = "LOG_TAG";

    public BackgroundLoader(Context context, Bundle bundle) {
        super(context);
        this.context = context;
        this.mBundle = bundle;
        db = new DatabaseHandler(context);
        if (bundle !=null){
            Log.i(TAG, "BackgroundLoader: bundle = " + bundle.toString());
            action = bundle.getString(EXTRA_LOADER_INSIDE_ACTION);

        }
        Log.i(TAG, "BackgroundLoader: context = " + context);
    }

    @Override
    public String loadInBackground() {
        Log.i(TAG, this.hashCode() + " loadInBackground start");
        if (action!=null){
            Log.i(TAG, "BackgroundLoader: action = " + action);
            switch (action){
                case EXTRA_LOADER_ACTION_SD_IMPORT:
                    String filename = mBundle.getString(EXTRA_LOADER_INSIDE_SD_FILENAME);
                    Log.i(TAG, "loadInBackground: filename = " + filename);
                    break;
            }
        }

        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(TAG, this.hashCode() + " loadInBackground end");
        return EXTRA_LOADER_RESULT_SUCCESS;
    }


    @Override
    public void forceLoad() {
        Log.i(TAG, this.hashCode() + "forceLoad");
        super.forceLoad();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.i(TAG, this.hashCode() + " onStartLoading");
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.i(TAG, this.hashCode() + " onStopLoading");
    }

    @Override
    public void deliverResult(String data) {
        Log.i(TAG, this.hashCode() + " deliverResult");
        super.deliverResult(data);
    }


    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }
    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
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
                Toast.makeText(context, "Ошибка чтения файла", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "getLocalizedMessage= " + e.getMessage());
                return;
            }

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
                        switch (cellCnt) {
                            case 0:
                                try {
                                    strDate = myCell.toString();
                                } catch (Exception e) {
                                    strDate = getCurrentDate();
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "strDate " + strDate);
                                break;
                            case 1:
                                try {
                                    strTime = myCell.toString();
                                } catch (Exception e) {
                                    strTime = "00:00";
                                    e.printStackTrace();
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
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "airplane_type " + airplane_type);
                                break;
                            case 3:
                                try {
                                    reg_no = myCell.toString();
                                } catch (Exception e) {
                                    reg_no = "";
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "reg_no " + reg_no);
                                break;
                            case 4:
                                try {
                                    day_night = (int)Float.parseFloat(myCell.toString());
                                } catch (Exception e) {
                                    day_night = 0;
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "day_night " + day_night);
                                break;
                            case 5:
                                try {
                                    ifr_vfr = (int)Float.parseFloat(myCell.toString());
                                } catch (Exception e) {
                                    ifr_vfr = 0;
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "ifr_vfr " + ifr_vfr);
                                break;
                            case 6:
                                try {
                                    flight_type = (int)Float.parseFloat(myCell.toString());
                                } catch (Exception e) {
                                    flight_type = 0;
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "flight_type " + flight_type);
                                break;
                            case 7:
                                try {
                                    strDesc = myCell.toString();
                                } catch (Exception e) {
                                    strDesc = "";
                                    e.printStackTrace();
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
                                    e.printStackTrace();
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
            e.printStackTrace();
        }
    }//readFile

    public long convertTimeStringToLong(String myTimestamp) {
        Calendar mCalendar = Calendar.getInstance();
        SimpleDateFormat curFormater = new SimpleDateFormat("dd-MMM-yyyy",Locale.getDefault());
        Date dateObj = null;
        try {
            dateObj = curFormater.parse(myTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mCalendar.setTime(dateObj);
        return mCalendar.getTimeInMillis();
    }


    private static int convertStringToTime(String time) {
        int hours = 0;
        int mins = 0;
        String delimeter = (time.contains(":"))? ":":".";
        int posDelim = time.indexOf(delimeter);
        try {
            hours = Integer.parseInt(time.substring(0, posDelim));
            mins = Integer.parseInt(time.substring(posDelim + 1, time.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mins + (hours * 60);
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
            dat = new SimpleDateFormat(strDateFormat,Locale.getDefault()).parse(strM);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        String fDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(dat);
        return d + " " + fDate + " " + y;
    }


}