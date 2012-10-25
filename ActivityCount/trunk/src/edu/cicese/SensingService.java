package edu.cicese;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 02:39 PM
 */
public class SensingService extends WakefulIntentService {

	private static final String TAG = "SensingService";
	public final static String SENSING_START_ACTION = "edu.cicese.SENSING_START_ACTION";
	public final static String SENSING_STOP_ACTION = "edu.cicese.SENSING_STOP_ACTION";
	public final static String ACTION_ID_FIELD_NAME = "action_id";

	private NotificationManager notificationManager;
	private Notification notification;

	// Unique ID for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.service_text;
	private AccelerometerManager accelerometerManager;
	private BatteryThread batteryThread;
	private static Context appContext;

	/**
	 * This constructor is never used directly, it is used by the superclass
	 * methods when it's first created.
	 */
	public SensingService() {
		super("SensingService");
	}

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
		Log.d(TAG, "DESTROY!");

		super.onDestroy();

		// Cancel the persistent notification.
		notificationManager.cancel(NOTIFICATION);

		stopSensing();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	/**
	 * This method is invoked on the worker thread with a request to process.
	 */
	protected void doWakefulWork(Intent intent) {
		Log.d(TAG, "Action received: " + intent.getAction());

		/* SENSING ACTION */
		if (intent.getAction().compareTo(SENSING_START_ACTION) == 0) {
			startSensing();
		} else if (intent.getAction().compareTo(SENSING_STOP_ACTION) == 0) {
			stopSensing();
		} else {
			Log.e(TAG, "Unknown action received: " + intent.getAction());
			return;
		}
	}

	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utilities.isSensing = true;

		startListening();

		// We want this service to continue running until it is explicitly stopped, so return sticky.
		return START_STICKY;
	}*/

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
		batteryThread.done();

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
