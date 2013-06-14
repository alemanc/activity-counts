package edu.cicese.sensit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cicese.sensit.icat.IcatUtil;
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
 * Uploads unsynced data to the server using the iCAT REST API.
 * The constructor receives a list of lists of ActivityCounts. Each list will be sent, sequentially,
 * in POST requests.
 * Upon response from each request, a broadcast will be sent indicating what data was synced, in order
 * to perform the corresponding updates in the local DB.
 * When the last response is received, another broadcast is sent to inform that the update process is completed,
 * with or without errors.
 *
 * Created by: Eduardo Quintana Contreras
 * Date: 12/06/13
 * Time: 05:51 PM
 */
public class DataUploadTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "SensIt.DataUploadTask";

	private List<List<ActivityCount>> lists;
	private Context context;
	private boolean syncError;

	public DataUploadTask(Context context, List<List<ActivityCount>> lists) {
		this.lists = lists;
		this.context = context;
	}

	@Override
	protected final Void doInBackground(Void... voids) {
		List<ActivityCount> counts = lists.get(0);
		if (lists.get(0) != null) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(IcatUtil.ICAT_URL + IcatUtil.ACTIVITY_COUNTS);
			HttpParams httpParams = httpPost.getParams();
			ConnManagerParams.setTimeout(httpParams, IcatUtil.TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, IcatUtil.TIMEOUT);
			HttpConnectionParams.setConnectionTimeout(httpParams, IcatUtil.TIMEOUT);

			try {
				Type listOfTestObject = new TypeToken<List<ActivityCount>>() {}.getType();
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				String gsonCounts = gson.toJson(counts, listOfTestObject);
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
							Intent broadcastIntent = new Intent(SensitActions.DATA_SYNCED);
//						broadcastIntent.putExtra(IcatUtil.EXTRA_SYNCED, true);
							broadcastIntent.putExtra(SensitActions.EXTRA_DATE_START, counts.get(0).getDate());
							broadcastIntent.putExtra(SensitActions.EXTRA_DATE_END, counts.get(counts.size() - 1).getDate());
							context.sendOrderedBroadcast(broadcastIntent, null);
						}

						// closing the input stream will trigger connection release
						inStream.close();
					} else {
						// error?
						syncError = true;
					}
				}
				else {
					// No username
					Log.e(TAG, "NULL Username. Well, this is awkward, this was never suppose to happen.");
					syncError = true;
				}

			} catch (ClientProtocolException e) {
				syncError = true;
				Log.e(TAG, "", e);
			} catch (IOException e) {
				syncError = true;
				Log.e(TAG, "", e);
			} catch (JSONException e) {
				syncError = true;
				Log.e(TAG, "", e);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void unused) {
		if (syncError) {
			Log.d(TAG, "Sync Error");
		}

		lists.remove(0);
		if (!lists.isEmpty() && !isCancelled() && !syncError) {
			new DataUploadTask(context, lists).execute();
		}
		else {
			Utilities.setSyncing(false);
			Intent broadcastIntent = new Intent(SensitActions.DATA_SYNC_DONE);
			context.sendBroadcast(broadcastIntent);
		}
	}
}
