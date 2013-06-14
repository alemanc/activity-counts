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
import edu.cicese.sensit.icat.IcatUtil;
import edu.cicese.sensit.util.Preferences;
import edu.cicese.sensit.util.SensitActions;

import java.util.ArrayList;
import java.util.List;

/**
 * Queries the local DB to obtain unsynced data.
 * In order to avoid request timeouts, the data is split in smaller requests.
 * Creates an AsyncTask with these requests, which will execute them sequentially.
 *
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
		// check connection preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean wifiOnly = settings.getBoolean(Preferences.KEY_PREF_WIFI_ONLY, true);

		// check connection
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
//		NetworkInfo nMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		Utilities.setSyncing(true);
		Intent syncingIntent = new Intent(SensitActions.DATA_SYNCING);
		context.sendBroadcast(syncingIntent);

		if ((wifiOnly && nWifi.isConnected()) || (!wifiOnly && (activeNetworkInfo != null && activeNetworkInfo.isConnected()))) {
			Log.d(TAG, "Connection available");
			Log.d(TAG, "Sync data");

			dbAdapter.open();

			Log.d(TAG, "Query data");
//			COLUMN_ACTIVITY_COUNT_COUNTS,
//			COLUMN_ACTIVITY_COUNT_DATE,
//			COLUMN_ACTIVITY_COUNT_CHARGING
			Cursor cursor = dbAdapter.queryCounts();

			List<ActivityCount> counts = new ArrayList<>();

			// at least one entry
			if (cursor.moveToFirst()) {
				do {
					counts.add(new ActivityCount(cursor.getString(1), cursor.getInt(0), cursor.getInt(2)));
				} while (cursor.moveToNext());
			}

			if (!counts.isEmpty()) {
				Log.d(TAG, "Sending from " + counts.get(0).getDate() + " to " + counts.get(counts.size() - 1).getDate());

				// split post request into smaller requests (max. 60 count insertions)
				List<List<ActivityCount>> lists = new ArrayList<>();
				for (int i = 0; i < counts.size(); i += IcatUtil.POST_COUNT_LIMIT) {
					lists.add(counts.subList(i, Math.min(i + IcatUtil.POST_COUNT_LIMIT, counts.size())));
				}
//				new DataUploadTask(context, lists).execute();
//				IcatUtil.postActivityCounts(context, counts);
			} else {
				Log.d(TAG, "Nothing to sync");

				Utilities.setSyncing(false);
				Intent syncedIntent = new Intent(SensitActions.DATA_SYNC_ERROR);
				syncedIntent.putExtra(SensitActions.EXTRA_MSG, "Nothing to sync");
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
			Intent syncedIntent = new Intent(SensitActions.DATA_SYNC_ERROR);
			syncedIntent.putExtra(SensitActions.EXTRA_MSG, "No connection");
			context.sendBroadcast(syncedIntent);
		}
	}
}
