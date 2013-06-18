package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import edu.cicese.sensit.db.DBAdapter;
import edu.cicese.sensit.ui.SurveyNotification;
import edu.cicese.sensit.util.ActivityUtil;
import edu.cicese.sensit.util.SensitActions;

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

	public final static String ACTION_ID_FIELD_NAME = "action_id";

	private ScheduledExecutorService scheduleTaskExecutor;
	private DBAdapter dbAdapter;

	protected static SurveyNotification surveyNotification;


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

//		Log.d(TAG, "Needed: " + Utilities.surveyNeeded(this));
//		if (surveyNotification == null) {
//			surveyNotification = new SurveyNotification(this);
//		}
//		surveyNotification.updateNotification();

		dbAdapter = new DBAdapter(this);

		// Register the broadcast receiver to receive TIME_TICK
		IntentFilter intentTimeFilter = new IntentFilter();
		intentTimeFilter.addAction(Intent.ACTION_TIME_TICK);
		intentTimeFilter.addAction(Intent.ACTION_TIME_CHANGED);
		intentTimeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(tickReceiver, intentTimeFilter);

		// Register receiver to wait until is stopped
//		IntentFilter intentFilter = new IntentFilter(SensingService.ACTION_SENSING_STOP);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SensitActions.ACTION_SENSING_START_COMPLETE);
		intentFilter.addAction(SensitActions.ACTION_SENSING_STOP_COMPLETE);
		registerReceiver(sensingActionReceiver, intentFilter);

		IntentFilter intentDBFilter = new IntentFilter();
		intentDBFilter.addAction(SensitActions.ACTION_DATA_SYNCED);
		registerReceiver(dataSyncReceiver, intentDBFilter);

		Thread.setDefaultUncaughtExceptionHandler(onRuntimeError);
		Log.d(TAG, "SensingService created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Cancel the persistent notification.
//		notificationManager.cancel(NOTIFICATION);

		Log.d(TAG, "SensingService stopped");
		Log.d(TAG, "Unregister broadcast receivers");
		unregisterReceiver(sensingActionReceiver);
		unregisterReceiver(tickReceiver);
		unregisterReceiver(dataSyncReceiver);
		Log.d(TAG, "Shutdown executor");
		if (scheduleTaskExecutor != null) {
			scheduleTaskExecutor.shutdown();
		}

		handler.removeCallbacksAndMessages(null);

		dbAdapter.close();

		Utilities.setReady(true);
	}

	/**
	 * This method is invoked on the worker thread with a request to process.
	 */
	protected void doWakefulWork(Intent intent) {
		switch (intent.getAction()) {
			case SensitActions.ACTION_SENSING_START:
				// Check if the user manually stopped the service
				if (!Utilities.isManuallyStopped()) {
					Log.i(TAG, "START!");
					startSensing();
				}
				else {
					Log.e(TAG, "Manually stopped! Couldn't restart");
				}
				break;
			case SensitActions.ACTION_SENSING_STOP:
				Log.e(TAG, "STOP!");
				stopSensing();
				break;
			default:
				Log.e(TAG, "Unknown action received: " + intent.getAction());
		}
	}

	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startSensing();

		// We want this service to continue running until it is explicitly stopped, so return sticky.
		return START_STICKY;
	}*/


	private SessionController controller;

	private void startSensing() {
		Log.d(TAG, "Start sensing");

		Utilities.setCharging(isPowerConnected());
		Utilities.setEpochCharging();
		Utilities.setSensing(true);

		controller = new SessionController(this);
		controller.start();
	}

	private void stopSensing() {
		Log.d(TAG, "Stop sensing");

		if (controller != null) {
			Log.d(TAG, "Stop sensing!!");
			controller.stop();
		}
	}

	private Thread.UncaughtExceptionHandler onRuntimeError = new Thread.UncaughtExceptionHandler() {
		public void uncaughtException(Thread thread, Throwable ex) {
			Log.e(ex.getClass().getName(), "UncaughtExceptionHandler " + ex.toString(), ex);

			Log.d(TAG, "EMERGENCY SHUTDOWN!");

			Utilities.setSensing(false);
			stopSensing();
			unregisterReceiver(sensingActionReceiver);
			unregisterReceiver(tickReceiver);
			unregisterReceiver(dataSyncReceiver);
			scheduleTaskExecutor.shutdown();
			handler.removeCallbacksAndMessages(null);
			dbAdapter.close();
			controller.setState(SessionController.ControllerState.INITIATED);
			Utilities.setReady(true);

//			SensingService.this.onDestroy();

			// Start service for it to run the recording session
			Intent sensingIntent = new Intent(SensingService.this, SensingService.class);
			// Point out the action
			sensingIntent.setAction(SensitActions.ACTION_SENSING_START);
			WakefulIntentService.sendWakefulWork(SensingService.this, sensingIntent);

			System.exit(0);

//			SensingService.this.onDestroy();

			/*Intent serviceIntent = new Intent(SensingService.this, SensingService.class);
			serviceIntent.putExtra(TAG, (long) 20000);
			PendingIntent pendingIntent = PendingIntent.getService(SensingService.this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			long trigger = System.currentTimeMillis() + 20000;
			AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent);

			System.exit(0);*/
		}
	};

	private void startBackgroundThreads() {
		scheduleTaskExecutor = Executors.newScheduledThreadPool(10);

		// Schedule a runnable task every minute
//		scheduleTaskExecutor.scheduleAtFixedRate(dataSyncThread, 0, 30, TimeUnit.MINUTES);
		scheduleTaskExecutor.scheduleAtFixedRate(new DataUploadThread(this), 30, 30, TimeUnit.MINUTES);
	}

	private class DataStoreThread extends Thread {
		private Date date;
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public DataStoreThread(Date date) {
			this.date = date;
		}

		@Override
		public void run() {
			Log.d(TAG, "Store data");
			dbAdapter.open();
			int counts = ActivityUtil.getCounts();
			Log.d(TAG, "Stored " + counts + " counts.");
			boolean epochCharging = Utilities.isEpochCharging();
			Utilities.resetEpochCharging();
			long inserted = dbAdapter.insertCounts(counts, dateFormat.format(date), epochCharging, 0);
			Log.d(TAG, "Inserted at row ID " + inserted);
			dbAdapter.close();

			Intent broadcastIntent = new Intent(SensitActions.ACTION_REFRESH_CHART);
			sendBroadcast(broadcastIntent);
		}
	}

	/*private Runnable dataSyncThread = new Runnable() {
		public void run() {
			Log.d(TAG, "Sync data");
			// Upload new data to server using the iCAT REST API
			// Make the necessary changes to the local db to indicate which data has been synced

			// Check connection preferences


			// Check if WiFi connection available


			dbAdapter.open();

			Log.d(TAG, "Query data");
			Cursor cursor = dbAdapter.queryCounts();

			List<ActivityCount> counts = new ArrayList<>();

			// at least one entry
			if (cursor.moveToFirst()) {
				do {
					counts.add(new ActivityCount(cursor.getString(3), cursor.getInt(1), cursor.getInt(4)));
				} while (cursor.moveToNext());
			}

			if (!counts.isEmpty()) {
				Log.d(TAG, "Sending from " + counts.get(0).getDate() + " to " + counts.get(counts.size() - 1).getDate());
				IcatUtil.postActivityCounts(SensingService.this, counts);
			}
			else {
				Log.d(TAG, "Nothing to sync.");
			}

			if (!cursor.isClosed()) {
				cursor.close();
			}

			dbAdapter.close();
		}
	};*/

	BroadcastReceiver sensingActionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case SensitActions.ACTION_SENSING_START_COMPLETE:
					Log.d(TAG, "Action ACTION_SENSING_START_COMPLETE received");
					Log.d(TAG, "Sensing started. Start background threads");
					startBackgroundThreads();
					Utilities.setReady(true);
					break;
				case SensitActions.ACTION_SENSING_STOP_COMPLETE:
					Log.d(TAG, "Action ACTION_SENSING_STOP_COMPLETE received");
//					Utilities.setReady(true);
					break;
				default:
					Log.e(TAG, "Unknown action received [sensingActionReceiver]");
			}
		}
	};

	private Handler handler = new Handler();

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
					(new DataStoreThread(date)).start();
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

	public BroadcastReceiver dataSyncReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case SensitActions.ACTION_DATA_SYNCED:
					Log.d(TAG, "Action ACTION_DATA_SYNCED received");

					int type = intent.getIntExtra(SensitActions.EXTRA_SYNCED_TYPE, -1);
					String dateStart = intent.getStringExtra(SensitActions.EXTRA_DATE_START);
					String dateEnd = intent.getStringExtra(SensitActions.EXTRA_DATE_END);

					Log.d(TAG, "Update");
					new Thread(new DataSyncedThread(SensingService.this, type, dateStart, dateEnd)).start();

					Log.d(TAG, "Resetting intent");
					abortBroadcast();
					break;
			}
		}
	};

	public boolean isPowerConnected() {
		Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		Log.d(TAG, "Initial charging: " + (batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0));
		return batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
	}
}
