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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AccelerometerManager implements SensorEventListener {

	private static SensorManager sensorManager;
	private static SensingService sensingService;

	private Sensor accelerometerSensor;

	private List<AccelerometerMeasure> accMeasures = new ArrayList<AccelerometerMeasure>();
	private long begin;

	public static Queue<ActivityCount> activityCounts = new LinkedList<ActivityCount>();

	// indicates whether or not Accelerometer Sensor is isSupported
	private Boolean isSupported;

	// indicates whether or not Accelerometer Sensor is running
	private boolean isRunning = false;

	public AccelerometerManager() {
		AccelerometerCountUtil.initiateGravity();

		sensorManager = (SensorManager) SensingService.getContext().getSystemService(Context.SENSOR_SERVICE);
		if (isSupported()) {
			accelerometerSensor = getSensorList().get(0);
		}
	}

	public void startListening(SensingService sensingService) {
		if (Utilities.isSensing) {
			Log.d("ACC", "Listening...");

			accMeasures.clear();
			begin = System.currentTimeMillis();
			isRunning = sensorManager.registerListener(this, accelerometerSensor, Utilities.RATE /*SensorManager.SENSOR_DELAY_GAME*/);
			AccelerometerManager.sensingService = sensingService;
		}
	}

	public void startListening() {
		startListening(sensingService);
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
			if (SensingService.getContext() != null) {
				List<Sensor> sensors = getSensorList();
				isSupported = (sensors.size() > 0);
			} else {
				isSupported = false;
			}
		}
		return isSupported;
	}

	private List<Sensor> getSensorList() {
		return sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
	}

	public void onSensorChanged(SensorEvent sensorEvent) {
		if (Utilities.isSensing) {
			double x = sensorEvent.values[0];
			double y = sensorEvent.values[1];
			double z = sensorEvent.values[2];

			long timestamp = System.currentTimeMillis();
			long elapsedTime = timestamp - begin;

			long epoch = Utilities.getEpoch();
			if (elapsedTime >= epoch) {
				new ActivityCountThread(this).start();
				begin += epoch;
				accMeasures.clear();
			}

			AccelerometerMeasure measure = new AccelerometerMeasure(x, y, z, timestamp);
			accMeasures.add(measure);
		}
	}

	public List<AccelerometerMeasure> getAccMeasures() {
		return accMeasures;
	}

	/*public void getActivityCounts() {
		new ActivityCountThread(new ArrayList<AccelerometerMeasure>(accMeasures)).start();
	}

	public long getEpoch() {
		return Utilities.MAIN_EPOCH;
	}*/

	public void onAccuracyChanged(Sensor sensor, int i) {
//		Log.e("ACC", "Accuracy changed");
	}

	/*public void setTextLog(EditText txtLog) {
		this.txtLog = txtLog;
	}*/

	public void saveActivityCounts(ActivityCount activityCount) {
		if (activityCounts.size() > Utilities.LOG_SIZE) {
			activityCounts.poll();
		}
		activityCounts.add(activityCount);

		sensingService.updateNotification(activityCount.getCount());

		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putLong("timestamp", activityCount.getTimestamp());
		bundle.putInt("count", activityCount.getCount());
		msg.setData(bundle);

		MainActivity.handler.sendMessage(msg);

		ObjectMapper mapper = new ObjectMapper();
		List<ActivityCount> list = new ArrayList<ActivityCount>();
		list.add(activityCount);
		try {
			if (isSDCardWriteable()) {
				Log.d("ACC", "Saving at: " + "/ac_" + activityCount.getTimestamp() + ".json --> " + mapper.writeValueAsString(list));

				Utilities.saveString("activity_counts/", "ac_" + activityCount.getTimestamp() + ".json", mapper.writeValueAsString(list));

			}
			else {
				Log.d("ACC", "No sdcard");
				FileOutputStream fOut = sensingService.openFileOutput("/activity_counts/ac_" + activityCount.getTimestamp() + ".json", Context.MODE_WORLD_READABLE);
				OutputStreamWriter osw = new OutputStreamWriter(fOut);

				osw.write(mapper.writeValueAsString(list));

				osw.flush();
				osw.close();
			}

		} catch (IOException e) {
			Log.d("ACC", e.getMessage());
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
