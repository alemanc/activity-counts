package edu.cicese.sensit;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.cicese.sensit.ui.SurveyNotification;

import java.util.Calendar;

/**
 * This alarm should be scheduled when we want to remove the Survey notification.
 * Note that this will not cancel any broadcast.
 *
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:01 PM
 */
public class OnClearNotificationsAlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "SensIt.OnClearNotificationsAlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String date = ActivityChart.DATE_FORMAT_LONG.format(Calendar.getInstance().getTime());
		Log.d(TAG, "Received at OnClearNotificationsAlarmReceiver at " + date);

		// cancel Survey notification
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(SurveyNotification.NOTIFICATION_ID);
	}
}