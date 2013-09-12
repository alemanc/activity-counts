package edu.cicese.sensit.util;

import android.util.Log;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 13/01/12
 * Time: 11:22 AM
 */
public class LocationUtil {
	private static final double R = 6371000;  // earths mean radius in m
	private static final double R2 = 6378137;  // earths mean radius in m

	public static boolean atHome = false;
	public static double homeLatitude, homeLongitude;

	// Spherical Law of Cosines
	public static double getSLCDistanceFrom(double lat1, double lon1, double lat2, double lon2) {
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		return R * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(dLon));
	}

	public static boolean inside(double lat1, double lon1, double lat2, double lon2, int radius) {
		Log.d("SensIt.LocationUtil", "Computing: " + lat1 + ", " + lon1 + " vs " + lat2 + ", " + lon2);
		double distance = getSLCDistanceFrom(lat1, lon1, lat2, lon2);
		double distance2 = getHaversineDistFrom(lat1, lon1, lat2, lon2);
		Log.d("SensIt.LocationUtil", "Distance: " + distance + " distance2: " + distance2);
		return distance <= radius;
	}

	// Haversine
	public static double getHaversineDistFrom(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
						Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	public static double[] getLatLon(double lat, double lon, int offset) {
		// Coordinate offsets in radians
		double dLat = offset / R2;
		double dLon = offset / (R2 * Math.cos(Math.PI * lat / 180));

		// OffsetPosition, decimal degrees
		return new double[]{lat + dLat * 180 / Math.PI, lon + dLon * 180 / Math.PI};
	}

	public static boolean isAtHome() {
		return atHome;
	}

	public static void setAtHome(double lat1, double lon1, int radius) {
		LocationUtil.atHome = homeLatitude != -1 && homeLongitude != -1 && inside(lat1, lon1, homeLatitude, homeLongitude, radius);
	}

	public static double getHomeLatitude() {
		return homeLatitude;
	}

	public static void setHomeLatitude(double homeLatitude) {
		LocationUtil.homeLatitude = homeLatitude;
	}

	public static double getHomeLongitude() {
		return homeLongitude;
	}

	public static void setHomeLongitude(double homeLongitude) {
		LocationUtil.homeLongitude = homeLongitude;
	}
}