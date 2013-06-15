package edu.cicese.sensit;

import android.content.Context;
import android.util.Log;
import edu.cicese.sensit.db.DBAdapter;

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
	private int type;

	public DataSyncedThread(Context context, int type, String dateStart, String dateEnd) {
		this.type = type;
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		dbAdapter = new DBAdapter(context);
	}

	@Override
	public void run() {
		Log.d(TAG, "Updating [" + type + "] from " + dateStart + " to " + dateEnd);

		int rowsUpdated = 0;

		dbAdapter.open();
		switch (type) {
			case Utilities.TYPE_COUNT:
				rowsUpdated = dbAdapter.updateCounts(dateStart, dateEnd);
				break;
			case Utilities.TYPE_SURVEY:
				rowsUpdated = dbAdapter.updateSurveys(dateStart, dateEnd);
				break;
			default:
				Log.e(TAG, "Unknown type: " + type);
		}
		dbAdapter.close();

		Log.d(TAG, rowsUpdated + " updated");
	}
}

