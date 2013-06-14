package edu.cicese.sensit.database;

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
	private static final int DATABASE_VERSION = 7;
	private static final String TABLE_ACTIVITY_COUNT = "activity_count";
	private static final String TABLE_SURVEY = "survey";

	private static final String COLUMN_ACTIVITY_COUNT_ACTIVITY_COUNT_ID = "activity_count_id";
	private static final String COLUMN_ACTIVITY_COUNT_USER_ID = "user_id";
	private static final String COLUMN_ACTIVITY_COUNT_COUNTS = "counts";
	private static final String COLUMN_ACTIVITY_COUNT_CALORIES = "calories";
	private static final String COLUMN_ACTIVITY_COUNT_DATE = "date";
	private static final String COLUMN_ACTIVITY_COUNT_CHARGING = "charging";
	private static final String COLUMN_ACTIVITY_COUNT_SYNCED = "synced";

	private static final String COLUMN_SURVEY_SURVEY_ID = "survey_id";
	private static final String COLUMN_SURVEY_DATE = "date";
	private static final String COLUMN_SURVEY_SYNCED = "synced";
//	public final String COLUMN_SURVEY_QUESTION_STRESS = "question_stress";
//	public final String COLUMN_SURVEY_QUESTION_CHALLENGE = "question_challenge";
//	public final String COLUMN_SURVEY_QUESTION_SKILL = "question_skill";
//	public final String COLUMN_SURVEY_QUESTION_AVOIDANCE = "question_avoidance";
//	public final String COLUMN_SURVEY_QUESTION_EFFORT = "question_effort";
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
			"CREATE TABLE IF NOT EXISTS [activity_count] (\n" +
					"[activity_count_id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
					"[user_id] STRING NOT NULL,\n" +
					"[counts] INTEGER DEFAULT '-1' NOT NULL,\n" +
					"[calories] INTEGER DEFAULT '-1' NOT NULL,\n" +
					"[date] DATETIME NOT NULL,\n" +
					"[charging] INTEGER DEFAULT '0' NOT NULL,\n" +
					"[synced] INTEGER DEFAULT '0' NOT NULL\n" +
					");\n" +
					"\n" +
					"CREATE UNIQUE INDEX [ux_activity_count_date] ON [activity_count](\n" +
					"[date] ASC\n" +
					");\n" +
					"\n" +
					"CREATE UNIQUE INDEX [pk_activity_count] ON [activity_count](\n" +
					"[activity_count_id] ASC\n" +
					");\n" +
					"\n" +
					"CREATE INDEX [ix_activity_count_synced] ON [activity_count](\n" +
					"[synced]  ASC\n" +
					");";

	private static final String CREATE_SURVEY =
			"CREATE TABLE IF NOT EXISTS [" + TABLE_SURVEY + "] (\n" +
					"[" + COLUMN_SURVEY_SURVEY_ID + "] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
					"[" + COLUMN_SURVEY_DATE + "] DATETIME NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_STRESS + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_CHALLENGE + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_SKILL + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_AVOIDANCE + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_VALUE_EFFORT + "] STRING DEFAULT '3' NOT NULL,\n" +
					"[" + COLUMN_SURVEY_SYNCED + "] INTEGER DEFAULT '0' NOT NULL\n" +
					");\n" +
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
					");";

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
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			/*Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_COUNT);
			onCreate(db);*/
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
		public long insertSurvey(int[] likertValues, String date, boolean synced) {
		// date example: "2010-11-17 14:12:23"
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

	public int updateSurveys(String dateStart, String dateEnd) {
		ContentValues args = new ContentValues();
		args.put(COLUMN_SURVEY_SYNCED, 1);

		return db.update(TABLE_SURVEY,
				args,
				COLUMN_SURVEY_DATE + " BETWEEN ? AND ?",
				new String[]{dateStart, dateEnd});
	}
}
