package com.arny.flightlogbook.presentation.settings.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arny.constants.CONSTS
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.settings.presenter.SettingsPresenter
import com.arny.helpers.utils.ToastMaker
import com.arny.helpers.utils.Utility
import com.arny.helpers.utils.alertDialog
import com.arny.helpers.utils.launchIntent
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.settings_fragment.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

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
        pDialog = ProgressDialog(context)
        pDialog?.setCancelable(false)
        pDialog?.setCanceledOnTouchOutside(false)
        rxPermissions = RxPermissions(this)
        btnLoadFromFile.setOnClickListener {
            alertDialog(
                    context,
                    getString(R.string.str_import_attention),
                    getString(R.string.str_import_massage),
                    getString(R.string.str_ok),
                    getString(R.string.str_cancel),
                    true,
                    {
                        launchIntent(CONSTS.REQUESTS.REQUEST_OPEN_FILE) {
                            action = Intent.ACTION_GET_CONTENT
                            addCategory(Intent.CATEGORY_OPENABLE)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            }
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            type = "*/*"
                        }
                    })
        }
        btnExportToFile.setOnClickListener {
            settingsPresenter.exportToFile()
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

    override fun toastError(msg: Int, error: String?) {
        if (error.isNullOrBlank()) {
            ToastMaker.toastError(requireContext(), getString(msg))
        } else {
            ToastMaker.toastError(requireContext(), getString(msg, error))
        }
    }

    override fun showResults(intRes: Int, path: String) {
        tvResultInfo.text = getString(intRes, path)
    }

    override fun showProgress(msg: Int) {
        Utility.showProgress(pDialog, getString(msg))
    }

    override fun hideProgress() {
        Utility.hideProgress(pDialog)
    }
}
