package com.arny.flightlogbook.presentation.main

import android.os.Bundle
import androidx.fragment.app.Fragment

interface AppRouter {
    fun navigateTo(
            item: NavigateItems,
            addToBackStack: Boolean = false,
            bundle: Bundle? = null,
            targetFragment:
            Fragment? = null,
            requestCode: Int? = null
    )

    fun onBackPress()
}