package edu.cicese.sensit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 06:01 PM
 */
public class OnAlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "SensIt.OnAlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received at OnAlarmReceiver");
		Intent sensingIntent = new Intent(context, SensingService.class);
		sensingIntent.setAction(SensingService.SENSING_START_ACTION);
		WakefulIntentService.sendWakefulWork(context, sensingIntent);
	}
}