package edu.cicese.sensit.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import edu.cicese.sensit.MainActivity;
import edu.cicese.sensit.R;

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
		activeSensors = new ArrayList<String>();
	}

	private String createNotificationContent() {
		StringBuilder sb = new StringBuilder(context.getString(R.string.service_title) + " ");
		String div = ", ", end = ".";
		for (String sensor : activeSensors) {
			sb.append(sensor.toLowerCase() + div);
		}
		sb.replace(sb.length() - div.length(), sb.length(), end);
		return sb.toString();
	}

	private Notification createNotification() {
		CharSequence tickerText = context.getText(R.string.service_ticker_text);

		return new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
	}

	private PendingIntent createNotificationIntent() {
		Intent intent = new Intent(context, MainActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

	public void updateNotificationWith(String sensor) {
		add(sensor);
		updateNotification();
	}

	public void updateNotificationWithout(String sensor) {
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
				String contentTitle = context.getString(R.string.service_title);
				// Text to display in the extended status window
				String contentText = createNotificationContent();

				// Intent to launch the activity when the extended text is clicked
				PendingIntent pendingIntent = createNotificationIntent();

				Notification notification = createNotification();
				notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);

				notification.flags |= Notification.FLAG_NO_CLEAR;

				manager.notify(NOTIFICATION_ID, notification);
			}
		}
	};
}