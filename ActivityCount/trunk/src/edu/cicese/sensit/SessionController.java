package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import edu.cicese.sensit.datatask.DataSource;
import edu.cicese.sensit.datatask.DataSourceFactory;
import edu.cicese.sensit.datatask.data.DataType;
import edu.cicese.sensit.util.SensitActions;
import edu.cicese.sensit.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 05:07 PM
 */
public class SessionController implements Runnable {
	private static final String TAG = "SensIt.SessionController";

	private List<DataSource> dataSources = new ArrayList<>();

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
		dataSources.clear();

		dataSources.add(DataSourceFactory.createDataSource(DataType.BATTERY_LEVEL, context));
		dataSources.add(DataSourceFactory.createDataSource(DataType.LINEAR_ACCELEROMETER, context));
//		dataSources.add(DataSourceFactory.createDataSource(DataType.ACCELEROMETER, context));
//		dataSources.add(DataSourceFactory.createDataSource(DataType.BLUETOOTH, context));
//		dataSources.add(DataSourceFactory.createDataSource(DataType.LOCATION, context));

		setState(ControllerState.PREPARED);
	}

	public void start() {
		Log.d(TAG, "Start");
		// Register receiver to wait until is stopped
		IntentFilter intentFilter = new IntentFilter(SensitActions.ACTION_SENSING_STOP);
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
		}
	}


	public void run() {
		// Start DataSources
		for (DataSource dt : dataSources) {
			Log.d(TAG, "Starting: " + dt.getName());
			dt.start();
		}

		// Send broadcast
		Intent broadcastIntent = new Intent(SensitActions.ACTION_SENSING_START_COMPLETE);
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
		Log.d(TAG, "Size: " + dataSources.size() + " sensing: " + Utilities.isSensing());
		for (DataSource dt : dataSources) {
			Log.d(TAG, "Stopping: " + dt.getName());
			dt.stop();
		}

		setState(ControllerState.STOPPED);

		// Send broadcast
		broadcastIntent = new Intent(SensitActions.ACTION_SENSING_STOP_COMPLETE);
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
			if (intent.getAction().compareTo(SensitActions.ACTION_SENSING_STOP) == 0) {
				Log.d(TAG, "Action ACTION_SENSING_STOP received");
				context.unregisterReceiver(this);
				setState(ControllerState.STOPPING);
			}
		}
	};
}
