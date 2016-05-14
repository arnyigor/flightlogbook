package com.arny.flightlogbook;
// imports start==========

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
// imports end==========


//==============Activitystart=========================
public class AboutActivity extends Activity {
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
		// ================Forms Ids end=========================
		// ==================onCreateCode start=========================


	}//============onCreate_end====================
	// ====================CustomCode_start================================

	// ====================CustomCode_end======================================
}// ===================Activity_end==================================
