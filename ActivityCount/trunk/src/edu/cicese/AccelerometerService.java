package edu.cicese;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 02:39 PM
 */
public class AccelerometerService extends Service {

	private NotificationManager notificationManager;
	private Notification notification;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.service_text;

	private static Context appContext;
	private static AccelerometerService instance;
	private AccelerometerManager accelerometerManager;

	private PowerManager.WakeLock wakeLock;


	public class MyBinder extends Binder {
		public AccelerometerService getService() {
			return AccelerometerService.this;
		}
	}
	private final IBinder mBinder = new MyBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// BroadcastReceiver for handling ACTION_SCREEN_OFF.
	/*public BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Check action just to be on the safe side.
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d("shake mediator screen off", "trying re-registration");
				// Unregisters the listener and registers it again.
				stopListening();
				startListening();
			}
		}
	};*/

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting.  Put an icon in the status bar.
		showNotification();

		instance = this;
		appContext = this.getApplicationContext();

		accelerometerManager = new AccelerometerManager();

		/*IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receiver, filter);*/

//		PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
//		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Dim screen");

		/*

            @Override
        protected void onPause() {
            super.onPause();
            wl.release();
        }//End of onPause*/
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utilities.isServiceRunning = true;

		startListening();
//		wakeLock.acquire();
		// We want this service to continue running until it is explicitly stopped, so return sticky.

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Utilities.isServiceRunning = false;
		// Unregister our receiver.
//		unregisterReceiver(receiver);

		// Cancel the persistent notification.
		notificationManager.cancel(NOTIFICATION);

		stopListening();

//		wakeLock.release();
	}

	private void startListening() {
		if (accelerometerManager.isSupported()) {
			accelerometerManager.startListening(this);
		}
	}

	private void stopListening() {
		if (accelerometerManager.isListening()) {
			accelerometerManager.stopListening();
		}
	}

	public static Context getContext() {
		return appContext;
	}

	public static AccelerometerService getInstance() {
		return instance;
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.service_text);

		// Set the icon, scrolling text and timestamp
		notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());

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

		notification.setLatestEventInfo(this, getText(R.string.service_title), getText(R.string.service_text) + " / " + count + " acs (last minute)", contentIntent);

		startForeground(1234, notification);
	}
}
