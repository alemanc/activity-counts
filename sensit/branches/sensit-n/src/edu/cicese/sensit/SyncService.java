package edu.cicese.sensit;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 15/06/13
 * Time: 05:21 PM
 */
public class SyncService extends IntentService{
	private static final String TAG = "SensIt.SyncService";

	public SyncService() {
		super("SyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Action received, syncing");
		new Thread(new DataUploadThread(this)).start();
	}
}
