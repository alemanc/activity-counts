package edu.cicese.sensit.sensor;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import edu.cicese.sensit.MainActivity;
import edu.cicese.sensit.Utilities;
import edu.cicese.sensit.datatask.data.Data;
import edu.cicese.sensit.ui.SensingNotification;

import java.util.ArrayList;
import java.util.List;

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
public abstract class Sensor/* implements Runnable*/ {
	private final static String TAG = "SensIt.Sensor";
	private final static int DEFAULT_PERIOD_TIME = 1000;
	private final static String DEFAULT_NAME = "Unknown";
	private Context context; // Most sensors need context access
	private String name; // Name of the sensor. It's used by the notifications of the application
	private float sampleFrequency; //
	private long periodTime; // Sleep time for each cycle (period time in milliseconds)
	private volatile boolean sensing = false; // True when sensor is active/running
	protected Data currentData = null; // Used when just one sensed data is generated
	protected List<Data> dataList = null; // Used when the sensor generates a set of data values (e.g. access points found by wifi sensors)
	protected static SensingNotification sensingNotification;

	private Thread thread = null;
	private boolean running = false;

	private Sensor() {
		sensing = false;
		dataList = null;
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

	/**
	 * @return the isRunning
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * @param running the isRunning to set
	 */
	public synchronized void setRunning(boolean running) {
		this.running = running;
	}


	/*public void run() {

	}*/


	/**
	 * Starts the sensing process, should be overridden by a child class.
	 */
	public synchronized void start() {
		sensing = true;
		sensingNotification.updateNotificationWith(name);
		Log.d(TAG, "SensingNotification updated");

//		thread = new Thread(this);
//		thread.start();
	}

	/**
	 * Stops the sensing process, should be overridden by a child class.
	 */
	public synchronized void stop() {
		sensing = false;
		sensingNotification.updateNotificationWithout(name);
		Log.d(TAG, "SensingNotification updated");

//		thread = null;
	}

	/**
	 * Returns the data generated/sensed. When data is cleared after accessed
	 * (similar to a pop() from a stack)
	 *
	 * @return
	 */
	public Data getData() {
		if (dataList == null) {
			Data temp = currentData;
			currentData = null;
			return temp;
		} else {
			if (dataList.isEmpty()) {
				return null;
			} else {
				return dataList.remove(0);
			}
		}
	}

	/**
	 * Returns a set of data sensed when required (e.g. access points found by a
	 * Wi-Fi sensor). If DataList is null (not applicable), Returns a list with
	 * only one Data element
	 *
	 * @return
	 */
	public List<Data> getDataList() {
		if (dataList != null) {
			List<Data> tmpList = dataList;
			dataList = null;
			dataList = new ArrayList<Data>();
			return tmpList;
		} else {
			dataList = new ArrayList<Data>(1);
			dataList.add(currentData);
			return dataList;
		}
	}

	/**
	 * Computes the period time (milliseconds) based on a sample frequency in Hz
	 *
	 * @param sampleFrequency
	 * @return
	 */
	private long computePeriodTime(float sampleFrequency) {
		long periodTime = (long) ((1.0f / sampleFrequency) * 1000f);
		return periodTime;
	}

	/**
	 * Computes the sample frequency (Hz) based on a period time in milliseconds
	 *
	 * @param periodTime
	 * @return
	 */
	private float computeSampleFrequency(float periodTime) {
		float sampleFrequency = (float) ((1f / periodTime) * 1000f);
		return sampleFrequency;
	}


	public void setSampleFrequency(float sampleFrequency) {
		this.sampleFrequency = sampleFrequency;
		periodTime = computePeriodTime(sampleFrequency);
	}

	public float getSampleFrequency() {
		return sampleFrequency;
	}

	public void setPeriodTime(long periodTime) {
		this.periodTime = periodTime;
		sampleFrequency = computeSampleFrequency(periodTime);
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

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/*public void handleEnable(int sensor, boolean enable) {
		Message msg = MainActivity.handlerUI.obtainMessage(sensor);
		Bundle bundle = new Bundle();
		bundle.putBoolean("enable", enable);
		msg.setData(bundle);
		MainActivity.handlerUI.sendMessage(msg);
	}*/

	public void refreshStatus() {
		Message msg = MainActivity.handlerUI.obtainMessage(Utilities.REFRESH_STATUS);
		MainActivity.handlerUI.sendMessage(msg);
	}

	public void updateUI(int sensor, Bundle bundle) {
		Message msg = MainActivity.handlerUI.obtainMessage(sensor);
		msg.setData(bundle);
		MainActivity.handlerUI.sendMessage(msg);
	}
}
