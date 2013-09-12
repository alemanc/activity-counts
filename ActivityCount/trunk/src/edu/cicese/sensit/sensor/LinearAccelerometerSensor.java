package edu.cicese.sensit.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import edu.cicese.sensit.util.AccelerometerCountUtil;
import edu.cicese.sensit.datatask.data.AccelerometerData;
import edu.cicese.sensit.util.ActivityUtil;
import edu.cicese.sensit.util.SensitActions;
import edu.cicese.sensit.util.Utilities;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 12:31 PM
 */
public class LinearAccelerometerSensor extends Sensor implements SensorEventListener {
	private static final String TAG = "SensIt.AccelerometerSensor";

	private ArrayList<AccelerometerData> frame = new ArrayList<>();

	private SensorManager sensorManager;
	private android.hardware.Sensor accelerometer;
	private long lastTimestamp; // last reading time
	private long wantedPeriod; // sample rate in nanoseconds
	private boolean hasGravity = false;

	private ScheduledThreadPoolExecutor stpe;

	public LinearAccelerometerSensor(Context context) {
		super(context);

		/*List<android.hardware.Sensor> sensorList = sensorManager.getSensorList(android.hardware.Sensor.TYPE_ALL);
		for (int i = 0; i < sensorList.size(); i++) {
			Log.d(TAG, "Sensor: " + sensorList.get(i).getName());
			Log.d(TAG, "Sensor: " + sensorList.get(i).getType());
		}*/

		// Check if hardware is available
		String service = Context.SENSOR_SERVICE;
		sensorManager = (SensorManager) context.getSystemService(service);
		accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LINEAR_ACCELERATION);
		if (accelerometer == null) {
			hasGravity = true;
			accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
		}

		if (accelerometer == null) {
			Log.d(TAG, "Linear Accelerometer not found");
		} else {
			Log.d(TAG, "Sensor initialized: " + accelerometer.getName());
		}

