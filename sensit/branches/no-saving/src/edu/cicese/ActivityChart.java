package edu.cicese;

import android.content.Context;
import android.graphics.Color;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Date;
import java.util.List;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 27/08/12
 * Time: 04:53 PM
 */
public class ActivityChart {
	private static final String DATE_FORMAT = "HH:mm:ss";

	private TimeSeries timeSeries;
	private XYMultipleSeriesRenderer renderer;

	public GraphicalView getView(Context context, List<ActivityCount> activityCounts) {
		String title = context.getString(R.string.chart_title);

		int[] colors = new int[]{Color.GREEN};
		PointStyle[] styles = new PointStyle[]{PointStyle.POINT};
		renderer = buildRenderer(colors, styles);

		setChartSettings(renderer, context.getString(R.string.chart_title),
				context.getString(R.string.date),
				context.getString(R.string.value),
				System.currentTimeMillis(),
				System.currentTimeMillis() + Utilities.GRAPH_RANGE,
				0, 1000, Color.GRAY, Color.LTGRAY);


		renderer.setXLabels(3);
		renderer.setYLabels(5);

		/*XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(0);
		seriesRenderer.setChartValuesSpacing(5f);
		seriesRenderer.setDisplayChartValues(false);
		seriesRenderer.setColor(Color.RED);
		seriesRenderer.setFillPoints(true);
		seriesRenderer.setPointStyle(PointStyle.CIRCLE);*/

		return ChartFactory.getTimeChartView(context,
				buildDateDataset(title, activityCounts), renderer, DATE_FORMAT);
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
	                                String title, String xTitle, String yTitle, double xMin,
	                                double xMax, double yMin, double yMax, int axesColor,
	                                int labelsColor) {
		renderer.setChartTitle("");
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
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
		renderer.setPanEnabled(true, true);
		renderer.setPointSize(2);
		renderer.setMargins(new int[]{10, 30, 8, 15}); //top, left, bottom, right
//		renderer.setZoomButtonsVisible(true);
		renderer.setBarSpacing(5);
		renderer.setShowGrid(true);
	}

	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
	                                                 PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRendererProperties(renderer, colors, styles);
		return renderer;
	}

	protected XYMultipleSeriesDataset buildDateDataset(String title,
	                                                   List<ActivityCount> activityCounts) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		timeSeries = new TimeSeries(title);
		for (ActivityCount activityCount : activityCounts) {
			timeSeries.add(TimeUtil.getDate(activityCount.getTimestamp()), activityCount.getCount());
		}
		dataset.addSeries(timeSeries);
		return dataset;
	}

	protected void setRendererProperties(XYMultipleSeriesRenderer renderer, int[] colors,
	                                     PointStyle[] styles) {
//		renderer.setAxisTitleTextSize(16);
//		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(12);
		renderer.setLegendTextSize(12);
//		renderer.setPointSize(5f);
//		renderer.setMargins(new int[]{20, 30, 15, 20});

		renderer.setShowLegend(false);
		renderer.setShowGridX(false);
		renderer.setShowCustomTextGrid(true);

		XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
		seriesRenderer.setChartValuesSpacing(5f);
		seriesRenderer.setDisplayChartValues(false);
		seriesRenderer.setColor(Color.RED);
		seriesRenderer.setFillPoints(true);
		seriesRenderer.setPointStyle(PointStyle.CIRCLE);
		renderer.addSeriesRenderer(seriesRenderer);

		/*int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}*/
	}

	public void addValue(Date date, int count) {
		timeSeries.add(date, count);
		setMinMax();
	}

	private void setMinMax() {
		double maxX = timeSeries.getMaxX() + (Utilities.GRAPH_RANGE / 2);
		double minX = timeSeries.getMaxX() - (Utilities.GRAPH_RANGE / 2);
		double maxY = timeSeries.getMaxY();
		double minY = 0;

		if (maxY < 100) {
			maxY = 100;
		}

		renderer.setRange(new double[]{minX, maxX, minY, maxY});
	}
}