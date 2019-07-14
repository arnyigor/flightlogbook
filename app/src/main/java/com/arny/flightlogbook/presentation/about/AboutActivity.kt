package com.arny.flightlogbook.presentation.about

import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView

import com.arny.flightlogbook.R

class AboutActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorTextWhite))
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.str_about)
        val textView = findViewById<TextView>(R.id.tvAboutInfo)
        val html = "<h1>Автор программы  - Игорь Седой.</h1><cite><p>Программа предназначена для пилотов,которым необходимо фиксировать полетное время.Проект будет дополняться и расширяться в соответствии с пожеланиями пользователей.</p></cite><p>Все недочеты и пожелания пишите на <b><a href=\"mailto:arnyigor@gmail.com?subject=Pilot LogBook\">arnyigor@gmail.com</a></b></p>"
        if (Build.VERSION.SDK_INT >= 24) {
            textView.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)

        } else {
            textView.text = Html.fromHtml(html)
        }
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}