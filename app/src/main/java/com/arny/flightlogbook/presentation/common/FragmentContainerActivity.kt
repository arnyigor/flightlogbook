package com.arny.flightlogbook.presentation.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.arny.flightlogbook.R
import com.arny.flightlogbook.constants.CONSTS.EXTRAS.EXTRA_ACTION_GET_CUSTOM_FIELD
import com.arny.flightlogbook.presentation.customfields.list.view.CustomFieldsListFragment
import com.arny.helpers.utils.replaceFragmentInActivity
import kotlinx.android.synthetic.main.about_layout.*

class FragmentContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val action = intent.action
//        val activityForResult = callingActivity?.className?.substringAfterLast(".") ?: ""
        var fragment: Fragment? = null
        when (action) {
            EXTRA_ACTION_GET_CUSTOM_FIELD -> {
                fragment = CustomFieldsListFragment.getInstance(request = true)
            }
        }
        fragment?.let { replaceFragmentInActivity(it, R.id.fragment_container) }
    }

    fun onSuccess(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
