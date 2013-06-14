package edu.cicese.sensit;

import android.content.Context;
import android.util.Log;
import edu.cicese.sensit.database.DBAdapter;

/**
 * Makes the necessary changes to the local DB to indicate which data has been synced.
 *
 * Created by: Eduardo Quintana Contreras
 * Date: 10/06/13
 * Time: 07:56 PM
 */
public class DataSyncedThread implements Runnable {
	public static final String TAG = "SensIt.DataSyncedThread";
	private DBAdapter dbAdapter;
	private String dateStart, dateEnd;

	public DataSyncedThread(Context context, String dateStart, String dateEnd) {
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		dbAdapter = new DBAdapter(context);
	}

	@Override
	public void run() {
		Log.d(TAG, "Updating from " + dateStart + " to " + dateEnd);

		dbAdapter.open();
		int rowsUpdated = dbAdapter.updateCounts(dateStart, dateEnd);
		dbAdapter.close();

		Log.d(TAG, rowsUpdated + " updated");
	}
}

