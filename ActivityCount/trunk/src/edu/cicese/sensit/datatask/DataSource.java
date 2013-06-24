package edu.cicese.sensit.datatask;

import edu.cicese.sensit.sensor.Sensor;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 05:22 PM
 */
public class DataSource {
	private final static String TAG = "SensIt.DataSource";

	private final static int DEFAULT_PERIOD_TIME = 1000;
	private float sampleFrequency; // Sample frequency
	protected long periodTime; // Sleep time for each cycle (period time in milliseconds)
	private Thread thread = null;
	private boolean running = false;
	private String name = null;
	private boolean triggered;

	Sensor sensor;

	public DataSource(Sensor sensor) {
		super();
		this.sensor = sensor;
	}

	public void start() {
		sensor.start();
//		super.start();
	}

	public void stop() {
//		super.stop();
		sensor.stop();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}