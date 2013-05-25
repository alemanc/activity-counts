package edu.cicese.sensit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Calendar;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 24/05/13
 * Time: 01:59 PM
 */
public class TestActivity extends Activity {
	final Calendar c = Calendar.getInstance();
	int mMinute = c.get(Calendar.MINUTE);
	int mHour = c.get(Calendar.HOUR_OF_DAY);
	int am = c.get(Calendar.AM_PM);
	int[] x = new int[1920];
	int[] sleep = {4, 3, 2, 1, 4, 3, 2, 1, 4, 3, 2, 1, 4, 1, 1, 1, 4, 4, 2, 2, 2, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 4, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2};

	public String getnext(int a) {
		String ap;
		if (am == 1) {
			ap = "PM";
		} else {
			ap = "AM";
		}
		String s = mHour + " " + ap;
		mMinute = mMinute + 5;

		if (mHour >= 12) {
			mHour = mHour - 12;
			switch (am) {
				case 0:
					am = 1;
					break;
				case 1:
					am = 0;
					break;
			}
		}
		if (mMinute >= 60) {
			mHour = mHour + 1;
			mMinute = mMinute - 60;
		}
		//Log.d("Gr","mMinute: "+mMinute);
		if (mMinute == 1 | mMinute == 2 | mMinute == 3 | mMinute == 4 | mMinute == 0) {
			s = mHour + " " + ap;
		} else {
			s = "";
		}

		return (s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//  setContentView(R.layout.activity_main);

		openChart();
	}


	private void openChart() {
		XYSeries sleepSeries = new XYSeries("Sleep");

		for (int i = 0; i < sleep.length; i++) {
			sleepSeries.add(i, sleep[i]);
		}


		// Creating a dataset to hold each series
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// Adding Income Series to the dataset
		dataset.addSeries(sleepSeries);


		// Creating XYSeriesRenderer to customize incomeSeries
		XYSeriesRenderer sleepRenderer = new XYSeriesRenderer();
		sleepRenderer.setColor(Color.GREEN);
		sleepRenderer.setFillPoints(true);

		sleepRenderer.setLineWidth((float) .2);

		// Creating a XYMultipleSeriesRenderer to customize the whole chart
		XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
		multiRenderer.setXLabels(0);
		multiRenderer.setYAxisMin(0);
		multiRenderer.setYLabels(0);
		multiRenderer.setYAxisMax(4);
		multiRenderer.setChartTitle("Sleep vs Time");
		multiRenderer.setBarSpacing(.5);
		//multiRenderer.setZoomButtonsVisible(true);
		multiRenderer.setLegendHeight((int) 5);
		multiRenderer.setPanEnabled(true, false);

		multiRenderer.addSeriesRenderer(sleepRenderer);

		// Creating an intent to plot bar chart using dataset and multipleRenderer
		Intent intent = ChartFactory.getBarChartIntent(getBaseContext(), dataset, multiRenderer, BarChart.Type.DEFAULT);

		// Start Activity
		startActivity(intent);

	}
}