package edu.cicese.sensit.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import edu.cicese.sensit.util.Utilities;
import edu.cicese.sensit.util.SensitActions;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 01:24 PM
 */
public class BatterySensor extends Sensor {
	private final static String TAG = "SensIt.BatterySensor";

	public BatterySensor(Context context) {
		super(context);
		Log.d(TAG, "Battery sensor created");

		setName("B");
	}

	@Override
	public void start() {
		super.start();
		Log.d(TAG, "Starting Battery sensor");

		IntentFilter batteryLevelFilter = new IntentFilter();
		batteryLevelFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		batteryLevelFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		getContext().registerReceiver(batteryReceiver, batteryLevelFilter);

//		handleEnable(Utilities.ENABLE_BATTERY, true);

		Log.d(TAG, "Starting Battery sensor [done]");

		Sensor.setSensorStatus(Sensor.SENSOR_BATTERY, Sensor.SENSOR_ON);
		refreshStatus();
	}

	@Override
	public void stop() {
		Log.d(TAG, "Stopping Battery sensor");

		try {
			getContext().unregisterReceiver(batteryReceiver);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "", e);
		}

		Sensor.setSensorStatus(Sensor.SENSOR_BATTERY, Sensor.SENSOR_OFF);
		refreshStatus();

		Log.d(TAG, "Stopping Battery sensor [done]");

		super.stop();
	}

	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			boolean plugged = false;
			switch (intent.getAction()) {
				case Intent.ACTION_POWER_CONNECTED:
					Log.d(TAG, "Action ACTION_POWER_CONNECTED received");
					plugged = true;
					break;
				case Intent.ACTION_POWER_DISCONNECTED:
					Log.d(TAG, "Action ACTION_POWER_DISCONNECTED received");
					plugged = false;
					break;
				default:
					Log.e(TAG, "Unknown action received: " + intent.getAction());
			}

			Utilities.setCharging(plugged);
			Utilities.setEpochCharging();

			Intent broadcastIntent = new Intent(SensitActions.ACTION_BATTERY_CHANGED);
//			broadcastIntent.putExtra(SensitActions.EXTRA_PLUGGED, plugged);
			context.sendBroadcast(broadcastIntent);

			/*int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int level = -1;
			if (rawLevel >= 0 && scale > 0) {
				level = (rawLevel * 100) / scale;
			}
			Log.d(TAG, "Battery data received: Level:" + level + ", Charging:" + plugged);*/
		}
	};
}