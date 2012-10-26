package edu.cicese;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
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
	private Intent battery;

	public BatteryThread(Context context) {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		battery = context.registerReceiver(null, filter);
	}

	public void done() {
		done = true;
	}

	public void run() {
		while(!done) {
			int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			float batteryPct = (level * 100) / (float) scale;

			long timestamp = System.currentTimeMillis();

			ObjectMapper mapper = new ObjectMapper();
			List<BatterySample> list = new ArrayList<BatterySample>();
			list.add(new BatterySample(timestamp, batteryPct));

			Log.d("ACC", "Battery level: " + batteryPct + "%");

			try {
				Utilities.batteryLevel = batteryPct;
				Utilities.saveString("activity_counts/", "b_" + timestamp + ".json", mapper.writeValueAsString(list));
			} catch (JsonProcessingException e) {
				Log.d("ACC", e.getMessage());
			}

			try {
				sleep(Utilities.BATTERY_RATE);
			} catch (InterruptedException e) { /*ignored*/ }
		}
	}
}
