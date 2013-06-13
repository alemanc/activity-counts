package edu.cicese.sensit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.cicese.sensit.database.DBAdapter;
import edu.cicese.sensit.icat.IcatApiUtil;
import edu.cicese.sensit.util.Preferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 07:07 PM
 */
public class DataUploadThread implements Runnable {
	public static final String TAG = "SensIt.DataUploadThread";
	private DBAdapter dbAdapter;
	private Context context;

	public DataUploadThread(Context context) {
		this.context = context;
		dbAdapter = new DBAdapter(context);
	}

	@Override
	public void run() {
		//Upload new data to server using the iCAT REST API
		// Make the necessary changes to the local db to indicate which data has been synced

		// Check connection preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean wifiOnly = settings.getBoolean(Preferences.KEY_PREF_WIFI_ONLY, true);

		Log.d(TAG, "Checking connection " + wifiOnly);

		// Check if WiFi connection available
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo nMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		Log.d(TAG, "Connections: " + nWifi.toString());
		Log.d(TAG, "Connections: " + nMobile.toString());

		Utilities.setSyncing(true);
		Intent syncingIntent = new Intent(SensingService.DATA_SYNCING);
		context.sendBroadcast(syncingIntent);

		if (nWifi.isConnected()) {
			Log.d(TAG, "Connection available");
			Log.d(TAG, "Sync data");

			dbAdapter.open();

			Log.d(TAG, "Query data");
			Cursor cursor = dbAdapter.queryCounts();

			List<ActivityCount> counts = new ArrayList<>();

			// at least one entry
			if (cursor.moveToFirst()) {
				do {
					counts.add(new ActivityCount(cursor.getString(3), cursor.getInt(1), cursor.getInt(4)));
				} while (cursor.moveToNext());
			}

			if (!counts.isEmpty()) {
				Log.d(TAG, "Sending from " + counts.get(0).getDate() + " to " + counts.get(counts.size() - 1).getDate());

				// split post request
				List<List<ActivityCount>> lists = new ArrayList<>();
				for (int i = 0; i < counts.size(); i += IcatApiUtil.POST_COUNT_LIMIT) {
					lists.add(counts.subList(i, Math.min(i + IcatApiUtil.POST_COUNT_LIMIT, counts.size())));
				}
//				new DataUploadTask(context, lists).execute();
//				IcatApiUtil.postActivityCounts(context, counts);
			} else {
				Log.d(TAG, "Nothing to sync");

				Utilities.setSyncing(false);
				Intent syncedIntent = new Intent(SensingService.DATA_SYNC_ERROR);
				syncedIntent.putExtra(IcatApiUtil.EXTRA_MSG, "Nothing to sync");
				context.sendBroadcast(syncedIntent);
			}

			if (!cursor.isClosed()) {
				cursor.close();
			}

			dbAdapter.close();
		}
		else {
			Log.d(TAG, "No connection");

			Utilities.setSyncing(false);
			Intent syncedIntent = new Intent(SensingService.DATA_SYNC_ERROR);
			syncedIntent.putExtra(IcatApiUtil.EXTRA_MSG, "No connection");
			context.sendBroadcast(syncedIntent);
		}
	}
}
