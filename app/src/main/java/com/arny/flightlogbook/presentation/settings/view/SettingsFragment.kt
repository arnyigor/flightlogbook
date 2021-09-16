package com.arny.flightlogbook.presentation.settings.view

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arny.core.CONSTS
import com.arny.core.utils.Utility
import com.arny.core.utils.alertDialog
import com.arny.core.utils.launchIntent
import com.arny.core.utils.shareFileWithType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.SettingsFragmentBinding
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.settings.presenter.SettingsPresenter
import com.tbruyelle.rxpermissions2.RxPermissions
import moxy.ktx.moxyPresenter

class SettingsFragment : BaseMvpFragment(), SettingsView {
    private lateinit var binding: SettingsFragmentBinding
    private var pDialog: ProgressDialog? = null
    private var rxPermissions: RxPermissions? = null

    companion object {
        fun getInstance(): SettingsFragment = SettingsFragment()
    }

    private val presenter by moxyPresenter { SettingsPresenter() }

    override fun getTitle(): String = getString(R.string.str_settings)

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
        pDialog = ProgressDialog(context)
        pDialog?.setCancelable(false)
        pDialog?.setCanceledOnTouchOutside(false)
        rxPermissions = RxPermissions(this)
        binding.btnLoadFromFile.setOnClickListener {
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
        binding.btnExportToFile.setOnClickListener {
            rxPermissions?.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                ?.subscribe { granted ->
                    if (granted) {
                        presenter.exportToFile()
                    }
                }
        }
        binding.chbAutoExport.setOnCheckedChangeListener { _, isChecked ->
            presenter.onAutoExportChanged(isChecked)
        }
        binding.chbSaveLastFlightData.setOnCheckedChangeListener { _, isChecked ->
            presenter.onSaveLastDataChanged(isChecked)
        }
        binding.ivShareFile.setOnClickListener {
            presenter.onShareFileClick()
        }
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
                    presenter.onFileImport(data?.data)
                }
            }
        }
    }

    override fun shareFile(uri: Uri, fileType: String) {
        requireActivity().shareFileWithType(uri, fileType)
    }

    override fun showError(msg: Int, error: String?) {
        if (error.isNullOrBlank()) {
            binding.tvResultInfo.text = getString(msg)
        } else {
            binding.tvResultInfo.text = getString(msg, error)
        }
    }

    override fun setAutoExportChecked(checked: Boolean) {
        binding.chbAutoExport.isChecked = checked
    }

    override fun setShareFileVisible(visible: Boolean) {
        binding.ivShareFile.isVisible = visible
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
        Utility.showProgress(pDialog, getString(msg))
    }

    override fun hideProgress() {
        Utility.hideProgress(pDialog)
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

    // TODO: 28.06.2020 использовать позже
    private fun openFileWith() {
        presenter.openDefauilFileWith()
    }
}
