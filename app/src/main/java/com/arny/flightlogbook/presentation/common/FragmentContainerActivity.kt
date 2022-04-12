package com.arny.flightlogbook.presentation.common

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_CUSTOM_FIELD
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_FLIGHT
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_PLANE_TYPE
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_AIRPORT
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_CUSTOM_FIELD
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_FLIGHT_TYPE
import com.arny.core.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_PLANE_TYPE
import com.arny.core.utils.replaceFragment
import com.arny.core.utils.replaceFragmentInActivity
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.ActivityFragmentContainerBinding
import com.arny.flightlogbook.presentation.airports.edit.AirportEditFragment
import com.arny.flightlogbook.presentation.airports.list.AirportsFragment
import com.arny.flightlogbook.presentation.customfields.edit.CustomFieldEditFragment
import com.arny.flightlogbook.presentation.customfields.list.CustomFieldsListFragment
import com.arny.flightlogbook.presentation.flights.addedit.view.AddEditFragment
import com.arny.flightlogbook.presentation.flighttypes.list.FlightTypesFragment
import com.arny.flightlogbook.presentation.main.AppRouter
import com.arny.flightlogbook.presentation.main.NavigateItems
import com.arny.flightlogbook.presentation.planetypes.edit.PlaneTypeEditFragment
import com.arny.flightlogbook.presentation.planetypes.list.PlaneTypesFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class FragmentContainerActivity : AppCompatActivity(), AppRouter, HasAndroidInjector {

    private lateinit var binding: ActivityFragmentContainerBinding

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentContainerBinding.inflate(layoutInflater);
        setContentView(binding.root);
        setSupportActionBar(binding.incMainToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getFragmentContainer(intent.action, intent.extras)
            ?.let { replaceFragmentInActivity(it.fragment, R.id.fragment_container, it.tag) }
    }


    private fun getAction(item: NavigateItems): String? {
        return when (item) {
            NavigateItems.PLANE_TYPE_SELECT -> EXTRA_ACTION_SELECT_PLANE_TYPE
            NavigateItems.FLIGHT_TYPE_SELECT -> EXTRA_ACTION_SELECT_FLIGHT_TYPE
            NavigateItems.PLANE_TYPE_EDIT -> EXTRA_ACTION_EDIT_PLANE_TYPE
            NavigateItems.ITEM_EDIT_FIELD -> EXTRA_ACTION_EDIT_CUSTOM_FIELD
            NavigateItems.ITEM_SELECT_FIELD -> EXTRA_ACTION_SELECT_CUSTOM_FIELD
            NavigateItems.AIRPORT_SELECT -> EXTRA_ACTION_SELECT_AIRPORT
            NavigateItems.EDIT_AIRPORT -> EXTRA_ACTION_EDIT_AIRPORT
            else -> null
        }
    }

    private fun getFragmentContainer(
        action: String?,
        bundle: Bundle?
    ): FragmentContainer? {
        return when (action) {
            EXTRA_ACTION_EDIT_FLIGHT -> FragmentContainer(
                AddEditFragment.getInstance(bundle),
                "AddEditFragment"
            )
            EXTRA_ACTION_EDIT_CUSTOM_FIELD -> FragmentContainer(
                CustomFieldEditFragment.getInstance(bundle),
                "CustomFieldEditFragment"
            )
            EXTRA_ACTION_SELECT_CUSTOM_FIELD -> FragmentContainer(
                CustomFieldsListFragment.getInstance(bundle),
                "CustomFieldsListFragment"
            )
            EXTRA_ACTION_SELECT_PLANE_TYPE -> FragmentContainer(
                PlaneTypesFragment.getInstance(bundle),
                "PlaneTypesFragment"
            )
            EXTRA_ACTION_SELECT_FLIGHT_TYPE -> FragmentContainer(
                FlightTypesFragment.getInstance(bundle),
                "FlightTypesFragment"
            )
            EXTRA_ACTION_EDIT_PLANE_TYPE -> FragmentContainer(
                PlaneTypeEditFragment.getInstance(bundle),
                "PlaneTypeEditFragment"
            )
            EXTRA_ACTION_SELECT_AIRPORT -> FragmentContainer(
                AirportsFragment.getInstance(bundle),
                "AirportsFragment"
            )
            EXTRA_ACTION_EDIT_AIRPORT -> FragmentContainer(
                AirportEditFragment.getInstance(bundle),
                "AirportEditFragment"
            )
            else -> null
        }
    }

    override fun navigateTo(
        item: NavigateItems,
        addToBackStack: Boolean,
        bundle: Bundle?,
        targetFragment: Fragment?,
        requestCode: Int?
    ) {
        val fragmentContainer = getFragmentContainer(getAction(item), bundle)
        fragmentContainer?.fragment?.let { fragment ->
            targetFragment?.let {
                fragment.setTargetFragment(it, requestCode ?: 0)
            }
            replaceFragment(
                fragment,
                R.id.fragment_container,
                addToBackStack,
                fragmentContainer.tag
            )
        }
    }

    override fun onReturnResult(intent: Intent?, resultCode: Int) {
        setResult(resultCode, intent)
        onBackPressed()
    }

    override fun setResultToTargetFragment(
        currentFragment: Fragment,
        intent: Intent?,
        resultCode: Int
    ) {
        if (currentFragment.targetFragment != null) {
            currentFragment.targetFragment?.onActivityResult(
                currentFragment.targetRequestCode,
                resultCode,
                intent
            )
        } else {
            setResult(resultCode, intent)
        }
        onBackPressed()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
