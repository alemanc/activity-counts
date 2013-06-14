package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import edu.cicese.sensit.ui.SurveyNotification;
import edu.cicese.sensit.util.SensitActions;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:01 PM
 */
public class OnSensingAlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "SensIt.OnSensingAlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received at OnSensingAlarmReceiver");

		if (!Utilities.isSensing()) {
			if (!Utilities.isManuallyStopped()) {
				Intent sensingIntent = new Intent(context, SensingService.class);
				sensingIntent.setAction(SensitActions.SENSING_START_ACTION);
				WakefulIntentService.sendWakefulWork(context, sensingIntent);
			}
			else {
				Log.d(TAG, "Can't start SensingService: User stopped it");
			}
		}
		else {
			Log.d(TAG, "No need to start SensingService: Already sensing");
		}
	}
}