package edu.cicese.sensit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import edu.cicese.sensit.util.ActivityUtil;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 22/05/13
 * Time: 05:43 PM
 */
public class DBAdapter {
	private static final String TAG = "SensIt.DBAdapter";

	private static final String DATABASE_NAME = "sensit.db";
	private static final int DATABASE_VERSION = 3;
	private static final String TABLE_ACTIVITY_COUNT = "activity_count";

	private static final String DATABASE_CREATE =
			"CREATE TABLE IF NOT EXISTS [activity_count] (\n" +
					"[activity_count_id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
					"[user_id] STRING NOT NULL,\n" +
					"[counts] INTEGER DEFAULT '-1' NOT NULL,\n" +
					"[calories] INTEGER DEFAULT '-1' NOT NULL,\n" +
					"[date] DATETIME  NOT NULL,\n" +
					"[synced] INTEGER DEFAULT '0' NOT NULL\n" +
					");\n" +
					"\n" +
					"CREATE UNIQUE INDEX [ux_activity_count_date] ON [activity_count](\n" +
					"[date] ASC\n" +
					");\n" +
					"\n" +
					"CREATE UNIQUE INDEX [pk_activity_count] ON [activity_count](\n" +
					"[activity_count_id] ASC\n" +
					");";
	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,
		                      int newVersion) {
			Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_COUNT);
			onCreate(db);
		}
	}

	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		DBHelper.close();
	}

	public final String COLUMN_ACTIVITY_COUNT_USER_ID = "user_id";
	public final String COLUMN_ACTIVITY_COUNT_COUNTS = "counts";
	public final String COLUMN_ACTIVITY_COUNT_CALORIES = "calories";
	public final String COLUMN_ACTIVITY_COUNT_DATE = "date";
	public final String COLUMN_ACTIVITY_COUNT_SYNCED = "synced";

	public long insertCounts(String userId, int counts, String date, int synced) {
		// date example: "2010-11-17 14:12:23"
		ContentValues values = new ContentValues();
		values.put(COLUMN_ACTIVITY_COUNT_USER_ID, userId);
		values.put(COLUMN_ACTIVITY_COUNT_COUNTS, counts);
		values.put(COLUMN_ACTIVITY_COUNT_CALORIES, ActivityUtil.getCalories(counts, ActivityUtil.bmi, ActivityUtil.EE_FREEDSON_VM3));
		values.put(COLUMN_ACTIVITY_COUNT_DATE, date);
		values.put(COLUMN_ACTIVITY_COUNT_SYNCED, synced);
		return db.insert(TABLE_ACTIVITY_COUNT, null, values);
	}

	//---deletes a particular title---
	/*public boolean deleteTitle(long rowId) {
		return db.delete(TABLE_ACTIVITY_COUNT, KEY_ROWID +
				"=" + rowId, null) > 0;
	}*/

	//---retrieves all the titles---
	/*public Cursor getAllTitles() {
		return db.query(TABLE_ACTIVITY_COUNT, new String[]{
				KEY_ROWID,
				KEY_ISBN,
				KEY_TITLE,
				KEY_PUBLISHER},
				null,
				null,
				null,
				null,
				null);
	}*/

	//---retrieves a particular title---
	/*public Cursor getTitle(long rowId) throws SQLException {
		Cursor mCursor =
				db.query(true, TABLE_ACTIVITY_COUNT, new String[]{
						KEY_ROWID,
						KEY_ISBN,
						KEY_TITLE,
						KEY_PUBLISHER
				},
						KEY_ROWID + "=" + rowId,
						null,
						null,
						null,
						null,
						null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}*/

	//---updates a title---
	/*public boolean updateTitle(long rowId, String isbn,
	                           String title, String publisher) {
		ContentValues args = new ContentValues();
		args.put(KEY_ISBN, isbn);
		args.put(KEY_TITLE, title);
		args.put(KEY_PUBLISHER, publisher);
		return db.update(TABLE_ACTIVITY_COUNT, args,
				KEY_ROWID + "=" + rowId, null) > 0;
	}*/
}
