package com.arny.flightlogbook.presentation.main

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.customfields.edit.view.CustomFieldEditFragment
import com.arny.flightlogbook.presentation.customfields.list.view.CustomFieldsListFragment
import com.arny.flightlogbook.presentation.flights.viewflights.view.FlightListFragment
import com.arny.flightlogbook.presentation.flighttypes.view.FlightTypesFragment
import com.arny.flightlogbook.presentation.planetypes.view.PlaneTypesFragment
import com.arny.flightlogbook.presentation.settings.view.SettingsFragment
import com.arny.flightlogbook.presentation.statistic.view.StatisticFragment
import com.arny.helpers.utils.*
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_home.*


class MainActivity : AppCompatActivity(), Drawer.OnDrawerListener, Router {
    companion object {
        private const val DRAWER_SELECTION = "drawer_selection"
        private const val TIME_DELAY = 2000
    }

    private var drawer: Drawer? = null
    private lateinit var toolbar: Toolbar
    private var pDialog: ProgressDialog? = null
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        pDialog = ProgressDialog(this)
        pDialog?.setCancelable(false)
        toolbar = findViewById(R.id.home_toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.fragment_logbook)
        drawer = DrawerBuilder()
                .withActivity(this)
                .withOnDrawerListener(this)
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        PrimaryDrawerItem().withIdentifier(NavigateItems.MENU_FLIGHTS.index)
                                .withName(R.string.fragment_logbook)
                                .withIcon(GoogleMaterial.Icon.gmd_flight_takeoff),
                        PrimaryDrawerItem().withIdentifier(NavigateItems.MENU_PLANE_TYPES.index)
                                .withName(R.string.fragment_plane_types)
                                .withIcon(GoogleMaterial.Icon.gmd_flight),
                        PrimaryDrawerItem().withIdentifier(NavigateItems.MENU_FLIGHT_TYPES.index)
                                .withName(R.string.fragment_flight_types)
                                .withIcon(GoogleMaterial.Icon.gmd_flight),
                        PrimaryDrawerItem().withIdentifier(NavigateItems.MENU_STATS.index)
                                .withName(R.string.fragment_stats)
                                .withIcon(GoogleMaterial.Icon.gmd_equalizer),
                        PrimaryDrawerItem().withIdentifier(NavigateItems.MENU_CUSTOM_FIELDS.index)
                                .withName(R.string.custom_fields)
                                .withIcon(GoogleMaterial.Icon.gmd_equalizer),
                        PrimaryDrawerItem().withIdentifier(NavigateItems.MENU_SETTINGS.index)
                                .withName(R.string.str_settings)
                                .withIcon(GoogleMaterial.Icon.gmd_settings_applications)
                )
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    selectItem(drawerItem.identifier)
                    true
                }
                .build()
        if (savedInstanceState == null) {
            selectItem(NavigateItems.MENU_FLIGHTS.index)
        } else {
            try {
                savedInstanceState.getString(DRAWER_SELECTION)?.parseLong()?.let { drawer!!.setSelection(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(MainActivity::class.java.simpleName, "onActivityResult: requestCode:$requestCode,resultCode:$resultCode," +
                "data:${data.dump()}");
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val state = drawer!!.saveInstanceState(outState)
        state.putString(DRAWER_SELECTION, drawer!!.currentSelection.toString())
        super.onSaveInstanceState(state)
    }

    override fun navigateTo(item: NavigateItems, addToBackStack: Boolean, bundle: Bundle?, targetFragment: Fragment?, requestCode: Int?) {
        selectItem(item.index, addToBackStack, bundle, targetFragment, requestCode)
    }

    override fun onBackPress() {
        super.onBackPressed()
    }

    private fun navigateFragments(position: Long, bundle: Bundle?): Fragment? {
        return when (position) {
            NavigateItems.MENU_FLIGHTS.index -> FlightListFragment.getInstance()
            NavigateItems.MENU_PLANE_TYPES.index -> PlaneTypesFragment.getInstance()
            NavigateItems.MENU_FLIGHT_TYPES.index -> FlightTypesFragment.getInstance()
            NavigateItems.MENU_CUSTOM_FIELDS.index -> CustomFieldsListFragment.getInstance()
            NavigateItems.MENU_STATS.index -> StatisticFragment.getInstance()
            NavigateItems.MENU_SETTINGS.index -> SettingsFragment.getInstance()
            NavigateItems.ITEM_EDIT_FIELD.index -> CustomFieldEditFragment.getInstance(bundle)
            else -> null
        }
    }

    private fun selectItem(
            position: Long,
            addToBackStack: Boolean = false,
            bundle: Bundle? = null,
            targetFragment: Fragment? = null,
            requestCode: Int? = null
    ) {
        val fragmentItem = navigateFragments(position, bundle)
        val fragmentTag = fragmentItem?.javaClass?.simpleName
        var fragment = getFragmentByTag(fragmentTag)
        if (fragment == null) {
            fragment = fragmentItem
        }
        if (fragment != null) {
            if (targetFragment != null) {
                fragment.setTargetFragment(targetFragment, requestCode ?: 0)
            }
            replaceFragment(fragment, R.id.container, addToBackStack)
            drawer?.closeDrawer()
        }
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen) {
            drawer!!.closeDrawer()
        } else {
            val fragments = supportFragmentManager.fragments
            var isMain = false
            var hasFragments = false
            for (curFrag in fragments) {
                if (curFrag is BackButtonListener) {
                    if (curFrag.onBackPressed()) {
                        hasFragments = true
                        break
                    }
                }
                if (curFrag != null && curFrag.isVisible && curFrag is FlightListFragment) {
                    isMain = true
                }
            }
            if (!hasFragments) {
                if (!isMain) {
                    selectItem(NavigateItems.MENU_FLIGHTS.index, requestCode = null)
                } else {
                    if (backPressedTime + TIME_DELAY > System.currentTimeMillis()) {
                        finish()
                    } else {
                        container.showSnackBar(getString(R.string.press_back_again_to_exit))
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onDrawerOpened(drawerView: View) {}

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
}
