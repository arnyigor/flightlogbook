package com.arny.flightlogbook.models;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class BackgroundIntentService extends IntentService {
    /*Extras*/
    public static final String ACTION = "com.arny.flightlogbook.models.BackgroundIntentService";
    public static final String EXTRA_KEY_OPERATION_CODE = "BackgroundIntentService.operation.code";
    public static final String EXTRA_KEY_FINISH = "BackgroundIntentService.operation.finish";
    public static final String EXTRA_KEY_FINISH_SUCCESS = "BackgroundIntentService.operation.success";
    /*Opearations*/
    public static final int OPERATION_IMPORT_SD = 100;
    public static final String OPERATION_IMPORT_SD_FILENAME = "BackgroundIntentService.operation.import.sd.filename";
    public static final int OPERATION_EXPORT = 101;
    /*other*/
    private static final String TAG = BackgroundIntentService.class.getName();
    private boolean mIsSuccess;
    private boolean mIsStopped;
    private int operation;

    public BackgroundIntentService() {
        super("BackgroundIntentService");
        mIsSuccess = false;
        mIsStopped = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        String notice;
        mIsStopped = true;
        if (mIsSuccess) {
            notice = "Операция завершена!";
            if (operation == OPERATION_IMPORT_SD) {
                notice = "Импорт завершен!";
            }
        } else {
            notice = "Операция НЕ завершена!";
            if (operation == OPERATION_IMPORT_SD) {
                notice = "Импорт не завершен!";
            }
        }
        sendBroadcastIntent();
        Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @NonNull
    private Intent initProadcastIntent() {
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent: ");
        operation = intent.getIntExtra(EXTRA_KEY_OPERATION_CODE, 0);
        switch (operation) {
            case OPERATION_IMPORT_SD:
                String FilePath = intent.getStringExtra(OPERATION_IMPORT_SD_FILENAME);
                readExcelFile(getApplicationContext(), null, true, FilePath);
                break;
        }
        mIsSuccess = true;
        sendBroadcastIntent();
    }


    private void sendBroadcastIntent() {
        Intent intent = initProadcastIntent();
        intent.putExtra(EXTRA_KEY_FINISH, mIsStopped);
        intent.putExtra(EXTRA_KEY_FINISH_SUCCESS, mIsSuccess);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void readExcelFile(Context context, String filename, boolean fromSystem, String systemPath) {
        DatabaseHandler db = new DatabaseHandler(this);
        boolean hasType = false;
        boolean checked = false;
        String strDate = null,strTime = null,airplane_type = null,reg_no = null,strDesc = null;
        int airplane_type_id = 0,day_night = 0,ifr_vfr = 0,flight_type = 0,logTime = 0;
        long mDateTime = 0;
        List<DataList> TypeData;
        File xlsfile;
        if (!Functions.isExternalStorageAvailable() || Functions.isExternalStorageReadOnly()) {
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
                                    strDate = Functions.getDateTime(0,"dd MMM yyyy");
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
                                    logTime = Functions.convertStringToTime(strTime);
                                    mDateTime = Functions.convertTimeStringToLong(strDate,"dd-MMM-yyyy");
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
                                    sendBroadcastIntent();
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



}
