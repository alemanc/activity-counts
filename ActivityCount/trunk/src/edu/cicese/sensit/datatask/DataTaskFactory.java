package edu.cicese.sensit.datatask;

import android.content.Context;
import edu.cicese.sensit.datatask.data.DataType;
import edu.cicese.sensit.sensor.AccelerometerSensor;
import edu.cicese.sensit.sensor.BatterySensor;
import edu.cicese.sensit.sensor.BluetoothSensor;
import edu.cicese.sensit.sensor.GpsSensor;
import edu.cicese.sensit.sensor.LinearAccelerometerSensor;
import edu.cicese.sensit.sensor.Sensor;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 05:37 PM
 */
public class DataTaskFactory {
	public static DataTask createDataTask(DataType dataType, Context context) {
		DataTask dataTask = null;

		switch (dataType) {
			case ACCELEROMETER:
				long frameTime = 60000;
				long duration = 60000;
				Sensor AccSensor = AccelerometerSensor.createAccelerometer(context, frameTime, duration);
				AccSensor.setSampleFrequency(25);
				AccSensor.setPeriodTime(0);
				dataTask = new DataSource(AccSensor);
				dataTask.setName("ACCELEROMETER");
				break;
			case LINEAR_ACCELEROMETER:
				long frameTime3 = 60000;
				long duration3 = 60000;
				Sensor LASensor = LinearAccelerometerSensor.createLinearAccelerometer(context, frameTime3, duration3);
				LASensor.setSampleFrequency(25);
				LASensor.setPeriodTime(0);
				dataTask = new DataSource(LASensor);
				dataTask.setName("LINEAR ACCELEROMETER");
				break;
			case BLUETOOTH:
				dataTask = new DataSource(new BluetoothSensor(context));
				dataTask.setName("BLUETOOTH");
				break;
			case GPS:
				dataTask = new DataSource(new GpsSensor(context));
				dataTask.setName("GPS");
				break;
			case GYROSCOPE:
				long frameTime2 = 1000;
				long duration2 = 500;
				Sensor GyroSensor = AccelerometerSensor.createGyroscope(context, frameTime2, duration2);
				GyroSensor.setSampleFrequency(25);
				GyroSensor.setPeriodTime(0);
				dataTask = new DataSource(GyroSensor);
				dataTask.setName("GYROSCOPE");
				break;
			case BATTERY_LEVEL:
				dataTask = new DataSource(new BatterySensor(context));
				dataTask.setName("BATTERY_LEVEL");
				break;

		}
		return dataTask;
	}
}