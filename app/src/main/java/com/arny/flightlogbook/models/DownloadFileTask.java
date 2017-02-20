package com.arny.flightlogbook.models;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
public class DownloadFileTask extends AsyncTask<FileMetadata, Void, File> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private File mPath;

    public interface Callback {
        void onDownloadComplete(File result);

        void onError(Exception e);
    }

    public DownloadFileTask(Context context, DbxClientV2 dbxClient, Callback callback,File path) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
        mPath = path;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {
//            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            File path = mPath;
            File file = new File(mPath, Functions.EXEL_FILE_NAME);
            Log.i(DownloadFileTask.class.getSimpleName(), "doInBackground: file = " + String.valueOf(file));
//            // Make sure the Downloads directory exists.
//            if (!path.exists()) {
//                if (!path.mkdirs()) {
//                    mException = new RuntimeException("Unable to create directory: " + path);
//                }
//            } else if (!path.isDirectory()) {
//                mException = new IllegalStateException("Download path is not a directory: " + path);
//                return null;
//            }

            // Download the file.
            try {
                OutputStream outputStream = new FileOutputStream(file);
                mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
                        .download(outputStream);

                // Tell android about the file
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                mContext.sendBroadcast(intent);

                return file;
            } catch (DbxException | IOException e) {
                mException = e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}