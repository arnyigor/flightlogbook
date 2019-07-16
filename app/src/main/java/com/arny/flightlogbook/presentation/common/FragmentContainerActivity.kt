package com.arny.flightlogbook.presentation.common

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.interfaces.FragmentResultListener
import com.arny.flightlogbook.presentation.types.PlaneTypesFragment
import com.arny.helpers.utils.getExtra
import com.arny.helpers.utils.replaceFragmentInActivity

class FragmentContainerActivity : AppCompatActivity(), FragmentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val tag = intent.getExtra<String>("fragment_tag")
        if (tag != null) {
            val activityForResult = callingActivity?.className?.substringAfterLast(".") ?: ""
            var fragment: Fragment? = null
            when (tag) {
                "type_list" -> {
                    supportActionBar?.title = getString(R.string.str_airplane_types)
                    fragment = PlaneTypesFragment.getInstance()
                }
            }
            fragment?.let { replaceFragmentInActivity(it, R.id.fragment_container) }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
    }

    override fun onSuccess(map: HashMap<String, String>) {
        for (entry in map) {
            intent.putExtra(entry.key, entry.value)
        }
        setResult(Activity.RESULT_OK, intent)
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
