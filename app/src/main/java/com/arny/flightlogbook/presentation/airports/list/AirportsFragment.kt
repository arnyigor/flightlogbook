package com.arny.flightlogbook.presentation.airports.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.CONSTS.EXTRAS.EXTRA_AIRPORT_ID
import com.arny.core.utils.getSystemLocale
import com.arny.core.utils.launchActivity
import com.arny.core.utils.toastError
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import io.reactivex.Observable
import kotlinx.android.synthetic.main.f_airports.*
import moxy.ktx.moxyPresenter

class AirportsFragment : BaseMvpFragment(), AirportsView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = AirportsFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private var appRouter: AppRouter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    private lateinit var airportsAdapter: AirportsAdapter
    private val presenter by moxyPresenter { AirportsPresenter() }

    override fun getLayoutId(): Int = R.layout.f_airports

    override fun getTitle(): String? = getString(R.string.airports)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequest = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        initAdapter(isRequest)
        presenter.onQueryChange(Observable.create { e ->
            edtAirport.doAfterTextChanged {
                if (edtAirport.isFocused) {
                    e.onNext(it.toString())
                }
            }
        })

        fabAddAirport.setOnClickListener {
            launchActivity<FragmentContainerActivity> {
                action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT
            }
        }
    }

    private fun initAdapter(isRequest: Boolean) {
        airportsAdapter = AirportsAdapter(requireContext().getSystemLocale()?.language == "ru")
        airportsAdapter.setViewHolderListener(object :
                SimpleAbstractAdapter.OnViewHolderListener<Airport> {
            override fun onItemClick(position: Int, item: Airport) {
                if (isRequest) {
                    appRouter?.setResultToTargetFragment(
                            this@AirportsFragment,
                            Intent().apply {
                                putExtra(CONSTS.EXTRAS.EXTRA_AIRPORT, item)
                            })
                } else {
                    appRouter?.navigateTo(
                            NavigateItems.EDIT_AIRPORT,
                            true,
                            bundleOf(
                                    CONSTS.REQUESTS.REQUEST to true,
                                    EXTRA_AIRPORT_ID to item.id
                            ),
                            requestCode = CONSTS.REQUESTS.REQUEST_EDIT_AIRPORT,
                            targetFragment = this@AirportsFragment
                    )
                }
            }
        })
        with(rvAirports) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = airportsAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTS.REQUESTS.REQUEST_EDIT_AIRPORT -> presenter.onEditResultOk()
            }
        }
    }

    override fun setAirports(list: List<Airport>) {
        airportsAdapter.addAll(list)
    }

    override fun showProgress() {
        pbLoader.isVisible = true
    }

    override fun hideProgress() {
        pbLoader.isVisible = false
    }

    override fun showError(message: String?) {
        toastError(message)
    }
}
