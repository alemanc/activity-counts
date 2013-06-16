package edu.cicese.sensit.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
	private static final int DATABASE_VERSION = 10;
	private static final String TABLE_ACTIVITY_COUNT = "activity_count";
	private static final String TABLE_SURVEY = "survey";

	private static final String COLUMN_ACTIVITY_COUNT_ACTIVITY_COUNT_ID = "activity_count_id";
	//TODO Remove user_id field from the table activity_count
	private static final String COLUMN_ACTIVITY_COUNT_USER_ID = "user_id";
	private static final String COLUMN_ACTIVITY_COUNT_COUNTS = "counts";
	//TODO Remove calories field from the table activity_count
	private static final String COLUMN_ACTIVITY_COUNT_CALORIES = "calories";
	private static final String COLUMN_ACTIVITY_COUNT_DATE = "date";
	private static final String COLUMN_ACTIVITY_COUNT_CHARGING = "charging";
	private static final String COLUMN_ACTIVITY_COUNT_SYNCED = "synced";

	private static final String COLUMN_SURVEY_SURVEY_ID = "survey_id";
	private static final String COLUMN_SURVEY_DATE = "date";
	private static final String COLUMN_SURVEY_SYNCED = "synced";
	private static final String COLUMN_SURVEY_VALUE_STRESS = "value_stress";
	private static final String COLUMN_SURVEY_VALUE_CHALLENGE = "value_challenge";
	private static final String COLUMN_SURVEY_VALUE_SKILL = "value_skill";
	private static final String COLUMN_SURVEY_VALUE_AVOIDANCE = "value_avoidance";
	private static final String COLUMN_SURVEY_VALUE_EFFORT = "value_effort";
	private static final String[] SURVEY_VALUES = new String[]{
			COLUMN_SURVEY_VALUE_STRESS,
			COLUMN_SURVEY_VALUE_CHALLENGE,
			COLUMN_SURVEY_VALUE_SKILL,
			COLUMN_SURVEY_VALUE_AVOIDANCE,
			COLUMN_SURVEY_VALUE_EFFORT};

	private static final String CREATE_ACTIVITY_COUNT =
			"CREATE TABLE IF NOT EXISTS [" + TABLE_ACTIVITY_COUNT + "] (\n" +
					"[" + COLUMN_ACTIVITY_COUNT_ACTIVITY_COUNT_ID + "] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
					"[" + COLUMN_ACTIVITY_COUNT_USER_ID + "] STRING NOT NULL,\n" +
					"[" + COLUMN_ACTIVITY_COUNT_COUNTS + "] INTEGER DEFAULT '-1' NOT NULL,\n" +
					"[" + COLUMN_ACTIVITY_COUNT_CALORIES + "] INTEGER DEFAULT '-1' NOT NULL,\n" +
					"[" + COLUMN_ACTIVITY_COUNT_DATE + "] DATETIME NOT NULL UNIQUE,\n" +
					"[" + COLUMN_ACTIVITY_COUNT_CHARGING + "] INTEGER DEFAULT '0' NOT NULL,\n" +
					"[" + COLUMN_ACTIVITY_COUNT_SYNCED + "] INTEGER DEFAULT '0' NOT NULL\n" +
					");";
					/* +
					"\n" +
					"CREATE UNIQUE INDEX [ux_activity_count_date] ON [" + TABLE_ACTIVITY_COUNT + "](\n" +
					"[" + COLUMN_ACTIVITY_COUNT_DATE + "] ASC\n" +
					");\n" +
					"\n" +
					"CREATE UNIQUE INDEX [pk_activity_count] ON [" + TABLE_ACTIVITY_COUNT + "](\n" +
					"[" + COLUMN_ACTIVITY_COUNT_ACTIVITY_COUNT_ID + "] ASC\n" +
					");\n" +
					"\n" +
					"CREATE INDEX [ix_activity_count_synced] ON [" + TABLE_ACTIVITY_COUNT + "](\n" +
					"[" + COLUMN_ACTIVITY_COUNT_SYNCED + "]  ASC\n" +
					");";*/

	//TODO
	//*Create indexes
	//*Check alarms
	//Test development mode, rails
	//Update project to heroku (with migration)
	//Tests
	//Power saving (on/off 10 minutes)
	//Power saving bug when un/plugging
	//Wakeful on Galaxy Ace

	private static final String CREATE_INDEX_IX_ACTIVITY_COUNT_SYNCED =
			"CREATE INDEX IF NOT EXISTS ix_activity_count_synced ON " + TABLE_ACTIVITY_COUNT + "(" + COLUMN_ACTIVITY_COUNT_SYNCED + " ASC)";
	private static final String CREATE_INDEX_IX_SURVEY_SYNCED =
			"CREATE INDEX IF NOT EXISTS ix_survey_synced ON " + TABLE_SURVEY + "(" + COLUMN_SURVEY_SYNCED + " ASC)";

	private static final String CREATE_SURVEY =
			"CREATE TABLE IF NOT EXISTS [" + TABLE_SURVEY + "] (\n" +
					"[" + COLUMN_SURVEY_SURVEY_ID + "] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
					"[" + COLUMN_SURVEY_DATE + "] DATETIME NOT NULL UNIQUE,\n" +
					"[" + COLUMN_SURVEY_VALUE_STRESS + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_CHALLENGE + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_SKILL + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_AVOIDANCE + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_EFFORT + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_SYNCED + "] INTEGER DEFAULT '0' NOT NULL\n" +
					");\n"/* +
					"\n" +
					"CREATE UNIQUE INDEX [ux_survey_date] ON [" + TABLE_SURVEY + "](\n" +
					"[" + COLUMN_SURVEY_DATE + "] ASC\n" +
					");\n" +
					"\n" +
					"CREATE UNIQUE INDEX [pk_survey] ON [" + TABLE_SURVEY + "](\n" +
					"[" + COLUMN_SURVEY_SURVEY_ID + "] ASC\n" +
					");\n" +
					"\n" +
					"CREATE INDEX [ix_survey_synced] ON [" + TABLE_SURVEY + "](\n" +
					"[" + COLUMN_SURVEY_SYNCED + "]  ASC\n" +
					");"*/;

