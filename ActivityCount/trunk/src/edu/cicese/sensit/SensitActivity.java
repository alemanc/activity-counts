package edu.cicese.sensit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import edu.cicese.sensit.db.DBAdapter;
import edu.cicese.sensit.sensor.Sensor;
import edu.cicese.sensit.util.ActivityUtil;
import edu.cicese.sensit.util.Preferences;
import edu.cicese.sensit.util.SensitActions;
import org.achartengine.GraphicalView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 03:49 PM
 */
public class SensitActivity extends Activity {
	private static final String TAG = "SensIt.Main";

	private Button btnAction;
	private ImageButton btnSync;
	private ActivityChart activityChart;
	private GraphicalView chartView;
	private TextView txtAccelerometer, /*txtLocation, */txtBattery/*, txtBluetooth*/;
	private View accIndicator, /*locationIndicator, */batteryIndicator/*, bluetoothIndicator*/;
	private EditText /*txtLatitude, txtLongitude, */txtHeight, txtWeight;
	private View lySyncing;

//	public static final String KEY_PREF_HOME_LATITUDE = "pref_key_home_latitude";
//	public static final String KEY_PREF_HOME_LONGITUDE = "pref_key_home_longitude";

	private DBAdapter dbAdapter;

	private Toast savedToast, syncedToast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("SensIt", "OnCreated " + (savedInstanceState != null));

		setContentView(R.layout.main);

		dbAdapter = new DBAdapter(this);

		/*if (!Utilities.isSensing()) {
			Log.d("SensIt", "Resetting sensor statuses");
			Utilities.initiateSensors();
		}*/

		btnAction = (Button) findViewById(R.id.btn_start);
		ImageButton btnSave = (ImageButton) findViewById(R.id.btn_save);
		btnSync = (ImageButton) findViewById(R.id.btn_sync);

		lySyncing = findViewById(R.id.syncing);

		accIndicator = findViewById(R.id.accelerometer_indicator);
//		locationIndicator = findViewById(R.id.location_indicator);
		batteryIndicator = findViewById(R.id.battery_indicator);
//		bluetoothIndicator = findViewById(R.id.bluetooth_indicator);

		txtAccelerometer = (TextView) findViewById(R.id.acc_text);
//		txtLocation = (TextView) findViewById(R.id.location_text);
		txtBattery = (TextView) findViewById(R.id.battery_text);
//		txtBluetooth = (TextView) findViewById(R.id.bluetooth_text);

//		txtLatitude = (EditText) findViewById(R.id.txt_latitude);
//		txtLongitude = (EditText) findViewById(R.id.txt_longitude);
		txtHeight = (EditText) findViewById(R.id.txt_height);
		txtWeight = (EditText) findViewById(R.id.txt_weight);

		// Load settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		// Load home location settings
		/*float latitude = settings.getFloat(KEY_PREF_HOME_LATITUDE, -1);
		float longitude = settings.getFloat(KEY_PREF_HOME_LONGITUDE, -1);
		if (latitude != -1) {
			txtLatitude.setText(latitude + "");
		}
		if (longitude != -1) {
			txtLongitude.setText(longitude + "");
		}
		LocationUtil.setHomeLatitude(latitude);
		LocationUtil.setHomeLongitude(longitude);*/

		// Load user's body information
		int height = settings.getInt(Preferences.PREF_KEY_HEIGHT, -1);
		int weight = settings.getInt(Preferences.KEY_PREF_WEIGHT, -1);
		if (height != -1) {
			txtHeight.setText(height + "");
		}
		if (weight != -1) {
			txtWeight.setText(weight + "");
		}
		ActivityUtil.setBMI(height, weight);

		// Start service for it to run the sensing session
		final Intent sensingIntent = new Intent(this, SensingService.class);

		btnAction.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Send unique id for this action
				long actionId = UUID.randomUUID().getLeastSignificantBits();
				sensingIntent.putExtra(SensingService.ACTION_ID_FIELD_NAME, actionId);

