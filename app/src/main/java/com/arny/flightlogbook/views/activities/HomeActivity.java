package com.arny.flightlogbook.views.activities;

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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.BackgroundIntentService;
import com.arny.flightlogbook.models.DataList;
import com.arny.flightlogbook.models.DatabaseHandler;
import com.arny.flightlogbook.models.Funct;
import com.arny.flightlogbook.models.Functions;
import com.arny.flightlogbook.models.Preferences;
import com.arny.flightlogbook.views.fragments.FlightList;
import com.arny.flightlogbook.views.fragments.StatisticFragment;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 110;
    private static final int SAVE_FILE_RESULT_CODE = 111;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String LOG_SHEET_MAIN = "Timelog";
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int MENU_FLIGHTS = 0;
    private static final int MENU_STATS = 1;
    private Drawer drawer = null;
    private Toolbar toolbar;
    private Intent fileintent,mMyServiceIntent;
    private Context context;
    private boolean autoExportXLSPref, metarPref;
    private DatabaseHandler db;
    private List<DataList> ExportData,TypeData;
    private int airplane_type_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_home);
        getPrefs();
        db = new DatabaseHandler(this);
        mMyServiceIntent = new Intent(context, BackgroundIntentService.class);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.fragment_logbook));

        drawer = new DrawerBuilder()
                .withActivity(this)
//                .withRootView(R.id.container)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(MENU_FLIGHTS).withName(R.string.fragment_logbook).withIcon(GoogleMaterial.Icon.gmd_flight),
                        new PrimaryDrawerItem().withIdentifier(MENU_STATS).withName(R.string.fragment_stats).withIcon(GoogleMaterial.Icon.gmd_equalizer)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        selectItem((int) drawerItem.getIdentifier());
                        return true;
                    }
                })
                .build();
        if (savedInstanceState == null) {
            selectItem(MENU_FLIGHTS);
        }
    }

    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case MENU_FLIGHTS:
                fragment = new FlightList();
                toolbar.setTitle(getString(R.string.fragment_logbook));
                break;
            case MENU_STATS:
                fragment = new StatisticFragment();
                toolbar.setTitle(getString(R.string.fragment_stats));
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
            drawer.closeDrawer();
        }
    }

    public void getPrefs() {
        SharedPreferences prefs = PreferenceManager .getDefaultSharedPreferences(getBaseContext());
        autoExportXLSPref = prefs.getBoolean("autoExportXLSPref", false);
        metarPref = prefs.getBoolean("metarCheckPref", false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean fileExist = Funct.isFileExist(context);
        MenuItem exelOpenAction = menu.findItem(R.id.action_open_file);
        MenuItem exelImportAction = menu.findItem(R.id.action_import_excel);
        exelOpenAction.setVisible(fileExist);
        exelImportAction.setVisible(fileExist);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_about:
                Intent intent = new Intent(context, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                Intent intentSettings = new Intent(context, Preferences.class);
                startActivity(intentSettings);
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
                Intent mAirplanesActivity = new Intent(context, AirplaneTypesActivity.class);
                startActivity(mAirplanesActivity);
                break;
            case R.id.action_import_from_file:
                showImportDialogSD();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private static boolean verifyStoragePermissions(Activity activity) {
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
        Log.i(HomeActivity.class.getSimpleName(), "onRequestPermissionsResult: requestCode = " + requestCode);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fileintent = new Intent();
                    fileintent.setAction(Intent.ACTION_GET_CONTENT);
                    fileintent.addCategory(Intent.CATEGORY_OPENABLE);
                    fileintent.setType("*/*");
                    startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
                } else {
                    Toast.makeText(context, R.string.str_storage_permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
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
                        permissionGranded = verifyStoragePermissions(HomeActivity.this);// Do something for lollipop and above versions
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
                    Toast.makeText(context, getString(R.string.str_error_import), Toast.LENGTH_SHORT).show();
                }

            }
        });
        alert.show();
    }

    public void openFileWith() {
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            File sdPath = Environment.getExternalStorageDirectory();
            File file = new File(sdPath + "/Android/data/com.arny.flightlogbook/files", Funct.EXEL_FILE_NAME);
            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            myIntent.setDataAndType(Uri.fromFile(file), mimetype);
            startActivity(myIntent);
        } catch (Exception e) {
            e.printStackTrace();
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
        Row row;
        if (!Functions.isExternalStorageAvailable() || Functions.isExternalStorageReadOnly()) {
            Toast.makeText(context, getString(R.string.storage_not_avalable), Toast.LENGTH_LONG).show();
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
            c.setCellValue(getDate(export.getDatetime()));
            c = row.createCell(1);
            c.setCellValue(Funct.strLogTime(export.getLogtime()));
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
            Toast.makeText(context, getString(R.string.str_file_saved) + " " + file, Toast.LENGTH_SHORT).show();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
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
                saveExcelFile(getBaseContext(), Funct.EXEL_FILE_NAME);
                Toast.makeText(context, getString(R.string.str_export_success), Toast.LENGTH_SHORT).show();
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
//				readExcelFile(getBaseContext(), Funct.EXEL_FILE_NAME, false, null);
				/*FlightData = db.getFlightListByDate();
				listView.setAdapter(new ViewAdapter());
				displayTotalTime();*/
                Toast.makeText(context, getString(R.string.str_import_success), Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
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
            dat = new SimpleDateFormat(strDateFormat, Locale.getDefault()).parse(strM);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        String fDate = new SimpleDateFormat("MMM", Locale.getDefault()).format(dat);
        return d + " " + fDate + " " + y;
    }

    private String getTime(long timestamp) {
        Date d = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm",Locale.getDefault());
        return format.format(d);
    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(context);
        quitDialog.setTitle(getString(R.string.str_exit));
        quitDialog.setNegativeButton(getString(R.string.str_cancel), null);
        quitDialog.setPositiveButton(getString(R.string.str_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getPrefs();
                        if (autoExportXLSPref) {
                            saveExcelFile(getBaseContext(), Funct.EXEL_FILE_NAME);
                        }
                        finish();
                    }
                });
        AlertDialog alert = quitDialog.create();
        alert.show();
    }
}
