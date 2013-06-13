package edu.cicese.sensit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cicese.sensit.icat.IcatApiUtil;
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

			if (counts.size() > IcatApiUtil.POST_COUNT_LIMIT) {
				// split post request
			}

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(IcatApiUtil.ICAT_URL + IcatApiUtil.ACTIVITY_COUNTS);
			HttpParams httpParams = httpPost.getParams();
			ConnManagerParams.setTimeout(httpParams, IcatApiUtil.TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, IcatApiUtil.TIMEOUT);
			HttpConnectionParams.setConnectionTimeout(httpParams, IcatApiUtil.TIMEOUT);

			try {
				Type listOfTestObject = new TypeToken<List<ActivityCount>>() {}.getType();
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				String gsonCounts = gson.toJson(counts, listOfTestObject);
				Log.d(TAG, "GSON! " + gsonCounts);

				String username = "b4:07:f9:f5:2c:10";
				String API_KEY = "iCAT-2013-1234567890";

				String bundle = "{\"api_key\":\"" + API_KEY + "\",\"username\":\"" + username + "\",\"activity_counts\":" + gsonCounts + "}\n";

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
					String result = Utilities.convertStreamToString(inStream);

					Log.d(TAG, "RESPONSE: " + result);
					JSONObject joResponse = new JSONObject(result);

					int status = joResponse.optInt(IcatApiUtil.ICAT_STATUS);
					if (status == IcatApiUtil.ICAT_STATUS_OK || status == IcatApiUtil.ICAT_STATUS_OK_WITH_ERRORS) {
						Intent broadcastIntent = new Intent(SensingService.DATA_SYNCED);
//						broadcastIntent.putExtra(IcatApiUtil.EXTRA_SYNCED, true);
						broadcastIntent.putExtra(IcatApiUtil.EXTRA_DATE_START, counts.get(0).getDate());
						broadcastIntent.putExtra(IcatApiUtil.EXTRA_DATE_END, counts.get(counts.size() - 1).getDate());
						context.sendOrderedBroadcast(broadcastIntent, null);
					}

					// closing the input stream will trigger connection release
					inStream.close();
				}
				else {
					// error?
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
	protected void onPreExecute() {

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
			Intent broadcastIntent = new Intent(SensingService.DATA_SYNC_DONE);
			context.sendBroadcast(broadcastIntent);
		}
	}
}
