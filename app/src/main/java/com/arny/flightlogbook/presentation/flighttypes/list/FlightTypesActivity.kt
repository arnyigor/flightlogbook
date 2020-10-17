package com.arny.flightlogbook.presentation.flighttypes.list

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.domain.models.FlightType
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS
import com.arny.helpers.utils.*
import kotlinx.android.synthetic.main.flight_types_list_layout.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class FlightTypesActivity : MvpAppCompatActivity(), FlightTypesView, View.OnClickListener {
    private var typesAdapter: FlightTypesAdapter? = null
    @InjectPresenter
    lateinit var flightTypesPresenter: FlightTypesPresenter

    @ProvidePresenter
    fun provideFlightTypesPresenter(): FlightTypesPresenter {
        return FlightTypesPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_types)
        setupActionBar(R.id.tool_bar) {
            title = getString(R.string.str_flight_types)
            this?.setDisplayHomeAsUpEnabled(true)
        }
        val request = getExtra<Boolean>(CONSTS.REQUESTS.REQUEST) == true
        if (request) {
            supportActionBar?.title = getString(R.string.str_select_flight_type)
        }
        fab_add_flight_type.setOnClickListener(this)
        initAdapter(request)
        flightTypesPresenter.loadTypes()
    }

    private fun initAdapter(request: Boolean) {
        typesAdapter = FlightTypesAdapter(request, object : FlightTypesAdapter.FlightTypesListener {
            override fun onEditType(position: Int, item: FlightType) {
                showEditDialog(item)
            }

            override fun onDeleteType(item: FlightType) {
                showConfirmDeleteDialog(item)
            }

            override fun onItemClick(position: Int, item: FlightType) {
                putExtras(Activity.RESULT_OK) {
                    putExtra(CONSTS.EXTRAS.EXTRA_FLIGHT_TYPE, item.id)
                }
                onBackPressed()
            }
        })
        rv_flight_types.layoutManager = LinearLayoutManager(this)
        rv_flight_types.adapter = typesAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_add_flight_type -> {
                val btnOkText = getString(android.R.string.ok)
                val btnCancelText = getString(android.R.string.cancel)
                inputDialog(
                        this,
                        getString(R.string.enter_flight_type),
                        null,
                        null,
                        null,
                        btnOkText,
                        btnCancelText,
                        dialogListener = { result ->
                            flightTypesPresenter.addType(result)
                        })
            }
        }
    }

    private fun showConfirmDeleteDialog(item: FlightType) {
        alertDialog(
                this,
                getString(R.string.confirm_delete_flight_type),
                null,
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                true,
                onConfirm = { flightTypesPresenter.removeFlightType(item) }
        )
    }

    private fun showEditDialog(item: FlightType) {
        inputDialog(
                this,
                getString(R.string.str_edt_flight_type),
                "",
                "",
                item.typeTitle,
                getString(R.string.str_ok),
                getString(R.string.str_cancel),
                false,
                InputType.TYPE_CLASS_TEXT,
                dialogListener = { result ->
                    flightTypesPresenter.editFlightTypeTitle(item, result)
                }
        )
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

    override fun toastError(msg: String?) {
        ToastMaker.toastError(this, msg)
    }

    override fun updateAdapter(list: List<FlightType>) {
        typesAdapter?.addAll(list)
    }

    override fun showEmptyView(vis: Boolean) {
        tv_flight_types_empty_view.isVisible = vis
    }
}
