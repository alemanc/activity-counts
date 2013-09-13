package edu.cicese.sensit.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import edu.cicese.sensit.OnClearNotificationsAlarmReceiver;
import edu.cicese.sensit.OnSensingAlarmReceiver;
import edu.cicese.sensit.OnSurveyAlarmReceiver;

import java.util.Calendar;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 28/06/13
 * Time: 01:10 PM
 */
public class AlarmUtil {
	private static final String TAG = "SensIt.AlarmUtil";

	private static final int RESTART_PERIOD = 300000; // 5 minutes
	private static final int WAIT_PERIOD = 120000; // 2 minutes

	private static final int SHOW_SURVEY_1_AT_HOUR = 11;
	private static final int SHOW_SURVEY_1_AT_MINUTE = 55;
	private static final int SHOW_SURVEY_2_AT_HOUR = 20;
	private static final int SHOW_SURVEY_3_AT_HOUR = 21;
	private static final int SHOW_SURVEY_4_AT_HOUR = 22;
	private static final int CLEAR_NOTIFICATIONS_AT_HOUR = 3;
	private static final int CLOSE_SURVEY_AT_HOUR = 5;
	public static final int BATTERY_CHECK_ENABLED_AT_HOUR = 12;
	public static final int BATTERY_CHECK_DISABLED_AT_HOUR = 21;

	public static void setAlarms(Context context) {
		Log.d(TAG, "Setting Alarms");

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// schedule alarms to start the Sensing service if not running
		Intent alarmSensingIntent = new Intent(context, OnSensingAlarmReceiver.class);
		PendingIntent piSensing = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_RESTART, alarmSensingIntent, 0);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + WAIT_PERIOD, RESTART_PERIOD, piSensing);

		// schedule alarms to show Survey notification at 11:55, 20:00, 21:00, and 22:00
		Intent alarmSurveyIntent = new Intent(context, OnSurveyAlarmReceiver.class);
		PendingIntent piSurvey1 = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_SURVEY_1, alarmSurveyIntent, 0);
		PendingIntent piSurvey2 = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_SURVEY_2, alarmSurveyIntent, 0);
		PendingIntent piSurvey3 = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_SURVEY_3, alarmSurveyIntent, 0);
		PendingIntent piSurvey4 = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_SURVEY_4, alarmSurveyIntent, 0);

		setAlarm(alarmManager, getCalendar(SHOW_SURVEY_1_AT_HOUR, SHOW_SURVEY_1_AT_MINUTE, true), piSurvey1);
//		setAlarm(alarmManager, getCalendar(16, 32, true), piSurvey1);
		setAlarm(alarmManager, getCalendar(SHOW_SURVEY_2_AT_HOUR, 0, true), piSurvey2);
		setAlarm(alarmManager, getCalendar(SHOW_SURVEY_3_AT_HOUR, 0, true), piSurvey3);
		setAlarm(alarmManager, getCalendar(SHOW_SURVEY_4_AT_HOUR, 0, true), piSurvey4);

		// schedule alarms to clear Survey notifications at 3 AM
		Intent alarmClearIntent = new Intent(context, OnClearNotificationsAlarmReceiver.class);
		PendingIntent piClear = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_CLEAR_SURVEY, alarmClearIntent, 0);
		setAlarm(alarmManager, getCalendar(CLEAR_NOTIFICATIONS_AT_HOUR, 0, true), piClear);
//		setAlarm(alarmManager, getCalendar(16, 33, true), piClear);

		// schedule alarms to close a Survey, if open at 5 AM
		Intent alarmCloseSurveyIntent = new Intent(SensitActions.ACTION_CLOSE_SURVEY);
		PendingIntent piCloseSurvey = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_CLOSE_SURVEY, alarmCloseSurveyIntent, 0);
		setAlarm(alarmManager, getCalendar(CLOSE_SURVEY_AT_HOUR, 0, false), piCloseSurvey);

		// schedule alarm to enable/disable battery check
//		Intent alarmDisableBatteryCheckIntent = new Intent(SensitActions.ACTION_DISABLE_BATTERY_CHECK);
//		PendingIntent piDisableBatteryCheck = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_DISABLE_BATTERY_CHECK, alarmDisableBatteryCheckIntent, 0);
//		setAlarm(alarmManager, getCalendar(BATTERY_CHECK_DISABLED_AT_HOUR, 0, false), piDisableBatteryCheck);

		// 9:00 AM
//		Intent alarmEnableBatteryCheckIntent = new Intent(SensitActions.ACTION_ENABLE_BATTERY_CHECK);
//		PendingIntent piEnableBatteryCheck = PendingIntent.getBroadcast(context, SensitActions.REQUEST_CODE_ENABLE_BATTERY_CHECK, alarmEnableBatteryCheckIntent, 0);
//		setAlarm(alarmManager, getCalendar(BATTERY_CHECK_ENABLED_AT_HOUR, 40, false), piEnableBatteryCheck);
	}

	private static void setAlarm(AlarmManager alarmManager, Calendar calendar, PendingIntent pendingIntent) {
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
	}

	private static Calendar getCalendar(int hourOfDay, int minute, boolean allowPastAlarms) {
		// offset the current time by +5 minutes, just in case
		Calendar calNow = Calendar.getInstance();
//		now.add(Calendar.MINUTE, 5);

		Calendar calAlarm = (Calendar) calNow.clone();
		calAlarm.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calAlarm.set(Calendar.MINUTE, minute);
		calAlarm.set(Calendar.SECOND, 0);

		if (!allowPastAlarms && (calNow.compareTo(calAlarm) >= 0)) {
			calAlarm.add(Calendar.DATE, 1);
		}

		Log.d(TAG, "Setting to " + TimeUtil.getTime(calAlarm.getTimeInMillis()));

		return calAlarm;
	}
}
