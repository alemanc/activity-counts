package edu.cicese;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AccelerometerManager implements SensorEventListener {

	private static SensorManager sensorManager;
	private static AccelerometerService service;

	private Sensor accelerometerSensor;

	private double x;
	private double y;
	private double z;

	private List<AccelerometerMeasure> accMeasures = new ArrayList<AccelerometerMeasure>();
	private long begin;

//	public static List<ActivityCount> activityCounts = new ArrayList<ActivityCount>();
	public static Queue<ActivityCount> activityCounts = new LinkedList<ActivityCount>();

	// indicates whether or not Accelerometer Sensor is isSupported
	private Boolean isSupported;

	// indicates whether or not Accelerometer Sensor is running
	private boolean isRunning = false;

	public AccelerometerManager() {
		AccelerometerCountUtil.initiateGravity();
//		sensorManager = (SensorManager) AccelerometerService.getContext().getSystemService(Context.SENSOR_SERVICE);
//		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
//		if (sensors.size() > 0) {
//			accelerometerSensor = sensors.get(0);
//		}
	}

	public void startListening(AccelerometerService accelerometerService) {
		Log.d("ACC", "Listening...");
		sensorManager = (SensorManager) AccelerometerService.getContext().getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			begin = System.currentTimeMillis();
			isRunning = sensorManager.registerListener(this, sensors.get(0), Utilities.RATE /*SensorManager.SENSOR_DELAY_GAME*/);
			service = accelerometerService;
		}
	}

	public void startListening() {
		startListening(service);
	}

	// Unregisters listener
	public void stopListening() {
		isRunning = false;
		Log.d("ACC", "Unregister");
		if (sensorManager != null/* && sensorEventListener != null*/) {
			sensorManager.unregisterListener(this);
		}
	}

	// Returns true if the manager is listening to orientation changes
	public boolean isListening() {
		Log.d("ACC", "Is listening?");
		return isRunning;
	}

	// Returns true if at least one Accelerometer sensor is available
	public boolean isSupported() {
		Log.d("ACC", "Is supported?");
		if (isSupported == null) {
			if (AccelerometerService.getContext() != null) {
				sensorManager = (SensorManager) AccelerometerService.getContext().getSystemService(Context.SENSOR_SERVICE);
				List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
				isSupported = (sensors.size() > 0);
			} else {
				isSupported = false;
			}
		}
		return isSupported;
	}

	public void onSensorChanged(SensorEvent sensorEvent) {
		x = sensorEvent.values[0];
		y = sensorEvent.values[1];
		z = sensorEvent.values[2];

		accMeasures.add(new AccelerometerMeasure(x, y, z, System.currentTimeMillis()));

		if (System.currentTimeMillis() - begin >= Utilities.EPOCH) {
			// calculate activity counts
			new ActivityCountThread(this).start();

			begin += Utilities.EPOCH;
			accMeasures.clear();
		}
	}

	public List<AccelerometerMeasure> getAccMeasures() {
		return accMeasures;
	}

	public void getActivityCounts() {
		new ActivityCountThread(new ArrayList<AccelerometerMeasure>(accMeasures)).start();
	}

	public long getEpoch() {
		return Utilities.EPOCH;
	}

	public void onAccuracyChanged(Sensor sensor, int i) {
		Log.e("ACC", "Accuracy changed");

	}

	/*public void setTextLog(EditText txtLog) {
		this.txtLog = txtLog;
	}*/

	public void saveActivityCounts(ActivityCount activityCount) {
		if (activityCounts.size() > Utilities.LOG_SIZE) {
			activityCounts.poll();
		}
		activityCounts.add(activityCount);

		service.updateNotification(activityCount.getCount());

		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putLong("timestamp", activityCount.getTimestamp());
		bundle.putInt("count", activityCount.getCount());
		msg.setData(bundle);

		MainActivity.handler.sendMessage(msg);


		try {
			if (isSDCardWriteable()) {

				/*File file = new File("/sdcard/activity_counts/");
				file.mkdirs();

				FileWriter fw = new FileWriter(file + "/" + activityCount.getTimestamp() + ".ac");
				BufferedWriter out = new BufferedWriter(fw);

				out.write(activityCount.getCount() + "");

				out.flush();
				out.close();*/
			}
			else {
				FileOutputStream fOut = service.openFileOutput("/activity_counts/" + activityCount.getTimestamp() + ".ac", Context.MODE_WORLD_READABLE);
				OutputStreamWriter osw = new OutputStreamWriter(fOut);

				osw.write(activityCount.getCount());

				osw.flush();
				osw.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private boolean isSDCardWriteable() {
		boolean externalWriteable;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			externalWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			externalWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			// to know is we can neither read nor write
			externalWriteable = false;
		}

		return externalWriteable;
	}
}
