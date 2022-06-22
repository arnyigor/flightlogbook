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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.arny.core.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.BackupsFragmentBinding
import com.arny.flightlogbook.domain.models.ExportFileType
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.flightlogbook.uicore.hideProgressDialog
import com.flightlogbook.uicore.showProgressDialog
import dagger.android.support.AndroidSupportInjection
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class BackupsFragment : BaseMvpFragment(), BackupsView {
    companion object {
        const val REQUEST_DEFAULT_FILE = 100
        const val REQUEST_EXTERNAL_FILE = 101
    }

    private var requestCode: Int = -1
    private lateinit var binding: BackupsFragmentBinding
    private val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var presenterProvider: Provider<BackupPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestPermissionAndroidR =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                requestOpenFile()
            }
        }
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                when (requestCode) {
                    REQUEST_DEFAULT_FILE -> presenter.chooseDefaultFile()
                    REQUEST_EXTERNAL_FILE -> requestPermission()
                    else -> {}
                }
            } else {
                onPermissionDenied()
            }
        }
    private val launchOpenFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                presenter.onFileImport(result.data?.data)
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
        binding = BackupsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = getString(R.string.import_export_settings)
        with(binding) {
            tvLoadFromFile.setOnClickListener { showAlertImport() }
            tvExportToFile.setOnClickListener { showAlertExportFile() }
            tvResultInfo.setOnClickListener { presenter.onShareFileClick() }
        }
    }

    override fun showFileData() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            presenter.showFileData()
        }, 1000)
    }

    private fun showAlertExportFile() {
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
            onConfirm = (::showAlertChooseFile)
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

    private fun showAlertChooseFile() {
        alertDialog(
            context = context,
            title = getString(R.string.str_import_attention),
            content = getString(R.string.str_import_local_file_or_disk),
            btnOkText = getString(R.string.str_import_local_file),
            btnCancelText = getString(R.string.str_import_file_from_disk),
            cancelable = true,
            onConfirm = {
                this.requestCode = REQUEST_DEFAULT_FILE
                requestPermission(
                    resultLauncher = permissionLauncher,
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                ) { presenter.chooseDefaultFile() }
            },
            onCancel = {
                this.requestCode = REQUEST_EXTERNAL_FILE
                requestPermission(
                    resultLauncher = permissionLauncher,
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                ) { requestPermission() }
            }
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
        val intent = Intent().apply {
            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse(
                String.format(
                    "package:%s",
                    requireContext().applicationContext.packageName
                )
            )
        }
        requestPermissionAndroidR.launch(intent)
    }

    override fun showAlertChooseDefault(filenames: List<String>) {
        listDialog(
            context = requireContext(),
            title = getString(R.string.str_choose_default_file),
            items = filenames,
            cancelable = true,
            onSelect = { _, text ->
                presenter.loadDefaultFile(text)
            }
        )
    }

    private fun onPermissionDenied() {
        alertDialog(
            context = context,
            title = getString(R.string.payment_permission_denied_title),
            content = getString(R.string.payment_permission_denied_read_external_storage),
            btnOkText = getString(R.string.str_settings),
            btnCancelText = getString(android.R.string.cancel),
            cancelable = true,
            onConfirm = {
                requireContext().goToAppInfo()
            },
        )
    }

    private fun requestOpenFile() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            addCategory(Intent.CATEGORY_OPENABLE)
            val extraMimeTypes = arrayOf("application/vnd.ms-excel", "application/json")
            putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
            type = "*/*"
        }
        launchOpenFile.launch(intent)
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

    override fun toastError(@StringRes msgRes: Int, error: String?) {
        ToastMaker.toastError(
            requireContext(),
            if (error.isNullOrBlank()) {
                getString(msgRes)
            } else {
                getString(msgRes, error)
            }
        )
    }

    override fun showSuccess(@StringRes msgRes: Int, msg: String?) {
        ToastMaker.toastSuccess(requireContext(), getString(msgRes, msg))
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

    override fun openWith(pair: Pair<Uri, String?>) {
        with(Intent(Intent.ACTION_VIEW)) {
            val (fromFile, mimetype) = pair
            setDataAndType(fromFile, mimetype)
            startActivity(this)
        }
    }
}
