package edu.cicese.sensit.datatask;

import android.content.Context;
import edu.cicese.sensit.datatask.data.DataType;
import edu.cicese.sensit.sensor.AccelerometerSensor;
import edu.cicese.sensit.sensor.BatterySensor;
import edu.cicese.sensit.sensor.LinearAccelerometerSensor;
import edu.cicese.sensit.sensor.Sensor;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 05:37 PM
 */
public class DataSourceFactory {
	public static DataSource createDataSource(DataType dataType, Context context) {
		DataSource dataSource = null;

		switch (dataType) {
			/*case ACCELEROMETER:
				Sensor AccSensor = AccelerometerSensor.createAccelerometer(context);
				AccSensor.setSampleFrequency(25);
				AccSensor.setPeriodTime(0);
				dataTask = new DataSource(AccSensor);
				dataTask.setName("ACCELEROMETER");
				break;*/
			case LINEAR_ACCELEROMETER:
				Sensor LASensor = LinearAccelerometerSensor.createLinearAccelerometer(context);
//				LASensor.setSampleFrequency(25);
//				LASensor.setPeriodTime(0);
				LASensor.setSampleFrequency(AccelerometerSensor.FREQUENCY_10_HZ);
				dataSource = new DataSource(LASensor);
				dataSource.setName("LINEAR ACCELEROMETER");
				break;
			/*case BLUETOOTH:
				dataTask = new DataSource(new BluetoothSensor(context));
				dataTask.setName("BLUETOOTH");
				break;
			case LOCATION:
				dataTask = new DataSource(new LocationSensor(context));
				dataTask.setName("LOCATION");
				break;*/
			/*case GYROSCOPE:
				long frameTime2 = 1000;
				long duration2 = 500;
				Sensor GyroSensor = AccelerometerSensor.createGyroscope(context, frameTime2, duration2);
				GyroSensor.setSampleFrequency(25);
				GyroSensor.setPeriodTime(0);
				dataTask = new DataSource(GyroSensor);
				dataTask.setName("GYROSCOPE");
				break;*/
			case BATTERY_LEVEL:
				dataSource = new DataSource(new BatterySensor(context));
				dataSource.setName("BATTERY_LEVEL");
				break;

		}
		return dataSource;
	}
}