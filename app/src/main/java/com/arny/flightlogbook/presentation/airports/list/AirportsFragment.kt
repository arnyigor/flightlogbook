package com.arny.flightlogbook.presentation.airports.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.domain.models.Airport
import com.arny.flightlogbook.R
import com.arny.helpers.utils.toastError
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.f_airports.*
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class AirportsFragment : MvpAppCompatFragment(), AirportsView {
    companion object {
        fun getInstance() = AirportsFragment()
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
        airportsAdapter = AirportsAdapter()
        with(rvAirports) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = airportsAdapter
        }

        presenter.onQueryChange(Observable.create { e ->
            edtAirport.doAfterTextChanged {
                if (edtAirport.isFocused) {
                    e.onNext(it.toString())
                }
            }
        })
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
