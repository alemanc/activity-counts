package edu.cicese.sensit;

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
	private boolean mainEpoch;

	public ActivityCountThread(AccelerometerManager accelerometerManager) {
		this.accelerometerManager = accelerometerManager;
	}

	public void setEpoch(long epoch) {
		mainEpoch = epoch == Utilities.MAIN_EPOCH || epoch == Utilities.MAIN_EPOCH - Utilities.CHECK_EPOCH;
	}

	public void run() {
		this.accMeasures = new ArrayList<AccelerometerMeasure>(accelerometerManager.getAccMeasures());

		int accFilteredMagnitudesRounded = 0;
		for (AccelerometerMeasure measure : accMeasures) {
			double[] accFM = AccelerometerCountUtil.getFilteredAcceleration(measure.getAxisX(), measure.getAxisY(), measure.getAxisZ());
			accFilteredMagnitudesRounded += StrictMath.round(Math.sqrt(
					(accFM[0] * accFM[0]) + (accFM[1] * accFM[1]) + (accFM[2] * accFM[2])
			));
		}

		Log.i("ACC", "Computed: " + accFilteredMagnitudesRounded + " Check counts: " + Utilities.checkingCounts);
		accFilteredMagnitudesRounded += Utilities.checkingCounts;

//		Utilities.wasSleeping = false;
		
		if (accFilteredMagnitudesRounded < Utilities.THRESHOLD) {
			Log.i("ACC", "Counts = " + accFilteredMagnitudesRounded + ", turning sensor listener off");

			accelerometerManager.stopListening();

//			Utilities.sleeping = true;
			accelerometerManager.setBeginTimestamp(Utilities.SLEEP_TIME);

			try {
				Log.i("ACC", "Sleeping for " + Utilities.SLEEP_TIME + " ms");
				sleep(Utilities.SLEEP_TIME);
			}
			catch (InterruptedException e) {
				/*ignored*/
			}
			finally {
				Utilities.setEpoch(Utilities.CHECK_EPOCH);
//				Utilities.sleeping = false;

//				Utilities.wasSleeping = true;
			}

			accelerometerManager.startListening();
		}
		else {

			
			if (!mainEpoch) {
				Utilities.setEpoch(Utilities.MAIN_EPOCH - Utilities.CHECK_EPOCH);

				Log.i("ACC", "Checking-Counts = " + accFilteredMagnitudesRounded);
				Utilities.checkingCounts = accFilteredMagnitudesRounded;
				Utilities.checkingTimestamp = accMeasures.get(0).getTimestamp();
			}
			else {
				Utilities.setEpoch(Utilities.MAIN_EPOCH);

				long timestamp;
				if (Utilities.checkingCounts != 0) {
//					accFilteredMagnitudesRounded += Utilities.checkingCounts;
					Utilities.checkingCounts = 0;
					timestamp = Utilities.checkingTimestamp;
				}
				else {
					timestamp = accMeasures.get(0).getTimestamp();
				}
				Log.i("ACC", "Counts = " + accFilteredMagnitudesRounded);
				try {
					accelerometerManager.saveActivityCounts(new ActivityCount(timestamp, accFilteredMagnitudesRounded, Utilities.MAIN_EPOCH));
				}
				catch(Exception e){ Log.e("ACC", e.getLocalizedMessage()); }
			}
		}
	}
}
