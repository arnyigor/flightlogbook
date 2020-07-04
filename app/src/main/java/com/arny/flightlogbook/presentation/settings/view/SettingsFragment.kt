package com.arny.flightlogbook.presentation.settings.view

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.settings.presenter.SettingsPresenter
import com.arny.helpers.utils.Utility
import com.arny.helpers.utils.alertDialog
import com.arny.helpers.utils.launchIntent
import com.arny.helpers.utils.shareFileWithType
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.settings_fragment.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.io.File


class SettingsFragment : MvpAppCompatFragment(), SettingsView {
    private var pDialog: ProgressDialog? = null
    private var rxPermissions: RxPermissions? = null

    companion object {
        fun getInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    @InjectPresenter
    lateinit var settingsPresenter: SettingsPresenter

    @ProvidePresenter
    fun provideSettingsPresenter(): SettingsPresenter {
        return SettingsPresenter()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.str_settings)
        pDialog = ProgressDialog(context)
        pDialog?.setCancelable(false)
        pDialog?.setCanceledOnTouchOutside(false)
        rxPermissions = RxPermissions(this)
        btnLoadFromFile.setOnClickListener {
            rxPermissions?.request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    ?.subscribe { granted ->
                        if (granted) {
                            showAlertImport()
                        }
                    }
        }
        btnExportToFile.setOnClickListener {
            rxPermissions?.request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    ?.subscribe { granted ->
                        if (granted) {
                            settingsPresenter.exportToFile()
                        }
                    }
        }
        chbAutoExport.setOnCheckedChangeListener { _, isChecked ->
            settingsPresenter.onAutoExportChanged(isChecked)
        }
        ivShareFile.setOnClickListener {
            settingsPresenter.onShareFileClick()
        }
    }

    private fun showAlertImport() {
        alertDialog(
                context,
                getString(R.string.str_import_attention),
                getString(R.string.str_import_massage),
                getString(R.string.str_ok),
                getString(R.string.str_cancel),
                true,
                (::chooseFile)
        )
    }

    private fun chooseFile() {
        alertDialog(
                context,
                getString(R.string.str_import_attention),
                getString(R.string.str_import_local_file_or_disk),
                getString(R.string.str_import_local_file),
                getString(R.string.str_import_file_from_disk),
                true, {
            settingsPresenter.loadDefaultFile()
        }, (::requestFile)
        )
    }

    private fun requestFile() {
        launchIntent(CONSTS.REQUESTS.REQUEST_OPEN_FILE) {
            action = Intent.ACTION_GET_CONTENT
            addCategory(Intent.CATEGORY_OPENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "*/*"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_OPEN_FILE -> {
                    val uri = data?.data
                    settingsPresenter.onFileImport(uri)
                }
            }
        }
    }

    override fun shareFile(uri: Uri, fileType: String) {
        requireActivity().shareFileWithType(uri, fileType)
    }

    override fun showError(msg: Int, error: String?) {
        if (error.isNullOrBlank()) {
            tvResultInfo.text = getString(msg)
        } else {
            tvResultInfo.text = getString(msg, error)
        }
    }

    override fun setAutoExportChecked(checked: Boolean) {
        chbAutoExport.isChecked = checked
    }

    override fun setShareFileVisible(visible: Boolean) {
        ivShareFile.isVisible = visible
    }

    override fun showResults(intRes: Int, path: String) {
        tvResultInfo.text = getString(intRes, path)
    }

    override fun showResults(results: String) {
        tvResultInfo.text = results
    }

    override fun hideResults() {
        tvResultInfo.text = ""
    }

    override fun showProgress(msg: Int) {
        Utility.showProgress(pDialog, getString(msg))
    }

    override fun hideProgress() {
        Utility.hideProgress(pDialog)
    }

    // TODO: 28.06.2020 использовать позже
    private fun openFileWith() {
        try {
            val myIntent = Intent(Intent.ACTION_VIEW)
            val sdPath = Environment.getExternalStorageDirectory()
            val file = File("$sdPath/Android/data/com.arny.flightlogbook/files", CONSTS.FILES.EXEL_FILE_NAME)
            val fromFile = Uri.fromFile(file)
            val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(fromFile.toString())
            val mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            myIntent.setDataAndType(fromFile, mimetype)
            startActivity(myIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
