package edu.cicese.sensit.icat;

import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 3/06/13
 * Time: 01:56 PM
 */
public class IcatUtil {
	private static final String TAG = "SensIt.IcatUtil";

	public static final String ICAT_URL = "http://icat2013.herokuapp.com";
	private static AsyncHttpClient client = new AsyncHttpClient();

	public static final String API_KEY = "iCAT-2013-1234567890";
	public static final String ACTIVITY_COUNTS = "/activity_counts/";
	public static final String SURVEYS = "/surveys/";

	public static final int POST_LIMIT = 60;

	public static final String ICAT_STATUS = "icat_status";
	public static final int ICAT_STATUS_OK = 200;
	public static final int ICAT_STATUS_OK_WITH_ERRORS = 250;

	public static final int TIMEOUT = 30000; //milliseconds

	private static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		String encodedUrl;
		encodedUrl = getAbsoluteUrl(url).replace(" ", "+");
		client.setTimeout(TIMEOUT);
		client.post(encodedUrl, params, responseHandler);
		Log.d(TAG, "POST: " + encodedUrl);
	}

	public static String getAbsoluteUrl(String relativeUrl) {
		return ICAT_URL + relativeUrl;
	}

	/*public static void postActivityCounts(final Context context, final List<ActivityCount> counts) {
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
		username = "TestUser";
		params.put("bundle", "{\"api_key\":\"" + API_KEY + "\",\"username\":\"" + username + "\",\"activity_counts\":" + gsonCounts + "}\n");

		post(ACTIVITY_COUNTS, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject response) {
				int status = response.optInt(ICAT_STATUS);
				Log.d(TAG, "Success! " + response + " status " + status);
				if (status == ICAT_STATUS_OK || status == ICAT_STATUS_OK_WITH_ERRORS) {
					Intent broadcastIntent = new Intent(SensitActions.DATA_SYNCED);
					broadcastIntent.putExtra(SensitActions.EXTRA_SYNCED, true);
					broadcastIntent.putExtra(SensitActions.EXTRA_DATE_START, counts.get(0).getDate());
					broadcastIntent.putExtra(SensitActions.EXTRA_DATE_END, counts.get(counts.size() - 1).getDate());
					broadcastIntent.putExtra(SensitActions.EXTRA_MSG, "Data synced");
					context.sendOrderedBroadcast(broadcastIntent, null);
				}
			}

			@Override
			public void onFailure(Throwable throwable, String response) {
				Log.e(TAG, "", throwable);

				Intent broadcastIntent = new Intent(SensitActions.DATA_SYNCED);
				broadcastIntent.putExtra(SensitActions.EXTRA_SYNCED, false);
				broadcastIntent.putExtra(SensitActions.EXTRA_MSG, "Sync error");
				context.sendOrderedBroadcast(broadcastIntent, null);
			}
		});
	}*/

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
