package edu.cicese.sensit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 27/08/12
 * Time: 01:34 PM
 */
public class TimeUtil {

	public static String getTime(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return sdf.format(cal.getTime());
	}

	public static Date getDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return cal.getTime();
	}
}
