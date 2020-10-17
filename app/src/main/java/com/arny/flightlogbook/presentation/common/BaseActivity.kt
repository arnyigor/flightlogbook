package com.arny.flightlogbook.presentation.common

import android.os.Bundle
import com.arny.flightlogbook.FlightApp
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import moxy.MvpAppCompatActivity

open class BaseActivity : MvpAppCompatActivity() {
    protected var navigator: Navigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = createNavigator()
    }

    open fun createNavigator(): Navigator? = null

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigator?.let { getNavigatorHolder().setNavigator(it) }
    }

    override fun onPause() {
        super.onPause()
        getNavigatorHolder().removeNavigator()
    }

    private fun getNavigatorHolder(): NavigatorHolder = FlightApp.INSTANCE.navigatorHolder

}