package edu.cicese.sensit.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import edu.cicese.sensit.Utilities;
import edu.cicese.sensit.util.ActivityUtil;
import edu.cicese.sensit.util.SensitActions;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 12:31 PM
 */
public class LinearAccelerometerSensor extends Sensor implements SensorEventListener {
//	public static final String ATT_FRAME_TIME = "frameTime";
//	public static final String ATT_DURATION = "duration";
//	public static final int MAX_FRAME_SIZE = 20;

	private static final String TAG = "SensIt.AccelerometerSensor";

	/* Attributes needed to resume SensorEventListener */
	private SensorManager sensorManager;
	private android.hardware.Sensor accelerometer;
//	private int sensorType;

	/* Attributes need to control sample rate */
	private long lastTimestamp; // last reading time
	private long wantedPeriod; // In nanoseconds

	/* Attributes needed to control frame times */
	private long frameTime; // frame time wanted
	private long duration; // duration of reading wanted within a frame
//	private long frameStartTime; // starting time of a frame
//	private Queue<double[]> frame;

//	private boolean forcedPaused = false;

	private ScheduledThreadPoolExecutor stpe;

	public LinearAccelerometerSensor(Context context, int sensorType, long frameTime, long duration) {
		super(context);
//		this.sensorType = sensorType;
		this.frameTime = frameTime;
		this.duration = duration;
//		frameStartTime = System.currentTimeMillis();
//		frame = new LinkedList<double[]>(); // Adding null elements to the LinkedList implementation of Queue should be prevented.

		// AccelerometerManager initialization
		String service = Context.SENSOR_SERVICE;
		sensorManager = (SensorManager) context.getSystemService(service);
		accelerometer = sensorManager.getDefaultSensor(sensorType);

		Log.d(TAG, "Sensor initialized: " + accelerometer.getName());

		IntentFilter batteryFilter = new IntentFilter();
		batteryFilter.addAction(SensitActions.BATTERY_CHANGED);
		context.registerReceiver(batteryReceiver, batteryFilter);
	}

	/**
	 * Static method to construct an LinearAccelerometerSensor with Linear accelerometer readings
	 */
	public static LinearAccelerometerSensor createLinearAccelerometer(Context context, long frameTime, long duration) {
		LinearAccelerometerSensor sensor = new LinearAccelerometerSensor(context, android.hardware.Sensor.TYPE_LINEAR_ACCELERATION, frameTime, duration);
		Log.d(TAG, "Linear Accelerometer sensor created");
		sensor.setName("LA");
		return sensor;
	}

	@Override
	public void start() {
		super.start();

		if (!Utilities.isCharging()) {
			resume();
		}
		else {
			Sensor.setSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER, Sensor.SENSOR_PAUSED);
			refreshStatus();
		}
	}

	@Override
	public void stop() {
		pause();
		super.stop();

		if (stpe != null) {
			stpe.shutdown();
		}

		try {
			getContext().unregisterReceiver(batteryReceiver);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "", e);
		}

		Sensor.setSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER, Sensor.SENSOR_OFF);
		refreshStatus();
	}

	private void resume() {
		super.start();

		setRunning(true);

		Log.d(TAG, "Starting " + getName() + " sensor");

		if (duration > frameTime) {
			duration = frameTime;
		}
		if (this.getPeriodTime() > duration) {
			this.setPeriodTime(duration);
		}
		wantedPeriod = getPeriodTime() * 1000000L; // milliseconds to nanoseconds?
		if (getSampleFrequency() == 44 || getSampleFrequency() == 4) {
			wantedPeriod = 1000000L;
		}
		lastTimestamp = 0;
		boolean success;
		/*if (this.getSampleFrequency() > 4) {
			Log.d(TAG, "SENSOR_DELAY_GAME");
			success = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		} else {
			Log.d(TAG, "SENSOR_DELAY_NORMAL");
			success = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}*/
		success = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

		wantedPeriod = 40 * 1000000L;

		if (success) {
			sensingNotification.updateNotificationWith(getName());
			Log.d(TAG, "SensingNotification updated");
			super.setSensing(true);
			Log.d(TAG, "SensorEventLister registered!");
		} else {
			super.setSensing(false);
			Log.d(TAG, "SensorEventLister NOT registered!");
		}

		Log.d(TAG, "Starting " + getName() + " sensor [done]");

		Sensor.setSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER, Sensor.SENSOR_ON);
		refreshStatus();

		if (stpe == null) {
			stpe = new ScheduledThreadPoolExecutor(1);
			stpe.scheduleAtFixedRate(pauseController, 0, Utilities.ACCELEROMETER_CHECK_TIME, TimeUnit.MILLISECONDS);
		}
	}

	private void pause() {
		setRunning(false);

		Log.d(TAG, "Pausing " + getName() + " sensor, counts: " + ActivityUtil.counts);

		sensorManager.unregisterListener(this);
		Log.d(TAG, "SensorEventLister unregistered!");

		Sensor.setSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER, Sensor.SENSOR_PAUSED);
		refreshStatus();

		Log.d(TAG, "Pausing " + getName() + " sensor [done]");
	}

	/**
	 * Stores new axis values in a AccelerometerData object.
	 */
