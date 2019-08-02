package com.arny.flightlogbook.presentation.planetypes

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.constants.CONSTS
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.helpers.utils.*
import kotlinx.android.synthetic.main.plane_types_layout.*

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
        val request = getExtra<Boolean>("is_request")==true
        if (request) {
            supportActionBar?.title = getString(R.string.str_select_plane_type)
        }
        initAdapter(request)
        fab_add_plane_type.setOnClickListener(this)
        typeListPresenter.loadTypes()
    }

    private fun initAdapter(request: Boolean) {
        rv_plane_types.layoutManager = LinearLayoutManager(this)
        adapter = PlaneTypesAdapter(object : PlaneTypesAdapter.PlaneTypesListener {
            override fun onEditType(position: Int, item: PlaneType) {
                showEditDialog(item, position)
            }

            override fun onDeleteType(position: Int, item: PlaneType) {
                showRemoveDialog(item, position)
            }

            override fun onItemClick(position: Int, item: PlaneType) {
                putExtras(Activity.RESULT_OK) {
                    putExtra(CONSTS.EXTRAS.EXTRA_PLANE_TYPE, item.typeId)
                }
                onBackPressed()
            }
        },request)
        rv_plane_types.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
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

    override fun setEmptyViewVisible(vis: Boolean) {
        tv_no_plane_types.setVisible(vis)
    }

    override fun toastError(msg: String?) {
        ToastMaker.toastError(this, msg)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_add_plane_type -> {
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
}
