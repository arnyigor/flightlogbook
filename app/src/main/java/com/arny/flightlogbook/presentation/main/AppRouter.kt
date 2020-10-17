package com.arny.flightlogbook.presentation.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

interface AppRouter {
    fun navigateTo(
            item: NavigateItems,
            addToBackStack: Boolean = false,
            bundle: Bundle? = null,
            targetFragment: Fragment? = null,
            requestCode: Int? = null
    )

    fun setResultToTargetFragment(
            currentFragment: Fragment,
            intent: Intent,
            resultCode: Int = Activity.RESULT_OK
    )

    fun onReturnResult(intent: Intent? = null, resultCode: Int = Activity.RESULT_OK)
}