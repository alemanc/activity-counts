package edu.cicese;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 28/08/12
 * Time: 02:57 PM
 */
public class Utilities {
	// Log entries
	public static final int LOG_SIZE = 100;

	// Sample RATE
	public static final int RATE = 100000; // 1000000 = 1 second

	// Sample period
	public static final int EPOCH = 2000; // milliseconds

	// Activity count chart range
	public static final int GRAPH_RANGE = 600000; // milliseconds

	// No-movement threshold
	public static final int THRESHOLD = 0; // activity counts

	// No-movement sleep time
	public static final int SLEEP_TIME = 60000; // milliseconds

	public static boolean isServiceRunning;
}
