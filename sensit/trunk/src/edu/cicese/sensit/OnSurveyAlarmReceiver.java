package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.cicese.sensit.ui.SurveyNotification;
import edu.cicese.sensit.util.Utilities;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:01 PM
 */
public class OnSurveyAlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "SensIt.OnSurveyAlarmReceiver";

	protected static SurveyNotification surveyNotification;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received at OnSurveyAlarmReceiver");

		if (Utilities.surveyNeeded(context)) {
			Log.d(TAG, "Survey notification needed");

			if (surveyNotification == null) {
				surveyNotification = new SurveyNotification(context);
			}
			surveyNotification.updateNotification();
		}
		else {
			Log.d(TAG, "Survey notification not needed");
		}
	}
}