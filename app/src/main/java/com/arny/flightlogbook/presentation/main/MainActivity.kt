package com.arny.flightlogbook.presentation.main

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.flights.viewflights.view.FlightListFragment
import com.arny.flightlogbook.presentation.flighttypes.view.FlightTypesFragment
import com.arny.flightlogbook.presentation.planetypes.view.PlaneTypesFragment
import com.arny.flightlogbook.presentation.settings.view.SettingsFragment
import com.arny.flightlogbook.presentation.statistic.view.StatisticFragment
import com.arny.helpers.utils.getFragmentByTag
import com.arny.helpers.utils.parseLong
import com.arny.helpers.utils.replaceFragmentInActivity
import com.arny.helpers.utils.showSnackBar
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_home.*

class MainActivity : AppCompatActivity(), Drawer.OnDrawerListener {
    private var drawer: Drawer? = null
    private lateinit var toolbar: Toolbar
    private var context: Context? = null
    private var pDialog: ProgressDialog? = null
    private var backPressedTime: Long = 0

    companion object {
        private const val MENU_FLIGHTS = 0
        private const val MENU_PLANE_TYPES = 1
        private const val MENU_FLIGHT_TYPES = 2
        private const val MENU_STATS = 3
        private const val MENU_SETTINGS = 5
        private const val DRAWER_SELECTION = "drawer_selection"
        private const val TIME_DELAY = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_home)
        pDialog = ProgressDialog(context)
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
                        PrimaryDrawerItem().withIdentifier(MENU_FLIGHTS.toLong())
                                .withName(R.string.fragment_logbook)
                                .withIcon(GoogleMaterial.Icon.gmd_flight_takeoff),
                        PrimaryDrawerItem().withIdentifier(MENU_PLANE_TYPES.toLong())
                                .withName(R.string.fragment_plane_types)
                                .withIcon(GoogleMaterial.Icon.gmd_flight),
                        PrimaryDrawerItem().withIdentifier(MENU_FLIGHT_TYPES.toLong())
                                .withName(R.string.fragment_flight_types)
                                .withIcon(GoogleMaterial.Icon.gmd_flight),
                        PrimaryDrawerItem().withIdentifier(MENU_STATS.toLong())
                                .withName(R.string.fragment_stats)
                                .withIcon(GoogleMaterial.Icon.gmd_equalizer),
                        PrimaryDrawerItem().withIdentifier(MENU_SETTINGS.toLong())
                                .withName(R.string.str_settings)
                                .withIcon(GoogleMaterial.Icon.gmd_settings_applications)
                )
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    selectItem(drawerItem.identifier.toInt())
                    true
                }
                .build()
        if (savedInstanceState == null) {
            selectItem(MENU_FLIGHTS)
        } else {
            try {
                savedInstanceState.getString(DRAWER_SELECTION)?.parseLong()?.let { drawer!!.setSelection(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val state = drawer!!.saveInstanceState(outState)
        state.putString(DRAWER_SELECTION, drawer!!.currentSelection.toString())
        super.onSaveInstanceState(state)
    }

    private fun selectItem(position: Int) {
        val fragmentTag = getFragmentTag(position)
        var fragment = getFragmentByTag(fragmentTag)
        if (fragment == null) {
            fragment = when (position) {
                MENU_FLIGHTS -> FlightListFragment.getInstance()
                MENU_PLANE_TYPES -> PlaneTypesFragment.getInstance()
                MENU_FLIGHT_TYPES -> FlightTypesFragment.getInstance()
                MENU_STATS -> StatisticFragment.getInstance()
                MENU_SETTINGS -> SettingsFragment.getInstance()
                else -> null
            }
        }
        if (fragment != null) {
            replaceFragmentInActivity(fragment, R.id.container, fragmentTag)
            drawer!!.closeDrawer()
        }
    }

    private fun getFragmentTag(id: Int): String? {
        return when (id) {
            MENU_FLIGHTS -> "fragment_tag_flights"
            MENU_PLANE_TYPES -> "fragment_tag_plane_types"
            MENU_FLIGHT_TYPES -> "fragment_tag_flight_types"
            MENU_STATS -> "fragment_tag_statistic"
            MENU_SETTINGS -> "fragment_tag_settings"
            else -> null
        }
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen) {
            drawer!!.closeDrawer()
        } else {
            val fragments = supportFragmentManager.fragments
            var isMain = false
            for (curFrag in fragments) {
                if (curFrag != null && curFrag.isVisible && curFrag is FlightListFragment) {
                    isMain = true
                }
            }
            if (!isMain) {
                selectItem(MENU_FLIGHTS)
            } else {
                if (backPressedTime + TIME_DELAY > System.currentTimeMillis()) {
                    super.onBackPressed()
                } else {
                    container.showSnackBar(getString(R.string.press_back_again_to_exit))
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    override fun onDrawerOpened(drawerView: View) {}

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
}
