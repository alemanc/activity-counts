package edu.cicese.sensit.util;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 13/01/12
 * Time: 11:22 AM
 */
public class LocationUtil {
	private static final double R = 6371000;  // earths mean radius in m
	private static final double R2 = 6378137;  // earths mean radius in m

	// Spherical Law of Cosines
	public static double getSLCDistanceFrom(double lat1, double lon1, double lat2, double lon2) {
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		return R * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(dLon));
	}

	public static boolean inside(double lat1, double lon1, double lat2, double lon2, int radius) {
		return getSLCDistanceFrom(lat1, lon1, lat2, lon2) <= radius;
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
}