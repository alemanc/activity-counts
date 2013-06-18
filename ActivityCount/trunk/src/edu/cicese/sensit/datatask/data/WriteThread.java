package edu.cicese.sensit.datatask.data;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cicese.sensit.Utilities;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 17/06/13
 * Time: 07:17 PM
 */
public class WriteThread implements Runnable {
	private final String TAG = "SensIt.WriteThread";
	private ArrayList<AccelerometerData> data;

	public WriteThread(ArrayList<AccelerometerData> data) {
		this.data = data;
	}

	@Override
	public void run() {
		Type listOfTestObject = new TypeToken<ArrayList<AccelerometerData>>() {
		}.getType();
		Gson gson = new GsonBuilder().create();
		String gsonCounts = gson.toJson(data, listOfTestObject);
		Log.d(TAG, "GSON! " + gsonCounts);

		Utilities.writeToFile(gsonCounts);
	}
}
