package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import edu.cicese.sensit.datatask.DataTask;
import edu.cicese.sensit.datatask.DataTaskFactory;
import edu.cicese.sensit.datatask.data.DataType;
import edu.cicese.sensit.util.SensitActions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 05:07 PM
 */
public class SessionController implements Runnable {
	private static final String TAG = "SensIt.SessionController";

	private List<DataTask> tasks = new ArrayList<DataTask>();

	public static enum ControllerState {
		INITIATED, PREPARED, STARTED, STOPPING, STOPPED
	};

	private volatile ControllerState state;
	private Context context;

	public SessionController(Context context) {
		this.context = context;
		setState(ControllerState.INITIATED);
	}

	private void prepareSession() {
		tasks.clear();

		tasks.add(DataTaskFactory.createDataTask(DataType.BATTERY_LEVEL, context));
		tasks.add(DataTaskFactory.createDataTask(DataType.LINEAR_ACCELEROMETER, context));
//		tasks.add(DataTaskFactory.createDataTask(DataType.ACCELEROMETER, context));
//		tasks.add(DataTaskFactory.createDataTask(DataType.BLUETOOTH, context));
//		tasks.add(DataTaskFactory.createDataTask(DataType.LOCATION, context));

		setState(ControllerState.PREPARED);
	}

	public void start() {
		Log.d(TAG, "Start");
		// Register receiver to wait until is stopped
		IntentFilter intentFilter = new IntentFilter(SensitActions.SENSING_STOP_ACTION);
		context.registerReceiver(sensingStopReceiver, intentFilter);

		if (getState() == ControllerState.INITIATED) {
			Log.d(TAG, "Prepare");
			prepareSession();
		}
		if (getState() == ControllerState.PREPARED || getState() == ControllerState.STOPPED) {
			setState(ControllerState.STARTED);
			Log.d(TAG, "Run");
			run();
		}
	}

	public void stop() {
		Log.d(TAG, "Stopping!" + " sensing: " + Utilities.isSensing());
		if (getState() == ControllerState.STARTED) {
			setState(ControllerState.STOPPING);
			while (getState() != ControllerState.STOPPED) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					Log.e(TAG, "Runnable sleep failed", e);
				}
			}

			/*Log.i(TAG, "Size: " + tasks.size() + " sensing: " + Utilities.isSensing());
			for (DataTask dt : tasks) {
				Log.i(TAG, "Stopping: " + dt.getName());
				dt.stop();
			}
			setState(ControllerState.STOPPED);
			Utilities.setSensing(false);*/




			/*Log.d(TAG, "Wait for every sensor to stop." + " sensing: " + Utilities.isSensing());
			while (isSensing()) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					Log.e(TAG, "Runnable sleep failed", e);
				}
			}*/
		}
	}

	/*private boolean isSensing() {
		boolean isSensing = false;
		for(DataTask dt : tasks) {
			isSensing |= ((DataSource) dt).isSensing();
		}

		return isSensing;
	}*/

	public void run() {
		// Start DataTasks
		for (DataTask dt : tasks) {
			Log.d(TAG, "Starting: " + dt.getName());
			dt.start();
		}

		// Send broadcast
		Intent broadcastIntent = new Intent(SensitActions.SENSING_START_ACTION_COMPLETE);
		context.sendBroadcast(broadcastIntent);

		// Block execution until state changes
		while (getState() == ControllerState.STARTED) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				Log.e(TAG, "Runnable sleep failed", e);
			}
		}

		// Stop DataTasks
		Log.d(TAG, "Size: " + tasks.size() + " sensing: " + Utilities.isSensing());
		for (DataTask dt : tasks) {
			Log.d(TAG, "Stopping: " + dt.getName());
			dt.stop();
		}

		/*for (DataTask dt : tasks) {
			Log.i(TAG, "Stoping: " + dt.getClass().getName());
			if (dt.isRunning()) {
				dt.stop();
			}
//			dt.clear();
		}*/
		setState(ControllerState.STOPPED);

		// Send broadcast
		broadcastIntent = new Intent(SensitActions.SENSING_STOP_ACTION_COMPLETE);
		context.sendBroadcast(broadcastIntent);
	}


	public synchronized void setState(ControllerState state) {
		this.state = state;
	}

	public synchronized ControllerState getState() {
		return state;
	}

	BroadcastReceiver sensingStopReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().compareTo(SensitActions.SENSING_STOP_ACTION) == 0) {
				Log.d(TAG, "Action SENSING_STOP_ACTION received");
				context.unregisterReceiver(this);
				setState(ControllerState.STOPPING);
			}
		}
	};
}
