package edu.cicese.sensit;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import edu.cicese.sensit.db.DBAdapter;
import edu.cicese.sensit.util.Preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 28/08/12
 * Time: 02:57 PM
 */
public class Utilities {
	private static final String TAG = "SensIt.Utilities";

	private static boolean sensing = false;
	private static boolean charging = false;
	private static boolean epochCharging = false;
	private static boolean checkEpochCharging = false;
	private static boolean ready = true;
	private static boolean manuallyStopped = false;
	private static boolean syncing;

	public static final int TYPE_COUNT = 1;
	public static final int TYPE_SURVEY = 2;

	private static DataUploadThread dataUploadThread;

	private static String macAddress = null;
	private static String userID = null;

	public static final long ACCELEROMETER_CHECK_TIME = 10000l;
	public static final long LOCATION_CHECK_TIME = 10000l;

	// Sample RATE
//	public static final int RATE = 40000; // 1000000 = 1 second | 40000 -> 25 samples per second (1000000/40000) = 25Hz
	public static final int RATE = 40;


	public static boolean isReady() {
		return ready;
	}

	public static void setReady(boolean ready) {
		Utilities.ready = ready;
	}

	public static boolean isSensing() {
		return sensing;
	}

	public static void setSensing(boolean sensing) {
		Utilities.sensing = sensing;
	}

	public static boolean isCharging() {
		return charging;
	}

	public static void setCharging(boolean charging) {
		Utilities.charging = charging;
	}

	public static boolean isEpochCharging() {
		return epochCharging;
	}

	public static void setEpochCharging() {
		Utilities.epochCharging |= isCharging();
		Utilities.checkEpochCharging |= isCharging();
	}

	public static void resetEpochCharging() {
		Utilities.epochCharging = isCharging();
	}

	public static boolean isCheckEpochCharging() {
		return checkEpochCharging;
	}

	/*public static void setCheckEpochCharging() {
		Utilities.checkEpochCharging |= ;
	}*/

	public static void resetCheckEpochCharging() {
		Utilities.checkEpochCharging = isCharging();
	}

	public static boolean isManuallyStopped() {
		return manuallyStopped;
	}

	public static void setManuallyStopped(boolean manuallyStopped) {
		Utilities.manuallyStopped = manuallyStopped;
	}

	public static String getMacAddress(Context context) {
		if (macAddress == null) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			macAddress = wInfo.getMacAddress();
		}
		return macAddress;
	}

	public static String getUserID(Context context) {
		if (userID == null) {
			/*TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String tmDevice, tmSerial, androidId;
			tmDevice = "" + tm.getDeviceId();
			tmSerial = "" + tm.getSimSerialNumber();
			androidId = "" + Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

			UUID deviceUuid = new UUID(androidId.hashCode(), (long) tmDevice.hashCode() << 32);
			userID = deviceUuid.toString();*/

			userID = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		}
		return userID;
	}

	public static void setSyncing(boolean syncing) {
		Utilities.syncing = syncing;
	}

	public static boolean isSyncing() {
		return Utilities.syncing;
	}

	/*public static String getDeviceId(Context context) {
		if (userID == null) {
			String buildSerial = Build.SERIAL;
			if (buildSerial != null) {
				userID = buildSerial;
			}
			else {
				String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
				if (deviceId != null) {
					userID = deviceId;
				} else {
					userID = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);;
				}
			}
			Log.d(TAG, "UserID:" + userID);
		}
		return userID;
	}*/

	private static final long SYNC_PERIOD = 1200000; //20 minutes

	public static boolean syncNeeded(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		long lastSync = settings.getLong(Preferences.KEY_PREF_LAST_SYNC, 0);

		return (System.currentTimeMillis() - lastSync) > SYNC_PERIOD;
	}

	public static void setLastSync(Context context, long lastSync) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Preferences.KEY_PREF_LAST_SYNC, lastSync);
		editor.commit();
	}

	/*public static void startDataUploadThread(Context context) {
		if (!isSyncing()) {
			new Thread(new DataUploadThread(context)).start();
		}
	}*/

	/*public static boolean startSync(Context context) {
//		boolean

		// check connection preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean wifiOnly = settings.getBoolean(Preferences.KEY_PREF_WIFI_ONLY, true);

		// check connection
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

		return  (wifiOnly && nWifi.isConnected()) || (!wifiOnly && (activeNetworkInfo != null && activeNetworkInfo.isConnected()));
	}*/

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static boolean surveyNeeded(Context context) {
		boolean needed = true;
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.querySurveys(1);
		// at least one entry
		if (cursor.moveToFirst()) {
			Calendar calendarToday = Calendar.getInstance();
			calendarToday.set(Calendar.HOUR_OF_DAY, 0);
			calendarToday.set(Calendar.MINUTE, 0);
			calendarToday.set(Calendar.SECOND, 0);
			calendarToday.set(Calendar.MILLISECOND, 0);
			Date today = calendarToday.getTime();
			try {
				Date last = dateFormat.parse(cursor.getString(0));
				if (last.before(today)) {
					needed = true;
				}
				else {
					needed = false;
				}
			} catch (ParseException e) {
				Log.e(TAG, "", e);
			}
		}
		dbAdapter.close();

		return needed;
	}

	public static void writeToFile(String json) {
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/sensit");
		dir.mkdirs();
		File file = new File(root + "/sensit", System.currentTimeMillis() + ".json");
		try {
			if (root.canWrite()) {
				FileWriter filewriter = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(filewriter);
				out.write(json);
				out.close();
			}
		} catch (IOException e) {
			Log.e("TAG", "Could not write file " + e.getMessage());
		}
	}

	private boolean checkExternalMedia() {
		boolean mExternalStorageAvailable;
		boolean mExternalStorageWriteable;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// Can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// Can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Can't read or write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageWriteable;
	}
}
