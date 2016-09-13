package com.arny.flightlogbook;
// imports start==========

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;
// imports end==========


//==============Activitystart=========================
public class AboutActivity extends AppCompatActivity {
	ActionBar actionBar;
	// =============Variables_start================

	// =============Variable_send================
// ====================onCreatestart=========================
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ================Forms Ids start=========================
		TextView txtView = new TextView(this);
		Spanned s = Html.fromHtml("<h1>Автор программы  - Игорь Седой.</h1><cite><p>Программа предназначена для пилотов,которым необходимо фиксировать полетное время.Проект будет дополняться и расширяться в соответствии с пожеланиями пользователей.</p></cite><p>Все недочеты и пожелания пишите на <b><a href=\"mailto:arnyigor@gmail.com?subject=Pilot LogBook\">arnyigor@gmail.com</a></b></p>");
		txtView.setText(s);
		txtView.setMovementMethod(LinkMovementMethod.getInstance());
		setContentView(txtView);
		try {
			actionBar = getSupportActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ================Forms Ids end=========================
		// ==================onCreateCode start=========================


	}//============onCreate_end====================

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				super. onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	// ====================CustomCode_start================================

	// ====================CustomCode_end======================================
}// ===================Activity_end==================================
