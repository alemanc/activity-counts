package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 24/10/12
 * Time: 12:23 PM
 */
public class BatteryThread extends Thread {

	private boolean done = false;
	private Context context;
	IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			Utilities.batteryLevel = (level * 100) / (float) scale;
		}
	};
	

	public BatteryThread(Context context) {
		this.context = context;

		Intent battery = context.getApplicationContext().registerReceiver(null, filter);
		int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		Utilities.batteryLevel = (level * 100) / (float) scale;

		context.getApplicationContext().registerReceiver(batteryReceiver, filter);
	}

	public void done() {
		done = true;
		context.getApplicationContext().unregisterReceiver(batteryReceiver);
	}

	public void run() {
		while(!done) {
			long timestamp = System.currentTimeMillis();

			ObjectMapper mapper = new ObjectMapper();
			List<BatterySample> list = new ArrayList<BatterySample>();
			list.add(new BatterySample(timestamp, Utilities.batteryLevel));

			Log.i("ACC", "Battery level: " + Utilities.batteryLevel + "%");

			try {
				Utilities.saveString("activity_counts/", "b_" + timestamp + ".json", mapper.writeValueAsString(list));
			} catch (Exception e) {
				Log.e("ACC", e.getMessage());
			}

			try {
				sleep(Utilities.BATTERY_RATE);
			} catch (InterruptedException e) { /*ignored*/ }
		}
	}
}
