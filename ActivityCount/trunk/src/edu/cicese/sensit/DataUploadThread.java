package edu.cicese.sensit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cicese.sensit.db.DBAdapter;
import edu.cicese.sensit.icat.IcatUtil;
import edu.cicese.sensit.util.Preferences;
import edu.cicese.sensit.util.SensitActions;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
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
	private boolean manual = false;

	public DataUploadThread(Context context) {
		Log.d(TAG, "Created");
		this.context = context;
		dbAdapter = new DBAdapter(context);
	}

	public DataUploadThread(Context context, boolean manual) {
		this(context);
		this.manual = manual;
	}

	@Override
	public void run() {
		Log.d(TAG, "Run");
		if (!Utilities.isSyncing()) {
			if (manual || Utilities.syncNeeded(context)) {
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
						boolean successful = true;

						if (!counts.isEmpty()) {
							Log.d(TAG, "Sending COUNTS from " + counts.get(0).getDate() + " to " + counts.get(counts.size() - 1).getDate());
							for (int i = 0; i < counts.size(); i += IcatUtil.POST_LIMIT) {
								countLists.add(counts.subList(i, Math.min(i + IcatUtil.POST_LIMIT, counts.size())));
							}
							successful &= postCounts(countLists);
						}
						if (!surveys.isEmpty()) {
							Log.d(TAG, "Sending SURVEYS from " + surveys.get(0).getDate() + " to " + surveys.get(surveys.size() - 1).getDate());
							for (int i = 0; i < surveys.size(); i += IcatUtil.POST_LIMIT) {
								surveyLists.add(surveys.subList(i, Math.min(i + IcatUtil.POST_LIMIT, surveys.size())));
							}
							successful &= postSurveys(surveyLists);
						}

						Log.d(TAG, "SYNC DONE");
						if (successful) {
							Utilities.setLastSync(context, System.currentTimeMillis());
						}
						Utilities.setSyncing(false);
						Intent broadcastIntent = new Intent(SensitActions.ACTION_DATA_SYNC_DONE);
						context.sendBroadcast(broadcastIntent);

//						new DataUploadTask(context, countLists, surveyLists).execute();
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

	private boolean postCounts(List<List<ActivityCount>> countLists) {
		boolean successful = true;

		for (List<ActivityCount> countList : countLists) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(IcatUtil.ICAT_URL + IcatUtil.ACTIVITY_COUNTS);
			HttpParams httpParams = httpPost.getParams();
			ConnManagerParams.setTimeout(httpParams, IcatUtil.TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, IcatUtil.TIMEOUT);
			HttpConnectionParams.setConnectionTimeout(httpParams, IcatUtil.TIMEOUT);

			try {
				Type listOfTestObject = new TypeToken<List<ActivityCount>>() {
				}.getType();
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				String gsonCounts = gson.toJson(countList, listOfTestObject);
				Log.d(TAG, "GSON! " + gsonCounts);

				String username = Utilities.getMacAddress(context);
				if (username != null) {
					String bundle = "{\"api_key\":\"" + IcatUtil.API_KEY + "\",\"username\":\"" + username + "\",\"activity_counts\":" + gsonCounts + "}\n";

					// add parameters
					List<NameValuePair> nameValuePairs = new ArrayList<>();
					nameValuePairs.add(new BasicNameValuePair("bundle", bundle));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					// execute HTTP Post Request
					HttpResponse response = httpclient.execute(httpPost);

					// get hold of the response entity
					HttpEntity entity = response.getEntity();
					// if the response does not enclose an entity, there is no need to worry about connection release

					if (entity != null) {
						// a Simple JSON Response Read
						InputStream inStream = entity.getContent();
						String result = IcatUtil.convertStreamToString(inStream);

						Log.d(TAG, "RESPONSE: " + result);
						JSONObject joResponse = new JSONObject(result);

						int status = joResponse.optInt(IcatUtil.ICAT_STATUS);
						if (status == IcatUtil.ICAT_STATUS_OK || status == IcatUtil.ICAT_STATUS_OK_WITH_ERRORS) {
							Intent broadcastIntent = new Intent(SensitActions.ACTION_DATA_SYNCED);
							broadcastIntent.putExtra(SensitActions.EXTRA_SYNCED_TYPE, Utilities.TYPE_COUNT);
							broadcastIntent.putExtra(SensitActions.EXTRA_DATE_START, countList.get(0).getDate());
							broadcastIntent.putExtra(SensitActions.EXTRA_DATE_END, countList.get(countList.size() - 1).getDate());
							context.sendOrderedBroadcast(broadcastIntent, null);
						}

						// closing the input stream will trigger connection release
						inStream.close();
					} else {
						// error?
						successful = false;
					}
				} else {
					// No username
					Log.e(TAG, "NULL Username. Well, this is awkward, this was never suppose to happen.");
					successful = false;
				}

			} catch (ClientProtocolException e) {
				Log.e(TAG, "", e);
				successful = false;
			} catch (IOException e) {
				Log.e(TAG, "", e);
				successful = false;
			} catch (JSONException e) {
				Log.e(TAG, "", e);
				successful = false;
			}
		}

		return successful;
	}

	private boolean postSurveys(List<List<Survey>> surveyLists) {
		boolean successful = true;

		for (List<Survey> surveys : surveyLists) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(IcatUtil.ICAT_URL + IcatUtil.SURVEYS);
			HttpParams httpParams = httpPost.getParams();
			ConnManagerParams.setTimeout(httpParams, IcatUtil.TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, IcatUtil.TIMEOUT);
			HttpConnectionParams.setConnectionTimeout(httpParams, IcatUtil.TIMEOUT);

			try {
				Type listOfTestObject = new TypeToken<List<Survey>>() {
				}.getType();
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				String gsonSurveys = gson.toJson(surveys, listOfTestObject);
				Log.d(TAG, "GSON! " + gsonSurveys);

				String username = Utilities.getMacAddress(context);
				if (username != null) {
					String bundle = "{\"api_key\":\"" + IcatUtil.API_KEY + "\",\"username\":\"" + username + "\",\"surveys\":" + gsonSurveys + "}\n";

					// add parameters
					List<NameValuePair> nameValuePairs = new ArrayList<>();
					nameValuePairs.add(new BasicNameValuePair("bundle", bundle));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					// execute HTTP Post Request
					HttpResponse response = httpclient.execute(httpPost);

					// get hold of the response entity
					HttpEntity entity = response.getEntity();
					// if the response does not enclose an entity, there is no need to worry about connection release

					if (entity != null) {
						// a Simple JSON Response Read
						InputStream inStream = entity.getContent();
						String result = IcatUtil.convertStreamToString(inStream);

						Log.d(TAG, "RESPONSE: " + result);
						JSONObject joResponse = new JSONObject(result);

						int status = joResponse.optInt(IcatUtil.ICAT_STATUS);
						if (status == IcatUtil.ICAT_STATUS_OK || status == IcatUtil.ICAT_STATUS_OK_WITH_ERRORS) {
							Intent broadcastIntent = new Intent(SensitActions.ACTION_DATA_SYNCED);
							broadcastIntent.putExtra(SensitActions.EXTRA_SYNCED_TYPE, Utilities.TYPE_SURVEY);
							broadcastIntent.putExtra(SensitActions.EXTRA_DATE_START, surveys.get(0).getDate());
							broadcastIntent.putExtra(SensitActions.EXTRA_DATE_END, surveys.get(surveys.size() - 1).getDate());
							context.sendOrderedBroadcast(broadcastIntent, null);
						}

						// closing the input stream will trigger connection release
						inStream.close();
					} else {
						// error?
						successful = false;
					}
				} else {
					// No username
					Log.e(TAG, "NULL Username. Well, this is awkward, this was never suppose to happen.");
					successful = false;
				}

			} catch (ClientProtocolException e) {
				Log.e(TAG, "", e);
				successful = false;
			} catch (IOException e) {
				Log.e(TAG, "", e);
				successful = false;
			} catch (JSONException e) {
				Log.e(TAG, "", e);
				successful = false;
			}
		}

		return successful;
	}
}
