package edu.cicese;

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
	public static boolean isSensing;
	public static float batteryLevel = 100;

	// Log entries
	public static final int LOG_SIZE = 100;

	// Sample RATE
	public static final int RATE = 40000; // 1000000 = 1 second | 40000 -> 25 samples per minute (1000/40) = 25Hz

	// Sample period
	public static final int MAIN_EPOCH = 60000; // milliseconds

	// Activity count chart range
	public static final int GRAPH_RANGE = 600000; // milliseconds



	// Battery level sample rate
	public static final int BATTERY_RATE = 600000; // milliseconds

	public static long getEpoch() {
		return MAIN_EPOCH;
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
		isSensing = false;
	}

	public static int getBatteryLevel() {
		return (int)batteryLevel;
	}
}
