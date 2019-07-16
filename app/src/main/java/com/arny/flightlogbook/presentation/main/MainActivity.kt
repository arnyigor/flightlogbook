package com.arny.flightlogbook.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.arny.constants.CONSTS
import com.arny.domain.Local
import com.arny.domain.service.BackgroundIntentService
import com.arny.flightlogbook.BuildConfig
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.about.AboutActivity
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.flightlogbook.presentation.flights.viewflights.FlightListFragment
import com.arny.flightlogbook.presentation.settings.Preferences
import com.arny.flightlogbook.presentation.statistic.StatisticFragment
import com.arny.flightlogbook.presentation.sync.DropboxSyncFragment
import com.arny.flightlogbook.presentation.types.PlaneTypesFragment
import com.arny.helpers.utils.*
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.tbruyelle.rxpermissions2.RxPermissions
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class MainActivity : AppCompatActivity(), Drawer.OnDrawerListener {
    private var mOperationResult: String? = null
    private var notif: String? = null
    private var drawer: Drawer? = null
    private var toolbar: Toolbar? = null
    private var fileintent: Intent? = null
    private var mMyServiceIntent: Intent? = null
    private var context: Context? = null
    private var operationSuccess: Boolean = false
    private var finishOperation = true
    private var pDialog: ProgressDialog? = null
    private val disposable = CompositeDisposable()
    private var rxPermissions: RxPermissions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_home)
        pDialog = ProgressDialog(context)
        pDialog?.setCancelable(false)
        initBgService()
        toolbar = findViewById<Toolbar>(R.id.home_toolbar)
        setSupportActionBar(toolbar)
        toolbar?.title = getString(R.string.fragment_logbook)
        rxPermissions = RxPermissions(this)
        drawer = DrawerBuilder()
                .withActivity(this)
                .withOnDrawerListener(this)
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar!!)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        PrimaryDrawerItem().withIdentifier(MENU_FLIGHTS.toLong()).withName(R.string.fragment_logbook).withIcon(GoogleMaterial.Icon.gmd_flight_takeoff),
                        PrimaryDrawerItem().withIdentifier(MENU_TYPES.toLong()).withName(R.string.fragment_types).withIcon(GoogleMaterial.Icon.gmd_flight),
                        PrimaryDrawerItem().withIdentifier(MENU_STATS.toLong()).withName(R.string.fragment_stats).withIcon(GoogleMaterial.Icon.gmd_equalizer),
                        PrimaryDrawerItem().withIdentifier(MENU_DROPBOX_SYNC.toLong()).withName(R.string.fragment_dropbox_sync).withIcon(R.drawable.ic_dropbox_sync)
                )
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    selectItem(drawerItem.identifier.toInt())
                    true
                }
                .build()
        if (savedInstanceState == null) {
            selectItem(MENU_FLIGHTS)
        } else {
            try {
                drawer!!.setSelection(java.lang.Long.parseLong(savedInstanceState.getString(DRAWER_SELECTION)))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val state = drawer!!.saveInstanceState(outState)
        state.putString(DRAWER_SELECTION, drawer!!.currentSelection.toString())
        super.onSaveInstanceState(state)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_about -> {
                val intent = Intent(context, AboutActivity::class.java)
                startActivity(intent)
            }
            R.id.action_settings -> {
                val intentSettings = Intent(context, Preferences::class.java)
                startActivity(intentSettings)
            }
            R.id.action_import_excel -> rxPermissions!!.request(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe({ permissionGranded ->
                        if (permissionGranded!!) {
                            showImportAlert()
                        }
                    }, { throwable -> ToastMaker.toastError(this, getString(R.string.str_error_import) + ":" + throwable.message) }
                    )
            R.id.action_export_excel -> rxPermissions!!.request(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe({ permissionGranded ->
                        if (permissionGranded!!) {
                            showExportAlert()
                        }
                    }, { throwable -> ToastMaker.toastError(this, getString(R.string.str_error_import) + ":" + throwable.message) }
                    )
            R.id.action_open_file -> rxPermissions!!.request(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe({ permissionGranded ->
                        if (permissionGranded!!) {
                            openFileWith()
                        }
                    }, { throwable -> ToastMaker.toastError(this, getString(R.string.str_error_import) + ":" + throwable.message) }
                    )
            R.id.action_type_edit -> {
                val i = Intent(this, FragmentContainerActivity::class.java)
                i.putExtra("fragment_tag", "type_list")
                startActivity(i)
            }
            R.id.action_import_from_file -> rxPermissions!!.request(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe({ permissionGranded ->
                        if (permissionGranded!!) {
                            showImportDialogSD()
                        }
                    }, { throwable -> ToastMaker.toastError(this, getString(R.string.str_error_import) + ":" + throwable.message) }
                    )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectItem(position: Int) {
        var fragment: Fragment? = null
        when (position) {
            MENU_FLIGHTS -> {
                fragment = FlightListFragment.newInstance()
                toolbar!!.title = getString(R.string.fragment_logbook)
            }
            MENU_TYPES -> {
                fragment = PlaneTypesFragment.getInstance()
                toolbar!!.title = getString(R.string.str_airplane_types)
            }
            MENU_STATS -> {
                fragment = StatisticFragment()
                toolbar!!.title = getString(R.string.fragment_stats)
            }
            MENU_DROPBOX_SYNC -> {
                fragment = DropboxSyncFragment()
                toolbar!!.title = getString(R.string.fragment_dropbox_sync)
            }
        }
        if (fragment != null) {
            this.replaceFragmentInActivity(fragment, R.id.container)
            drawer!!.closeDrawer()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen) {
            drawer!!.closeDrawer()
        } else {
            val fragments = supportFragmentManager.fragments
            var isMain = false
            for (curFrag in fragments) {
                if (curFrag != null && curFrag.isVisible && curFrag is FlightListFragment) {
                    isMain = true
                }
            }
            if (!isMain) {
                selectItem(MENU_FLIGHTS)
            } else {
                if (back_pressed + TIME_DELAY > System.currentTimeMillis()) {
                    val autoExportXLSPref = Prefs.getInstance(this).get<Boolean>("autoExportXLSPref")?:false
                    if (autoExportXLSPref) {
                        initBgService()
                        mMyServiceIntent!!.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_EXPORT)
                        startService(mMyServiceIntent)
                    }
                    super.onBackPressed()
                } else {
                    Toast.makeText(baseContext, R.string.press_back_again_to_exit,
                            Toast.LENGTH_SHORT).show()
                }
                back_pressed = System.currentTimeMillis()
            }
        }
    }

    @SuppressLint("CheckResult")
    public override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BackgroundIntentService.ACTION)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(broadcastReceiver, filter)
        Observable.fromCallable { Utility.isMyServiceRunning(BackgroundIntentService::class.java, this@MainActivity) }
                .observeOnMain()
                .subscribe({ aBoolean ->
                    if (aBoolean!!) {
                        getOperationNotif(this)
                        showProgress(notif)
                    } else {
                        hideProgress()
                    }
                }) {
                    it.printStackTrace()
                }

        rxPermissions?.request(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ?.subscribe({ permissionGranded ->
                    if (permissionGranded) {
                        val menu = toolbar?.menu
                        val fileExist = Local.isAppFileExist(context)
                        val exelOpenAction = menu?.findItem(R.id.action_open_file)
                        val exelImportAction = menu?.findItem(R.id.action_import_excel)
                        if (exelOpenAction != null) {
                            exelOpenAction.isVisible = fileExist
                        }
                        if (exelImportAction != null) {
                            exelImportAction.isVisible = fileExist
                        }
                    }
                }, { throwable -> ToastMaker.toastError(this, getString(R.string.str_error_import) + ":" + throwable.message) }
                )

    }

    private fun hideProgress() {
        try {
            if (pDialog != null) {
                pDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showProgress(notif: String?) {
        try {
            if (pDialog != null) {
                Log.d(MainActivity::class.java.simpleName, "hideProgress: pDialog.isShowing() = " + pDialog!!.isShowing)
                pDialog!!.setMessage(notif)
                if (!pDialog!!.isShowing) {
                    pDialog!!.show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    public override fun onPause() {
        super.onPause()
        hideProgress()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(broadcastReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CONSTS.REQUESTS.REQUEST_EXTERNAL_STORAGE_XLS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fileintent = Intent()
                fileintent!!.action = Intent.ACTION_GET_CONTENT
                fileintent!!.addCategory(Intent.CATEGORY_OPENABLE)
                fileintent!!.type = "*/*"
                startActivityForResult(fileintent, PICKFILE_RESULT_CODE)
            } else {
                Toast.makeText(context, R.string.str_storage_permission_denied, Toast.LENGTH_SHORT).show()
            }
            CONSTS.REQUESTS.REQUEST_DBX_EXTERNAL_STORAGE -> if (BasePermissions.permissionGranted(grantResults)) {
                val dropboxSyncFragment = supportFragmentManager.findFragmentById(R.id.container)
                if (dropboxSyncFragment is DropboxSyncFragment) {
                    dropboxSyncFragment.getFileData()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun showImportDialogSD() {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(getString(R.string.str_import_attention))
        alert.setMessage(getString(R.string.str_import_massage))
        alert.setNegativeButton(getString(R.string.str_cancel)) { dialog, which -> dialog.cancel() }
        alert.setPositiveButton(getString(R.string.str_ok)) { dialog, which ->
            fileintent = Intent()
            fileintent!!.action = Intent.ACTION_GET_CONTENT
            fileintent!!.addCategory(Intent.CATEGORY_OPENABLE)
            fileintent!!.type = "*/*"
            startActivityForResult(fileintent, PICKFILE_RESULT_CODE)

        }
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICKFILE_RESULT_CODE -> if (resultCode == Activity.RESULT_OK) {
                val FilePath = data!!.data!!.path
                if (BuildConfig.DEBUG)
                    Log.d(MainActivity::class.java.simpleName, "onActivityResult: FilePath = " + FilePath!!)
                initBgService()
                mMyServiceIntent!!.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD)
                mMyServiceIntent!!.putExtra(BackgroundIntentService.EXTRA_KEY_IMPORT_SD_FILENAME, FilePath)
                startService(mMyServiceIntent)
                showProgress(getString(R.string.str_import_excel))
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.str_error_import), Toast.LENGTH_SHORT).show()
            }
            SAVE_FILE_RESULT_CODE -> if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                data.data!!.path
            }
        }
    }

    private fun openFileWith() {
        try {
            val myIntent = Intent(Intent.ACTION_VIEW)
            val sdPath = Environment.getExternalStorageDirectory()
            val file = File(sdPath.toString() + "/Android/data/com.arny.flightlogbook/files", CONSTS.FILES.EXEL_FILE_NAME)
            val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
            val mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            myIntent.setDataAndType(Uri.fromFile(file), mimetype)
            startActivity(myIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getOperationNotif(context: Context) {
        when (BackgroundIntentService.getOperation()) {
            BackgroundIntentService.OPERATION_DBX_SYNC -> notif = context.resources.getString(R.string.dropbox_sync_files)
            BackgroundIntentService.OPERATION_EXPORT -> notif = context.resources.getString(R.string.str_export_excel)
            BackgroundIntentService.OPERATION_IMPORT_SD -> notif = context.resources.getString(R.string.str_import_excel)
            else -> notif = context.resources.getString(R.string.str_import_excel)
        }
    }

    private fun showExportAlert() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.str_export_attention))
        alert.setNegativeButton(getString(R.string.str_cancel)) { dialog, which -> dialog.cancel() }
        alert.setPositiveButton(getString(R.string.str_ok)) { dialog, which ->
            initBgService()
            mMyServiceIntent!!.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_EXPORT)
            startService(mMyServiceIntent)
            showProgress(getString(R.string.str_export_excel))
        }
        alert.show()
    }

    private fun showImportAlert() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.str_import_attention))
        alert.setMessage(getString(R.string.str_import_massage))
        alert.setNegativeButton(getString(R.string.str_cancel)) { dialog, which -> dialog.cancel() }
        alert.setPositiveButton(getString(R.string.str_ok)) { dialog, which ->
            initBgService()
            mMyServiceIntent!!.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD)
            mMyServiceIntent!!.putExtra(BackgroundIntentService.EXTRA_KEY_IMPORT_SD_FILENAME, "")
            startService(mMyServiceIntent)
            ToastMaker.toastSuccess(this, "Импорт данных из файла стартовал")
        }
        alert.show()
    }

    private fun initBgService() {
        if (mMyServiceIntent == null) {
            mMyServiceIntent = Intent(this@MainActivity, BackgroundIntentService::class.java)
        }
    }

    override fun onDrawerOpened(drawerView: View) {}

    override fun onDrawerClosed(drawerView: View) {

    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false)
                intent.getIntExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD)
                mOperationResult = intent.getStringExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_RESULT)
                operationSuccess = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH_SUCCESS, false)
            } catch (e: Exception) {
                e.printStackTrace()
                hideProgress()
            }

            Log.d(MainActivity::class.java.simpleName, "onReceive: finishOperation = $finishOperation")
            Log.d(MainActivity::class.java.simpleName, "onReceive: operationSuccess = $operationSuccess")
            if (finishOperation) {
                hideProgress()
                if (operationSuccess) {
                    Toasty.success(context, mOperationResult!!, Toast.LENGTH_SHORT).show()
                } else {
                    Toasty.error(context, mOperationResult!!, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private val SAVE_FILE_RESULT_CODE = 111
        private val MENU_DROPBOX_SYNC = 112
        private val PICKFILE_RESULT_CODE = 1
        private val MENU_FLIGHTS = 0
        private val MENU_TYPES = 1
        private val MENU_STATS = 2
        private val DRAWER_SELECTION = "drawer_selection"
        private val TIME_DELAY = 2000
        private var back_pressed: Long = 0
    }
}
