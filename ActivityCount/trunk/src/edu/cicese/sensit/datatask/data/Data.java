package edu.cicese.sensit.datatask.data;

import android.os.Bundle;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 9/05/13
 * Time: 12:38 PM
 */
public abstract class Data {
	private long timestamp;
	private DataType dataType = DataType.NULL;
	private Bundle extras;

	public Data(DataType dataType) {
		setTimestamp(System.currentTimeMillis());
		setDataType(dataType);
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	protected void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param extras the bundle to set
	 */
	public void setExtras(Bundle extras) {
		this.extras = extras;
	}

	/**
	 * @return the bundle
	 */
	public Bundle getExtras() {
		if (extras == null) {
			extras = new Bundle();
		}
		return extras;
	}
}