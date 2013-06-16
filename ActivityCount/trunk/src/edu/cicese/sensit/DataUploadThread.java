package edu.cicese.sensit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.cicese.sensit.db.DBAdapter;
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
		Log.d(TAG, "Created");
		this.context = context;
		dbAdapter = new DBAdapter(context);
	}

	@Override
	public void run() {
		Log.d(TAG, "Run");
		if (!Utilities.isSyncing()) {
			if (Utilities.syncNeeded(context)) {
				Utilities.setSyncing(true);

				// check connection preferences
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
				boolean wifiOnly = settings.getBoolean(Preferences.KEY_PREF_WIFI_ONLY, true);

				// check connection
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo nWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
//		        NetworkInfo nMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

				Intent syncingIntent = new Intent(SensitActions.ACTION_DATA_SYNCING);
				context.sendBroadcast(syncingIntent);

				if ((wifiOnly && nWifi.isConnected()) || (!wifiOnly && (activeNetworkInfo != null && activeNetworkInfo.isConnected()))) {
					Log.d(TAG, "Connection available");
					Log.d(TAG, "Sync data");

					dbAdapter.open();

					Log.d(TAG, "Query data");

					Log.d(TAG, "Querying unsynced counts");
//			        COLUMN_ACTIVITY_COUNT_COUNTS,
//			        COLUMN_ACTIVITY_COUNT_DATE,
//			        COLUMN_ACTIVITY_COUNT_CHARGING
					Cursor countsCursor = dbAdapter.queryCounts();
					List<ActivityCount> counts = new ArrayList<>();
					// at least one entry
					if (countsCursor.moveToFirst()) {
						do {
							counts.add(new ActivityCount(countsCursor.getString(1), countsCursor.getInt(0), countsCursor.getInt(2)));
						} while (countsCursor.moveToNext());
					}

					Log.d(TAG, "Querying unsynced surveys");
//					COLUMN_SURVEY_DATE,
//					COLUMN_SURVEY_VALUE_STRESS,
//					COLUMN_SURVEY_VALUE_CHALLENGE,
//					COLUMN_SURVEY_VALUE_SKILL,
//					COLUMN_SURVEY_VALUE_AVOIDANCE,
//					COLUMN_SURVEY_VALUE_EFFORT,
					Cursor surveysCursor = dbAdapter.querySurveys();
					List<Survey> surveys = new ArrayList<>();
					// at least one entry
					if (surveysCursor.moveToFirst()) {
						do {
							surveys.add(new Survey(
									surveysCursor.getString(0),
									surveysCursor.getInt(1),
									surveysCursor.getInt(2),
									surveysCursor.getInt(3),
									surveysCursor.getInt(4),
									surveysCursor.getInt(5)));
						} while (surveysCursor.moveToNext());
					}

					if (counts.isEmpty() && surveys.isEmpty()) {
						Log.d(TAG, "Nothing to sync");

						Utilities.setSyncing(false);
						Intent syncedIntent = new Intent(SensitActions.ACTION_DATA_SYNC_ERROR);
						syncedIntent.putExtra(SensitActions.EXTRA_MSG, "Nothing to sync");
						context.sendBroadcast(syncedIntent);
					} else {
						// split post requests into smaller requests (max. 60 insertions)
						List<List<ActivityCount>> countLists = new ArrayList<>();
						List<List<Survey>> surveyLists = new ArrayList<>();

						if (!counts.isEmpty()) {
							Log.d(TAG, "Sending COUNTS from " + counts.get(0).getDate() + " to " + counts.get(counts.size() - 1).getDate());
							for (int i = 0; i < counts.size(); i += IcatUtil.POST_LIMIT) {
								countLists.add(counts.subList(i, Math.min(i + IcatUtil.POST_LIMIT, counts.size())));
							}
						}
						if (!surveys.isEmpty()) {
							Log.d(TAG, "Sending SURVEYS from " + surveys.get(0).getDate() + " to " + surveys.get(surveys.size() - 1).getDate());
							for (int i = 0; i < surveys.size(); i += IcatUtil.POST_LIMIT) {
								surveyLists.add(surveys.subList(i, Math.min(i + IcatUtil.POST_LIMIT, surveys.size())));
							}
						}

						new DataUploadTask(context, countLists, surveyLists).execute();
					}

					if (!countsCursor.isClosed()) {
						countsCursor.close();
					}
					if (!surveysCursor.isClosed()) {
						surveysCursor.close();
					}

					dbAdapter.close();
				}
				else {
					Log.d(TAG, "No connection");

					Utilities.setSyncing(false);
					Intent syncedIntent = new Intent(SensitActions.ACTION_DATA_SYNC_ERROR);
					syncedIntent.putExtra(SensitActions.EXTRA_MSG, "No connection");
					context.sendBroadcast(syncedIntent);
				}
			}
			else {
				Log.d(TAG, "No need to sync, last sync was less than 20 minutes ago");
			}
		}
		else {
			Log.d(TAG, "There is another task performing the data sync");
		}
	}
}
