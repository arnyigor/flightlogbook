package com.arny.flightlogbook.presentation.about

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}