package edu.cicese.sensit.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import edu.cicese.sensit.SurveyActivity;
import edu.cicese.sensit.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 12:50 PM
 */
public class SurveyNotification {
	private static final String TAG = "SensIt.SurveyNotification";

	private Context context;
	private NotificationManager manager;
	public final static int NOTIFICATION_ID = 1525;

	public SurveyNotification(Context context) {
		this.context = context;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/*private String createNotificationContent() {
		String text = context.getString(R.string.survey_notification_text);
		return "";
	}

	private Notification createNotification() {
		CharSequence tickerText = context.getText(R.string.survey_notification_ticker_text);
		return new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
	}

	private PendingIntent createNotificationIntent() {
		Intent intent = new Intent(context, SurveyActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		return pendingIntent;
	}*/

	public void updateNotification() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(notificationUpdater);
	}

	private Runnable notificationUpdater = new Runnable() {
		public void run() {
			Log.d(TAG, "Creating notification");
			// instantiate a Builder object.
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			// creates an Intent for the Activity
			Intent notifyIntent = new Intent(context, SurveyActivity.class);
			// sets the Activity to start in a new, empty task
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK/* | Intent.FLAG_ACTIVITY_CLEAR_TASK*/);
			// creates the PendingIntent
			PendingIntent pendingIntent = PendingIntent.getActivity(
							context,
							0,
							notifyIntent,
							PendingIntent.FLAG_UPDATE_CURRENT
					);
			// puts the PendingIntent into the notification builder
			builder.setContentIntent(pendingIntent);
			builder.setTicker(context.getText(R.string.survey_notification_ticker_text));
			builder.setContentTitle(context.getText(R.string.survey_notification_title));
			builder.setContentText(context.getText(R.string.survey_notification_text));
			builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon));
			builder.setSmallIcon(R.drawable.icon_survey);
			builder.setDefaults(Notification.DEFAULT_VIBRATE);

					Notification notification = builder.build();
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// Builds an anonymous Notification object from the builder, and
			// passes it to the NotificationManager
			manager.notify(NOTIFICATION_ID, notification);
		}
	};
}