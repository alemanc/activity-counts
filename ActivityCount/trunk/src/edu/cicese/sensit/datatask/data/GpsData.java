package edu.cicese.sensit.datatask.data;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 01:25 PM
 */

import android.location.Location;
import android.os.Bundle;

public class GpsData extends Data {
	private double altitude;
	private double latitude;
	private double longitude;
	private float accuracy;
	private int satellites; // the number of satellites used to derive the  fix
	private double speed;
	private String provider;

	public GpsData(Location location) {
		super(DataType.GPS);
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		altitude = location.getAltitude();
		accuracy = location.getAccuracy();
		setProvider(location.getProvider());
		Bundle bundle = location.getExtras();
		if (bundle != null) {
			satellites = bundle.getInt("satellites");
		}
		setTimestamp(location.getTime());
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public int getSatellites() {
		return satellites;
	}

	public void setSatellites(int satellites) {
		this.satellites = satellites;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
}
