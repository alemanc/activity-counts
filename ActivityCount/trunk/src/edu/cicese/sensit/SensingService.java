package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import edu.cicese.sensit.database.DBAdapter;
import edu.cicese.sensit.util.ActivityUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 23/08/12
 * Time: 02:39 PM
 */
public class SensingService extends WakefulIntentService/* extends WakefulIntentService*/ {

	public static final String TAG = "SensIt.SensingService";
//	public static PowerManager.WakeLock WAKE_LOCK = null;

	public final static String SENSING_START_ACTION = "edu.cicese.sensit.SENSING_START_ACTION";
	public final static String SENSING_STOP_ACTION = "edu.cicese.sensit.SENSING_STOP_ACTION";
	public final static String SENSING_START_ACTION_COMPLETE = "edu.cicese.sensit.SENSING_START_ACTION_COMPLETE";
	public final static String SENSING_STOP_ACTION_COMPLETE = "edu.cicese.sensit.SENSING_STOP_ACTION_COMPLETE";
	public final static String ACTION_ID_FIELD_NAME = "action_id";
	private long actionId;

	private ScheduledExecutorService scheduleTaskExecutor;

	private DBAdapter dbAdapter;

//	private NotificationManager notificationManager;
//	private Notification notification;

	// Unique ID for the Notification.
	// We use it on Notification start, and to cancel it.
//	private int NOTIFICATION = R.string.service_text;
//	private AccelerometerManager accelerometerManager;
//	private BatteryThread batteryThread;
//	private static Context appContext;

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

		dbAdapter = new DBAdapter(this);

		// Register the broadcast receiver to receive TIME_TICK
		IntentFilter intentTimeFilter = new IntentFilter();
		intentTimeFilter.addAction(Intent.ACTION_TIME_TICK);
		intentTimeFilter.addAction(Intent.ACTION_TIME_CHANGED);
		intentTimeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(tickReceiver, intentTimeFilter);

		// Register receiver to wait until is stopped
//		IntentFilter intentFilter = new IntentFilter(SensingService.SENSING_STOP_ACTION);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SensingService.SENSING_START_ACTION_COMPLETE);
		intentFilter.addAction(SensingService.SENSING_STOP_ACTION_COMPLETE);
		registerReceiver(sensingActionReceiver, intentFilter);

//		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting. Put an icon in the status bar.
//		showNotification();

//		appContext = this.getApplicationContext();
//		accelerometerManager = new AccelerometerManager();

		Thread.setDefaultUncaughtExceptionHandler(onRuntimeError);
		Log.d(TAG, "SensingService created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Cancel the persistent notification.
//		notificationManager.cancel(NOTIFICATION);

		Log.d(TAG, "SensingService stopped");
		Log.d(TAG, "Unregister broadcast receiver");
		unregisterReceiver(sensingActionReceiver);
		Log.d(TAG, "Shutdown executor");
		scheduleTaskExecutor.shutdown();

		handler.removeCallbacksAndMessages(null);

		unregisterReceiver(tickReceiver);

		dbAdapter.close();

		Utilities.setReady(true);
	}

	/*@Override
	public IBinder onBind(Intent intent) {
		return null;
	}*/


	/**
	 * This method is invoked on the worker thread with a request to process.
	 */
	protected void doWakefulWork(Intent intent) {
		if (intent.getAction().compareTo(SENSING_START_ACTION) == 0) {
			Log.e(TAG, "START!");
			startSensing();

			/*// Send broadcast the end of this process
			actionId = intent.getLongExtra(ACTION_ID_FIELD_NAME, -1);
			// Send broadcast the end of this process
			Intent broadcastIntent = new Intent(SENSING_START_ACTION_COMPLETE);
			broadcastIntent.putExtra(ACTION_ID_FIELD_NAME, actionId);
			sendBroadcast(broadcastIntent);*/

		} else if (intent.getAction().compareTo(SENSING_STOP_ACTION) == 0) {
			Log.e(TAG, "STOP!");
			stopSensing();
		} else {
			Log.e(TAG, "Unknown action received: " + intent.getAction());
			return;
		}
	}

	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startSensing();

