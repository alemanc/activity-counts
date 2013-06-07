package edu.cicese.sensit.icat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import edu.cicese.sensit.ActivityCount;
import edu.cicese.sensit.SensingService;
import edu.cicese.sensit.Utilities;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 3/06/13
 * Time: 01:56 PM
 */
public class IcatUtil {
	private static final String TAG = "SensIt.IcatUtil";

	private static final String ICAT_URL = "http://icat2013.herokuapp.com";
	public static final String EXTRA_DATE_START = "extra_date_start";
	public static final String EXTRA_DATE_END = "extra_date_end";
	private static AsyncHttpClient client = new AsyncHttpClient();

	private static final String ACTIVITY_COUNTS = "/activity_counts/";

	private static final String ICAT_STATUS = "icat_status";
	private static final int ICAT_STATUS_OK = 200;
	private static final int ICAT_STATUS_OK_WITH_ERRORS = 250;

	private static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		String encodedUrl;
		encodedUrl = getAbsoluteUrl(url).replace(" ", "+");
		client.post(encodedUrl, params, responseHandler);
		Log.d(TAG, "POST: " + encodedUrl);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return ICAT_URL + relativeUrl;
	}

	public static void postActivityCounts(final Context context, final List<ActivityCount> counts) {
		RequestParams params = new RequestParams();
//		params.put("username", Utilities.getMacAddress(context));
//		params.put("username", "Eduardo");

		Type listOfTestObject = new TypeToken<List<ActivityCount>>(){}.getType();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		String gsonCounts = gson.toJson(counts, listOfTestObject);
		Log.d(TAG, "GSON! " + gsonCounts);

//		JsonObject joCounts = new JsonObject();
//		joCounts.addProperty("activity_counts", s);

		String username = Utilities.getMacAddress(context);
		params.put("bundle", "{\"api_key\":\"icat1234\",\"username\":\"" + username + "\",\"activity_counts\":" + gsonCounts + "}\n");

		post(ACTIVITY_COUNTS, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject response) {
				int status = response.optInt(ICAT_STATUS);
				Log.d(TAG, "Success! " + response + " status " + status);
				if (status == ICAT_STATUS_OK || status == ICAT_STATUS_OK_WITH_ERRORS) {
					Intent broadcastIntent = new Intent(SensingService.DATA_SYNCED);
					broadcastIntent.putExtra(EXTRA_DATE_START, counts.get(0).getDate());
					broadcastIntent.putExtra(EXTRA_DATE_END, counts.get(counts.size() - 1).getDate());
					context.sendBroadcast(broadcastIntent);
				}
			}

			@Override
			public void onFailure(Throwable throwable, String response) {
				String error = response != null ? response : "Null response.";
				Log.d(TAG, "Error! " + error);
			}
		});

		/*Log.d(TAG, "SECOND TRY");

		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("icat2013.herokuapp.com/activity_counts");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<>(2);
			nameValuePairs.add(new BasicNameValuePair("username", "Karina"));
			nameValuePairs.add(new BasicNameValuePair("counts", "{\"activity_counts\":[{\"date\":\"2013-05-16 15:16:00\",\"counts\":1350,\"epoch\":60,\"charging\":1},\\r\\n\n" +
					"{\"date\":\"2013-05-16 15:17:00\",\"counts\":50,\"epoch\":60,\"charging\":1}]}"));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpClient.execute(httpPost);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				// A Simple JSON Response Read
				InputStream inStream = entity.getContent();
				String result = convertStreamToString(inStream);

				Log.d(TAG, "RESPONSE: " + result);

				// Closing the input stream will trigger connection release
				inStream.close();
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Error! " + e.toString(), e);
		} catch (IOException e) {
			Log.e(TAG, "Error! " + e.toString(), e);
		}*//* catch (JSONException e) {
			Log.e(TAG, "Error! " + e.toString(), e);
		}*//*

		Log.d(TAG, "THIRD TRY");

		// Create a new HttpClient and Post Header
		HttpClient httpClient3 = new DefaultHttpClient();
		HttpGet httpPost3 = new HttpGet("icat2013.herokuapp.com/users");
		HttpGet httpPost4 = new HttpGet("http://icat2013.herokuapp.com/users");

		try {
			// Add your data
			*//*List<NameValuePair> nameValuePairs = new ArrayList<>(2);
			nameValuePairs.add(new BasicNameValuePair("username", "Karina"));
			nameValuePairs.add(new BasicNameValuePair("counts", "{\"activity_counts\":[{\"date\":\"2013-05-16 15:16:00\",\"counts\":1350,\"epoch\":60,\"charging\":1},\\r\\n\n" +
					"{\"date\":\"2013-05-16 15:17:00\",\"counts\":50,\"epoch\":60,\"charging\":1}]}"));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));*//*

			// Execute HTTP Post Request
			Log.d(TAG, "THIRD TRY (1)");
			HttpResponse response = httpClient3.execute(httpPost3);
			Log.d(TAG, "THIRD TRY (2)");
			HttpResponse response2 = httpClient3.execute(httpPost4);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				// A Simple JSON Response Read
				InputStream inStream = entity.getContent();
				String result = convertStreamToString(inStream);

				Log.d(TAG, "RESPONSE: " + result);

				// Closing the input stream will trigger connection release
				inStream.close();
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Error! " + e.toString(), e);
		} catch (IOException e) {
			Log.e(TAG, "Error! " + e.toString(), e);
		}*/
	}

	/**
	 * To convert the InputStream to String we use the BufferedReader.readLine()
	 * method. We iterate until the BufferedReader return null which means
	 * there's no more data to read. Each line will appended to a StringBuilder
	 * and returned as String.
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
