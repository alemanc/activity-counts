package edu.cicese;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 06:25 PM
 */
public class AccelerometerMeasure {
	private double axisX, axisY, axisZ;
	private long timestamp;

	public AccelerometerMeasure(double axisX, double axisY, double axisZ, long timestamp) {
		this.axisX = axisX;
		this.axisY = axisY;
		this.axisZ = axisZ;
		this.timestamp = timestamp;
	}

	public double getAxisX() {
		return axisX;
	}

	public double getAxisY() {
		return axisY;
	}

	public double getAxisZ() {
		return axisZ;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
