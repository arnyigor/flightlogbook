package com.arny.flightlogbook;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.arny.flightlogbook.data.Local;
import com.arny.flightlogbook.data.models.Flight;
import com.arny.flightlogbook.utils.DateTimeUtils;
import com.arny.flightlogbook.utils.MathUtils;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static java.sql.DriverManager.println;
import static org.assertj.core.api.Assertions.assertThat;
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InstrumentsTests {
	private static final Context context = InstrumentationRegistry.getTargetContext();
	private static final Object syncObject = new Object();

	@Test
	public void aa_getAppContext() {
		String packageName = context.getPackageName();
		println("packageName:$packageName");
		assertThat("com.arny.flightlogbook").isEqualTo(packageName);
	}

	@Test
	public void ab_generateObjects() {
		ArrayList<Flight> flights = new ArrayList<>();
		new Thread(() -> {
			for (int i = 0; i < 1000; i++) {
				Flight flight = new Flight();
				String dateFormat = "yyyy-MM-dd HH:mm:ss";
				String dateTime = DateTimeUtils.getDateTime(dateFormat);
				flight.setDatetime(DateTimeUtils.getDateTime(dateTime, dateFormat).getMillis());
				flight.setLogtime(MathUtils.randInt(10, 60));
				Integer airplanetypeid = flight.getAircraft_id() == null ? 0 : flight.getAircraft_id();
				Integer daynight = flight.getDaynight() == null ? 0 : flight.getDaynight();
				Integer ifrvfr = flight.getIfrvfr() == null ? 0 : flight.getIfrvfr();
				Integer flighttype = flight.getFlighttype() == null ? 0 : flight.getFlighttype();
				Long datetime = flight.getDatetime() == null ? 0 : flight.getDatetime();
				Integer logtime = flight.getLogtime() == null ? 0 : flight.getLogtime();
				long addFlight = Local.addFlight(datetime, logtime, flight.getReg_no(), airplanetypeid, daynight, ifrvfr, flighttype, flight.getDescription(), context);
				Log.i(InstrumentsTests.class.getSimpleName(), "ab_generateObjects iter " + i + ":  = " + flight);
				if (addFlight > 0) {
					flights.add(flight);
				}
			}
			synchronized (syncObject) {
				syncObject.notify();
			}
		}).start();
		synchronized (syncObject) {
			try {
				syncObject.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.i(InstrumentsTests.class.getSimpleName(), "ab_generateObjects:  = " + flights.size());
		assertThat(flights).isNotEmpty();
	}
}
