package com.arny.flightlogbook.presentation.types

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.PlaneType
import com.arny.flightlogbook.data.utils.*
import com.arny.flightlogbook.data.utils.dialogs.ConfirmDialogListener
import com.arny.flightlogbook.data.utils.dialogs.InputDialogListener
import com.arny.flightlogbook.data.utils.dialogs.confirmDialog
import com.arny.flightlogbook.data.utils.dialogs.inputDialog
import kotlinx.android.synthetic.main.types_layout.*

class PlaneTypesActivity : MvpAppCompatActivity(), PlaneTypesView, View.OnClickListener {
    private var adapter: PlaneTypesAdapter? = null

    @InjectPresenter
    lateinit var typeListPresenter: PlaneTypesPresenter

    @ProvidePresenter
    fun provideTypeListPresenter(): PlaneTypesPresenter {
        return PlaneTypesPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plane_types)
        val toolbar = findViewById<Toolbar>(R.id.types_toolbar)
        setupActionBar(toolbar) {
            this?.title = getString(R.string.str_airplane_types)
            this?.setDisplayHomeAsUpEnabled(true)
        }
        rv_plane_types.layoutManager = LinearLayoutManager(this)
        rv_plane_types.itemAnimator = DefaultItemAnimator()
        adapter = PlaneTypesAdapter(object : PlaneTypesAdapter.FlightTypesListener {
            override fun onTypeEdit(position: Int, item: PlaneType) {
                showEditDialog(item, position)
            }

            override fun onTypeDelete(position: Int, item: PlaneType) {
                showRemoveDialog(item,position)
            }

            override fun onItemClick(position: Int, item: PlaneType) {

            }
        })
        rv_plane_types.adapter = adapter
        btn_remove_all_plane_types.setOnClickListener(this)
        btn_add_plane_type.setOnClickListener(this)
        typeListPresenter.loadTypes()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateAdapter(list: List<PlaneType>) {
        adapter?.addAll(list)
    }

    override fun showEditDialog(type: PlaneType, position: Int) {
        inputDialog(this, getString(R.string.str_edt_airplane_types), "", type.typeName, getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
            override fun onConfirm(newName: String) {
                typeListPresenter.updatePlaneTypeTitle(type,newName,position)
            }

            override fun onCancel() {

            }
        })
    }

    override fun notifyItemChanged(position: Int) {
         adapter?.notifyItemChanged(position)
    }

    override fun itemRemoved(position: Int) {
        adapter?.remove(position)
    }

    override fun clearAdapter() {
        adapter?.clear(true)
    }

    override fun toastSuccess(string: String) {
        ToastMaker.toastSuccess(this, string)
    }

    override fun showRemoveDialog(item: PlaneType, position: Int) {
        confirmDialog(this, "Вы хотите удалить ${item.typeName}", null, "Да", "Нет", false, object : ConfirmDialogListener {
            override fun onConfirm() {
                typeListPresenter.removeType(item)
            }

            override fun onCancel() {

            }
        })
    }

    override fun setAdapterVisible(vis: Boolean) {
        rv_plane_types.setVisible(vis)
    }

    override fun setEmptyViewVisible(vis: Boolean) {
        tv_no_plane_types.setVisible(vis)
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(this, msg)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_remove_all_plane_types -> {
                showAlertDialog(getString(R.string.str_remove_all) + "?",positivePair = Pair(getString(android.R.string.ok),{
                    typeListPresenter.removeAllPlaneTypes()
                }),negativePair = Pair(getString(android.R.string.cancel),{}))
            }
            R.id.btn_add_plane_type -> {
                inputDialog(this, getString(R.string.str_add_airplane_types), "", "", getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, object : InputDialogListener {
                    override fun onConfirm(name: String) {
                        if (!name.empty()) {
                            typeListPresenter.addType(name)
                        } else {
                            Toast.makeText(this@PlaneTypesActivity, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancel() {

                    }
                })

            }
        }
    }

    override fun setBtnRemoveAllVisible(vis: Boolean) {
        btn_remove_all_plane_types.setVisible(vis)
    }
}
