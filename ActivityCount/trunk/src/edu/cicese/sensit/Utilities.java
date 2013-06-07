package edu.cicese.sensit;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 28/08/12
 * Time: 02:57 PM
 */
public class Utilities {
	public static final int REFRESH_STATUS = 100;
	public static boolean sensing = false;
	public static boolean charging = false;
	public static boolean epochCharging = false;
	public static boolean ready = true;

	public static String macAddress = null;

	public static final int SENSOR_OFF = 0;
	public static final int SENSOR_ON = 1;
	public static final int SENSOR_PAUSED = 2;

	public static final int SENSOR_LINEAR_ACCELEROMETER = 0;
	public static final int SENSOR_LOCATION = 1;
	public static final int SENSOR_BATTERY = 2;
	public static final int SENSOR_BLUETOOTH = 3;

	public static int[] sensorStatus = new int[SENSOR_BLUETOOTH + 1];


	public static final int UPDATE_ACCELEROMETER = 1;
	public static final int UPDATE_LOCATION = 2;
	public static final int UPDATE_BATTERY = 3;
	public static final int UPDATE_BLUETOOTH = 4;


	public static final long ACCELEROMETER_CHECK_TIME = 10000l;
	public static final long LOCATION_CHECK_TIME = 10000l;

	public static boolean sleeping = false;
	public static float batteryLevel = 100;

	public static int checkingCounts = 0;
	public static long checkingTimestamp;

	// Log entries
	public static final int LOG_SIZE = 100;

	// Sample RATE
//	public static final int RATE = 40000; // 1000000 = 1 second | 40000 -> 25 samples per minute (1000/40) = 25Hz
	public static final int RATE = 40;

	// Sample period
	public static final int MAIN_EPOCH = 60000; // milliseconds

	// No-movement threshold
	public static final int THRESHOLD = 5; // activity counts

	// Sample period when checking if there is movement
	public static final int CHECK_EPOCH = 3000; // milliseconds

	// No-movement sleep time
	public static final int SLEEP_TIME = 30000; // milliseconds

	// Battery level sample rate
	public static final int BATTERY_RATE = 600000; // milliseconds

//	public static boolean wasSleeping = true;
	public static long epoch = CHECK_EPOCH;

	/*public static long getEpoch() {
		if (!wasSleeping) {
			if (checkingCounts == 0) return MAIN_EPOCH;
			return MAIN_EPOCH - CHECK_EPOCH;
		}
		return CHECK_EPOCH;
	}*/

	public static void initiateSensors() {
		for (int i = 0; i <= SENSOR_BLUETOOTH; i++) {
			sensorStatus[i] = SENSOR_OFF;
		}
	}

	public static int isSensing(int sensor) {
		return sensorStatus[sensor];
	}

	public static boolean isReady() {
		return ready;
	}

	public static void setReady(boolean ready) {
		Utilities.ready = ready;
	}

	public static boolean isSensing() {
		return sensing;
	}

	public static void setSensing(boolean sensing) {
		Utilities.sensing = sensing;
	}

	public static boolean isCharging() {
		return charging;
	}

	public static void setCharging(boolean charging) {
		Utilities.charging = charging;
	}

	public static boolean isEpochCharging() {
		return epochCharging;
	}

	public static void setEpochCharging() {
		Utilities.epochCharging |= isCharging();
	}

	public static void resetEpochCharging() {
		Utilities.epochCharging = isCharging();
	}

	public static void setEpoch(long epoch) {
		Log.i("ACC", "Epoch set to: " + epoch);
		Utilities.epoch = epoch;
	}

	public static void saveString(String dir, String filename, String text) {
		File file = new File("/sdcard/" + dir);
		file.mkdirs();

		FileWriter fw;
		try {
			fw = new FileWriter(file + "/" + filename);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(text);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void resetValues() {
//		wasSleeping = true;
		sleeping = false;
		sensing = false;
		checkingCounts = 0;
		epoch = CHECK_EPOCH;
	}

	public static int getBatteryLevel() {
		return (int) batteryLevel;
	}

	public static String getMacAddress(Context context) {
		if (macAddress == null) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			macAddress = wInfo.getMacAddress();
		}
		return macAddress;
	}
}
