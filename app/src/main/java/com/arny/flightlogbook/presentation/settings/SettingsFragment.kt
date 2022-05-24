package com.arny.flightlogbook.presentation.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.arny.core.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.SettingsFragmentBinding
import com.arny.flightlogbook.domain.models.ExportFileType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.flightlogbook.uicore.hideProgressDialog
import com.flightlogbook.uicore.showProgressDialog
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class SettingsFragment : BaseMvpFragment(), SettingsView {
    private lateinit var binding: SettingsFragmentBinding
    private val handler = Handler(Looper.getMainLooper())
    private var requestFilesApi30Success: Boolean = false

    companion object {
        fun getInstance(): SettingsFragment = SettingsFragment()
    }

    @Inject
    lateinit var presenterProvider: Provider<SettingsPresenter>

    private val presenter by moxyPresenter { presenterProvider.get() }

    private val requestPermissionImport =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                showAlertImport()
            }
        }
    private val requestPermissionExport =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                chooseExportFile()
            }
        }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = getString(R.string.str_settings)
        with(binding) {
            tvLoadFromFile.setOnClickListener {
                requestPermission(
                    requestPermissionImport,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) {
                    showAlertImport()
                }
            }
            tvExportToFile.setOnClickListener {
                requestPermission(
                    requestPermissionExport,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) {
                    chooseExportFile()
                }
            }
            chbAutoExport.setOnCheckedChangeListener { _, isChecked ->
                presenter.onAutoExportChanged(isChecked)
            }
            chbSaveLastFlightData.setOnCheckedChangeListener { _, isChecked ->
                presenter.onSaveLastDataChanged(isChecked)
            }
            tvResultInfo.setOnClickListener {
                presenter.onShareFileClick()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestFilesApi30Success) {
            requestFilesApi30Success = false
            requestFile()
        }
    }

    override fun showFileData() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            presenter.showFileData()
        }, 1500)
    }

    private fun chooseExportFile() {
        alertDialog(
            context = context,
            title = getString(R.string.str_export_attention),
            content = getString(R.string.str_export_confirm_description),
            btnOkText = getString(R.string.str_export_confirm_xls),
            btnCancelText = getString(R.string.str_export_confirm_json),
            cancelable = true,
            onConfirm = { presenter.exportToFile(ExportFileType.XLS) },
            onCancel = { presenter.exportToFile(ExportFileType.JSON) }
        )
    }

    private fun showAlertImport() {
        alertDialog(
            context = context,
            title = getString(R.string.str_import_attention),
            content = getString(R.string.str_import_massage),
            btnOkText = getString(R.string.str_ok),
            btnCancelText = getString(R.string.str_cancel),
            cancelable = true,
            onConfirm = (::chooseFile)
        )
    }

    override fun showFilesToShare(filenames: List<String>) {
        listDialog(
            context = requireContext(),
            title = getString(R.string.str_share_confirm_title),
            items = filenames,
            cancelable = true,
            onSelect = { _, text ->
                presenter.shareSelectedFile(text)
            }
        )
    }

    private fun chooseFile() {
        alertDialog(
            context = context,
            title = getString(R.string.str_import_attention),
            content = getString(R.string.str_import_local_file_or_disk),
            btnOkText = getString(R.string.str_import_local_file),
            btnCancelText = getString(R.string.str_import_file_from_disk),
            cancelable = true, onConfirm = {
                presenter.loadDefaultFile()
            }, onCancel = (::requestFile)
        )
    }

    private fun requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            requestAccessAndroidR()
        } else {
            requestOpenFile()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestAccessAndroidR() {
        requestFilesApi30Success = true
        requestPermissionAndroidR.launch("")
    }

    private fun requestFile() {
        requestPermission()
    }

    private fun requestOpenFile() {
        val intent = newIntent().apply {
            action = Intent.ACTION_GET_CONTENT
            addCategory(Intent.CATEGORY_OPENABLE)
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            val extraMimeTypes = arrayOf("application/vnd.ms-excel", "application/json")
            putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "*/*"
        }
        requestPermissionOpenFile.launch(intent)
    }

    private val requestPermissionOpenFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                presenter.onFileImport(result.data?.data)
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private class AccessFilesPermissionR : ActivityResultContract<String, Boolean?>() {
        override fun createIntent(context: Context, input: String?): Intent = newIntent().apply {
            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse(String.format("package:%s", context.applicationContext.packageName))
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            // FIXME не возвращается результат, проверяем отдельно
            return Environment.isExternalStorageManager()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestPermissionAndroidR =
        registerForActivityResult(AccessFilesPermissionR()) { granted ->
            if (granted == true) {
                requestOpenFile()
            } else {
                requestFilesApi30Success = false
            }
        }

    override fun shareFile(uri: Uri, fileType: String) {
        requireActivity().shareFileWithType(uri, fileType)
    }

    override fun showError(@StringRes msgRes: Int, error: String?) {
        if (error.isNullOrBlank()) {
            binding.tvResultInfo.text = getString(msgRes)
        } else {
            binding.tvResultInfo.text = getString(msgRes, error)
        }
    }

    override fun showSuccess(@StringRes msgRes: Int, msg: String?) {
        ToastMaker.toastSuccess(requireContext(), getString(msgRes, msg))
    }

    override fun setAutoExportChecked(checked: Boolean) {
        binding.chbAutoExport.isChecked = checked
    }

    override fun setShareFileVisible(visible: Boolean) {
        binding.tvResultInfo.isVisible = visible
    }

    override fun showResults(intRes: Int, path: String) {
        binding.tvResultInfo.text = getString(intRes, path)
    }

    override fun showResults(results: String) {
        binding.tvResultInfo.text = results
    }

    override fun hideResults() {
        binding.tvResultInfo.text = ""
    }

    override fun showProgress(msg: Int) {
        childFragmentManager.showProgressDialog(getString(msg))
    }

    override fun hideProgress() {
        childFragmentManager.hideProgressDialog()
    }

    override fun setSaveLastFlightData(checked: Boolean) {
        binding.chbSaveLastFlightData.isChecked = checked
    }

    override fun openWith(pair: Pair<Uri, String?>) {
        with(Intent(Intent.ACTION_VIEW)) {
            val (fromFile, mimetype) = pair
            setDataAndType(fromFile, mimetype)
            startActivity(this)
        }
    }
}
