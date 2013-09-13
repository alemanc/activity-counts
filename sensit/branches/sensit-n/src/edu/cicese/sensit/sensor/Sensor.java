package edu.cicese.sensit.sensor;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.cicese.sensit.ui.SensingNotification;
import edu.cicese.sensit.util.SensitActions;

/**
 * Abstract class with basic sensor functionality. This class cannot be
 * instantiated. Please extends this class when implementing a new sensor.
 * <p/>
 * In implementations of Sensor, Sensed data should be stored in the currentData
 * attribute. If a sensor generates a set of data (not just one), it should use
 * dataList to store them.
 * <p/>
 * Sample frequency could be set but depends to the technology and
 * implementation to really use it.
 *
 * @author mxpxgx
 */
public abstract class Sensor {
	private final static String TAG = "SensIt.Sensor";

	public static final int SENSOR_OFF = 0;
	public static final int SENSOR_ON = 1;
	public static final int SENSOR_PAUSED = 2;

	public static final int SENSOR_LINEAR_ACCELEROMETER = 0;
	public static final int SENSOR_BATTERY = 1;
	public static final int SENSOR_LOCATION = 2;
	public static final int SENSOR_BLUETOOTH = 3;

	private static int[] sensorStatus = new int[SENSOR_BLUETOOTH + 1];

	private final static int DEFAULT_PERIOD_TIME = 1000;
	private final static String DEFAULT_NAME = "Unknown";
	private Context context; // Most sensors need context access
	private String name; // Name of the sensor. It's used by the notifications of the application
	private int sampleFrequency; //
	private long periodTime; // Sleep time for each cycle (period time in milliseconds)
	private volatile boolean sensing = false; // True when sensor is active/running
	protected static SensingNotification sensingNotification;

	private boolean running = false;

	private Sensor() {
		sensing = false;
		setPeriodTime(DEFAULT_PERIOD_TIME);
	}

	protected Sensor(Context context) {
		this();
		this.setContext(context);
		setName(DEFAULT_NAME);
		if (sensingNotification == null) {
			sensingNotification = new SensingNotification(context);
			Log.d(TAG, "SensingNotification initiated");
		}
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Starts the sensing process, should be overridden by a child class.
	 */
	public synchronized void start() {
//		sensing = true;
		sensingNotification.updateNotificationWith(name);
//		Log.d(TAG, "SensingNotification updated");
	}

	/**
	 * Stops the sensing process, should be overridden by a child class.
	 */
	public synchronized void stop() {
//		sensing = false;
		sensingNotification.updateNotificationWithout(name);
//		Log.d(TAG, "SensingNotification updated");
	}

	public void setSampleFrequency(int sampleFrequency) {
		this.sampleFrequency = sampleFrequency;
//		periodTime = computePeriodTime(sampleFrequency);
	}

	public int getSampleFrequency() {
		return sampleFrequency;
	}

	public void setPeriodTime(long periodTime) {
		this.periodTime = periodTime;
//		sampleFrequency = computeSampleFrequency(periodTime);
	}

	protected long getPeriodTime() {
		return periodTime;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public void setSensing(boolean sensing) {
		this.sensing = sensing;
	}

	public boolean isSensing() {
		return sensing;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void refreshStatus() {
		Intent broadcastIntent = new Intent(SensitActions.ACTION_REFRESH_SENSOR);
		context.sendBroadcast(broadcastIntent);
	}

	public static void initiateSensors() {
		for (int i = 0; i < sensorStatus.length; i++) {
			sensorStatus[i] = SENSOR_OFF;
		}
	}

	public static int getSensorStatus(int sensor) {
		return sensorStatus[sensor];
	}

	public static void setSensorStatus(int sensor, int value) {
		sensorStatus[sensor] = value;
	}
}
