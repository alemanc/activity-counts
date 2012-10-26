package edu.cicese;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 02:39 PM
 */
public class SensingService extends Service/* extends WakefulIntentService*/ {

	public static final String TAG = "SensingService";
	public static PowerManager.WakeLock WAKE_LOCK = null;

	private NotificationManager notificationManager;
	private Notification notification;

	// Unique ID for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.service_text;
	private AccelerometerManager accelerometerManager;
	private BatteryThread batteryThread;
	private static Context appContext;

	@Override
	public void onCreate() {
		super.onCreate();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting. Put an icon in the status bar.
		showNotification();

		appContext = this.getApplicationContext();
		accelerometerManager = new AccelerometerManager();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "Destroy Sensing Service.");

		// Cancel the persistent notification.
		notificationManager.cancel(NOTIFICATION);

		stopSensing();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startSensing();

		// We want this service to continue running until it is explicitly stopped, so return sticky.
		return START_STICKY;
	}

	private void startSensing() {
		Log.d(TAG, "Start sensing");
		Utilities.isSensing = true;

		// Start sensing battery level
		batteryThread = new BatteryThread(this);
		batteryThread.start();

		// Start sensing activity counts
		if (accelerometerManager.isSupported()) {
			accelerometerManager.startListening(this);
		}
	}

	private void stopSensing() {
		Log.d(TAG, "Stop sensing");

		Utilities.resetValues();
		// Start sensing battery level
		if (batteryThread != null) {
			batteryThread.done();
		}
		WAKE_LOCK.release();
		WAKE_LOCK = null;

		// Stop sensing activity counts
		if (accelerometerManager.isListening()) {
			accelerometerManager.stopListening();
		}
	}

	public static Context getContext() {
		return appContext;
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.service_text);

		// Set the icon, scrolling text and timestamp
		notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());

		//TODO change to Notification.Bundle API 11

		// The PendingIntent to launch our activity if the user selects this notification
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// Set the info for the views that show in the notification panel
		notification.setLatestEventInfo(this, getText(R.string.service_title), text, contentIntent);

		// Set the vibration
//		notification.defaults |= Notification.DEFAULT_VIBRATE;

		// Send the notification
//		notificationManager.notify(NOTIFICATION, notification);

		// Prevent service self kill
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(1234, notification);
	}

	public void updateNotification(int count) {
		// The PendingIntent to launch our activity if the user selects this notification
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		notification.setLatestEventInfo(this, getText(R.string.service_title), getText(R.string.service_text) + " / " +
				count + " acs (last epoch) / " + Utilities.getBatteryLevel() + "%", contentIntent);

		startForeground(1234, notification);
	}
}
