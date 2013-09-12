package edu.cicese;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 06:32 PM
 */
public class ActivityCountThread extends Thread {

	private List<AccelerometerMeasure> accMeasures;
	private AccelerometerManager accelerometerManager;

	public ActivityCountThread(AccelerometerManager accelerometerManager) {
		this.accMeasures = new ArrayList<AccelerometerMeasure>(accelerometerManager.getAccMeasures());
		this.accelerometerManager = accelerometerManager;
	}

	public void run() {
		int accFilteredMagnitudesRounded = 0;
		for (AccelerometerMeasure measure : accMeasures) {
			double[] accFM = AccelerometerCountUtil.getFilteredAcceleration(measure.getAxisX(), measure.getAxisY(), measure.getAxisZ());
			accFilteredMagnitudesRounded += StrictMath.round(Math.sqrt(
					Math.pow(accFM[0], 2) + Math.pow(accFM[1], 2) + Math.pow(accFM[2], 2)));
		}

		Log.d("ACC", "Counts = " + accFilteredMagnitudesRounded);
		accelerometerManager.saveActivityCounts(new ActivityCount(accMeasures.get(0).getTimestamp(), accFilteredMagnitudesRounded, Utilities.MAIN_EPOCH));
	}
}
