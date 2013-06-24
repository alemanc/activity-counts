package edu.cicese.sensit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import edu.cicese.sensit.util.SensitActions;
import edu.cicese.sensit.util.Utilities;

import java.util.Calendar;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:05 PM
 */
public class OnBootReceiver extends BroadcastReceiver {
	private static final int RESTART_PERIOD = 300000; // 5 minutes
	private static final int WAIT_PERIOD = 180000; // 2 minutes
	private static final String TAG = "SensIt.OnBootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// double check for only boot complete event
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(TAG, "Action ACTION_BOOT_COMPLETED received");

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			// schedule alarms to start the Sensing service if not running
			Intent alarmSensingIntent = new Intent(context, OnSensingAlarmReceiver.class);
			PendingIntent piSensing = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_RESTART, alarmSensingIntent, 0);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + WAIT_PERIOD, RESTART_PERIOD, piSensing);

			// schedule alarms to show Survey notification at 11:55, 20:00 and 21:00
			Calendar calendarShowNotification = Calendar.getInstance();
			calendarShowNotification.set(Calendar.HOUR_OF_DAY, 11);
			calendarShowNotification.set(Calendar.MINUTE, 55);
			calendarShowNotification.set(Calendar.SECOND, 0);

			Intent alarmSurveyIntent = new Intent(context, OnSurveyAlarmReceiver.class);
			PendingIntent piSurvey1 = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_SURVEY_1, alarmSurveyIntent, 0);
			PendingIntent piSurvey2 = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_SURVEY_2, alarmSurveyIntent, 0);
			PendingIntent piSurvey3 = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_SURVEY_3, alarmSurveyIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarShowNotification.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piSurvey1);
			calendarShowNotification.set(Calendar.HOUR_OF_DAY, 20);
			calendarShowNotification.set(Calendar.MINUTE, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarShowNotification.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piSurvey2);
			calendarShowNotification.set(Calendar.HOUR_OF_DAY, 21);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarShowNotification.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piSurvey3);

//			PendingIntent piSurvey4 = PendingIntent.getBroadcast(context, 121, alarmSurveyIntent, 0);
//			PendingIntent piSurvey5 = PendingIntent.getBroadcast(context, 122, alarmSurveyIntent, 0);
//			PendingIntent piSurvey6 = PendingIntent.getBroadcast(context, 123, alarmSurveyIntent, 0);
//			calendarShowNotification.set(Calendar.HOUR_OF_DAY, 9);
//			calendarShowNotification.set(Calendar.MINUTE, 57);
//			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarShowNotification.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piSurvey4);
//			calendarShowNotification.set(Calendar.MINUTE, 58);
//			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarShowNotification.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piSurvey5);
//			calendarShowNotification.set(Calendar.MINUTE, 59);
//			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarShowNotification.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piSurvey6);

			// schedule alarms to clear Survey notifications at midnight
			Calendar calendarClearNotifications = Calendar.getInstance();
			// 12:00 AM
			calendarClearNotifications.set(Calendar.HOUR_OF_DAY, 0);
			calendarClearNotifications.set(Calendar.MINUTE, 0);
			calendarClearNotifications.set(Calendar.SECOND, 0);

			Intent alarmClearIntent = new Intent(context, OnClearNotificationsAlarmReceiver.class);
			PendingIntent piClear = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_CLEAR_SURVEY, alarmClearIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarClearNotifications.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piClear);

			// schedule alarms to close a Survey, if open at 5 AM
			Calendar calendarCloseSurvey = Calendar.getInstance();
			// 5:00 AM
			calendarCloseSurvey.set(Calendar.HOUR_OF_DAY, 5);
			calendarCloseSurvey.set(Calendar.MINUTE, 0);
			calendarCloseSurvey.set(Calendar.SECOND, 0);

			Intent alarmCloseSurveyIntent = new Intent(SensitActions.ACTION_CLOSE_SURVEY);
			PendingIntent piCloseSurvey = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_CLOSE_SURVEY, alarmCloseSurveyIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarCloseSurvey.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piCloseSurvey);

			// schedule alarm to enable/disable battery check
			Calendar calendarEnableBatteryCheck = Calendar.getInstance();
			// 9:00 PM
			calendarEnableBatteryCheck.set(Calendar.HOUR_OF_DAY, Utilities.BATTERY_CHECK_DISABLED_AT);
			calendarEnableBatteryCheck.set(Calendar.MINUTE, 0);
			calendarEnableBatteryCheck.set(Calendar.SECOND, 0);

			Intent alarmDisableBatteryCheckIntent = new Intent(SensitActions.ACTION_DISABLE_BATTERY_CHECK);
			PendingIntent piDisableBatteryCheck = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_DISABLE_BATTERY_CHECK, alarmDisableBatteryCheckIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarEnableBatteryCheck.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piDisableBatteryCheck);

			// 9:00 AM
			calendarEnableBatteryCheck.set(Calendar.HOUR_OF_DAY, Utilities.BATTERY_CHECK_ENABLED_AT);

			Intent alarmEnableBatteryCheckIntent = new Intent(SensitActions.ACTION_ENABLE_BATTERY_CHECK);
			PendingIntent piEnableBatteryCheck = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_ENABLE_BATTERY_CHECK, alarmEnableBatteryCheckIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarEnableBatteryCheck.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piEnableBatteryCheck);
		}
	}
}
