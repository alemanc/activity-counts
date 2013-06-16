package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.cicese.sensit.ui.SurveyNotification;

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

		Log.d(TAG, "Needed: " + Utilities.surveyNeeded(context));
		//TODO Add 'if' statement

		if (surveyNotification == null) {
			surveyNotification = new SurveyNotification(context);
		}
		surveyNotification.updateNotification();
	}
}