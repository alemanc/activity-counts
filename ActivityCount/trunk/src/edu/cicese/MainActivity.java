package edu.cicese;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import org.achartengine.GraphicalView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 03:49 PM
 */
public class MainActivity extends Activity {

	private Button btnAction;
	private static EditText txtLog;
	private static ActivityChart activityChart;
	private static GraphicalView chartView;

	// Handler gets created on the UI-thread
	public static final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			updateLog(msg.getData());
		}
	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btnAction = (Button) findViewById(R.id.ButtonAction);
		txtLog = (EditText) findViewById(R.id.TextLog);

		btnAction.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Intent AccelerometerIntent = new Intent(MainActivity.this, SensingService.class);
				// Send unique id for this action
				long actionId = UUID.randomUUID().getLeastSignificantBits();
				AccelerometerIntent.putExtra(SensingService.ACTION_ID_FIELD_NAME, actionId);

				if (!Utilities.isSensing) {
//					startService(AccelerometerIntent);
//					startBatteryLog();
//					Utilities.isSensing = true;

					// Point out the action triggered by a user
					AccelerometerIntent.setAction(SensingService.SENSING_START_ACTION);

					btnAction.setText("Stop");
				} else {
//					stopService(AccelerometerIntent);
//					Utilities.resetValues();

					// Point out the action triggered by a user
					AccelerometerIntent.setAction(SensingService.SENSING_STOP_ACTION);

					btnAction.setText("Start");
				}

				WakefulIntentService.sendWakefulWork(MainActivity.this, AccelerometerIntent);
			}
		});

		activityChart = new ActivityChart();
		chartView = activityChart.getView(this, new ArrayList<ActivityCount>());
		LinearLayout layout = (LinearLayout) findViewById(R.id.Chart);
		layout.addView(chartView);
	}

	private void startBatteryLog() {
		new BatteryThread(this).start();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (Utilities.isSensing) {
			btnAction.setText("Stop");
		} else {
			btnAction.setText("Start");
		}
		printActivityCounts();
	}

	private void printActivityCounts() {
		txtLog.setText("");

		for (ActivityCount activityCount : AccelerometerManager.activityCounts) {
			addValue(activityCount.getTimestamp(), activityCount.getCount());
		}
	}

	//! Shows a message toast
	public static void updateLog(Bundle bundle) {
		addValue(bundle.getLong("timestamp"), bundle.getInt("count"));
	}

	private static void addValue(long timestamp, int count) {
		activityChart.addValue(TimeUtil.getDate(timestamp), count);
		chartView.repaint();

		txtLog.append("[" + TimeUtil.getTime(timestamp) + "] -> " + count + "\n");
		txtLog.requestFocus();
		txtLog.setSelection(txtLog.getText().length() - 1);
	}

	

	/*private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mBoundService = ((SensingService.MyBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
		}
	};

	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(MainActivity.this, SensingService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}*/
}
