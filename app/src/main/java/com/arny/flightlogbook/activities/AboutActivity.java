package com.arny.flightlogbook.activities;
// imports start==========

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.arny.flightlogbook.R;

public class AboutActivity extends AppCompatActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.colorText));
		setSupportActionBar(toolbar);
		toolbar.setTitle(R.string.str_about);
		final TextView textView = findViewById(R.id.tvAboutInfo);
		String html = "<h1>Автор программы  - Игорь Седой.</h1><cite><p>Программа предназначена для пилотов,которым необходимо фиксировать полетное время.Проект будет дополняться и расширяться в соответствии с пожеланиями пользователей.</p></cite><p>Все недочеты и пожелания пишите на <b><a href=\"mailto:arnyigor@gmail.com?subject=Pilot LogBook\">arnyigor@gmail.com</a></b></p>";
		if (Build.VERSION.SDK_INT >= 24) {
			textView.setText(Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY));

		} else {
			textView.setText(Html.fromHtml(html));
		}
		textView.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				super. onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}