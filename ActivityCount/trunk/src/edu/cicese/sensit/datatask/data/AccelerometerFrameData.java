package edu.cicese.sensit.datatask.data;

/*
 * Accelerometer data contains a frame consisting of a matrix of n*4 of double values.
 */
public class AccelerometerFrameData extends Data {
	public final static int X_AXIS = 0;
	public final static int Y_AXIS = 1;
	public final static int Z_AXIS = 2;
	public final static int TIMESTAMP = 3;
	public final static int MAGNITUDE = 4;
	private double[][] frame;

	public AccelerometerFrameData(DataType type, double[][] frame) {
		super(type);
		this.frame = frame;
	}

	/*public AccelerometerFrameData(double[][] frame) {
		this(DataType.ACCELEROMETER, frame);
	}*/

	/*public static AccelerometerFrameData createGyroFrameData(double[][] frame) {
		AccelerometerFrameData gyroData = new AccelerometerFrameData(DataType.GYROSCOPE, frame);
		return gyroData;
	}*/

	public void setFrame(double[][] frame) {
		this.frame = frame;
	}

	public double[][] getFrame() {
		return frame;
	}
}