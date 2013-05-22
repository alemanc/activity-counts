package edu.cicese.sensit.ui;

import android.content.Context;
import edu.cicese.sensit.datatask.DataTask;

import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 15/05/13
 * Time: 12:57 PM
 */
public class SessionFilterThread extends Thread{
	private boolean done = false;
	private Context context;
	private List<DataTask> dataTasks;

	public SessionFilterThread(Context context, List<DataTask> dataTasks) {
		this.context = context;
		this.dataTasks = dataTasks;
	}

	public void done() {
		done = true;
	}

	/*public void run() {
		while (Utilities.isSensing) {
			if (Utilities.charging) {
				for (DataTask task : dataTasks) {
					task.isRunning()
				}
				if (isSensing()) {
					Log.d(TAG, "Stopping sensor [battery check]");
					stop();
				}
			} else if (!isSensing()) {
				Log.d(TAG, "Registering sensor [battery check]");
				register();
			}
			try {
				Log.d(TAG, "Sleeping [battery check]");
				Thread.sleep(Utilities.ACCELEROMETER_CHECK_TIME);
			} catch (InterruptedException e) {
				Log.d(TAG, e.getLocalizedMessage());
			}
			while (Utilities.isSensing) {
			}

			try {
				sleep(Utilities.BATTERY_RATE);
			} catch (InterruptedException e) { *//*ignored*//* }
		}
	}*/
}
