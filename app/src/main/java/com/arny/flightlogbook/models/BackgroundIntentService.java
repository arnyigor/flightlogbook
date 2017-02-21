package com.arny.flightlogbook.models;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Debug;
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
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BackgroundIntentService extends IntentService {
    /*Extras*/
    public static final String ACTION = "com.arny.flightlogbook.models.BackgroundIntentService";
    public static final String EXTRA_KEY_OPERATION_CODE = "BackgroundIntentService.operation.code";
    public static final String EXTRA_KEY_OPERATION_RESULT = "BackgroundIntentService.operation.result";
    public static final String EXTRA_KEY_FINISH = "BackgroundIntentService.operation.finish";
    public static final String EXTRA_KEY_FINISH_SUCCESS = "BackgroundIntentService.operation.success";
    /*Opearations*/
    public static final int OPERATION_IMPORT_SD = 100;
    public static final int OPERATION_DBX_SYNC = 102;
    public static final String OPERATION_IMPORT_SD_FILENAME = "BackgroundIntentService.operation.import.sd.filename";
    public static final int OPERATION_EXPORT = 101;
    /*other*/
    private static final String TAG = BackgroundIntentService.class.getName();
    private boolean mIsSuccess;
    private boolean mIsStopped;
    private int operation;
    private DbxClientV2 client;
    private File syncFolder;
    private FileMetadata remoteMetadata;
    private Exception mException;

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
        Log.i(BackgroundIntentService.class.getSimpleName(), "onDestroy: ");
        onServiceDestroy();
        super.onDestroy();
    }

    private void onServiceDestroy() {
        Log.i(BackgroundIntentService.class.getSimpleName(), "onServiceDestroy: mIsSuccess = " + mIsSuccess);
        String notice;
        mIsStopped = true;
        if (mIsSuccess) {
            notice = "Операция завершена!";
            switch (operation){
                case OPERATION_IMPORT_SD:
                    notice = "Импорт завершен!";
                    break;
                case OPERATION_DBX_SYNC:
                    notice = "Синхронизация завершена!";
                    break;
            }
        } else {
            notice = "Операция НЕ завершена!";
            switch (operation){
                case OPERATION_IMPORT_SD:
                    notice = "Импорт не завершен!";
                    break;
                case OPERATION_DBX_SYNC:
                    notice = "Синхронизация не завершена!";
                    break;
            }
        }
        sendBroadcastIntent(notice);
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
        operation = intent.getIntExtra(EXTRA_KEY_OPERATION_CODE, 0);
        switch (operation) {
            case OPERATION_IMPORT_SD:
                String FilePath = intent.getStringExtra(OPERATION_IMPORT_SD_FILENAME);
                readExcelFile(getApplicationContext(), null, true, FilePath);
                mIsSuccess = true;
                sendBroadcastIntent(getApplicationContext().getResources().getString(R.string.str_import_success));
                break;
            case OPERATION_DBX_SYNC:
                client = DropboxClientFactory.getClient();
                try {
                    // Get files and folder metadata from Dropbox root directory
                    ListFolderResult result = client.files().listFolder("");
                    Log.i(BackgroundIntentService.class.getSimpleName(), "doInBackground: result = " + String.valueOf(result));
                    while (true) {

                        for (Metadata metadata : result.getEntries()) {
                            Log.i(BackgroundIntentService.class.getSimpleName(), "doInBackground: hasname = " + metadata.getName().compareToIgnoreCase(Functions.EXEL_FILE_NAME));
                            if (metadata.getName().compareToIgnoreCase(Functions.EXEL_FILE_NAME)==0){
                                if (metadata instanceof  FileMetadata){
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
                    syncFile(remoteMetadata);
                } catch (DbxException e) {
                    e.printStackTrace();
                    mIsSuccess = false;
                    onServiceDestroy();
                }
                break;
        }

    }


    private void sendBroadcastIntent(String result) {
        Intent intent = initProadcastIntent();
        intent.putExtra(EXTRA_KEY_FINISH, mIsStopped);
        intent.putExtra(EXTRA_KEY_FINISH_SUCCESS, mIsSuccess);
        intent.putExtra(EXTRA_KEY_OPERATION_CODE, operation);
        intent.putExtra(EXTRA_KEY_OPERATION_RESULT, result);
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
                                    try {
                                        strTime = Functions.match(myCell.getDateCellValue().toString(), "(\\d{2}:\\d{2})", 1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
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
                                    mDateTime = Functions.convertTimeStringToLong(strDate,"dd MMM yyyy");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//readFile


    private void downloadFile(FileMetadata metadata) {
        try {
            File file = new File(syncFolder, Functions.EXEL_FILE_NAME);
            Log.i(DownloadFileTask.class.getSimpleName(), "doInBackground: file = " + String.valueOf(file));
            try {
                OutputStream outputStream = new FileOutputStream(file);
                client.files().download(metadata.getPathLower(), metadata.getRev()).download(outputStream);

                // Tell android about the file
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                getApplicationContext().sendBroadcast(intent);
                Log.i(BackgroundIntentService.class.getSimpleName(), "downloadFile: file = " + file.length());
                if (file.length()>0){
                    mIsSuccess = true;
                    onServiceDestroy();
                }else{
                    mIsSuccess = false;
                    onServiceDestroy();
                }
            } catch (DbxException | IOException e) {
                e.printStackTrace();
                mIsSuccess = false;
                onServiceDestroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mIsSuccess = false;
            onServiceDestroy();
        }

//        new DownloadFileTask(getApplicationContext(), DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
//            @Override
//            public void onDownloadComplete(File result) {
//
//                if (result != null) {
//                    mIsSuccess = true;
//                    onServiceDestroy();
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                e.printStackTrace();
//                Toast.makeText(getApplicationContext(), getString(R.string.dropbox_sync_error), Toast.LENGTH_SHORT).show();
//                mIsSuccess = false;
//                onServiceDestroy();
//            }
//        }, syncFolder).execute(file);

    }

    private void uploadFile() {
        try {
            File localFile = new File(getApplicationContext().getExternalFilesDir(null), Functions.EXEL_FILE_NAME);
            String remoteFileName = localFile.getName();
            InputStream inputStream = new FileInputStream(localFile);
            FileMetadata result =  client.files().uploadBuilder("/" + remoteFileName).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            Log.i(BackgroundIntentService.class.getSimpleName(), "uploadFile: uploadFileMetadata =" + result);
            if (result !=null){
                mIsSuccess = true;
                onServiceDestroy();
            }else{
                mIsSuccess = false;
                onServiceDestroy();
            }
        } catch (DbxException | IOException e) {
            e.printStackTrace();
            mIsSuccess = false;
            onServiceDestroy();
        }
//        new UploadFileTask(getApplicationContext(), DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
//            @Override
//            public void onUploadComplete(FileMetadata result) {
////                String message = result.getName() + " size " + result.getSize() + " modified " +
////                        DateFormat.getDateTimeInstance().format(result.getClientModified());
////                Toast.makeText(context, message, Toast.LENGTH_SHORT) .show();
//                mIsSuccess = true;
//                onServiceDestroy();
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.e(BackgroundIntentService.class.getSimpleName(), "onError: Failed to upload file " + e.getMessage());
//                Toast.makeText(getApplicationContext(), "An error has occurred", Toast.LENGTH_SHORT).show();
//                mIsSuccess = false;
//                onServiceDestroy();
//            }
//        }).execute();
    }

    private void syncFile(final FileMetadata remoteFile) {
        File localFile = new File(getApplicationContext().getExternalFilesDir(null), Functions.EXEL_FILE_NAME);
        syncFolder = getApplicationContext().getExternalFilesDir(null);
        Date localLastModified, remoteLastModified;
        try {
            if (remoteFile ==null){
                Log.i(BackgroundIntentService.class.getSimpleName(), "syncFile: remote is null");
                uploadFile();
                return;
            }
            remoteLastModified = remoteFile.getClientModified();
            localLastModified = new Date(localFile.lastModified());

            Log.i(BackgroundIntentService.class.getSimpleName(), "syncFolder: remoteLastModified = " + Functions.getDateTime(remoteLastModified,null));
            Log.i(BackgroundIntentService.class.getSimpleName(), "syncFolder: localLastModified = " + Functions.getDateTime(localLastModified,null));
            if (remoteLastModified.after(localLastModified) || !localFile.isFile() || localFile.length() == 0) {
                Log.i(BackgroundIntentService.class.getSimpleName(), "syncFile: download");
                downloadFile(remoteFile);
            } else if (remoteLastModified.before(localLastModified) || remoteFile.getSize()==0) {
                Log.i(BackgroundIntentService.class.getSimpleName(), "syncFile: upload");
                uploadFile();
            }else{
                Log.i(BackgroundIntentService.class.getSimpleName(), "syncFile: not requed");
                mIsSuccess = true;
                onServiceDestroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mIsSuccess = false;
            onServiceDestroy();
        }
    }

}