				if (Utilities.isReady()) {
					if (!Utilities.isSensing()) {
						Utilities.setManuallyStopped(false);

						Utilities.setReady(false);
						Utilities.setSensing(true);

						// Point out the action triggered by a user
						sensingIntent.setAction(SensitActions.SENSING_START_ACTION);
						WakefulIntentService.sendWakefulWork(SensitActivity.this, sensingIntent);

						btnAction.setText("Stop");
					} else {
						Utilities.setManuallyStopped(true);

						Utilities.setReady(false);
						Utilities.setSensing(false);

						Intent broadcastIntent = new Intent(SensitActions.SENSING_STOP_ACTION);
						sendBroadcast(broadcastIntent);

						// Point out the action triggered by a user
//					sensingIntent.setAction(SensingService.SENSING_STOP_ACTION);

					/*Intent stopIntent = new Intent(SensitActivity.this, SensingService.class);
					// Point out this action was triggered by a user
					stopIntent.setAction(SensingService.SENSING_STOP_ACTION);
					// Send unique id for this action
					long actionID = UUID.randomUUID().getLeastSignificantBits();
					stopIntent.putExtra(SensingService.ACTION_ID_FIELD_NAME, actionID);
					startService(stopIntent);*/

						btnAction.setText("Start");
					}
				}
			}
		});

		btnSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				saveSettings();
			}
		});

		btnSync.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!Utilities.isSyncing()) {
					new Thread(new DataUploadThread(SensitActivity.this)).start();
//					Utilities.startDataUploadThread(SensitActivity.this);
				}
			}
		});

		activityChart = new ActivityChart();
		chartView = activityChart.getView(this, new ArrayList<ActivityCount>());
		chartView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return true;
			}
		});
		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		layout.addView(chartView);
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d("SensIt", "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d("SensIt", "onResume");

		if (Utilities.isSensing()) {
			btnAction.setText("Stop");
		} else {
			btnAction.setText("Start");
		}
		refreshSensors();
		refreshChart();
		refreshSyncing();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SensitActions.REFRESH_CHART);
		intentFilter.addAction(SensitActions.REFRESH_SENSOR);
		intentFilter.addAction(SensitActions.DATA_SYNCING);
		intentFilter.addAction(SensitActions.DATA_SYNCED);
		intentFilter.addAction(SensitActions.DATA_SYNC_DONE);
		intentFilter.addAction(SensitActions.DATA_SYNC_ERROR);
		registerReceiver(uiRefreshReceiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d("SensIt", "onPause");

//		unregisterReceiver(uiRefreshReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();

		Log.d("SensIt", "onStop");

		unregisterReceiver(uiRefreshReceiver);
		dbAdapter.close();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d("SensIt", "onDestroy");

//		unregisterReceiver(uiRefreshReceiver);
//		dbAdapter.close();
	}

	private void saveSettings() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtHeight.getWindowToken(), 0);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();

		// Latitude
		/*String latitudeString = txtLatitude.getText().toString();
		float latitude = -1;
		if (latitudeString.compareTo("") != 0) {
			latitude = Float.parseFloat(latitudeString);
			editor.putFloat(KEY_PREF_HOME_LATITUDE, latitude);
		}
		else {
			editor.remove(KEY_PREF_HOME_LATITUDE);
		}
		LocationUtil.setHomeLatitude(latitude);

		// Longitude
		String longitudeString = txtLongitude.getText().toString();
		float longitude = -1;
		if (longitudeString.compareTo("") != 0) {
			longitude = Float.parseFloat(longitudeString);
			editor.putFloat(KEY_PREF_HOME_LONGITUDE, longitude);
		} else {
			editor.remove(KEY_PREF_HOME_LONGITUDE);
		}
		LocationUtil.setHomeLongitude(longitude);*/

		// Height
		String heightString = txtHeight.getText().toString();
		int height = -1;
		if (heightString.compareTo("") != 0) {
			height = Integer.parseInt(heightString);
			editor.putInt(Preferences.PREF_KEY_HEIGHT, height);
		} else {
			editor.remove(Preferences.PREF_KEY_HEIGHT);
		}

		// Weight
		String weightString = txtWeight.getText().toString();
		int weight = -1;
		if (weightString.compareTo("") != 0) {
			weight = Integer.parseInt(weightString);
			editor.putInt(Preferences.KEY_PREF_WEIGHT, weight);
		} else {
			editor.remove(Preferences.KEY_PREF_WEIGHT);
		}

		ActivityUtil.setBMI(height, weight);

		editor.commit();

		if (savedToast == null) {
			savedToast = Toast.makeText(this, "Saved", Toast.LENGTH_SHORT);
		}
		savedToast.show();
	}

	private static void refresh(View view, TextView txt, int status) {
		switch (status) {
			case Sensor.SENSOR_ON:
				view.setBackgroundResource(R.color.green);
				break;
			case Sensor.SENSOR_OFF:
				view.setBackgroundResource(R.color.red);
				txt.setText("");
				break;
			case Sensor.SENSOR_PAUSED:
				view.setBackgroundResource(R.color.blue);
				break;
		}
	}

	private void refreshSensors() {
		refresh(accIndicator, txtAccelerometer, Sensor.getSensorStatus(Sensor.SENSOR_LINEAR_ACCELEROMETER));
		refresh(batteryIndicator, txtBattery, Sensor.getSensorStatus(Sensor.SENSOR_BATTERY));
//		refresh(locationIndicator, txtLocation, Utilities.sensorStatus[Utilities.SENSOR_LOCATION]);
//		refresh(bluetoothIndicator, txtBluetooth, Utilities.sensorStatus[Utilities.SENSOR_BLUETOOTH]);
	}

	private void refreshChart() {
		dbAdapter.open();

		Log.d(TAG, "Query chart data");
		Cursor cursor = dbAdapter.queryCounts(ActivityUtil.GRAPH_RANGE_X);

		List<ActivityCount> counts = new ArrayList<>();

		/**
		 COLUMN_ACTIVITY_COUNT_COUNTS,
		 COLUMN_ACTIVITY_COUNT_DATE,
		 COLUMN_ACTIVITY_COUNT_CHARGING
		 */

		// at least one entry
		if (cursor.moveToFirst()) {
			do {
				counts.add(new ActivityCount(cursor.getString(1), cursor.getInt(0), cursor.getInt(2)));
			} while (cursor.moveToNext());
		}

		if (!counts.isEmpty()) {
			activityChart.setCounts(counts);
			chartView.repaint();
		}

		if (!cursor.isClosed()) {
			cursor.close();
		}
	}

	private void refreshSyncing() {
		boolean syncing = Utilities.isSyncing();
		Log.d(TAG, "Setting " + syncing);
		btnSync.setEnabled(!syncing);
		if (syncing) {
			lySyncing.setVisibility(View.VISIBLE);
			btnSync.setVisibility(View.GONE);
		}
		else {
			lySyncing.setVisibility(View.GONE);
			btnSync.setVisibility(View.VISIBLE);
		}
	}

	public BroadcastReceiver uiRefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()){
				case SensitActions.REFRESH_CHART:
					Log.d(TAG, "Refresh Action received");
					refreshChart();
					break;
				case SensitActions.REFRESH_SENSOR:
					refreshSensors();
					break;
				case SensitActions.DATA_SYNCING:
//					setSyncing(true);
					refreshSyncing();
					break;
				case SensitActions.DATA_SYNCED:
					Log.d(TAG, "Action DATA_SYNCED received");

					int type = intent.getIntExtra(SensitActions.EXTRA_SYNCED_TYPE, -1);
					String dateStart = intent.getStringExtra(SensitActions.EXTRA_DATE_START);
					String dateEnd = intent.getStringExtra(SensitActions.EXTRA_DATE_END);

					Log.d(TAG, "Update");
//					new Thread(new DataSyncedThread(SensingService.this, type, dateStart, dateEnd)).start();

					Log.d(TAG, "Resetting intent");
					abortBroadcast();
					break;
				case SensitActions.DATA_SYNC_DONE:
					Log.d(TAG, "Action DATA_SYNC_DONE received");
//					setSyncing(false);
					refreshSyncing();
					break;
				case SensitActions.DATA_SYNC_ERROR:
					Log.d(TAG, "Action DATA_SYNC_ERROR received");
//					setSyncing(false);
					refreshSyncing();

					if (syncedToast == null) {
						syncedToast = Toast.makeText(SensitActivity.this, "", Toast.LENGTH_SHORT);
					}

					String msg = intent.getStringExtra(SensitActions.EXTRA_MSG);
					if (msg != null) {
						syncedToast.setText(msg);
						syncedToast.show();
					}
					break;
			}
		}
	};
}
