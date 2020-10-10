package com.arny.flightlogbook.presentation.airports.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.flightlogbook.adapters.SimpleAbstractAdapter
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_AIRPORT_ID
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.helpers.utils.getSystemLocale
import com.arny.helpers.utils.launchActivity
import com.arny.helpers.utils.toastError
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.f_airports.*
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class AirportsFragment : MvpAppCompatFragment(), AirportsView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = AirportsFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private val compositeDisposable = CompositeDisposable()

    private lateinit var airportsAdapter: AirportsAdapter
    private val presenter by moxyPresenter { AirportsPresenter() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.f_airports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequest = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        requireActivity().title = getString(R.string.airports)
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
        airportsAdapter.setViewHolderListener(object : SimpleAbstractAdapter.OnViewHolderListener<Airport> {
            override fun onItemClick(position: Int, item: Airport) {
                if (isRequest) {
                    val requireActivity = requireActivity()
                    if (requireActivity is FragmentContainerActivity) {
                        requireActivity.onSuccess(Intent().apply {
                            putExtra(CONSTS.EXTRAS.EXTRA_AIRPORT, item)
                        })
                        requireActivity.onBackPressed()
                    }
                } else {
                    launchActivity<FragmentContainerActivity> {
                        action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT
                        putExtra(EXTRA_AIRPORT_ID, item.id)
                    }
                }
            }
        })
        with(rvAirports) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = airportsAdapter
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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
