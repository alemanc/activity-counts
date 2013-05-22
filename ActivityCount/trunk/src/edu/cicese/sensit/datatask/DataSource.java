package edu.cicese.sensit.datatask;

import android.util.Log;
import edu.cicese.sensit.datatask.data.Data;
import edu.cicese.sensit.sensor.Sensor;

import java.util.ArrayList;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 05:22 PM
 */
public class DataSource extends DataTask {
	private final static String TAG = "SensIt.DataSource";
	Sensor sensor;

	public DataSource(Sensor sensor) {
		super();
		this.sensor = sensor;
		outputs = new ArrayList<Output>();
		clear();
	}

	@Override
	public void start() {
		sensor.start();
//		super.start();
	}

	@Override
	public void stop() {
//		super.stop();
		sensor.stop();
//		compute();
	}

	@Override
	protected void compute() {
		Data newData;
		Log.i(getClass().getName(), "Asking for new data");
		newData = sensor.getData();
		if (newData != null) {
			this.pushToOutputs(newData);
			Log.i(TAG, "New data pushed");
		} else {
			Log.i(getClass().getName(), "NO DATA");
		}
	}

	@Override
	public void addOutput(Output o) {
		super.addOutput(o);
	}

	/*public boolean isSensing() {
		return sensor.isSensing();
	}*/
}