package edu.cicese.sensit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
	private ImageButton btnSave;
	private static ActivityChart activityChart;
	private static GraphicalView chartView;
	private static TextView txtAccelerometer, txtGps, txtBattery, txtBluetooth;
	private static View accIndicator, gpsIndicator, batteryIndicator, bluetoothIndicator;
	private EditText txtLatitude, txtLongitude;

	public static final String KEY_PREF_HOME_LATITUDE = "pref_key_home_latitude";
	public static final String KEY_PREF_HOME_LONGITUDE = "pref_key_home_longitude";

	// Handler gets created on the UI-thread
	public static final Handler handlerUI = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			switch (msg.what) {
				case Utilities.UPDATE_ACCELEROMETER:
					int counts = bundle.getInt("counts");
					setAccelerometerText(counts);
					break;
				case Utilities.UPDATE_GPS:
					String provider = bundle.getString("provider");
					double latitude = bundle.getDouble("latitude");
					double longitude = bundle.getDouble("longitude");
					setGpsText(latitude, longitude, provider);
					break;
				case Utilities.UPDATE_BATTERY:
					int level = bundle.getInt("level");
					int plugged = bundle.getInt("plugged");
					setBatteryText(level, plugged);
					break;
				case Utilities.UPDATE_BLUETOOTH:
					String device = bundle.getString("device");
					setBluetoothText(device);
					break;
				case Utilities.REFRESH_STATUS:
					refreshSensors();
					break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Utilities.initiateSensors();

		btnAction = (Button) findViewById(R.id.btn_start);
		btnSave = (ImageButton) findViewById(R.id.btn_save);

		accIndicator = findViewById(R.id.accelerometer_indicator);
		gpsIndicator = findViewById(R.id.gps_indicator);
		batteryIndicator = findViewById(R.id.battery_indicator);
		bluetoothIndicator = findViewById(R.id.bluetooth_indicator);

		txtAccelerometer = (TextView) findViewById(R.id.acc_text);
		txtGps = (TextView) findViewById(R.id.gps_text);
		txtBattery = (TextView) findViewById(R.id.battery_text);
		txtBluetooth = (TextView) findViewById(R.id.bluetooth_text);

		txtLatitude = (EditText) findViewById(R.id.txt_latitude);
		txtLongitude = (EditText) findViewById(R.id.txt_longitude);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		float latitude = settings.getFloat(KEY_PREF_HOME_LATITUDE, 31.8595769f);
		float longitude = settings.getFloat(KEY_PREF_HOME_LONGITUDE, -116.606428f);
		txtLatitude.setText(latitude + "");
		txtLongitude.setText(longitude + "");

		Utilities.setHomeLatitude(latitude);
		Utilities.setHomeLongitude(longitude);

		// Start service for it to run the sensing session
		final Intent sensingIntent = new Intent(this, SensingService.class);

		btnAction.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				/*if (SensingService.WAKE_LOCK == null) {
					PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					SensingService.WAKE_LOCK = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SensingService.TAG);
					SensingService.WAKE_LOCK.acquire();
					startService(new Intent(getApplicationContext(), SensingService.class));
					btnAction.setText("Stop");
				}
				else {
					stopService(new Intent(getApplicationContext(), SensingService.class));
					btnAction.setText("Start");
				}*/


				// Point out this action was triggered by a user
//				sensingIntent.setAction(SensingService.SENSING_START_ACTION);
				// Send unique id for this action
				long actionId = UUID.randomUUID().getLeastSignificantBits();
				sensingIntent.putExtra(SensingService.ACTION_ID_FIELD_NAME, actionId);

				if (!Utilities.isSensing()) {
//					startService(AccelerometerIntent);
//					startBatteryLog();
					Utilities.setSensing(true);

					// Point out the action triggered by a user
					sensingIntent.setAction(SensingService.SENSING_START_ACTION);
					WakefulIntentService.sendWakefulWork(MainActivity.this, sensingIntent);

					btnAction.setText("Stop");
				} else {
//					stopService(AccelerometerIntent);
//					Utilities.resetValues();

					Utilities.setSensing(false);

					Intent broadcastIntent = new Intent(SensingService.SENSING_STOP_ACTION);
					sendBroadcast(broadcastIntent);

					// Point out the action triggered by a user
//					sensingIntent.setAction(SensingService.SENSING_STOP_ACTION);

					/*Intent stopIntent = new Intent(MainActivity.this, SensingService.class);
					// Point out this action was triggered by a user
					stopIntent.setAction(SensingService.SENSING_STOP_ACTION);
					// Send unique id for this action
					long actionID = UUID.randomUUID().getLeastSignificantBits();
					stopIntent.putExtra(SensingService.ACTION_ID_FIELD_NAME, actionID);
					startService(stopIntent);*/

					btnAction.setText("Start");
				}
			}
		});

		btnSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				saveHomeCoordinates();
			}
		});

		activityChart = new ActivityChart();
		chartView = activityChart.getView(this, new ArrayList<ActivityCount>());
		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		layout.addView(chartView);
	}

	private Toast mToast;

	private void saveHomeCoordinates() {
		txtLatitude.clearFocus();
		txtLongitude.clearFocus();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(txtLatitude.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(txtLongitude.getWindowToken(), 0);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();


		editor.putFloat(KEY_PREF_HOME_LATITUDE, Float.parseFloat(txtLatitude.getText().toString()));
		editor.putFloat(KEY_PREF_HOME_LONGITUDE, Float.parseFloat(txtLongitude.getText().toString()));
		editor.commit();

		if (mToast == null) {
			mToast = Toast.makeText(this, "Saved", Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	/*private void startBatteryLog() {
		new BatteryThread(this).start();
	}*/

	@Override
	public void onResume() {
		super.onResume();
		
		if (Utilities.isSensing()) {
			btnAction.setText("Stop");
		} else {
			btnAction.setText("Start");
		}
	}



	//! Shows a message toast
	/*public static void updateLog(Bundle bundle) {
		addValue(bundle.getLong("timestamp"), bundle.getInt("count"));
	}*/

	/*private static void addValue(long timestamp, int count) {
		activityChart.addValue(TimeUtil.getDate(timestamp), count);
		chartView.repaint();

		txtLog.append("[" + TimeUtil.getTime(timestamp) + "] -> " + count + "\n");
		txtLog.requestFocus();
		txtLog.setSelection(txtLog.getText().length() - 1);
	}*/

	private static void setGpsText(double latitude, double longitude, String provider) {
		txtGps.setText("- [" + provider + "] " + latitude + ", " + longitude);
	}

	private static void setBluetoothText(String device) {
		txtBluetooth.setText("- Device: " + device);
	}

	private static void setAccelerometerText(int counts) {
		activityChart.addValue(TimeUtil.getDate(System.currentTimeMillis()), counts);
		chartView.repaint();
		txtAccelerometer.setText("- " + counts + " CPM");
	}

	private static void setBatteryText(int level, int status) {
		txtBattery.setText("- " + level + "%, status: " + (status != 0 ? "Charging" : "Discharging"));
	}

	/*private static void resetAccelerometerText() {
		txtAccelerometer.setText("Accelerometer");
	}

	private static void resetGpsText() {
		txtGps.setText("GPS");
	}

	private static void resetBluetoothText() {
		txtBluetooth.setText("Bluetooth");
	}

	private static void resetBatteryText() {
		txtBattery.setText("Battery");
	}
*/
	/*private static void enable(View view, boolean enable) {
		if (enable) {
			view.setBackgroundResource(R.color.green);
		}
		else {
			view.setBackgroundResource(R.color.red);
		}
	}*/

	private static void refresh(View view, TextView txt, int status) {
		switch (status) {
			case Utilities.SENSOR_ON:
				view.setBackgroundResource(R.color.green);
				break;
			case Utilities.SENSOR_OFF:
				view.setBackgroundResource(R.color.red);
				txt.setText("");
				break;
			case Utilities.SENSOR_PAUSED:
				view.setBackgroundResource(R.color.blue);
				break;
		}
	}

	private static void refreshSensors() {
		refresh(accIndicator, txtAccelerometer, Utilities.sensorStatus[Utilities.SENSOR_LINEAR_ACCELEROMETER]);
		refresh(batteryIndicator, txtBattery, Utilities.sensorStatus[Utilities.SENSOR_BATTERY]);
		refresh(gpsIndicator, txtGps, Utilities.sensorStatus[Utilities.SENSOR_GPS]);
		refresh(bluetoothIndicator, txtBluetooth, Utilities.sensorStatus[Utilities.SENSOR_BLUETOOTH]);
	}
}
