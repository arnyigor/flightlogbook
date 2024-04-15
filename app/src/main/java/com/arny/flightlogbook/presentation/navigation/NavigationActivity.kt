package com.arny.flightlogbook.presentation.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.arny.flightlogbook.R
import com.arny.flightlogbook.databinding.ActivityNavigationBinding
import com.arny.flightlogbook.presentation.utils.showSnackBar
import com.google.android.material.navigation.NavigationView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class NavigationActivity : AppCompatActivity(), OpenDrawerListener, HasAndroidInjector {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private var backPressedTime: Long = 0

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarNavigation.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = navController()
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        navController.let {
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }

    private fun navController(): NavController =
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_navigation) as NavHostFragment).navController

    override fun onChangeHomeButton(change: Boolean) {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(!change)
            setHomeButtonEnabled(!change)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = navController()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val controller = navController()
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