package edu.cicese.sensit.util;

/**
 * Created by: Eduardo Quintana Contreras
 * Date: 16/05/13
 * Time: 03:57 PM
 */
public class ActivityUtil {
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

	public static final int EE_WILLIAMS = 1;
	public static final int EE_FREEDSON = 2;
	public static final int EE_FREEDSON_VM3 = 3;

	// TODO: Add more equations

	public static double getCalories(int counts, float bm, int equation) {
		switch (equation) {
			case EE_WILLIAMS:
				return (counts * 0.0000191 * bm);
			case EE_FREEDSON:
				return (0.00094 * counts) + (0.1346 * bm) - 7.37418;
			case EE_FREEDSON_VM3:
				return (0.001064 * counts) + (0.087512 * bm) - 5.500229;
		}

		return 0;
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
}