		// We want this service to continue running until it is explicitly stopped, so return sticky.
		return START_STICKY;
	}*/


	private static SessionController controller;

	private void startSensing() {
		Log.d(TAG, "Start sensing");
		Utilities.setSensing(true);

		controller = new SessionController(this);
		controller.start();

		/*Thread thread = new Thread() {
			@Override
			public void run() {
				while (Utilities.isSensing()) {}
			}
		};
		thread.start();
		Log.d(TAG, "Thread done");*/

//		while(Utilities.isSensing()) {}

		// Start sensing battery level
		/*batteryThread = new BatteryThread(this);
		batteryThread.start();

		// Start sensing activity counts
		if (accelerometerManager.isSupported()) {
			accelerometerManager.startListening(this);
		}*/
	}

	private void stopSensing() {
		Log.d(TAG, "Stop sensing");

		if (controller != null) {
			Log.d(TAG, "Stop sensing!!");
			controller.stop();
		}

//		Log.d(TAG, "Shutting down");
//		scheduleTaskExecutor.shutdown();

		/*while (controller != null && controller.getState() != SessionController.ControllerState.STOPPED) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				Log.e(TAG, "Runnable sleep failed", e);
			}
		}*/

//		Utilities.setSensing(false);

		/*Utilities.resetValues();
		// Start sensing battery level
		if (batteryThread != null) {
			batteryThread.done();
		}
		WAKE_LOCK.release();
		WAKE_LOCK = null;

		// Stop sensing activity counts
		if (accelerometerManager.isListening()) {
			accelerometerManager.stopListening();
		}*/
	}

	/*public static Context getContext() {
		return appContext;
	}*/

	/**
	 * Show a notification while this service is running.
	 */
	/*private void showNotification() {
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

		notification.setLatestEventInfo(this, getText(R.string.service_title), getText(R.string.service_text) + " / " +
				count + " acs / " + Utilities.getBatteryLevel() + "%", contentIntent);

		startForeground(1234, notification);
	}*/

	private Thread.UncaughtExceptionHandler onRuntimeError = new Thread.UncaughtExceptionHandler() {
		private long actionId;

		public void uncaughtException(Thread thread, Throwable ex) {
			/*Log.e(ex.getClass().getName(), "UncaughtExceptionHandler " + ex.toString(), ex);

			Utilities.setSensing(false);

			stopSensing();

			Log.d(TAG, "EMERGENCY SHUTDOWN!");
			unregisterReceiver(sensingActionReceiver);
			scheduleTaskExecutor.shutdown();
			handler.removeCallbacksAndMessages(null);
			unregisterReceiver(tickReceiver);
			dbAdapter.close();
			controller.setState(SessionController.ControllerState.INITIATED);
			Utilities.setReady(true);

			// Start service for it to run the recording session
			Intent sensingIntent = new Intent(SensingService.this, SensingService.class);
			// Point out the action
			sensingIntent.setAction(SensingService.SENSING_START_ACTION);
//			WakefulIntentService.sendWakefulWork(SensingService.this, sensingIntent);

//			SensingService.this.onDestroy();


//			Log.d("SensIt2", );

			Intent serviceIntent = new Intent(SensingService.this, SensingService.class);
			serviceIntent.putExtra(TAG, (long) 20000);
			PendingIntent pendingIntent = PendingIntent.getService(SensingService.this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			long trigger = System.currentTimeMillis() + 20000;
//			alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent);

			AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, sensingIntent);
			mgr.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent);

			System.exit(0);*/
		}
	};

	private void startBackgroundThreads() {
		scheduleTaskExecutor = Executors.newScheduledThreadPool(10);

		// Schedule a runnable task every minute
//		scheduleTaskExecutor.scheduleAtFixedRate(new DataStoreThread(), 10, 10, TimeUnit.SECONDS);
		scheduleTaskExecutor.scheduleAtFixedRate(new DataSyncThread(), 5, 5, TimeUnit.MINUTES);
//		scheduleTaskExecutor.scheduleAtFixedRate(new SleepAnalysisThread(), 1, 1, TimeUnit.MINUTES);
	}

	private class DataStoreThread extends Thread {
		private Date date;
		private long timestamp;
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public DataStoreThread(long timestamp, Date date) {
			this.date = date;
			this.timestamp = timestamp;
		}

		@Override
		public void run() {
			Log.d(TAG, "Store data");
			// Insert activity counts into the SensIt DB
			dbAdapter.open();
			int counts = ActivityUtil.getCounts();
			Log.d(TAG, "Stored " + counts + " counts.");
//			refreshChart(timestamp, counts);
			dbAdapter.insertCounts(Utilities.getMacAddress(SensingService.this), counts, dateFormat.format(date), 0);
//			dbAdapter.close();
		}
	}

	private void refreshChart(long timestamp, int counts) {
		Bundle bundle = new Bundle();
		bundle.putInt("counts", counts);
		bundle.putLong("timestamp", timestamp);
		Message msg = MainActivity.handlerUI.obtainMessage(Utilities.UPDATE_ACCELEROMETER);
		msg.setData(bundle);
		MainActivity.handlerUI.sendMessage(msg);
	}

	private class DataSyncThread implements Runnable {
		public void run() {
			Log.d(TAG, "Sync data");
			// Upload new data to server using the iCAT REST API
			// Make the necessary changes to the local db to indicate which data has been synced
		}
	}
	private class SleepAnalysisThread implements Runnable {
		public void run() {
			Log.d(TAG, "Compute sleep data");
			//
		}
	}

	BroadcastReceiver sensingActionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case SensingService.SENSING_START_ACTION_COMPLETE:
					Log.d(TAG, "Action SENSING_START_ACTION_COMPLETE received");
					Log.d(TAG, "Sensing started. Start background threads");
					startBackgroundThreads();
					Utilities.setReady(true);
					break;
				case SensingService.SENSING_STOP_ACTION_COMPLETE:
					Log.d(TAG, "Action SENSING_STOP_ACTION_COMPLETE received");
