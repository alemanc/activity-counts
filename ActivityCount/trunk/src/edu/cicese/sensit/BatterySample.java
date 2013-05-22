package edu.cicese.sensit;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 24/10/12
 * Time: 12:51 PM
 */
public class BatterySample {
	private long timestamp;
	private float battery;

	public BatterySample(long timestamp, float battery) {
		this.timestamp = timestamp;
		this.battery = battery;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public float getBattery() {
		return battery;
	}
}
