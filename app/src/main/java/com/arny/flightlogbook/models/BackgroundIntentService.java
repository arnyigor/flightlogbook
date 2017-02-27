package com.arny.flightlogbook.models;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.arny.flightlogbook.BuildConfig;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.views.fragments.DropboxSyncFragment;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class BackgroundIntentService extends IntentService {
    /*Extras*/
    public static final String ACTION = "com.arny.flightlogbook.models.BackgroundIntentService";
    public static final String EXTRA_KEY_OPERATION_CODE = "BackgroundIntentService.operation.code";
    public static final String EXTRA_KEY_OPERATION_RESULT = "BackgroundIntentService.operation.result";
    public static final String EXTRA_KEY_FINISH = "BackgroundIntentService.operation.finish";
    public static final String EXTRA_KEY_FINISH_SUCCESS = "BackgroundIntentService.operation.success";
    public static final String EXTRA_KEY_IMPORT_SD_FILENAME = "BackgroundIntentService.operation.import.sd.filename";
    public static final String EXTRA_KEY_OPERATION_DATA = "BackgroundIntentService.operation.data";
    public static final String EXTRA_KEY_OPERATION_DATA_REMOTE_DATE = "BackgroundIntentService.operation.data.remote.date";
    public static final String EXTRA_KEY_OPERATION_DATA_LOCAL_DATE = "BackgroundIntentService.operation.data.local.date";
    /*Opearations*/
    public static final int OPERATION_IMPORT_SD = 100;
    public static final int OPERATION_DBX_SYNC = 102;
    public static final int OPERATION_EXPORT = 101;
    public static final int OPERATION_DBX_DOWNLOAD = 103;
    public static final int OPERATION_DBX_UPLOAD = 104;
    /*other*/
    private static final String LOG_SHEET_MAIN = "Timelog";
    private static final String TAG = BackgroundIntentService.class.getName();
    private static int operation;
    private List<DataList> ExportData,TypeData;
    private boolean mIsSuccess;
    private boolean mIsStopped;
    private int airplane_type_id;
    private DbxClientV2 client;
    private File syncFolder;
    private FileMetadata remoteMetadata;
    private HashMap<String, String> hashMap;

    public static int getOperation(){
        return operation;
    }

    private void setOperation(int operation){
        BackgroundIntentService.operation = operation;
    }

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
        onServiceDestroy();
        super.onDestroy();
    }

    private void onServiceDestroy() {
        mIsStopped = true;
        sendBroadcastIntent(getResultNotif());
    }

    private String getResultNotif() {
        String notice;
        if (mIsSuccess) {
            notice = "Операция завершена!";
            switch (operation){
                case OPERATION_IMPORT_SD:
                    notice = "Импорт завершен!";
                    break;
                case OPERATION_DBX_SYNC:
                    notice = "Синхронизация завершена!";
                    break;
                case OPERATION_DBX_DOWNLOAD:
                    notice = "Загрузка файла завершена!";
                    break;
                case OPERATION_DBX_UPLOAD:
                    notice = "Выгрузка файла завершена!";
                    break;
                case OPERATION_EXPORT:
                    notice = "Экспорт завершен!";
                    break;
            }
        } else {
            notice = "Операция не завершена!";
            switch (operation){
                case OPERATION_IMPORT_SD:
                    notice = "Импорт не завершен!";
                    break;
                case OPERATION_DBX_SYNC:
                    notice = "Синхронизация не завершена!";
                    break;
                case OPERATION_DBX_DOWNLOAD:
                    notice = "Загрузка файла не завершена!";
                    break;
                case OPERATION_DBX_UPLOAD:
                    notice = "Выгрузка файла не завершена!";
                    break;
                case OPERATION_EXPORT:
                    notice = "Экспорт не завершен!";
                    break;
            }
        }
        return notice;
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
        hashMap = new HashMap<>();
        setOperation(intent.getIntExtra(EXTRA_KEY_OPERATION_CODE, 0));
        switch (getOperation()) {
            case OPERATION_IMPORT_SD:
                String mPath = intent.getStringExtra(EXTRA_KEY_IMPORT_SD_FILENAME);
                if (Functions.empty(mPath)){
                    readExcelFile(getApplicationContext(), Functions.EXEL_FILE_NAME, true);
                }else{
                    Uri uri = Uri.fromFile(new File(mPath));
                    readExcelFile(getApplicationContext(),Functions.getSDFilePath(getApplicationContext(),uri), false);
                }
                break;
            case OPERATION_DBX_SYNC:
                try {
                    client = DropboxClientFactory.getClient();
                    if (client !=null){
                        getRemoteMetaData();
                        syncFile(remoteMetadata);
                    }else{
                        mIsSuccess = false;
                    }
                } catch (DbxException e) {
                    e.printStackTrace();
                    mIsSuccess = false;
                }
                break;
            case OPERATION_DBX_DOWNLOAD:
                try {
                    client = DropboxClientFactory.getClient();
                    if (client !=null){
                        getRemoteMetaData();
                        downloadFile(remoteMetadata);
                    }else{
                        mIsSuccess = false;
                    }
                } catch (DbxException e) {
                    e.printStackTrace();
                    mIsSuccess = false;
                }
                break;
            case OPERATION_DBX_UPLOAD:
                try {
                    client = DropboxClientFactory.getClient();
                    if (client !=null){
                        uploadFile();
                    }else{
                        mIsSuccess = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mIsSuccess = false;
                }

                break;
            case OPERATION_EXPORT:
                mIsSuccess = saveExcelFile(getApplicationContext(), Functions.EXEL_FILE_NAME);
                break;
        }

    }

    private void getRemoteMetaData() throws DbxException {
        ListFolderResult result = client.files().listFolder("");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                if (metadata.getName().compareToIgnoreCase(Functions.EXEL_FILE_NAME)==0){
                    if (metadata instanceof FileMetadata){
                        remoteMetadata = (FileMetadata)metadata;
                        break;
                    }
                }
            }
            if (!result.getHasMore()) {
                break;
            }
            result = client.files().listFolderContinue(result.getCursor());
        }
    }

    private void sendBroadcastIntent(String result) {
        Intent intent = initProadcastIntent();
        intent.putExtra(EXTRA_KEY_FINISH, mIsStopped);
        intent.putExtra(EXTRA_KEY_FINISH_SUCCESS, mIsSuccess);
        intent.putExtra(EXTRA_KEY_OPERATION_CODE, operation);
        intent.putExtra(EXTRA_KEY_OPERATION_RESULT, result);
        intent.putExtra(EXTRA_KEY_OPERATION_DATA, hashMap);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean saveExcelFile(Context context, String fileName) {
        DatabaseHandler db = new DatabaseHandler(this);
        Row row;
        if (!Functions.isExternalStorageAvailable() || Functions.isExternalStorageReadOnly()) {
//            Toasty.error(context, getString(R.string.storage_not_avalable), Toast.LENGTH_LONG).show();
//            Toast.makeText(context, getString(R.string.storage_not_avalable), Toast.LENGTH_LONG).show();
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
            airplane_type_id = export.getAirplanetypeid();
            TypeData = db.getTypeItem(airplane_type_id);
            String airplane_type = "";
            for (DataList type : TypeData) {
                airplane_type = type.getAirplanetypetitle();
            }

            row = sheet_main.createRow(rows);
            c = row.createCell(0);
            c.setCellValue(Functions.getDateTime(export.getDatetime(),"dd MMM yyyy"));
            c = row.createCell(1);
            c.setCellValue(Functions.strLogTime(export.getLogtime()));
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
//            Toasty.success(context, getString(R.string.str_file_saved) + " " + file, Toast.LENGTH_SHORT).show();
//            Toast.makeText(context, getString(R.string.str_file_saved) + " " + file, Toast.LENGTH_SHORT).show();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    private void readExcelFile(Context context, String filename, boolean fromSystem) {
        DatabaseHandler db = new DatabaseHandler(this);
        boolean hasType = false;
        boolean checked = false;
        HSSFWorkbook myWorkBook;
        String strDate = null,strTime = null,airplane_type = null,reg_no = null,strDesc;
        int airplane_type_id = 0,day_night = 0,ifr_vfr = 0,flight_type = 0,logTime = 0;
        long mDateTime = 0;
        List<DataList> TypeData;
        File xlsfile;
        if (!Functions.isExternalStorageAvailable() || Functions.isExternalStorageReadOnly()) {
            return;
        }
        try {
            if (fromSystem) {
                xlsfile = new File(context.getExternalFilesDir(null), Functions.EXEL_FILE_NAME);
            } else {
                xlsfile = new File("", filename);
            }
            try {
                FileInputStream fileInputStream = new FileInputStream(xlsfile);
                myWorkBook = new HSSFWorkbook(fileInputStream);
            } catch (IOException e) {
                e.printStackTrace();
                mIsSuccess = false;
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
                                    if (myCell.getCellType()==Cell.CELL_TYPE_NUMERIC){
                                        strTime = Functions.match(myCell.getDateCellValue().toString(), "(\\d{2}:\\d{2})", 1);
                                    }else{
                                        strTime = myCell.toString();
                                    }
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
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String format = "dd MMM yyyy";
                                try {
                                    format = Functions.dateFormatChooser(strDate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mDateTime = Functions.convertTimeStringToLong(strDate,format);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
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
                                    sendBroadcastIntent(null);
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
            mIsSuccess = true;
           
        } catch (Exception e) {
            e.printStackTrace();
            mIsSuccess = false;
           
        }
    }//readFile

    private void downloadFile(FileMetadata metadata) {
        try {
            syncFolder = getApplicationContext().getExternalFilesDir(null);
            File file = new File(syncFolder, Functions.EXEL_FILE_NAME);
            try {
                OutputStream outputStream = new FileOutputStream(file);
                client.files().download(metadata.getPathLower(), metadata.getRev()).download(outputStream);
                // Tell android about the file
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                getApplicationContext().sendBroadcast(intent);
                mIsSuccess = file.length() > 0;
            } catch (DbxException | IOException e) {
                e.printStackTrace();
                mIsSuccess = false;
               
            }
        } catch (Exception e) {
            e.printStackTrace();
            mIsSuccess = false;
           
        }
    }

    private void uploadFile() {
        try {
            File localFile = new File(getApplicationContext().getExternalFilesDir(null), Functions.EXEL_FILE_NAME);
            String remoteFileName = localFile.getName();
            InputStream inputStream = new FileInputStream(localFile);
            FileMetadata result =  client.files().uploadBuilder("/" + remoteFileName).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            mIsSuccess = result != null;
        } catch (DbxException | IOException e) {
            e.printStackTrace();
            mIsSuccess = false;
        }
    }

    private void syncFile(FileMetadata remoteFile) {
        File localFile = new File(getApplicationContext().getExternalFilesDir(null), Functions.EXEL_FILE_NAME);
        String remoteVal, localVal;
        try {
            if (remoteFile == null) {
                remoteVal = null;
            } else {
                remoteVal = Functions.getDateTime(remoteFile.getServerModified(), "dd MMM yyyy HH:mm:ss");
            }
            if (localFile.length() == 0) {
                localVal = null;
            } else {
                localVal = Functions.getDateTime(new Date(localFile.lastModified()), "dd MMM yyyy HH:mm:ss");
            }
            hashMap.put(EXTRA_KEY_OPERATION_DATA_REMOTE_DATE, remoteVal);
            hashMap.put(EXTRA_KEY_OPERATION_DATA_LOCAL_DATE, localVal);
            mIsSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            mIsSuccess = false;
           
        }
    }

}
