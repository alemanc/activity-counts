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
	private long epoch;
	private boolean mainEpoch;

	/*public ActivityCountThread(List<AccelerometerMeasure> accMeasures) {
		this.accMeasures = accMeasures;
	}*/

	public ActivityCountThread(AccelerometerManager accelerometerManager, long epoch) {
		this.accMeasures = new ArrayList<AccelerometerMeasure>(accelerometerManager.getAccMeasures());
		this.accelerometerManager = accelerometerManager;
		mainEpoch = epoch == Utilities.MAIN_EPOCH || epoch == Utilities.MAIN_EPOCH - Utilities.CHECK_EPOCH;
	}

	public void run() {
		int accFilteredMagnitudesRounded = 0;
		for (AccelerometerMeasure measure : accMeasures) {
			double[] accFM = AccelerometerCountUtil.getFilteredAcceleration(measure.getAxisX(), measure.getAxisY(), measure.getAxisZ());
			accFilteredMagnitudesRounded += StrictMath.round(Math.sqrt(
					Math.pow(accFM[0], 2) + Math.pow(accFM[1], 2) + Math.pow(accFM[2], 2)));
		}

		Log.d("ACC", "Computed: " + accFilteredMagnitudesRounded + " Check counts: " + Utilities.checkingCounts);
		accFilteredMagnitudesRounded += Utilities.checkingCounts;

		if (accFilteredMagnitudesRounded < Utilities.THRESHOLD) {
			Log.d("ACC", "Counts = " + accFilteredMagnitudesRounded + ", turning sensor listener off");
			accelerometerManager.stopListening();
			Utilities.sleeping = true;
			try {
				sleep(Utilities.SLEEP_TIME);
			} catch (InterruptedException e) { /*ignored*/ }
			accelerometerManager.startListening();
		}
		else {
			if (!mainEpoch) {
				Log.d("ACC", "Checking-Counts = " + accFilteredMagnitudesRounded);
				Utilities.checkingCounts = accFilteredMagnitudesRounded;
				Utilities.checkingTimestamp = accMeasures.get(0).getTimestamp();
			}
			else {
				long timestamp;
				if (Utilities.checkingCounts != 0) {
//					accFilteredMagnitudesRounded += Utilities.checkingCounts;
					Utilities.checkingCounts = 0;
					timestamp = Utilities.checkingTimestamp;
				}
				else {
					timestamp = accMeasures.get(0).getTimestamp();
				}
				Log.d("ACC", "Counts = " + accFilteredMagnitudesRounded);
				accelerometerManager.saveActivityCounts(new ActivityCount(timestamp, accFilteredMagnitudesRounded, Utilities.MAIN_EPOCH));
			}
		}
	}
}
