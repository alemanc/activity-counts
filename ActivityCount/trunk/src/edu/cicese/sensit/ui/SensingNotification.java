package edu.cicese.sensit.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import edu.cicese.sensit.R;
import edu.cicese.sensit.SensitActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 12:50 PM
 */
public class SensingNotification {
	private Context context;
	private NotificationManager manager;
	private List<String> activeSensors;
	private final static int NOTIFICATION_ID = 1524;

	public SensingNotification(Context context) {
		this.context = context;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		activeSensors = new ArrayList<>();
	}

	//TODO FIX CONCURRENT MODIFICATION ERROR
	private String createNotificationContent() {
		StringBuilder sb = new StringBuilder(context.getString(R.string.sensing_notification_text) + " [");
		String div = ", ", end = "]";
		for (String sensor : activeSensors) {
			sb.append(sensor + div);
		}
		sb.replace(sb.length() - div.length(), sb.length(), end);
		return sb.toString();
	}

	private Notification createNotification() {
		CharSequence tickerText = context.getText(R.string.sensing_notification_ticker_text);

		return new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
	}

	private PendingIntent createNotificationIntent() {
		Intent intent = new Intent(context, SensitActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		return pendingIntent;
	}

	public void add(String sensor) {
		if (!activeSensors.contains(sensor)) {
			activeSensors.add(sensor);
		}
	}

	public void remove(String sensor) {
		activeSensors.remove(sensor);
	}

	public synchronized void updateNotificationWith(String sensor) {
		add(sensor);
		updateNotification();
	}

	public synchronized void updateNotificationWithout(String sensor) {
		remove(sensor);
		updateNotification();
	}

	public void updateNotification() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(notificationUpdater);
	}

	private Runnable notificationUpdater = new Runnable() {
		public void run() {
			if (activeSensors.isEmpty()) {
				manager.cancel(NOTIFICATION_ID);
			} else {

				// Title for the expanded status
				String contentTitle = context.getString(R.string.sensing_notification_title);
				// Text to display in the extended status window
				String contentText = createNotificationContent();

				// instantiate a Builder object.
				/*NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
				// creates an Intent for the Activity
				Intent notifyIntent = new Intent(context, SensitActivity.class);
				notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				// creates the PendingIntent
				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);*/


				// Intent to launch the activity when the extended text is clicked
				Intent intent = new Intent(context, SensitActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

				// puts the PendingIntent into the notification builder
				/*builder.setContentIntent(pendingIntent);
				builder.setTicker(context.getText(R.string.sensing_notification_ticker_text));
				builder.setContentTitle(context.getText(R.string.sensing_notification_title));
				builder.setContentText(createNotificationContent());
				builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon));


				Notification notification = builder.build();
				notification.flags |= Notification.FLAG_NO_CLEAR;*/
				// Builds an anonymous Notification object from the builder, and
				// passes it to the NotificationManager

				Notification notification = createNotification();
				notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);
				notification.flags |= Notification.FLAG_NO_CLEAR;

				manager.notify(NOTIFICATION_ID, notification);
			}
		}
	};
}