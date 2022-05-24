package com.arny.flightlogbook.presentation.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.arny.core.utils.showSnackBar
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.ActivityNavigationBinding
import com.google.android.material.navigation.NavigationView

class NavigationActivity : AppCompatActivity(), OpenDrawerListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarNavigation.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_navigation)
                ?.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.nav_home,
                R.id.nav_planeTypes,
                R.id.nav_flightTypes,
                R.id.nav_stats,
                R.id.nav_fields,
                R.id.nav_airports,
                R.id.nav_settings
            ),
            drawerLayout = drawerLayout
        )
        navController?.let {
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }

    override fun onChangeHomeButton(change: Boolean) {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(!change)
            setHomeButtonEnabled(!change)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_navigation)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val controller = findNavController(R.id.nav_host_fragment_content_navigation)
            val isLastFragment = controller.currentDestination?.id == R.id.nav_home
            if (isLastFragment) {
                if (backPressedTime + TIME_DELAY > System.currentTimeMillis()) {
                    super.onBackPressed()
                } else {
                    binding.drawerLayout.showSnackBar(getString(R.string.press_back_again_to_exit))
                }
                backPressedTime = System.currentTimeMillis()
            } else {
                super.onBackPressed()
            }
        }
    }

    private companion object {
        const val TIME_DELAY = 2000
    }
}