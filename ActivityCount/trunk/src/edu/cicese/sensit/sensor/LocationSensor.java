package edu.cicese.sensit.sensor;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import edu.cicese.sensit.util.Utilities;
import edu.cicese.sensit.util.LocationUtil;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Receives location updates with a BroadcastReceiver. It includes a controller
 * (Runnable) that turns off LOCATION for while (MAX_TIME_WITHOUT_NEW_LOCATION +
 * RESTART_TIME), if there is no new location in certain time
 * (MAX_TIME_WITHOUT_NEW_LOCATION).
 *
 * @author mxpxgx
 */

public class LocationSensor extends Sensor {
	private final static String TAG = "SensIt.LocationSensor";

	private final static String LOCATION_UPDATE_ACTION = "locationUpdate";
	private final static long MIN_RATE_TIME = 120L * 1000L; // 20 seconds
	private final static float MIN_DISTANCE = 2.0F; // In meters
//	private final static long MAX_TIME_WITHOUT_NEW_LOCATION = 2L * 60L * 1000L; // 2 minutes
//	private final static long RESTART_TIME = 5L * 60L * 1000L; // 5 minutes
	private ScheduledThreadPoolExecutor stpe;

	private LocationManager locationManager;
//	private long lastLocationTime;
//	private boolean locationAdded;
	private PendingIntent pendingIntent;

	public LocationSensor(Context context) {
		super(context);
		Log.d(TAG, "Location sensor created");

		setName("L");
//		locationAdded = false;
		// LocationManager initialization
		String service = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) context.getSystemService(service);
		Intent intent = new Intent(LOCATION_UPDATE_ACTION);
		pendingIntent = PendingIntent.getBroadcast(getContext(), 5000, intent, 0);
	}

	private Location registerProvider(String provider) {
		long minTime = this.getPeriodTime() < MIN_RATE_TIME ? MIN_RATE_TIME : getPeriodTime();
		Location location = null;
		try {
			Log.d(TAG, "Time rate: " + minTime);
			locationManager.requestLocationUpdates(provider, minTime, MIN_DISTANCE, pendingIntent);

			// Initialize it with the last known location (it is better than nothing at all).
			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				Log.d(TAG, "New location: " + location.toString());
			}
		} catch (Exception e) {
			Log.e(TAG, "Requesting location updates failed", e);
		}
		Log.d(TAG, "Location Provider registered: " + provider);
		return location;
	}

	@Override
	public void start() {
		super.start();
		setRunning(true);

		Log.d(TAG, "Starting Location sensor");

		// Use any provider (LOCATION, Network or Passive)
		addLocationListenerWithAllProviders();

		IntentFilter intentFilter = new IntentFilter(LOCATION_UPDATE_ACTION);
		getContext().registerReceiver(locationReceiver, intentFilter);

		Log.d(TAG, "Starting Location sensor [done]");

		Sensor.setSensorStatus(Sensor.SENSOR_LOCATION, Sensor.SENSOR_ON);
		refreshStatus();

		if (stpe == null) {
			stpe = new ScheduledThreadPoolExecutor(1);
			stpe.scheduleAtFixedRate(controller, 0,
					Utilities.LOCATION_CHECK_TIME, TimeUnit.MILLISECONDS);
			/*stpe.scheduleAtFixedRate(controller, MAX_TIME_WITHOUT_NEW_LOCATION,
					MAX_TIME_WITHOUT_NEW_LOCATION, TimeUnit.MILLISECONDS);*/
		}
	}

	private void addLocationListenerWithAllProviders() {
		List<String> providers = locationManager.getAllProviders();
		Location location = null;
		/*if (providers.contains(LocationManager.PASSIVE_PROVIDER)) {
			location = registerProvider(LocationManager.PASSIVE_PROVIDER);
		}
		*/
		if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			location = registerProvider(LocationManager.NETWORK_PROVIDER);
		}
		if (providers.contains(LocationManager.GPS_PROVIDER)) {
			location = registerProvider(LocationManager.GPS_PROVIDER);
		}

		// Very important flags
//		lastLocationTime = System.currentTimeMillis();
//		locationAdded = true;
		Log.d(TAG, "Finished adding listener");
	}

	private void removeLocationListener() {
		locationManager.removeUpdates(pendingIntent);
//		locationAdded = false;
	}

	@Override
	public void stop() {
		pause();
		stpe.shutdown();
		super.stop();

		Sensor.setSensorStatus(Sensor.SENSOR_LOCATION, Sensor.SENSOR_OFF);
		refreshStatus();
	}

	private void pause() {
		Log.d(TAG, "Pausing Location sensor");

		removeLocationListener();
		try {
			getContext().unregisterReceiver(locationReceiver);
		}catch(IllegalArgumentException ex){
			Log.e(TAG, ex.toString());
		}
		Sensor.setSensorStatus(Sensor.SENSOR_LOCATION, Sensor.SENSOR_PAUSED);
		refreshStatus();

		setRunning(false);

		Log.d(TAG, "Pausing Location sensor [done]");
	}

	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(LOCATION_UPDATE_ACTION)) {
//				try {
					Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
//				location = null;
				Log.d(TAG, "New location: [" + location.getProvider() + "]" + location.toString());

					LocationUtil.setAtHome(location.getLatitude(), location.getLongitude(), 100);
					Log.d(TAG, "At home? " + LocationUtil.isAtHome());
//				} catch (Exception ex) {
//					Log.e(ex.getClass().getName(), "", ex);
//				}

			}
		}
	};

	private Runnable controller = new Runnable() {
		public void run() {
			Log.d(TAG, "Checking LOCATION [battery check]");
			if (Utilities.isCharging()) {
				if (isRunning()) {
					Log.d(TAG, "Pausing " + getName() + " sensor [battery check]");
//					pause();
				}
			} else {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [battery check]");
//					start();
				}
			}

			/*long timeElapsed = System.currentTimeMillis() - lastLocationTime;

			Log.d(TAG, "Check " + timeElapsed + " > " + MAX_TIME_WITHOUT_NEW_LOCATION);
			if (timeElapsed > MAX_TIME_WITHOUT_NEW_LOCATION && locationAdded) {
				removeLocationListener();
				Log.d(TAG, "LocationListener removed: " + !locationAdded);
			} else if (timeElapsed > (MAX_TIME_WITHOUT_NEW_LOCATION + RESTART_TIME)
					&& !locationAdded) {
				addLocationListenerWithAllProviders();
				Log.d(TAG, "LocationListener added: " + locationAdded);
			}
			Log.d(TAG, "sensorController finished");*/
		}
	};
}
