package edu.cicese.sensit;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AccelerometerManager implements SensorEventListener {

	private static SensorManager sensorManager;
	private static SensingService sensingService;

	private ActivityCountThread activityCountThread;

	private Sensor accelerometerSensor;

	private List<AccelerometerMeasure> accMeasures = new ArrayList<AccelerometerMeasure>();
	private long beginTimestamp;

	public static Queue<ActivityCount> activityCounts = new LinkedList<ActivityCount>();

	// indicates whether or not Accelerometer Sensor is isSupported
	private Boolean isSupported;

	// indicates whether or not Accelerometer Sensor is running
	private boolean isRunning = false;

	private long lastTimestamp;
	private long wantedPeriod;

	public AccelerometerManager() {
		AccelerometerCountUtil.initiateGravity();

		activityCountThread = new ActivityCountThread(this);

		/*sensorManager = (SensorManager) SensingService().getSystemService(Context.SENSOR_SERVICE);
		if (isSupported()) {
			accelerometerSensor = getSensorList().get(0);
		}*/
	}

	public void startListening(SensingService sensingService) {
		if (Utilities.sensing) {
			Log.i("ACC", "Listening...");

			lastTimestamp = 0;
			wantedPeriod = Utilities.RATE * 1000000L;

			accMeasures.clear();
			beginTimestamp = System.currentTimeMillis();
//			isRunning = sensorManager.registerListener(this, accelerometerSensor, Utilities.RATE /*SensorManager.SENSOR_DELAY_GAME*/);
			isRunning = sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
			AccelerometerManager.sensingService = sensingService;
		}
	}

	public void startListening() {
		startListening(sensingService);
	}

	// Unregisters listener
	public void stopListening() {
		isRunning = false;
		Log.i("ACC", "Unregister");
		if (sensorManager != null/* && sensorEventListener != null*/) {
			sensorManager.unregisterListener(this);
		}
	}

	// Returns true if the manager is listening to orientation changes
	public boolean isListening() {
		Log.i("ACC", "Is listening?");
		return isRunning;
	}

	// Returns true if at least one Accelerometer sensor is available
	public boolean isSupported() {
		Log.i("ACC", "Is supported?");
		/*if (isSupported == null) {
			if (SensingService.getContext() != null) {
				List<Sensor> sensors = getSensorList();
				isSupported = (sensors.size() > 0);
			} else {
				isSupported = false;
			}
		}*/
		return isSupported;
	}

	private List<Sensor> getSensorList() {
		return sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
	}

	/*public void onSensorChanged(SensorEvent sensorEvent) {
		if (Utilities.sensing && !Utilities.sleeping) {
			double x = sensorEvent.values[0];
			double y = sensorEvent.values[1];
			double z = sensorEvent.values[2];

			long timestamp = System.currentTimeMillis();
			long elapsedTime = timestamp - beginTimestamp;

//			long epoch = Utilities.getEpoch();
			if (elapsedTime >= Utilities.epoch) {
//				Utilities.sleeping = false;
				if (!activityCountThread.isAlive()) {
					activityCountThread.setEpoch(Utilities.epoch);
					activityCountThread.run();
				}
				beginTimestamp += Utilities.epoch;
				accMeasures.clear();
			}

			AccelerometerMeasure measure = new AccelerometerMeasure(x, y, z, timestamp);
			accMeasures.add(measure);
		}
	}*/

	public void onSensorChanged(SensorEvent event) {
		long currentTimestamp = System.currentTimeMillis();
		long period = event.timestamp - lastTimestamp;

		/*try {
			Thread.sleep(40);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		if (Utilities.sensing && /*!Utilities.sleeping && */period >= wantedPeriod) {
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];

//			Log.i("ACC", x + ", " + y + ", " + z);

			long elapsedTime = currentTimestamp - beginTimestamp;

			if (elapsedTime >= Utilities.epoch) {

				beginTimestamp += Utilities.epoch;

				if (!activityCountThread.isAlive()) {
					activityCountThread.setEpoch(Utilities.epoch);
					activityCountThread.run();
				}

				accMeasures.clear();
			}

			AccelerometerMeasure measure = new AccelerometerMeasure(x, y, z, currentTimestamp);
			accMeasures.add(measure);

			lastTimestamp = event.timestamp;
		}
	}

	public void setBeginTimestamp(long timestamp) {
		beginTimestamp += timestamp;
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

//		sensingService.updateNotification(activityCount.getCount());

		/*Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putLong("timestamp", activityCount.getTimestamp());
		bundle.putInt("count", activityCount.getCount());
		msg.setData(bundle);

		MainActivity.handlerUI.sendMessage(msg);*/

		ObjectMapper mapper = new ObjectMapper();
		List<ActivityCount> list = new ArrayList<ActivityCount>();
		list.add(activityCount);
		try {
			if (isSDCardWriteable()) {
//				Log.i("ACC", "Saving at: " + "/ac_" + activityCount.getTimestamp() + ".json --> " + mapper.writeValueAsString(list));
//
//				Utilities.saveString("activity_counts/", "ac_" + activityCount.getTimestamp() + ".json", mapper.writeValueAsString(list));

				/*File file = new File("/sdcard/activity_counts/");
				file.mkdirs();

				FileWriter fw = new FileWriter(file + "/ac_" + activityCount.getTimestamp() + ".json");
				BufferedWriter out = new BufferedWriter(fw);

				out.write(mapper.writeValueAsString(list));

				out.flush();
				out.close();*/
			}
			else {
				Log.i("ACC", "No sdcard");
				/*FileOutputStream fOut = sensingService.openFileOutput("/activity_counts/ac_" + activityCount.getTimestamp() + ".json", Context.MODE_WORLD_READABLE);
				OutputStreamWriter osw = new OutputStreamWriter(fOut);

				osw.write(mapper.writeValueAsString(list));

				osw.flush();
				osw.close();*/
			}

		} catch (Exception e) {
			Log.e("ACC", e.getMessage());
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
