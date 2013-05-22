package edu.cicese.sensit.sensor;

import edu.cicese.sensit.datatask.data.DataType;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 21/05/13
 * Time: 02:06 PM
 */
public class SensorStatus {
	private DataType type;
	private int status;

	public SensorStatus(DataType type, int status) {
		this.type = type;
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
