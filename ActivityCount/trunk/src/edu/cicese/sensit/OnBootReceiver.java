package edu.cicese.sensit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:05 PM
 */
public class OnBootReceiver extends BroadcastReceiver {
	private static final int RESTART_PERIOD = 300000; // 5 minutes
	private static final String TAG = "SensIt.OnBootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// double check for only boot complete event
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(TAG, "Action ACTION_BOOT_COMPLETED received");

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			// schedule alarms for SensingService
			Intent alarmSensingIntent = new Intent(context, OnSensingAlarmReceiver.class);
			PendingIntent piSensing = PendingIntent.getBroadcast(context, 0, alarmSensingIntent, 0);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, RESTART_PERIOD, piSensing);

			// schedule alarms for SurveyNotification
			Calendar calendar = Calendar.getInstance();
			// 11:55 AM
			calendar.set(Calendar.HOUR_OF_DAY, 22);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);

			Intent alarmSurveyIntent = new Intent(context, OnSurveyAlarmReceiver.class);
			PendingIntent piSurvey = PendingIntent.getBroadcast(context, 0, alarmSurveyIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piSurvey);
		}
	}
}
