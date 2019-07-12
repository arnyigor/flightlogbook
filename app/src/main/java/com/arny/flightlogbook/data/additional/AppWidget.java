package com.arny.flightlogbook.data.additional;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.presentation.flights.addedit.AddEditActivity;

public class AppWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		Intent configIntent = new Intent(context, AddEditActivity.class);

		PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

		remoteViews.setOnClickPendingIntent(R.id.widget, configPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}
}