//	long n = 100000;

	/*private void setNewReadings(double newX, double newY, double newZ, long timestamp, int counts) {
		long currentFrameTime = timestamp - frameStartTime;
		if (currentFrameTime > frameTime) {
			frameStartTime = frameStartTime + frameTime;
		}

		if (currentFrameTime <= duration) {
			frame.offer(new double[]{newX, newY, newZ, timestamp});

//			Log.d(TAG, "Counts: " + counts);

			if (frame.size() >= this.getSampleFrequency()) {
				// Make available to DataSource
				currentData = createNewData();
			}
		} else if (!frame.isEmpty()) {
			*//*Log.d(TAG, "----------------------------Counts [L]: " + counts);
			Bundle bundle = new Bundle();
			bundle.putInt("counts", counts);
			updateUI(Utilities.UPDATE_ACCELEROMETER, bundle);
			this.counts -= counts;
			// Make available to DataSource
			currentData = createNewData();*//*
		}
	}*/

	/*private void setNewCountReading(long timestamp) {
		long currentFrameTime = timestamp - frameStartTime;
		if (currentFrameTime > frameTime) {
			frameStartTime = frameStartTime + frameTime;
		}

		if (currentFrameTime <= duration) {
			frame.offer(new double[]{newX, newY, newZ, timestamp});
			if (frame.size() >= this.getSampleFrequency()) {
				// Make available to DataSource
				currentData = createNewData();
			}
		} else if (!frame.isEmpty()) {
			// Make available to DataSource
			currentData = createNewData();
		}
	}*/

	/*private AccelerometerFrameData createNewData() {
		double[][] doubleFrame = frame.toArray(new double[frame.size()][]);
		AccelerometerFrameData data;
		switch (sensorType) {
			case Sensor.TYPE_ACCELEROMETER:
				data = new AccelerometerFrameData(DataType.ACCELEROMETER, doubleFrame);
				break;
			case Sensor.TYPE_GYROSCOPE:
				data = new AccelerometerFrameData(DataType.GYROSCOPE, doubleFrame);
				break;
			default:
				data = new AccelerometerFrameData(DataType.LINEAR_ACCELEROMETER, doubleFrame);
		}

		if (frame.size() > 0) {
			long time = (long) (doubleFrame[frame.size() - 1][3] - doubleFrame[0][3]);
			int freq = computeFrameFrequency(time);
			autoFixFrequency(freq);

//			Log.d(TAG, "Accelerometer: " + (doubleFrame[frame.size() - 1][3]) + " - " + (doubleFrame[0][3]));
//			Log.d(TAG, "Accelerometer with time: " + time + "ms " + frame.size() + " samples.");
//			Log.d(TAG, "Accelerometer with frequency: " + freq + "Hz");
		}
		frame.clear();
		return data;
	}*/

	/*private int computeFrameFrequency(long frameTime) {
		// Careful not to divide by zero
		return (int) (frame.size() / (frameTime / 1000f));
	}

	private void autoFixFrequency(int freq) {
		// Auto regulate frequency
		if (freq != getSampleFrequency() && getSampleFrequency() != 44 || getSampleFrequency() != 4) {
			long e = 2;
			if (freq > (this.getSampleFrequency() + e)) {
				wantedPeriod = wantedPeriod < (n * 1000L) ? wantedPeriod + n : wantedPeriod;
			} else if (freq < (this.getSampleFrequency() - e)) {
				wantedPeriod = wantedPeriod > n ? wantedPeriod - n : wantedPeriod;
			} else if (n > 100) {
				n /= 10;
			}
		}
	}

	int counts = 0;*/

    /* SensorEventListener methods */

	/**
	 * Stores new accelerometer values when a change is sensed
	 */
	public void onSensorChanged(SensorEvent event) {
		long period = event.timestamp - lastTimestamp;
		Log.d(TAG, "Substracting: "+event.timestamp+" - "+lastTimestamp+" = "+period);
		Log.d(TAG, "Comparing: "+period+" >= "+wantedPeriod);
		if (period >= wantedPeriod) {
			double axisX = event.values[0];
			double axisY = event.values[1];
			double axisZ = event.values[2];

			double magnitude = Math.floor(Math.sqrt((axisX * axisX) + (axisY * axisY) + (axisZ * axisZ)));
			ActivityUtil.counts += magnitude;
			ActivityUtil.checkEpochCounts += magnitude;

			lastTimestamp = event.timestamp;
		}
	}

	public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
	}

	private Runnable pauseController = new Runnable() {
		public void run() {
			Log.d(TAG, "Checking ACC [pause controller]");

			// get 10-seconds-epoch (check-epoch) counts
			int checkEpochCounts = ActivityUtil.checkEpochCounts;
			// reset check-epoch counts
			ActivityUtil.checkEpochCounts -= checkEpochCounts;

			// check if the device was charging within the check-epoch
//			boolean checkEpochCharging = Utilities.isCheckEpochCharging();
			// reset charging-within-check-epoch flag
//			Utilities.resetCheckEpochCharging();

			/*if (Utilities.isCharging()*//* || LocationUtil.isAtHome()*//*) {
				if (isRunning()) {
					Log.d(TAG, "Pausing " + getName() + " sensor [battery check]");
					pause();
				}
//				ActivityUtil.setEpochsInactive(0);
			} else {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [battery check]");
					start();
				}
				*//*else {
					// if the user was active in the check-epoch
					if (isMoving(checkEpochCounts)) {
						// reset inactivity check-epoch counter
						ActivityUtil.setEpochsInactive(0);
					}
					else {
						ActivityUtil.addEpochInactive();
						if (ActivityUtil.getEpochsInactive() > 3)
					}
				}*//*
			}*/



			/*// check if the user was active in the check-epoch
			boolean isMoving = isMoving(checkEpochCounts);

			if (checkEpochCharging) {
				Log.d(TAG, "Adding pause queue [charging]");
				ActivityUtil.setPauseQueue(1);
			} else {
				Log.d(TAG, "Epoch not-charging");
				if (isRunning()) {
					Log.d(TAG, "Running");
					// if inactive, schedule pause-epoch
					if (!isMoving) {
						// the number of paused epochs depends on the inactivity level
						Log.d(TAG, "Adding pause queue [inactivity]");
						ActivityUtil.setPauseQueue(1);
					}
					else {
						Log.d(TAG, "Reset pause queue");
						ActivityUtil.resetPauseQueue();
					}
				}
				else {
					Log.d(TAG, "Not running");
				}
			}

			Log.d(TAG, "Pause queue: " + ActivityUtil.getPauseQueue());
			// if a paused-epoch is scheduled
			if (ActivityUtil.getPauseQueue() != 0) {
				ActivityUtil.setPauseQueue(-1);

				if (isRunning()) {
					Log.d(TAG, "Pausing " + getName() + " sensor [check]");
					pause();
				}
			}
			else {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [check]");
					start();
				}
			}*/

			//TODO Bug la primera vez que se desconecta el cable, se queda pausado.

			if (!Utilities.isCharging()) {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [check]");
					resume();
				}
				else {
					if (!isMoving(checkEpochCounts)) {
						Log.d(TAG, "Pausing " + getName() + " sensor [check]");
						pause();
					}
				}
			}
		}
	};

	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (Utilities.isCharging()) {
				if (isRunning()) {
					Log.d(TAG, "Pausing " + getName() + " sensor [battery check]");
					pause();
				}
			} else {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [battery check]");
					resume();
				}
			}
		}
	};

	private boolean isMoving(int counts) {
		return counts >= ActivityUtil.MIN_ACTIVE_COUNTS;
	}
}