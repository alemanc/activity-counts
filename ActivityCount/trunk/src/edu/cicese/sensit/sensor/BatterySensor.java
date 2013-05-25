package edu.cicese.sensit.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import edu.cicese.sensit.Utilities;
import edu.cicese.sensit.datatask.data.BatteryData;

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

		IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		getContext().registerReceiver(batteryReceiver, batteryLevelFilter);

//		handleEnable(Utilities.ENABLE_BATTERY, true);

		Log.d(TAG, "Starting Battery sensor [done]");

		Utilities.sensorStatus[Utilities.SENSOR_BATTERY] = Utilities.SENSOR_ON;
		refreshStatus();
	}

	@Override
	public void stop() {
		Log.d(TAG, "Stopping Battery sensor");

		try {
			getContext().unregisterReceiver(batteryReceiver);
		} catch (IllegalArgumentException ex) {
			Log.e(TAG, ex.toString());
		}

//		handleEnable(Utilities.ENABLE_BATTERY, false);

		Log.d(TAG, "Stopping Battery sensor [done]");

		Utilities.sensorStatus[Utilities.SENSOR_BATTERY] = Utilities.SENSOR_OFF;
		refreshStatus();

		super.stop();
	}

	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
//			context.unregisterReceiver(this);
			int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int level = -1;
			if (rawLevel >= 0 && scale > 0) {
				level = (rawLevel * 100) / scale;
			}

			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

			Utilities.setCharging(plugged != 0);

			Log.d(TAG, "Battery data received: Level:" + level + ", Charging:" + plugged);

			Bundle bundle = new Bundle();
			bundle.putInt("level", level);
			bundle.putInt("plugged", plugged);
			updateUI(Utilities.UPDATE_BATTERY, bundle);

			setData(level, plugged);
		}
	};

	private void setData(int level, int plugged) {
		currentData = new BatteryData(level, plugged);
	}
}