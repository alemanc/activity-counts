package edu.cicese.sensit.util;

import android.util.Log;
import edu.cicese.sensit.ActivityCount;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 16/05/13
 * Time: 03:57 PM
 */
public class ActivityUtil {

	// Activity count chart ranges
	public static final int GRAPH_RANGE_X = 20;
	public static final int GRAPH_RANGE_Y = 6000;
	public static final double GRAPH_SLEEP_VALUE = 100;

	// CPM = Counts per Minute
	// BM = Body Mass in Kg
	// kcals = Total Calories for a Single Epoch

	/////////////////
	// Energy Expenditure calculations are only accurate for adults, age 19 years and up
	/////////////////

	// Williams Work-Energy ('98)
	// Kcal Estimates from Activity Counts using the Potential Energy Method
	// kcals = CPM × 0.0000191 × BM

	// Freedson ('98) (>1951 counts)
	// Calibration of the Computer Science and Applications, Inc. Accelerometer
	// kcals = (0.00094 × CPM) + (0.1346 × BM) - 7.37418

	// Freedson VM3 ('11) (>2453 counts)
	// Validation and Comparison of ActiGraph
	// kcals = (0.001064 × CPM) + (0.087512 × BM) - 5.500229
	// NOTE: Previous equations consider data collected from one axis

	// TODO: Add more equations
	public static final int EE_WILLIAMS = 1;
	public static final int EE_FREEDSON = 2;
	public static final int EE_FREEDSON_VM3 = 3;
	public static final int SLEEP_THRESHOLD = 20;

	public static float bmi = -1;
	public static int counts;
	public static int checkEpochCounts;
	// stores the CPM from the current minute, and the last 9 minutes to compute the sleep time
	private static Queue<ActivityCount> countsFrame = new LinkedList<>();

	// Counter for inactive epochs. Can be used to sleep the accelerometer
	private static int epochsInactive = 0;

	// No-movement threshold
	public static final int MIN_ACTIVE_COUNTS = 5; // activity counts

	private static int pauseQueue = 0;


	public static int getPauseQueue() {
		return pauseQueue;
	}

	public static void resetPauseQueue() {
		pauseQueue = 0;
	}

	public static void setPauseQueue(int value) {
		pauseQueue += value;
		if (pauseQueue < 0) {
			pauseQueue = 0;
		}
	}

	public static void resetEpochsInactive() {
	}

	public static int getEpochsInactive() {
		return epochsInactive;
	}

	public static void setEpochsInactive(int epochsInactive) {
		ActivityUtil.epochsInactive = epochsInactive;
	}

	public static void addEpochInactive() {
		ActivityUtil.epochsInactive++;
	}

	public static int getCheckEpochCounts() {
		return checkEpochCounts;
	}

	public static void setCheckEpochCounts(int checkEpochCounts) {
		ActivityUtil.checkEpochCounts = checkEpochCounts;
	}

	/**
	 * Computes the calories burned according the activity counts measured.
	 *
	 * @param counts the activity counts measured.
	 * @param bmi the body mass index.
	 * @param equation the equation to compute calories burned.
	 *
	 * @return the calories burned.
	 */
	public static int getCalories(int counts, float bmi, int equation) {
		if (bmi != -1) {
			switch (equation) {
				case EE_WILLIAMS:
					return (int) Math.round(counts * 0.0000191 * bmi);
				case EE_FREEDSON:
					return (int) Math.round((0.00094 * counts) + (0.1346 * bmi) - 7.37418);
				case EE_FREEDSON_VM3:
					return (int) Math.round((0.001064 * counts) + (0.087512 * bmi) - 5.500229);
			}
		}
		return -1;
	}

	/**
	 * Computes the BMI (body mass index).
	 *
	 * @param height the height in centimeters.
	 * @param weight the weight in kilograms.
	 *
	 * @return the BMI.
	 */
	public static void setBMI(int height, int weight) {
		if(height != -1 && weight != -1) {
			float heightInM = height / 100f;
			bmi = weight / (heightInM * heightInM);
			Log.d("SensIt.ActivityUtil", "BMI: " + bmi);
		}
	}

	public static float getBmi() {
		return bmi;
	}

	////////////////
	// MET
	////////////////

	// 1 MET represents the amount of energy the human body expends at rest.
	// This is equivalent to their Basal Metabolic Rate (or BMR).
	// A MET rate of 2.0, for instance, indicates that during that time period, the subject was expending twice their normal sedentary energy (BMR*2).
	// This value will never fall below 1.0.

	// MET level categories typically used in the literature, and the corresponding CPM
	// | Intensity | MET Range |    CPM    |
	// | Light     |   <3.00   |   <1952   |
	// | Moderate  | 3.00-5.99 | 1952-5724 |
	// | Hard      | 6.00-8.99 | 5725-9498 |
	// | Very hard |   >8.99   |   >9498   |

	// Activity count cut-offs corresponding to MET levels
	// Freedson ('98)
	// Calibration of the Computer Science and Applications, Inc. Accelerometer
	// METs = 1.439008 + (0.000795 × CPM)
	// This formula was obtained from 50 adults (25 males, 25 females) during treadmill exercise at three different speeds (4.8, 6.4, and 9.7 km/hr)

	public enum METEquation {
		MET_FREEDSON_ADULT,
		MET_HENDELMAN_ADULT_1,
		MET_HENDELMAN_ADULT_2,
		MET_SWARTZ_ADULT,
		MET_LEENDERS_ADULT,
		MET_YNGVE_ADULT,
		MET_BROOKS_ADULT,
		MET_BROOKS_BM_ADULT,
		MET_FREEDSON_CHILDREN
	};

	public static double getMETs(float[] values, METEquation equation) {
		float counts = values[0];
		switch (equation) {
			case MET_FREEDSON_ADULT:
				return 1.439008 + (0.000795 * counts);
			case MET_HENDELMAN_ADULT_1:
				return 1.602 + (0.000638 * counts);
			case MET_HENDELMAN_ADULT_2:
				return 2.922 + (0.000409 * counts);
			case MET_SWARTZ_ADULT:
				return 2.606 + (0.0006863 * counts);
			case MET_LEENDERS_ADULT:
				return 2.240 + (0.0006 * counts);
			case MET_YNGVE_ADULT:
				return 0.751 + (0.0008198 * counts);
			case MET_BROOKS_ADULT:
				return 2.32 + (0.000389 * counts);
			case MET_BROOKS_BM_ADULT:
				return 3.33 + (0.000370 * counts) - (0.012 * values[1]);
			case MET_FREEDSON_CHILDREN:
				return 2.757 + (0.0015 * counts) - (0.08957 * values[2]) - (0.000038 * counts * values[2]);
		}

		return 1;
	}

	// TODO What's best? computing intensity from counts, or METs?

	// TODO Add a constants with different CPM ranges, for each author
	public static int getIntensity(int counts) {
		return 0;
	}

	public static int getIntensity(double METs) {
		return 0;
	}

	public static int getCounts() {
		int countsTmp = counts;
		// Just in case the accelerometer updates the value before returning it.
		counts -= countsTmp;
		return countsTmp;
	}

	/*public static void addToCountsFrame(Date date, int counts) {
		if (countsFrame.size() > Utilities.LOG_SIZE) {
			countsFrame.poll();
		}
		countsFrame.add(new ActivityCount(date, counts));
	}*/
}
