package edu.cicese.sensit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:05 PM
 */
public class OnBootReceiver extends BroadcastReceiver {
	private static final int PERIOD = 300000; // 5 minutes
	private static final String TAG = "SensIt.OnBootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		//double check for only boot complete event
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			//here we start the service
//			Intent serviceIntent = new Intent(context, AndroidStartServiceOnBoot.class);
//			context.startService(serviceIntent);

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent alarmIntent = new Intent(context, OnAlarmReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
//		    mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, pi);
			Log.d(TAG, "Received at OnBootReceiver");
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, PERIOD, pi);
		}
	}
}
