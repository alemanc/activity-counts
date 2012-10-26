package edu.cicese;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.achartengine.GraphicalView;

import java.util.ArrayList;

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

				if (SensingService.WAKE_LOCK == null) {
					PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					SensingService.WAKE_LOCK = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SensingService.TAG);
					SensingService.WAKE_LOCK.acquire();
					startService(new Intent(getApplicationContext(), SensingService.class));
					btnAction.setText("Stop");
				}
				else {
					stopService(new Intent(getApplicationContext(), SensingService.class));
					btnAction.setText("Start");
				}

				/*Intent AccelerometerIntent = new Intent(MainActivity.this, SensingService.class);
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

				WakefulIntentService.sendWakefulWork(MainActivity.this, AccelerometerIntent);*/
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
}
