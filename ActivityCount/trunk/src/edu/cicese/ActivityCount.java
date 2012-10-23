package edu.cicese;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 27/08/12
 * Time: 12:24 PM
 */
public class ActivityCount implements Comparable<ActivityCount> {
	private long timestamp;
	private Integer count;

	public ActivityCount(long timestamp, int count) {
		this.timestamp = timestamp;
		this.count = count;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getCount() {
		return count;
	}

	public int compareTo(ActivityCount activityCount) {
		return count.compareTo(activityCount.getCount());
	}
}
