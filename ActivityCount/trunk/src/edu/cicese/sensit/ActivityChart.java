package edu.cicese.sensit;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import edu.cicese.sensit.util.ActivityUtil;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 27/08/12
 * Time: 04:53 PM
 */
public class ActivityChart {
	private static final String DATE_FORMAT = "HH:mm";

	private XYSeries activitySeries, sleepSeries, awakeSeries;
	private XYMultipleSeriesRenderer renderer;

	public GraphicalView getView(Context context, List<ActivityCount> activityCounts) {
		String title = context.getString(R.string.chart_title);

		renderer = buildRenderer();

		setChartSettings(renderer, title,
				context.getString(R.string.date),
				context.getString(R.string.value),
				0, ActivityUtil.GRAPH_RANGE_X,
				0, ActivityUtil.GRAPH_RANGE_Y,
				Color.GRAY,
				Color.LTGRAY);

		return ChartFactory.getCombinedXYChartView(context, buildDateDataset(title, activityCounts), renderer, new String[]{BarChart.TYPE, ScatterChart.TYPE, ScatterChart.TYPE});
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
	                                String title, String xTitle, String yTitle, double xMin,
	                                double xMax, double yMin, double yMax, int axesColor,
	                                int labelsColor) {
		renderer.setChartTitle("");
		renderer.setXTitle("");
		renderer.setYTitle("");
		renderer.setShowLegend(false);
//		renderer.setShowLabels(false);
		renderer.setXLabelsAlign(Paint.Align.CENTER);
		renderer.setYLabelsAlign(Paint.Align.RIGHT);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
		renderer.setMarginsColor(Color.WHITE);
		renderer.setAxesColor(Color.LTGRAY);
		renderer.setAxisTitleTextSize(16);
		renderer.setFitLegend(true);
		renderer.setGridColor(Color.LTGRAY);
//		renderer.setMargins(new int[]{10, 30, 8, 15}); //top, left, bottom, right
		renderer.setBarWidth(10f);
		renderer.setBarSpacing(0.2);
		renderer.setShowGrid(true);

		renderer.setXLabels(0);
	}

	protected XYMultipleSeriesRenderer buildRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setLabelsTextSize(12);
		renderer.setLegendTextSize(12);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(false, false);
		renderer.setShowLegend(false);
		renderer.setShowGridX(false);
		renderer.setShowCustomTextGrid(true);

		XYSeriesRenderer activityRenderer = new XYSeriesRenderer();
		activityRenderer.setColor(Color.BLUE);

		XYSeriesRenderer sleepRenderer = new XYSeriesRenderer();
		sleepRenderer.setColor(Color.RED);
		sleepRenderer.setPointStyle(PointStyle.SQUARE);
		sleepRenderer.setFillPoints(true);
		sleepRenderer.setPointStrokeWidth(10);
//		sleepRenderer.setLineWidth(0);

		XYSeriesRenderer awakeRenderer = new XYSeriesRenderer();
		awakeRenderer.setColor(Color.GREEN);
		awakeRenderer.setPointStyle(PointStyle.SQUARE);
		awakeRenderer.setFillPoints(true);
		awakeRenderer.setPointStrokeWidth(10);
//		awakeRenderer.setLineWidth(0);

		renderer.addSeriesRenderer(sleepRenderer);
		renderer.addSeriesRenderer(awakeRenderer);
		renderer.addSeriesRenderer(0, activityRenderer);
		return renderer;
	}

	protected XYMultipleSeriesDataset buildDateDataset(String title, List<ActivityCount> activityCounts) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		activitySeries = new XYSeries(title);
		sleepSeries = new XYSeries(title);
		awakeSeries = new XYSeries(title);
		for (int i = 0; i < activityCounts.size(); i++) {
			activitySeries.add(i, activityCounts.get(i).getCounts());
			sleepSeries.add(i, ActivityUtil.GRAPH_SLEEP_VALUE);
			awakeSeries.add(i, ActivityUtil.GRAPH_SLEEP_VALUE);
		}
		/*activitySeries.add(0, 500);
		activitySeries.add(1, 160);
		activitySeries.add(2, 150);
		activitySeries.add(3, 160);
		activitySeries.add(4, 170);
		activitySeries.add(5, 200);
		activitySeries.add(6, 230);
		activitySeries.add(7, 200);

		for (int i = 0; i < 4; i++) {
			sleepSeries.add(i, ActivityUtil.GRAPH_SLEEP_VALUE);
		}
		for (int i = 4; i < 8; i++) {
			awakeSeries.add(i, ActivityUtil.GRAPH_SLEEP_VALUE);
		}*/

		dataset.addSeries(sleepSeries);
		dataset.addSeries(awakeSeries);
		dataset.addSeries(0, activitySeries);

		return dataset;
	}

	public void addCounts(Date date, int counts) {
		int nextIndex = activitySeries.getItemCount();
		activitySeries.add(nextIndex, counts);
		if (counts < 20) {
			sleepSeries.add(nextIndex, ActivityUtil.GRAPH_SLEEP_VALUE);
		}
		else {
			awakeSeries.add(nextIndex, ActivityUtil.GRAPH_SLEEP_VALUE);
		}

		Log.d("SensIt.Chart", "nextIndex: " + nextIndex);

		if (nextIndex % 5 == 0) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			renderer.removeXTextLabel(nextIndex);
			renderer.addXTextLabel(nextIndex, sdf.format(date));
		}

		setRange();
	}

	private void setRange() {
		double maxX = activitySeries.getMaxX()/* + (ActivityUtil.GRAPH_RANGE_X / 2)*/;
		double minX = activitySeries.getMaxX() - ActivityUtil.GRAPH_RANGE_X;
		double maxY = activitySeries.getMaxY();
		double minY = 0;

		Log.d("SensIt.Chart", "maxX:" + maxX + ", minX:" + minX + ", maxY:" + maxY + ", minY:" + minY);

		if (maxY < 100) {
			maxY = 100;
		}
		else if (maxY > ActivityUtil.GRAPH_RANGE_Y) {
			maxY = ActivityUtil.GRAPH_RANGE_Y;
		}

		if (minX < 0) {
			minX = 0;
			maxX = ActivityUtil.GRAPH_RANGE_X;
		}
		/*if (maxX < ActivityUtil.GRAPH_RANGE_X) {
			maxX = ActivityUtil.GRAPH_RANGE_X;
		}*/

		renderer.setRange(new double[]{minX, maxX, minY, maxY});
	}
}