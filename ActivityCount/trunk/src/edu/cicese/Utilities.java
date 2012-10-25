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
	public static boolean sleeping = true;
	public static boolean isSensing = false;

	public static int checkingCounts = 0;
	public static long checkingTimestamp;

	// Log entries
	public static final int LOG_SIZE = 100;

	// Sample RATE
	public static final int RATE = 40000; // 1000000 = 1 second | 40000 -> 25 samples per minute (1000/40) = 25Hz

	// Sample period
	public static final int MAIN_EPOCH = 60000; // milliseconds

	// Activity count chart range
	public static final int GRAPH_RANGE = 600000; // milliseconds

	// No-movement threshold
	public static final int THRESHOLD = 5; // activity counts

	// Sample period when checking if there is movement
	public static final int CHECK_EPOCH = 3000; // milliseconds

	// No-movement sleep time
	public static final int SLEEP_TIME = 10000; // milliseconds

	// Battery level sample rate
	public static final int BATTERY_RATE = 60000; // milliseconds

	public static long getEpoch() {
		if (!sleeping) {
			if (checkingCounts == 0) return MAIN_EPOCH;
			return MAIN_EPOCH - CHECK_EPOCH;
		}
		return CHECK_EPOCH;
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
		sleeping = true;
		checkingCounts = 0;
		isSensing = false;
	}
}
