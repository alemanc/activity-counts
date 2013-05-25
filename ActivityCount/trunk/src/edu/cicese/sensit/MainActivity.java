package edu.cicese.sensit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import edu.cicese.sensit.util.ActivityUtil;
import edu.cicese.sensit.util.LocationUtil;
import org.achartengine.GraphicalView;

import java.util.ArrayList;
import java.util.Date;
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
	private EditText txtLatitude, txtLongitude, txtHeight, txtWeight;

	public static final String KEY_PREF_HOME_LATITUDE = "pref_key_home_latitude";
	public static final String KEY_PREF_HOME_LONGITUDE = "pref_key_home_longitude";
	public static final String KEY_PREF_HOME_HEIGHT = "pref_key_height";
	public static final String KEY_PREF_HOME_WEIGHT = "pref_key_weight";

	// Handler gets created on the UI-thread
	public static final Handler handlerUI = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			switch (msg.what) {
				case Utilities.UPDATE_ACCELEROMETER:
					int counts = bundle.getInt("counts");
					long timestamp = bundle.getLong("timestamp");
					setAccelerometerText(timestamp, counts);
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

		Log.d("SensIt", "OnCreated");

		if (!Utilities.isSensing()) {
			Log.d("SensIt", "Resetting sensor statuses");
			Utilities.initiateSensors();
		}

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
		txtHeight = (EditText) findViewById(R.id.txt_height);
		txtWeight = (EditText) findViewById(R.id.txt_weight);

		// Load settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		// Load home location settings
		float latitude = settings.getFloat(KEY_PREF_HOME_LATITUDE, -1);
		float longitude = settings.getFloat(KEY_PREF_HOME_LONGITUDE, -1);
		if (latitude != -1) {
			txtLatitude.setText(latitude + "");
		}
		if (longitude != -1) {
			txtLongitude.setText(longitude + "");
		}
		LocationUtil.setHomeLatitude(latitude);
		LocationUtil.setHomeLongitude(longitude);

		// Load user's body information
		int height = settings.getInt(KEY_PREF_HOME_HEIGHT, -1);
		int weight = settings.getInt(KEY_PREF_HOME_WEIGHT, -1);
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
						Utilities.setReady(false);
						Utilities.setSensing(true);
						// Point out the action triggered by a user
						sensingIntent.setAction(SensingService.SENSING_START_ACTION);
						WakefulIntentService.sendWakefulWork(MainActivity.this, sensingIntent);

						btnAction.setText("Stop");
					} else {
						Utilities.setReady(false);
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
			}
		});

		btnSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				saveSettings();
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

	private Toast mToast;

	private void saveSettings() {
//		txtLatitude.clearFocus();
//		txtLongitude.clearFocus();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(txtLatitude.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(txtLongitude.getWindowToken(), 0);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();

		// Latitude
		String latitudeString = txtLatitude.getText().toString();
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
		LocationUtil.setHomeLongitude(longitude);

		// Height
		String heightString = txtHeight.getText().toString();
		int height = -1;
		if (heightString.compareTo("") != 0) {
			height = Integer.parseInt(heightString);
			editor.putInt(KEY_PREF_HOME_HEIGHT, height);
		} else {
			editor.remove(KEY_PREF_HOME_HEIGHT);
		}

		// Weight
		String weightString = txtWeight.getText().toString();
		int weight = -1;
		if (weightString.compareTo("") != 0) {
			weight = Integer.parseInt(weightString);
			editor.putInt(KEY_PREF_HOME_WEIGHT, weight);
		} else {
			editor.remove(KEY_PREF_HOME_WEIGHT);
		}

		ActivityUtil.setBMI(height, weight);

		editor.commit();

		if (mToast == null) {
			mToast = Toast.makeText(this, "Saved", Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (Utilities.isSensing()) {
			btnAction.setText("Stop");
		} else {
			btnAction.setText("Start");
		}
		refreshSensors();
	}

	private static void setGpsText(double latitude, double longitude, String provider) {
		txtGps.setText("- [" + provider + "] " + latitude + ", " + longitude);
	}

	private static void setBluetoothText(String device) {
		txtBluetooth.setText("- Device: " + device);
	}

	private static void setAccelerometerText(long timestamp, int counts) {
		activityChart.addCounts(new Date(timestamp), counts);
		chartView.repaint();
		txtAccelerometer.setText("- " + counts + " CPM");
	}

	private static void setBatteryText(int level, int status) {
		txtBattery.setText("- " + level + "%, status: " + (status != 0 ? "Charging" : "Discharging"));
	}

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
