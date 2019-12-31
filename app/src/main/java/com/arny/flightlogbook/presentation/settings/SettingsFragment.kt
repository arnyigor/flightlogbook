package com.arny.flightlogbook.presentation.settings

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arny.constants.CONSTS
import com.arny.flightlogbook.R
import com.arny.helpers.utils.alertDialog
import com.arny.helpers.utils.dump
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pDialog = ProgressDialog(context)
        pDialog?.setCancelable(false)
        pDialog?.setCanceledOnTouchOutside(false)
        rxPermissions = RxPermissions(this)
        btn_load_from_file.setOnClickListener {
            alertDialog(context, getString(R.string.str_import_attention), getString(R.string.str_import_massage), getString(R.string.str_ok), getString(R.string.str_cancel), true, {
                launchIntent(CONSTS.REQUESTS.REQUEST_OPEN_FILE) {
                    action = Intent.ACTION_GET_CONTENT
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(SettingsFragment::class.java.simpleName, "onActivityResult: requestCode:$requestCode;resultCode:$resultCode;data:" + data.dump())
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_OPEN_FILE -> {
                    val path = data?.data?.path
                    settingsPresenter.onFileImport(path)
                }
            }
        }
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(context,msg)
    }

    override fun showProgress(msg: String) {
        Utility.showProgress(pDialog, msg)
    }

    override fun hideProgress() {
        Utility.hideProgress(pDialog)
    }


}
