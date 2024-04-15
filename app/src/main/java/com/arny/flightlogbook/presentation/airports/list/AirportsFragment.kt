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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.arny.core.utils.getExtra
import com.arny.core.utils.getSystemLocale
import com.arny.core.utils.setDrawableRightClick
import com.arny.core.utils.toastError
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.models.Airport
import com.arny.flightlogbook.data.models.AirportRequestType
import com.arny.flightlogbook.databinding.FAirportsBinding
import com.arny.flightlogbook.presentation.mvp.BaseMvpFragment
import com.arny.flightlogbook.presentation.navigation.OpenDrawerListener
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class AirportsFragment : BaseMvpFragment(), AirportsView {
    private val args: AirportsFragmentArgs by navArgs()
    private lateinit var binding: FAirportsBinding
    private var openDrawerListener: OpenDrawerListener? = null

    @Inject
    lateinit var presenterProvider: Provider<AirportsPresenter>
    private val presenter by moxyPresenter { presenterProvider.get() }
    private lateinit var airportsAdapter: AirportsAdapter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is OpenDrawerListener) {
            openDrawerListener = context
        }
    }

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
        title = getString(R.string.airports)
        val isRequest = args.isRequest
        openDrawerListener?.onChangeHomeButton(isRequest)
        val requestType = args.requestType
        val navController = view.findNavController()
        initAdapter(isRequest, requestType, navController)
        presenter.onQueryChange(Observable.create { e ->
            binding.edtAirport.doAfterTextChanged {
                if (binding.edtAirport.isFocused) {
                    e.onNext(it.toString())
                }
            }
        })
        binding.fabAddAirport.setOnClickListener {
            navController.navigate(
                AirportsFragmentDirections.actionNavAirportsToAirportEditFragment(-1)
            )
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

    private fun initAdapter(
        isRequest: Boolean,
        requestType: AirportRequestType,
        navController: NavController
    ) {
        airportsAdapter =
            AirportsAdapter(requireContext().getSystemLocale()?.language == "ru") { _, item ->
                if (isRequest) {
                    setFragmentResult(
                        requestType.toString(),
                        bundleOf(CONSTS.EXTRAS.EXTRA_AIRPORT to item)
                    )
                    navController.popBackStack()
                } else {
                    item.id?.let {
                        navController.navigate(
                            AirportsFragmentDirections.actionNavAirportsToAirportEditFragment(it)
                        )
                    }
                }
            }
        with(binding.rvAirports) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = airportsAdapter
        }
    }

    override fun setAirports(list: List<Airport>) {
        airportsAdapter.submitList(list.toMutableList())
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
