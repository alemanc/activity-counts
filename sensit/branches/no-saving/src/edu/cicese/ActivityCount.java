package edu.cicese;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 27/08/12
 * Time: 12:24 PM
 */
public class ActivityCount implements Comparable<ActivityCount> {
	private long timestamp;
	private Integer count;
	private long epoch;

	public ActivityCount(long timestamp, int count, long epoch) {
		this.timestamp = timestamp;
		this.count = count;
		this.epoch = epoch;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getCount() {
		return count;
	}

	public long getEpoch() {
		return epoch;
	}

	public int compareTo(ActivityCount activityCount) {
		return count.compareTo(activityCount.getCount());
	}
}
