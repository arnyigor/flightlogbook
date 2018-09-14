package com.arny.flightlogbook.utils


import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

/**
 * The `fragment` is added to the container view with id `frameId`. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, @IdRes frameId: Int, addToback: Boolean = false, tag: String? = null, onLoadFunc: () -> Unit? = {}) {
    supportFragmentManager.transact {
        replace(frameId, fragment)
        if (addToback) {
            addToBackStack(tag)
        }
        onLoadFunc()
    }
}

fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transact {
        replace(frameId, fragment)
    }
}

/**
 * The `fragment` is added to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.addFragmentToActivity(fragment: Fragment, tag: String) {
    supportFragmentManager.transact {
        add(fragment, tag)
    }
}

fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: (ActionBar?.() -> Unit)? = null) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action?.let { it() }
    }
}

fun AppCompatActivity.setupActionBar(toolbar: Toolbar?, action: (ActionBar?.() -> Unit)? = null) {
    setSupportActionBar(toolbar)
    supportActionBar?.run {
        action?.let { it() }
    }
}

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}