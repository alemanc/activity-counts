package edu.cicese.sensit.sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import edu.cicese.sensit.Utilities;
import edu.cicese.sensit.datatask.data.BluetoothData;
import edu.cicese.sensit.datatask.data.Data;

import java.util.ArrayList;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 03:02 PM
 */
public class BluetoothSensor extends Sensor {
	private final static String TAG = "SensIt.BluetoothSensor";

	public BluetoothSensor(Context context) {
		super(context);
		Log.d(TAG, "Bluetooth sensor created");

		setName("BT");
		// Initialize list where results will be stored
		dataList = new ArrayList<Data>();
		context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
	}

	// Initialize broadcast receiver
	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

			Log.i(TAG, "New device discovered: " + remoteDeviceName + ".");

			Bundle bundle = new Bundle();
			bundle.putString("device", remoteDeviceName);
			updateUI(Utilities.UPDATE_BLUETOOTH, bundle);

			BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BluetoothData data = new BluetoothData(remoteDevice);
			if (!dataList.contains(data)) {
				dataList.add(data);
			}
		}
	};

	@Override
	public void start() {
		super.start();
		Log.d(TAG, "Starting Bluetooth sensor");
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.startDiscovery();
		}
//		handleEnable(Utilities.ENABLE_BLUETOOTH, true);
		Log.d(TAG, "Starting Bluetooth sensor [done]");
	}

	@Override
	public void stop() {
		super.stop();
		Log.d(TAG, "Stopping Bluetooth sensor");
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			if (bluetoothAdapter.isDiscovering()) {
				bluetoothAdapter.cancelDiscovery();
			}
		}
		try {
			getContext().unregisterReceiver(bluetoothReceiver);
		}
		catch (IllegalArgumentException ex) {
			Log.e(TAG, ex.toString());
		}
//		handleEnable(Utilities.ENABLE_BLUETOOTH, false);
		Log.d(TAG, "Stopping Bluetooth sensor [done]");

//		Utilities.sensorStatus[Utilities.SENSOR_BLUETOOTH] = Utilities.SENSOR_OFF;
//		refreshStatus();
	}
}