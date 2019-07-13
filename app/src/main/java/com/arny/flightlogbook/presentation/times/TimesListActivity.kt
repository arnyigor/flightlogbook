package com.arny.flightlogbook.presentation.times

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.utils.setupActionBar

class TimesListActivity : AppCompatActivity() {

    @InjectPresenter
    lateinit var timesListPresenter: TimesListPresenter

    @ProvidePresenter
    fun provideTimesListPresenter(): TimesListPresenter {
        return TimesListPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_times_list)
        setupActionBar(R.id.tool_bar) {
            title = "Типы времени"
            this?.setDisplayHomeAsUpEnabled(true)
        }
    }
}
