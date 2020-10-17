package com.arny.flightlogbook.presentation.common

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_CUSTOM_FIELD
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_FLIGHT
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_EDIT_PLANE_TYPE
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_AIRPORT
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_CUSTOM_FIELD
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_FLIGHT_TYPE
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_SELECT_PLANE_TYPE
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
import com.arny.helpers.utils.replaceFragment
import com.arny.helpers.utils.replaceFragmentInActivity
import kotlinx.android.synthetic.main.about_layout.*
import moxy.MvpAppCompatFragment

class FragmentContainerActivity : AppCompatActivity(), AppRouter {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getFragment(intent.action, intent.extras)
                ?.let { replaceFragmentInActivity(it, R.id.fragment_container) }
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

    private fun getFragment(
            action: String?,
            bundle: Bundle?
    ): MvpAppCompatFragment? {
        return when (action) {
            EXTRA_ACTION_EDIT_FLIGHT -> AddEditFragment.getInstance(bundle)
            EXTRA_ACTION_EDIT_CUSTOM_FIELD -> CustomFieldEditFragment.getInstance()
            EXTRA_ACTION_SELECT_CUSTOM_FIELD -> CustomFieldsListFragment.getInstance(bundle)
            EXTRA_ACTION_SELECT_PLANE_TYPE -> PlaneTypesFragment.getInstance(bundle)
            EXTRA_ACTION_SELECT_FLIGHT_TYPE -> FlightTypesFragment.getInstance(bundle)
            EXTRA_ACTION_EDIT_PLANE_TYPE -> PlaneTypeEditFragment.getInstance(bundle)
            EXTRA_ACTION_SELECT_AIRPORT -> AirportsFragment.getInstance(bundle)
            EXTRA_ACTION_EDIT_AIRPORT -> AirportEditFragment.getInstance(bundle)
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
        val fragment = getFragment(getAction(item), bundle)
        if (fragment != null) {
            if (targetFragment != null) {
                fragment.setTargetFragment(targetFragment, requestCode ?: 0)
            }
            replaceFragment(
                    fragment,
                    R.id.fragment_container,
                    addToBackStack
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
        currentFragment.targetFragment?.onActivityResult(
                currentFragment.targetRequestCode,
                resultCode,
                intent
        )
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
