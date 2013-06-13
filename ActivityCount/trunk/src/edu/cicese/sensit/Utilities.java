package edu.cicese.sensit;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 28/08/12
 * Time: 02:57 PM
 */
public class Utilities {
	private static boolean sensing = false;
	private static boolean charging = false;
	private static boolean epochCharging = false;
	private static boolean checkEpochCharging = false;
	private static boolean ready = true;

	private static boolean manuallyStopped = false;

	private static String macAddress = null;

	public static final int SENSOR_OFF = 0;
	public static final int SENSOR_ON = 1;
	public static final int SENSOR_PAUSED = 2;

	public static final int SENSOR_LINEAR_ACCELEROMETER = 0;
	public static final int SENSOR_LOCATION = 1;
	public static final int SENSOR_BATTERY = 2;
	public static final int SENSOR_BLUETOOTH = 3;

	private static int[] sensorStatus = new int[SENSOR_BLUETOOTH + 1];

	public static final long ACCELEROMETER_CHECK_TIME = 10000l;
	public static final long LOCATION_CHECK_TIME = 10000l;

	// Sample RATE
//	public static final int RATE = 40000; // 1000000 = 1 second | 40000 -> 25 samples per second (1000000/40000) = 25Hz
	public static final int RATE = 40;

	public static void initiateSensors() {
		for (int i = 0; i <= SENSOR_BLUETOOTH; i++) {
			sensorStatus[i] = SENSOR_OFF;
		}
	}

	public static int getSensorStatus(int sensor) {
		return sensorStatus[sensor];
	}

	public static void setSensorStatus(int sensor, int value) {
		sensorStatus[sensor] = value;
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
		Utilities.checkEpochCharging |= isCharging();
	}

	public static void resetEpochCharging() {
		Utilities.epochCharging = isCharging();
	}

	public static boolean isCheckEpochCharging() {
		return checkEpochCharging;
	}

	/*public static void setCheckEpochCharging() {
		Utilities.checkEpochCharging |= ;
	}*/

	public static void resetCheckEpochCharging() {
		Utilities.checkEpochCharging = isCharging();
	}

	public static boolean isManuallyStopped() {
		return manuallyStopped;
	}

	public static void setManuallyStopped(boolean manuallyStopped) {
		Utilities.manuallyStopped = manuallyStopped;
	}

	public static String getMacAddress(Context context) {
		if (macAddress == null) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			macAddress = wInfo.getMacAddress();
		}
		return macAddress;
	}

	/**
	 * To convert the InputStream to String we use the BufferedReader.readLine()
	 * method. We iterate until the BufferedReader return null which means
	 * there's no more data to read. Each line will appended to a StringBuilder
	 * and returned as String.
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static boolean syncing;
	public static void setSyncing(boolean syncing) {
		Utilities.syncing = syncing;
	}

	public static boolean isSyncing() {
		return Utilities.syncing;
	}
}
