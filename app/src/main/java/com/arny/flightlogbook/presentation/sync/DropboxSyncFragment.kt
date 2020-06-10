package com.arny.flightlogbook.presentation.sync

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arny.constants.CONSTS
import com.arny.data.remote.dropbox.DropboxClientFactory
import com.arny.data.remote.dropbox.GetCurrentAccountTask
import com.arny.data.service.BackgroundIntentService
import com.arny.domain.common.PreferencesProvider
import com.arny.domain.flights.FlightsRepository
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.users.FullAccount
import es.dmoral.toasty.Toasty
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dropbox_sync.*
import java.util.*
import javax.inject.Inject

class DropboxSyncFragment : Fragment(), CompositeDisposableComponent {
    private var accessToken: String? = null
    private var mOperationResult: String? = null
    private var dbxEmail: String? = null
    private var dbxName: String? = null
    private var notif: String? = null
    private var localfileDate: String? = null
    private var remoteFileDate: String? = null
    private var syncDateTime: String? = null
    private var pDialog: ProgressDialog? = null
    private var finishOperation: Boolean = false
    private var operationSuccess: Boolean = false
    private var mMyServiceIntent: Intent? = null
    private var mOperation: Int = 0
    private var hashMap = hashMapOf<String, String>()
    @Inject
    lateinit var preferencesProvider: PreferencesProvider
    @Inject
    lateinit var repository: FlightsRepository
    override val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                finishOperation = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH, false)
                mOperation = intent.getIntExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_IMPORT_SD)
                mOperationResult = intent.getStringExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_RESULT)
                operationSuccess = intent.getBooleanExtra(BackgroundIntentService.EXTRA_KEY_FINISH_SUCCESS, false)
                hashMap = intent.getSerializableExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_DATA) as HashMap<String, String>
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (finishOperation) {
                hideProgress()
                if (operationSuccess) {
                    operationResult()
                    mOperationResult?.let { Toasty.success(context, it, Toast.LENGTH_SHORT).show() }
                } else {
                    mOperationResult?.let { Toasty.error(context, it, Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_dropbox_sync, container, false)
        mMyServiceIntent = Intent(context, BackgroundIntentService::class.java)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pDialog = ProgressDialog(context)
        pDialog?.setCancelable(false)
        initListeners()
        initState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun initState() {
        localfileDate = preferencesProvider.getPrefString(PREF_DBX_LOCAL_DATETIME, "")
        remoteFileDate = preferencesProvider.getPrefString(PREF_DBX_REMOTE_DATETIME, "")
        syncDateTime = preferencesProvider.getPrefString(PREF_DBX_SYNC_DATETIME, "")
        dbxEmail = preferencesProvider.getPrefString(PREF_DBX_EMAIL, "")
        dbxName = preferencesProvider.getPrefString(PREF_DBX_NAME, "")
        tvDpxEmail.text = String.format(getString(R.string.dropbox_email), dbxEmail)
        tvDpxName.text = String.format(getString(R.string.dropbox_name), dbxName)
        val autoImport = preferencesProvider.getPrefBoolean(CONSTS.PREFS.PREF_DROPBOX_AUTOIMPORT_TO_DB, false)
        setSyncDataFileDateTime()
    }

    private fun initListeners() {
        btnDpxLogin.setOnClickListener {
            Auth.startOAuth2Authentication(context, getString(R.string.dropbox_app_key))
        }
        btnSync.setOnClickListener {
            getFileData()
        }
        btnSyncUp.setOnClickListener {
            alertDialog(
                    context = requireActivity(),
                    title = getString(R.string.warning),
                    content = getString(R.string.dropbox_sync_upload) + "?",
                    btnCancelText = getString(R.string.str_cancel),
                    onConfirm = { uploadFile() }
            )
        }
        btnSyncDown.setOnClickListener {
            alertDialog(
                    context = requireActivity(),
                    title = getString(R.string.warning),
                    content = getString(R.string.dropbox_sync_warning_content),
                    onConfirm = { downLoadFile() }
            )
        }
        /*checkBoxAutoImport.setOnCheckedChangeListener { _, b ->
            preferencesProvider.setPrefBoolean(CONSTS.PREFS.PREF_DROPBOX_AUTOIMPORT_TO_DB, b)
        }*/
    }

    private fun setSyncDataFileDateTime() {
        val dpxData = StringBuilder()
        val downVis = if (!Utility.empty(remoteFileDate)) View.VISIBLE else View.GONE
        val upVis = if (!Utility.empty(localfileDate)) View.VISIBLE else View.GONE
        dpxData.append(getString(R.string.dropbox_sync_files)).append(if (!Utility.empty(syncDateTime)) syncDateTime else getString(R.string.dropbox_sync_files_no_data)).append("\n")
        dpxData.append(getString(R.string.dropbox_sync_files_local)).append(if (!Utility.empty(localfileDate)) localfileDate else getString(R.string.dropbox_sync_files_no_data)).append("\n")
        dpxData.append(getString(R.string.dropbox_sync_files_remote)).append(if (!Utility.empty(remoteFileDate)) remoteFileDate else getString(R.string.dropbox_sync_files_no_data)).append("\n")
        btnSyncDown.visibility = downVis
        btnSyncUp.visibility = upVis
        tvDpxData.text = dpxData.toString()
    }

    private fun downLoadFile() {
        try {
            if (DropboxClientFactory.getClient() != null) {
                showProgress(getString(R.string.dropbox_sync_downloading))
                if (mMyServiceIntent == null) {
                    mMyServiceIntent = Intent(context, BackgroundIntentService::class.java)
                }
                mMyServiceIntent?.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_DBX_DOWNLOAD)
                context?.startService(mMyServiceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.dropbox_auth_error), Toast.LENGTH_SHORT).show()
        }

    }

    fun uploadFile() {
        try {
            if (DropboxClientFactory.getClient() != null) {
                showProgress(getString(R.string.dropbox_sync_uploading))
                if (mMyServiceIntent == null) {
                    mMyServiceIntent = Intent(context, BackgroundIntentService::class.java)
                }
                mMyServiceIntent?.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_DBX_UPLOAD)
                context?.startService(mMyServiceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.dropbox_auth_error), Toast.LENGTH_SHORT).show()
        }

    }

    fun getFileData() {
        try {
            if (DropboxClientFactory.getClient() != null) {
                showProgress(getString(R.string.dropbox_sync_files))
                if (mMyServiceIntent == null) {
                    mMyServiceIntent = Intent(context, BackgroundIntentService::class.java)
                }
                mMyServiceIntent?.putExtra(BackgroundIntentService.EXTRA_KEY_OPERATION_CODE, BackgroundIntentService.OPERATION_DBX_SYNC)
                context?.startService(mMyServiceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.dropbox_auth_error), Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BackgroundIntentService.ACTION)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(broadcastReceiver, filter) }

        if (Utility.isMyServiceRunning(BackgroundIntentService::class.java, context)) {
            getOperationNotif()
            showProgress(notif)
        } else {
            hideProgress()
            setUiVisibility()
            fromCallable {
                getAccessToken()
                true
            }.subsribeFromPresenter({
                setUiVisibility()
            })
        }
    }

    private fun getOperationNotif() {
        when (BackgroundIntentService.getOperation()) {
            BackgroundIntentService.OPERATION_DBX_SYNC -> notif = getString(R.string.dropbox_sync_files)
            BackgroundIntentService.OPERATION_EXPORT -> notif = getString(R.string.str_export_excel)
            BackgroundIntentService.OPERATION_IMPORT_SD -> notif = getString(R.string.str_import_excel)
        }
    }

    override fun onPause() {
        super.onPause()
        hideProgress()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver) }
    }

    private fun hideProgress() {
        if (pDialog != null) {
            Log.d(DropboxSyncFragment::class.java.simpleName, "hideProgress: pDialog.isShowing() = " + pDialog?.isShowing)
            if (pDialog?.isShowing == true) {
                pDialog?.dismiss()
            }
        }
    }

    private fun showProgress(notif: String?) {
        if (pDialog != null) {
            pDialog?.setMessage(notif)
            Log.d(DropboxSyncFragment::class.java.simpleName, "showProgress: pDialog.isShowing() = " + pDialog?.isShowing)
            if (pDialog?.isShowing == false) {
                pDialog?.show()
            }
        }
    }

    private fun setUiVisibility() {
        val hasToken = hasToken()
        btnDpxLogin.setVisible(!hasToken)
        tvDpxEmail.setVisible(hasToken)
        tvDpxName.setVisible(hasToken)
        btnSync.setVisible(hasToken)
    }

    private fun getAccessToken() {
        accessToken = preferencesProvider.getPrefString(DROPBOX_STR_TOKEN, null)
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                preferencesProvider.setPrefString(DROPBOX_STR_TOKEN, accessToken)
                initAndLoadData(accessToken!!)
            }
        } else {
            initAndLoadData(accessToken!!)
        }
    }

    private fun hasToken(): Boolean {
        if (accessToken == null) {
            accessToken = preferencesProvider.getPrefString(DROPBOX_STR_TOKEN)
        }
        return accessToken != null
    }

    private fun loadData() {
        GetCurrentAccountTask(DropboxClientFactory.getClient(), object : GetCurrentAccountTask.Callback {
            override fun onComplete(result: FullAccount) {
                preferencesProvider.setPrefString(PREF_DBX_EMAIL, result.email)
                preferencesProvider.setPrefString(PREF_DBX_NAME, result.name.displayName)
                if (view != null) {
                    tvDpxEmail.text = String.format(getString(R.string.dropbox_email), result.email)
                    tvDpxName.text = String.format(getString(R.string.dropbox_name), result.name.displayName)
                }
            }

            override fun onError(e: Exception) {
                Log.e(javaClass.name, "Failed to get account details.", e)
            }
        }).execute()
    }

    private fun initAndLoadData(accessToken: String) {
        DropboxClientFactory.init(accessToken)
        loadData()
    }

    private fun operationResult() {
        when (mOperation) {
            BackgroundIntentService.OPERATION_DBX_SYNC -> {
                localfileDate = hashMap[BackgroundIntentService.EXTRA_KEY_OPERATION_DATA_LOCAL_DATE]
                remoteFileDate = hashMap[BackgroundIntentService.EXTRA_KEY_OPERATION_DATA_REMOTE_DATE]
                syncDateTime = DateTimeUtils.getDateTime(0.toLong(), "dd MMM yyyy HH:mm:ss")
                preferencesProvider.setPrefString(PREF_DBX_LOCAL_DATETIME, localfileDate)
                preferencesProvider.setPrefString(PREF_DBX_REMOTE_DATETIME, remoteFileDate)
                preferencesProvider.setPrefString(PREF_DBX_SYNC_DATETIME, syncDateTime)
                setSyncDataFileDateTime()
            }
        }
    }

    companion object {
        private val DROPBOX_STR_TOKEN = "access-token"
        private val PREF_DBX_EMAIL = "dbx_email"
        private val PREF_DBX_NAME = "dbx_name"
        private val PREF_DBX_LOCAL_DATETIME = "dbx_local_datetime"
        private val PREF_DBX_REMOTE_DATETIME = "dbx_remote_datetime"
        private val PREF_DBX_SYNC_DATETIME = "dbx_sync_datetime"
    }

}// Required empty public constructor
