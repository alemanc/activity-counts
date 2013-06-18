package edu.cicese.sensit.util;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 13/06/13
 * Time: 12:16 PM
 */
public class SensitActions {
	public final static String ACTION_SENSING_START = "edu.cicese.sensit.ACTION_SENSING_START";
	public final static String ACTION_SENSING_STOP = "edu.cicese.sensit.ACTION_SENSING_STOP";
	public final static String ACTION_SENSING_START_COMPLETE = "edu.cicese.sensit.ACTION_SENSING_START_COMPLETE";
	public final static String ACTION_SENSING_STOP_COMPLETE = "edu.cicese.sensit.ACTION_SENSING_STOP_COMPLETE";

	public static final String ACTION_DATA_SYNCING = "edu.cicese.sensit.ACTION_DATA_SYNCING";
	public static final String ACTION_DATA_SYNCED = "edu.cicese.sensit.ACTION_DATA_SYNCED";
	public static final String ACTION_DATA_SYNC_DONE = "edu.cicese.sensit.ACTION_DATA_SYNC_DONE";
	public static final String ACTION_DATA_SYNC_ERROR = "edu.cicese.sensit.ACTION_DATA_SYNC_ERROR";

	public static final String ACTION_REFRESH_CHART = "edu.cicese.sensit.ACTION_REFRESH_CHART";
	public static final String ACTION_REFRESH_SENSOR = "edu.cicese.sensit.ACTION_REFRESH_SENSOR";

	public static final String ACTION_BATTERY_CHANGED = "edu.cicese.sensit.ACTION_BATTERY_CHANGED";

	public static final String EXTRA_DATE_START = "extra_date_start";
	public static final String EXTRA_DATE_END = "extra_date_end";
	public static final String EXTRA_MSG = "extra_message";
	public static final String EXTRA_SYNCED_TYPE = "extra_synced_type";

	public static final String ACTION_CLOSE_SURVEY = "edu.cicese.sensit.ACTION_CLOSE_SURVEY";

	public static final int REQUEST_CODE_RESTART = 1001;
	public static final int REQUEST_CODE_SURVEY_1 = 1002;
	public static final int REQUEST_CODE_SURVEY_2 = 1003;
	public static final int REQUEST_CODE_SURVEY_3 = 1004;
	public static final int REQUEST_CODE_CLEAR_SURVEY = 1005;
	public static final int REQUEST_CODE_CLOSE_SURVEY = 1006;
}