//	private final String[] likertDBValues = new String[]{"STRONGLY_DISAGREE", "DISAGREE", "NEUTRAL", "AGREE", "STRONGLY_DISAGREE"};

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context context) {
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_ACTIVITY_COUNT);
			db.execSQL(CREATE_SURVEY);
			db.execSQL(CREATE_INDEX_IX_ACTIVITY_COUNT_SYNCED);
			db.execSQL(CREATE_INDEX_IX_SURVEY_SYNCED);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
			/*
			onCreate(db);*/
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_COUNT);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEY);
			onCreate(db);
		}
	}

	public DBAdapter open() throws SQLException {
		if (db == null || !db.isOpen()) {
			db = DBHelper.getWritableDatabase();
		}
		return this;
	}

	public void close() {
		if (db != null && db.isOpen()) {
			DBHelper.close();
		}
	}

	public long insertCounts(/*String userId, */int counts, String date, boolean epochCharging, int synced) {
		// date example: "2010-11-17 14:12:23"
		ContentValues values = new ContentValues();
//		values.put(COLUMN_ACTIVITY_COUNT_USER_ID, userId);
		values.put(COLUMN_ACTIVITY_COUNT_USER_ID, "unused");
		values.put(COLUMN_ACTIVITY_COUNT_COUNTS, counts);
		values.put(COLUMN_ACTIVITY_COUNT_CALORIES, ActivityUtil.getCalories(counts, ActivityUtil.bmi, ActivityUtil.EE_WILLIAMS));
		values.put(COLUMN_ACTIVITY_COUNT_DATE, date);
		values.put(COLUMN_ACTIVITY_COUNT_CHARGING, epochCharging);
		values.put(COLUMN_ACTIVITY_COUNT_SYNCED, synced);
		return db.insert(TABLE_ACTIVITY_COUNT, null, values);
	}

	public Cursor queryCounts() {
		return db.query(TABLE_ACTIVITY_COUNT,
				new String[]{
//						COLUMN_ACTIVITY_COUNT_USER_ID,
						COLUMN_ACTIVITY_COUNT_COUNTS,
//						COLUMN_ACTIVITY_COUNT_CALORIES,
						COLUMN_ACTIVITY_COUNT_DATE,
						COLUMN_ACTIVITY_COUNT_CHARGING},
				COLUMN_ACTIVITY_COUNT_SYNCED + " = 0",
				null,
				null,
				null,
				null);
	}

	public Cursor queryCounts(int max) {
		return db.query(TABLE_ACTIVITY_COUNT,
				new String[]{
						COLUMN_ACTIVITY_COUNT_COUNTS,
						COLUMN_ACTIVITY_COUNT_DATE,
						COLUMN_ACTIVITY_COUNT_CHARGING
				},
				null,
				null,
				null,
				null,
				COLUMN_ACTIVITY_COUNT_ACTIVITY_COUNT_ID + " DESC",
				max + "");
	}

	public int updateCounts(String dateStart, String dateEnd) {
		ContentValues args = new ContentValues();
		args.put(COLUMN_ACTIVITY_COUNT_SYNCED, 1);

		return db.update(TABLE_ACTIVITY_COUNT,
				args,
				COLUMN_ACTIVITY_COUNT_DATE + " BETWEEN ? AND ?",
				new String[]{dateStart, dateEnd});
	}

	// Surveys
	public long insertSurvey(int[] likertValues, String date, int synced) {
		// date example: "2010-11-17"

		Log.d(TAG, "Date: " + date);

		ContentValues values = new ContentValues();
		for (int i = 0; i < SURVEY_VALUES.length; i++) {
			values.put(SURVEY_VALUES[i], likertValues[i]);
		}
		values.put(COLUMN_SURVEY_DATE, date);
		values.put(COLUMN_SURVEY_SYNCED, synced);
		return db.insert(TABLE_SURVEY, null, values);
	}

	public Cursor querySurveys() {
		return db.query(TABLE_SURVEY,
				new String[]{
						COLUMN_SURVEY_DATE,
						COLUMN_SURVEY_VALUE_STRESS,
						COLUMN_SURVEY_VALUE_CHALLENGE,
						COLUMN_SURVEY_VALUE_SKILL,
						COLUMN_SURVEY_VALUE_AVOIDANCE,
						COLUMN_SURVEY_VALUE_EFFORT,
				},
				COLUMN_SURVEY_SYNCED + " = 0",
				null,
				null,
				null,
				null);
	}

	public Cursor querySurveys(int max) {
		return db.query(TABLE_SURVEY,
				new String[]{
						COLUMN_SURVEY_DATE
				},
				null,
				null,
				null,
				null,
				COLUMN_SURVEY_SURVEY_ID + " DESC",
				max + "");
	}

	public int updateSurveys(String dateStart, String dateEnd) {
		ContentValues args = new ContentValues();
		args.put(COLUMN_SURVEY_SYNCED, 1);

		return db.update(TABLE_SURVEY,
				args,
				COLUMN_SURVEY_DATE + " BETWEEN ? AND ?",
				new String[]{dateStart, dateEnd});
	}
}
