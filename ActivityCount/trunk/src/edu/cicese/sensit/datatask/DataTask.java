package edu.cicese.sensit.datatask;

import android.os.Looper;
import android.util.Log;
import edu.cicese.sensit.Utilities;
import edu.cicese.sensit.datatask.data.Data;
import edu.cicese.sensit.datatask.data.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 12:24 PM
 */
public abstract class DataTask implements Runnable {
	private final static String TAG = "SensIt.DataTask";
	private final static int DEFAULT_PERIOD_TIME = 1000;
	protected List<Output> outputs;
	private float sampleFrequency; // Sample frequency
	protected long periodTime; // Sleep time for each cycle (period time in milliseconds)
	private Thread thread = null;
	private boolean running = false;
	private String name = null;
	private boolean triggered;

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

	public DataTask() {
		// thread = new Thread(this);
		running = false;
		triggered = false;
		setPeriodTime(DEFAULT_PERIOD_TIME);
	}

	protected void clearOutputs() {
		if (outputs != null)
			outputs.clear();
		outputs = new ArrayList<Output>();
	}

	public void clear() {
		clearOutputs();
	}

	protected abstract void compute();

	/**
	 * Inputs & Outputs **
	 */

	protected void addOutput(Output o) {
		if (outputs != null)
			outputs.add(o);
	}

	protected void pushToOutputs(Data data) {
		if (outputs != null) {
			for (Output o : outputs) {
				o.pushData(data);
			}
		}
	}

	/**
	 * Threads & Runnable
	 */

	public void run() {
		Log.i(TAG, "RUNNING!");
		Looper.prepare();
		while (isRunning()) {
//			compute();
//			if (getPeriodTime() > 1) {
			if (name == DataType.ACCELEROMETER.toString()) {
				if (Utilities.isCharging()) {
					try {
						Log.d(TAG, getName() + " sleep!!");
						Thread.sleep(10000);
					} catch (Exception e) {
						Log.e(TAG, "Sleep: " + e);
					}
				}
			}
//			}
		}
	}

	public void start() {
		thread = new Thread(this);
		setRunning(true);
		thread.start();
		Log.d(TAG, getName() + " started");
	}

	public void stop() {
		setRunning(false);
		if (thread != null) {
			thread = null;
		}
	}

	/**
	 * Computes the period time (milliseconds) based on a sample frequency in Hz
	 */
	private long computePeriodTime(float sampleFrequency) {
		long periodTime = (long) ((1.0f / sampleFrequency) * 1000f);
		return periodTime;
	}

	/**
	 * Computes the sample frequency (Hz) based on a period time in milliseconds
	 */
	private float computeSampleFrequency(float periodTime) {
		float sampleFrequency = (1f / periodTime) * 1000f;
		return sampleFrequency;
	}

    /* SETS AND GETS */

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
}