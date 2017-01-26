package com.arny.flightlogbook.views.activities;

import android.support.v7.app.AppCompatActivity;
import com.arny.flightlogbook.R;
import com.dropbox.sync.android.DbxAccountManager;

import android.content.Context;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class DropboxActivity extends AppCompatActivity {
    private static final String TAG = "LOG_TAG";
    private DbxAccountManager mDbxAcctMgr;
    private static final String DROPBOX_APP_KEY = "rzzuhol6y1aibdx";
    private static final String DROPBOX_APP_SECRET = "pmc8mcnz1gx7r9x";
    static final int REQUEST_LINK_TO_DBX = 0;  // This value is up to you

    Context ctx = this;
    Button btnConnectDropbox;
    TextView tvDropboxOutput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropboxsync);
        if (isOnline()) {
            try {
                mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), DROPBOX_APP_KEY, DROPBOX_APP_SECRET);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        btnConnectDropbox = (Button) findViewById(R.id.btnConnectDropbox);
        tvDropboxOutput = (TextView) findViewById(R.id.tvDropboxOutput);
        btnConnectDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mDbxAcctMgr.startLink(DropboxActivity.this, REQUEST_LINK_TO_DBX);
                } catch (Exception e) {
                   e.printStackTrace();
                    Toast.makeText(DropboxActivity.this, "Ошибка синхронизации", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected()) {
            Toast.makeText(DropboxActivity.this, "Online", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            Toast.makeText(DropboxActivity.this, "Offline", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

   /* private void doDropboxTest() {
        try {
            final String TEST_DATA = "Hello Kitty";
            final String TEST_FILE_NAME = "hello_kitty.txt";
            DbxPath testPath = new DbxPath(DbxPath.ROOT, TEST_FILE_NAME);

            // Create DbxFileSystem for synchronized file access.
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
                    .getLinkedAccount());

            // Print the contents of the root folder. This will block until we
            // can
            // sync metadata the first time.
            List infos = dbxFs.listFolder(DbxPath.ROOT);
            tvDropboxOutput.setText("\nContents of app folder:\n");
            for (DbxFileInfo  info : infos) {
                tvDropboxOutput.append("    " + info.path + ", " + info.modifiedTime
                        + '\n');
            }

            // Create a test file only if it doesn't already exist.
            if (!dbxFs.exists(testPath)) {
                DbxFile testFile = dbxFs.create(testPath);
                try {
                    testFile.writeString(TEST_DATA);
                } finally {
                    testFile.close();
                }
                tvDropboxOutput.append("\nCreated new file '" + testPath + "'.\n");
            }

            // Read and print the contents of test file. Since we're not making
            // any attempt to wait for the latest version, this may print an
            // older cached version. Use getSyncStatus() and/or a listener to
            // check for a new version.
            if (dbxFs.isFile(testPath)) {
                String resultData;
                DbxFile testFile = dbxFs.open(testPath);
                try {
                    resultData = testFile.readString();
                } finally {
                    testFile.close();
                }
                tvDropboxOutput.append("\nRead file '" + testPath
                        + "' and got data:\n    " + resultData);
            } else if (dbxFs.isFolder(testPath)) {
                tvDropboxOutput.append("'" + testPath.toString() + "' is a folder.\n");
            }
        } catch (IOException e) {
            tvDropboxOutput.setText("Dropbox test failed: " + e);
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        if (mDbxAcctMgr.hasLinkedAccount()) {
            showLinkedView();
//            doDropboxTest();
        } else {
            showUnlinkedView();
        }
    }

    private void showLinkedView() {
        btnConnectDropbox.setVisibility(View.GONE);
        tvDropboxOutput.setVisibility(View.VISIBLE);
    }

    private void showUnlinkedView() {
        btnConnectDropbox.setVisibility(View.VISIBLE);
        tvDropboxOutput.setVisibility(View.GONE);
    }

    // Обрабатываем полученный результат
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(DropboxActivity.this, "Соединение успешно прошло", Toast.LENGTH_SHORT).show();
                // Можно работать с файлами Dropbox
//                doDropboxTest();
            } else {
                tvDropboxOutput.setText("Соединиться не получилось или пользователь отменил операцию");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}