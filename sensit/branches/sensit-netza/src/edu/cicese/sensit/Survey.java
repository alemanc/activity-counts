package edu.cicese.sensit;

import com.google.gson.annotations.Expose;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 14/06/13
 * Time: 09:23 PM
 */
public class Survey {
	private final String TAG = "SensIt.Survey";

	@Expose
	private String date;
	@Expose
	private int question1;
	@Expose
	private int question2;
	@Expose
	private int question3;
	@Expose
	private int question4;
	@Expose
	private int question5;

	public Survey(String date, int question1, int question2, int question3, int question4, int question5) {
		this.date = date;
		this.question1 = question1;
		this.question2 = question2;
		this.question3 = question3;
		this.question4 = question4;
		this.question5 = question5;
	}

	public String getDate() {
		return date;
	}
}
