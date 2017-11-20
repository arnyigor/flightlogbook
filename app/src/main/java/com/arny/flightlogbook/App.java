package com.arny.flightlogbook;

import android.app.Application;
import android.support.multidex.MultiDex;
import com.arny.arnylib.database.DBProvider;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
        MultiDex.install(this);
		Fabric.with(this, new Crashlytics());
		DBProvider.initDB(getApplicationContext(),"PilotDB",12);
	}
}