		IntentFilter batteryFilter = new IntentFilter();
		batteryFilter.addAction(SensitActions.ACTION_BATTERY_CHANGED);
		context.registerReceiver(batteryReceiver, batteryFilter);
	}

	public static LinearAccelerometerSensor createLinearAccelerometer(Context context) {
		LinearAccelerometerSensor sensor = new LinearAccelerometerSensor(context);
		Log.d(TAG, "Linear Accelerometer sensor created");
		sensor.setName("LA");
		return sensor;
	}

	@Override
	public void start() {
		super.start();

		wantedPeriod = getSampleFrequency() * 1000000L;

		resume();
//		if (Utilities.isEnabled()) {
//		} else {
//			Sensor.setSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER, Sensor.SENSOR_PAUSED);
//			refreshStatus();
//		}
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

		/*if (duration > frameTime) {
			duration = frameTime;
		}
		if (this.getPeriodTime() > duration) {
			this.setPeriodTime(duration);
		}
		wantedPeriod = getPeriodTime() * 1000000L; // milliseconds to nanoseconds?
		if (getSampleFrequency() == 44 || getSampleFrequency() == 4) {
			wantedPeriod = 1000000L;
		}*/
		/*if (this.getSampleFrequency() > 4) {
			Log.d(TAG, "SENSOR_DELAY_GAME");
			success = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		} else {
			Log.d(TAG, "SENSOR_DELAY_NORMAL");
			success = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}*/
//		wantedPeriod = 19 * 1000000L;
		lastTimestamp = 0;
		boolean success = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

		if (success) {
			sensingNotification.updateNotificationWith(getName());
			Log.d(TAG, "Starting " + getName() + " sensor [done]");
//			super.setSensing(true);
		} else {
			sensingNotification.updateNotificationWithout(getName());
			Log.d(TAG, "Starting " + getName() + " sensor [error: NOT registered]");
//			super.setSensing(false);
		}

		Sensor.setSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER, Sensor.SENSOR_ON);
		refreshStatus();

		if (stpe == null) {
			stpe = new ScheduledThreadPoolExecutor(1);
			stpe.scheduleAtFixedRate(pauseController, Utilities.ACCELEROMETER_CHECK_TIME, Utilities.ACCELEROMETER_CHECK_TIME, TimeUnit.MILLISECONDS);
		}
	}

	private void pause() {
		setRunning(false);

		Log.d(TAG, "Pausing " + getName() + " sensor, counts: " + ActivityUtil.counts);

		sensorManager.unregisterListener(this);
//		Log.d(TAG, "SensorEventLister unregistered!");

		Sensor.setSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER, Sensor.SENSOR_PAUSED);
		refreshStatus();

		Log.d(TAG, "Pausing " + getName() + " sensor [done]");
	}

	/**
	 * Stores new accelerometer values when a change is sensed
	 */
	public void onSensorChanged(SensorEvent event) {
		long period = event.timestamp - lastTimestamp;
//		Log.d(TAG, "Subtracting: " + event.timestamp + " - " + lastTimestamp + " = " + period);
//		Log.d(TAG, "Comparing: " + period + " >= " + wantedPeriod);
		if (period >= wantedPeriod) {
			double axisX = event.values[0];
			double axisY = event.values[1];
			double axisZ = event.values[2];

			double magnitude;
			if (!hasGravity) {
				magnitude = Math.floor(Math.sqrt((axisX * axisX) + (axisY * axisY) + (axisZ * axisZ)));
			} else {
				double[] axises = AccelerometerCountUtil.getFilteredAcceleration(axisX, axisY, axisZ);
				magnitude = Math.floor(Math.sqrt((axises[0] * axises[0]) + (axises[1] * axises[1]) + (axises[2] * axises[2])));
			}

			//Remove frame
			//TODO Save 'Stop' action in settings**
			/*frame.add(new AccelerometerData(magnitude, System.currentTimeMillis()));
			if (frame.size() >= 2000) {
				ArrayList<AccelerometerData> tmp = (ArrayList<AccelerometerData>) frame.clone();
				frame.clear();
				new Thread(new WriteThread(tmp)).start();
			}*/

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

			if (!Utilities.isCharging()) {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [check]");
					resume();
				} else {
					if (!isMoving(checkEpochCounts)) {
						Log.d(TAG, "Pausing " + getName() + " sensor [check]");
						pause();
					} else {
						Log.d(TAG, "Continuing: " + getName() + " sensor [check][" + checkEpochCounts + "]");
					}
				}
			}

			// if checking battery status
			/*if (Utilities.isBatteryCheckEnabled()) {
				if (!Utilities.isCharging()) {
					if (!isRunning()) {
						Log.d(TAG, "Starting " + getName() + " sensor [check]");
						resume();
					} else {
						if (!isMoving(checkEpochCounts)) {
							Log.d(TAG, "Pausing " + getName() + " sensor [check]");
							pause();
						} else {
							Log.d(TAG, "Continuing: " + getName() + " sensor [check][" + checkEpochCounts + "]");
						}
					}
				}
				else {
					if (isRunning()) {
						Log.d(TAG, "Pausing " + getName() + " sensor [battery check]");
						pause();
					}
				}
			}
			else {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [check]");
					resume();
				} else {
					if (!isMoving(checkEpochCounts) && !Utilities.isCharging()) {
						Log.d(TAG, "Pausing " + getName() + " sensor [check]");
						pause();
					} else {
						Log.d(TAG, "Continuing: " + getName() + " sensor [check][" + checkEpochCounts + "]");
					}
				}
			}*/
		}
	};

	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Action ACTION_BATTERY_CHANGED received");
			if (Utilities.isCharging()) {
				if (!isRunning()) {
					Log.d(TAG, "Starting " + getName() + " sensor [battery check]");
					resume();
				}
			}

//			if (Utilities.isBatteryCheckEnabled()) {
//				if (Utilities.isCharging()) {
//					if (isRunning()) {
//						Log.d(TAG, "Pausing " + getName() + " sensor [battery check]");
//						pause();
//					}
//				} else {
//					if (!isRunning()) {
//						Log.d(TAG, "Starting " + getName() + " sensor [battery check]");
//						resume();
//					}
//				}
//			} else {
//				Log.d(TAG, "Ignore [battery check]");
//			}
		}
	};

	private boolean isMoving(int counts) {
		return counts >= ActivityUtil.MIN_ACTIVE_COUNTS;
	}
}