//					Utilities.setReady(true);
					break;
				default:
					Log.e(TAG, "Unknown action received [sensingActionReceiver]");
			}
		}
	};

	private Handler handler = new Handler();

	private Runnable TestRunnable = new Runnable() {
		//TODO re-adjust milliseconds
		@Override
		public void run() {
			try {
				long time = System.currentTimeMillis();
				Date date = new Date(time);

				Calendar calendar = Calendar.getInstance();
				long time2 = calendar.getTimeInMillis();
				Date date2 = calendar.getTime();

				long time3 = SystemClock.elapsedRealtime();
				Date date3 = new Date(time3);

				Log.d(TAG, "Execute at: [1]" + time + " - " + date + " [2]" + time2 + " - " + date2 + " [3]" + time3 + " - " + date3);

			} catch (Exception e) {
				Log.e(TAG, "Error at Runnable. " + e);
			} finally {
				handler.postDelayed(this, 60000);
			}
		}
	};

	//Create a broadcast receiver to handle change in time
	BroadcastReceiver tickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive (Context context, Intent intent){
			Calendar calendar = Calendar.getInstance();
			long timestamp = calendar.getTimeInMillis();
			Date date = calendar.getTime();
			switch (intent.getAction()) {
				case Intent.ACTION_TIME_TICK:
					Log.d(TAG, "Action ACTION_TIME_TICK Received at: " + timestamp + ", " + date);
					(new DataStoreThread(timestamp, date)).start();
					break;
				case Intent.ACTION_TIME_CHANGED:
					Log.d(TAG, "Action ACTION_TIME_CHANGED Received at: " + timestamp + ", " + date);
					break;
				case Intent.ACTION_TIMEZONE_CHANGED:
					Log.d(TAG, "Action ACTION_TIMEZONE_CHANGED Received at: " + timestamp + ", " + date);
					break;
			}
		}
	};
}
