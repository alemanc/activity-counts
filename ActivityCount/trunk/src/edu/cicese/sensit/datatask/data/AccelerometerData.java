package edu.cicese.sensit.datatask.data;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 01:25 PM
 */
public class AccelerometerData {
	private double magnitude;
	private long timestamp;

	public AccelerometerData(double magnitude, long timestamp) {
		this.magnitude = magnitude;
		this.timestamp = timestamp;
	}
}