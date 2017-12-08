package com.arny.flightlogbook;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import com.arny.arnylib.database.DBProvider;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import io.fabric.sdk.android.Fabric;
public class App extends Application {
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Crashlytics crashlyticsKit = new Crashlytics.Builder()
				.core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
				.build();
		// Инициализируем Fabric с выключенным crashlytics.
		Fabric.with(this, crashlyticsKit);
		DBProvider.initDB(getApplicationContext(), "PilotDB", 12);
	}
}
