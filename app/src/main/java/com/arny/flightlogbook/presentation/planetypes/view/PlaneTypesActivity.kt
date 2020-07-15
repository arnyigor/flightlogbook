package com.arny.flightlogbook.presentation.planetypes.view

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.domain.models.PlaneType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.presentation.planetypes.presenter.PlaneTypesPresenter
import com.arny.helpers.utils.*
import kotlinx.android.synthetic.main.plane_types_layout.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

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
        val request = getExtra<Boolean>("is_request") == true
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
        }, request)
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
        inputDialog(
                this,
                getString(R.string.str_edt_airplane_types),
                "",
                "",
                type.typeName,
                getString(R.string.str_ok),
                getString(R.string.str_cancel),
                false,
                InputType.TYPE_CLASS_TEXT
        ) { result ->
            typeListPresenter.updatePlaneTypeTitle(type, result, position)
        }
    }

    override fun notifyItemChanged(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    override fun toastSuccess(string: String) {
        ToastMaker.toastSuccess(this, string)
    }

    override fun showRemoveDialog(item: PlaneType, position: Int) {
        alertDialog(
                this,
                "${getString(R.string.str_delete)} ${item.typeName}?",
                null,
                "Да",
                "Нет",
                false,
                onConfirm = {
                    typeListPresenter.removeType(item)
                }
        )
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
                inputDialog(
                        context = this,
                        title = getString(R.string.str_add_airplane_types),
                        btnOkText = getString(R.string.str_ok),
                        btnCancelText = getString(R.string.str_cancel),
                        cancelable = false,
                        type = InputType.TYPE_CLASS_TEXT,
                        dialogListener = {
                            if (it.isNotBlank()) {
                                typeListPresenter.addType(it)
                            } else {
                                Toast.makeText(this@PlaneTypesActivity, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show()
                            }
                        }
                )
            }
        }
    }
}