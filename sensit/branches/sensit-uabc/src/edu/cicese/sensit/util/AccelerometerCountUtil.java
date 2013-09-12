package edu.cicese.sensit.util;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 06:19 PM
 */
public class AccelerometerCountUtil {

	private static float alpha = 0.94f;
	private static float alphaI = 0.06f;
	private static double[] gravity = new double[3];

	//Experimental
	public static void setAlphaValue(float updateFreq, float cutOffFreq) {
		// alpha is calculated as t / (t + dT)
		// with t, the low-pass filter's time-constant
		// and dT, the event delivery rate

		float t = 1.0f / cutOffFreq;
		float dT = 1.0f / updateFreq;

		alpha = t / (t + dT);
		alphaI = 1 - alpha;
	}

	public static void setAlpha(float alpha) {
		AccelerometerCountUtil.alpha = alpha;
		alphaI = 1 - alpha;
	}

	public static float getAlpha() {
		return alpha;
	}

	public static void initiateGravity() {
		gravity = new double[]{-9, 0, 0};
	}

	public static double[] getFilteredAcceleration(double axisX, double axisY, double axisZ) {
		gravity[0] = (alpha * gravity[0]) + (alphaI * axisX);
		gravity[1] = (alpha * gravity[1]) + (alphaI * axisY);
		gravity[2] = (alpha * gravity[2]) + (alphaI * axisZ);

		return new double[]{axisX - gravity[0], axisY - gravity[1], axisZ - gravity[2]};
	}
}