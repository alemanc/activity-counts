package edu.cicese.sensit.datatask.data;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 01:27 PM
 */
public class BluetoothData extends Data {
	private String address;
	private int state;
	private String name;
	private int deviceClass;
	private int majorDeviceClass;

	public BluetoothData(BluetoothDevice bluetoothDevice) {
		super(DataType.BLUETOOTH);
		address = bluetoothDevice.getAddress();
		state = bluetoothDevice.getBondState();
		name = bluetoothDevice.getName();
		BluetoothClass bluetoothClass = bluetoothDevice.getBluetoothClass();
		deviceClass = bluetoothClass.getDeviceClass();
		majorDeviceClass = bluetoothClass.getMajorDeviceClass();
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDeviceClass() {
		return deviceClass;
	}

	public void setDeviceClass(int deviceClass) {
		this.deviceClass = deviceClass;
	}

	public int getMajorDeviceClass() {
		return majorDeviceClass;
	}

	public void setMajorDeviceClass(int majorDeviceClass) {
		this.majorDeviceClass = majorDeviceClass;
	}
}