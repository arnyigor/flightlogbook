package com.arny.flightlogbook.presentation.airports.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.CONSTS
import com.arny.core.CONSTS.EXTRAS.EXTRA_AIRPORT_ID
import com.arny.core.CONSTS.REQUESTS.REQUEST_AIRPORT
import com.arny.core.CONSTS.REQUESTS.REQUEST_AIRPORT_ARRIVAL
import com.arny.core.CONSTS.REQUESTS.REQUEST_AIRPORT_DEPARTURE
import com.arny.core.CONSTS.REQUESTS.REQUEST_SELECT_AIRPORT_DEPARTURE
import com.arny.core.utils.*
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.FAirportsBinding
import com.arny.flightlogbook.domain.models.Airport
import com.arny.flightlogbook.presentation.common.BaseMvpFragment
import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class AirportsFragment : BaseMvpFragment(), AirportsView {
    companion object {
        fun getInstance(bundle: Bundle? = null) = AirportsFragment().apply {
            bundle?.let { arguments = it }
        }
    }

    private lateinit var binding: FAirportsBinding
    private var appRouter: AppRouter? = null

    @Inject
    lateinit var presenterProvider: Provider<AirportsPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }
    private lateinit var airportsAdapter: AirportsAdapter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is AppRouter) {
            appRouter = context
        }
    }

    override fun getTitle(): String = getString(R.string.airports)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FAirportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequest = arguments?.getBoolean(CONSTS.REQUESTS.REQUEST) == true
        initAdapter(isRequest)
        presenter.onQueryChange(Observable.create { e ->
            binding.edtAirport.doAfterTextChanged {
                if (binding.edtAirport.isFocused) {
                    e.onNext(it.toString())
                }
            }
        })

        binding.fabAddAirport.setOnClickListener {
            launchActivity<FragmentContainerActivity> {
                action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT
            }
        }
        binding.edtAirport.setDrawableRightClick {
            binding.edtAirport.setText("")
        }
        setFragmentResultListener(CONSTS.REQUESTS.REQUEST_AIRPORT_EDIT) { _, data ->
            if (data.getExtra<Boolean>(CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT) == true) {
                presenter.onEditResultOk()
            }
        }
    }

    private fun initAdapter(isRequest: Boolean) {
        airportsAdapter =
            AirportsAdapter(requireContext().getSystemLocale()?.language == "ru") { _, item ->
                if (isRequest) {
                    val requestKey =
                        if (arguments.getExtra<Int>(REQUEST_AIRPORT) == REQUEST_SELECT_AIRPORT_DEPARTURE) {
                            REQUEST_AIRPORT_DEPARTURE
                        } else {
                            REQUEST_AIRPORT_ARRIVAL
                        }
                    setFragmentResult(
                        requestKey,
                        bundleOf(CONSTS.EXTRAS.EXTRA_AIRPORT to item)
                    )
                    requireActivity().onBackPressed()
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
        with(binding.rvAirports) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = airportsAdapter
        }
    }

    override fun setAirports(list: List<Airport>) {
        airportsAdapter.submitList(list)
    }

    override fun showProgress() {
        binding.pbLoader.isVisible = true
    }

    override fun hideProgress() {
        binding.pbLoader.isVisible = false
    }

    override fun showError(message: String?) {
        toastError(message)
    }
}
