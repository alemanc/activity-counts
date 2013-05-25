package edu.cicese.sensit;

import java.util.Date;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 27/08/12
 * Time: 12:24 PM
 */
public class ActivityCount/* implements Comparable<ActivityCount>*/ {
//	private long timestamp;
//	private Integer count;
//	private long epoch;

	private Date date;
	private int counts;

	/*public ActivityCount(long timestamp, int count, long epoch) {
		this.timestamp = timestamp;
		this.count = count;
		this.epoch = epoch;
	}*/

	public ActivityCount(Date date, int counts) {
		this.date = date;
		this.counts = counts;
	}

	/*public long getTimestamp() {
		return timestamp;
	}*/

	/*public int getCount() {
		return count;
	}*/

	public Date getDate() {
		return date;
	}

	public int getCounts() {
		return counts;
	}

	/*public int compareTo(ActivityCount activityCount) {
		return count.compareTo(activityCount.getCount());
	}*/
}
