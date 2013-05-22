package edu.cicese.sensit.datatask.data;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 01:25 PM
 */
public class BatteryData extends Data {
	private int level = -1;
	private boolean plugged;

	public BatteryData(int level, int plugged) {
		super(DataType.BATTERY_LEVEL);
		this.level = level;
		this.plugged = plugged != 0;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public boolean isPlugged() {
		return plugged;
	}

	public void setPlugged(boolean plugged) {
		this.plugged = plugged;
	}
}