package edu.cicese.sensit.datatask.data;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 01:25 PM
 */
public class AccelerometerData extends Data {
	private double axisX;
	private double axisY;
	private double axisZ;

	public AccelerometerData(double x, double y, double z) {
		super(DataType.ACCELEROMETER);
		setAxisX(x);
		setAxisY(y);
		setAxisZ(z);
	}

	public void setAxisX(double axisX) {
		this.axisX = axisX;
	}

	public void setAxisY(double axisY) {
		this.axisY = axisY;
	}

	public void setAxisZ(double axisZ) {
		this.axisZ = axisZ;
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

	public String toString() {
		return "x: " + axisX + ", y: " + axisY + ", z:" + axisZ;
	}

	public void setTimestamp(long timestamp) {
		super.setTimestamp(timestamp);
	}
}