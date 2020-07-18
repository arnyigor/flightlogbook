package com.arny.flightlogbook.presentation.main

import android.app.ProgressDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.customfields.edit.view.CustomFieldEditFragment
import com.arny.flightlogbook.presentation.customfields.list.view.CustomFieldsListFragment
import com.arny.flightlogbook.presentation.flights.viewflights.view.FlightListFragment
import com.arny.flightlogbook.presentation.flighttypes.view.FlightTypesFragment
import com.arny.flightlogbook.presentation.planetypes.view.PlaneTypesFragment
import com.arny.flightlogbook.presentation.settings.view.SettingsFragment
import com.arny.flightlogbook.presentation.statistic.view.StatisticFragment
import com.arny.helpers.utils.getFragmentByTag
import com.arny.helpers.utils.replaceFragment
import com.arny.helpers.utils.showSnackBar
import kotlinx.android.synthetic.main.activity_home.*


class MainActivity : AppCompatActivity(), Router {
    companion object {
        private const val DRAWER_SELECTION = "drawer_selection"
        private const val TIME_DELAY = 2000
    }

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
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
        actionBarDrawerToggle = ActionBarDrawerToggle(
                this,
                dlMain,
                toolbar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        )
        dlMain.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true;
        actionBarDrawerToggle.syncState()
        navViewMain.setNavigationItemSelectedListener { item ->
            val navItem = toNavigateItem(item)
            if (navItem != -1L) {
                selectItem(navItem)
                dlMain.closeDrawers()
                true
            } else {
                false
            }
        }
        /*slider.itemAdapter
                .add(
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
        slider.onDrawerItemClickListener = { _, item, _ ->
            selectItem(item.identifier)
            false
        }*/
        if (savedInstanceState == null) {
            selectItem(NavigateItems.MENU_FLIGHTS.index)
        } else {
            try {
                savedInstanceState.getString(DRAWER_SELECTION)?.toLong()?.let { index ->
                    toMenuItem(index)?.let { navViewMain.setCheckedItem(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toNavigateItem(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_flights -> NavigateItems.MENU_FLIGHTS.index
        R.id.menu_flight_types -> NavigateItems.MENU_FLIGHT_TYPES.index
        R.id.menu_plane_types -> NavigateItems.MENU_PLANE_TYPES.index
        R.id.menu_fields -> NavigateItems.MENU_CUSTOM_FIELDS.index
        R.id.menu_settings -> NavigateItems.MENU_SETTINGS.index
        R.id.menu_stats -> NavigateItems.MENU_STATS.index
        else -> -1
    }

    fun lockNavigationDrawer() {
        dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = false
        actionBarDrawerToggle.syncState()
    }

    fun unLockNavigationDrawer() {
        dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
    }

    private fun toMenuItem(index: Long) = when (index) {
        NavigateItems.MENU_FLIGHTS.index -> R.id.menu_flights
        NavigateItems.MENU_FLIGHT_TYPES.index -> R.id.menu_flight_types
        NavigateItems.MENU_PLANE_TYPES.index -> R.id.menu_plane_types
        NavigateItems.MENU_CUSTOM_FIELDS.index -> R.id.menu_fields
        NavigateItems.MENU_SETTINGS.index -> R.id.menu_settings
        NavigateItems.MENU_STATS.index -> R.id.menu_stats
        else -> null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(DRAWER_SELECTION, toNavigateItem(navViewMain.checkedItem).toString())
        super.onSaveInstanceState(outState)
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
            dlMain.closeDrawer(navViewMain)
        }
    }

    override fun onBackPressed() {
        val drawerLayout = dlMain
        if (drawerLayout?.isDrawerOpen(navViewMain) == true) {
            drawerLayout.closeDrawer(navViewMain)
        } else {
            val fragments = supportFragmentManager.fragments
            var isMain = false
            var hasFragments = false
            for (curFrag in fragments) {
                if (curFrag is BackButtonListener) {
                    if (curFrag.onBackPressed()) {
                        lockNavigationDrawer()
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
                unLockNavigationDrawer()
                super.onBackPressed()
            }
        }
    }
}
