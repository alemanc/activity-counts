package edu.cicese.sensit;

import com.google.gson.annotations.Expose;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 27/08/12
 * Time: 12:24 PM
 */
public class ActivityCount {
	private final String TAG = "SensIt.ActivityCount";

//	private Date date;
	@Expose private String date;
	@Expose private int counts;
	@Expose private boolean charging;
//	@Expose private int epoch = 60;

//	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public ActivityCount(String date, int counts, int charging) {
		this.counts = counts;
		this.charging = (charging == 1);
		this.date = date;

		// date format: 2013-06-03 13:27:00
		/*try {
			this.date = sdf.parse(date);
		} catch (ParseException e) {
			Log.d(TAG, "Unable to parse date string " + date);
		}*/
	}

	public String getDate() {
		return date;
	}

	public int getCounts() {
		return counts;
	}
}
