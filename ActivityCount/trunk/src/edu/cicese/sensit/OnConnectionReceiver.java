package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:01 PM
 */
public class OnConnectionReceiver extends BroadcastReceiver {
	private static final String TAG = "SensIt.OnConnectionReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received action " + intent.getAction() + " at OnConnectionReceiver");

		if (Utilities.syncNeeded(context)) {
			try {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					boolean connected = true;
					if (bundle.containsKey(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
						connected = false;
					}
					Log.d(TAG, "Connected: " + connected);
					if (connected) {
						new Thread(new DataUploadThread(context)).start();
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "", e);
			}
		}
		else {
			Log.d(TAG, "No need to sync");
		}

		/*ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
		if (isConnected) {
			Log.d(TAG, "connected" + isConnected);
		}
		else {
			Log.d(TAG, "not connected" + isConnected);
		}*/

		/*// check connection preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean wifiOnly = settings.getBoolean(Preferences.KEY_PREF_WIFI_ONLY, true);

		// check connection
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

		new Thread(new DataUploadThread(context)).start();*/
	}
}