package edu.cicese.sensit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import edu.cicese.sensit.db.DBAdapter;
import edu.cicese.sensit.util.SensitActions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SurveyActivity extends Activity implements OnSeekBarChangeListener {
	private static final String TAG = "SensIt.SurveyActivity";

	//	private int valueStress = 0;
//	private int valueChallenge = 0;
//	private int valueSkill = 0;
//	private int valueAvoidance = 0;
//	private int valueEffort = 0;
	private ArrayList<TextView> textViews = new ArrayList<>();
	private ArrayList<SeekBar> seekBars = new ArrayList<>();
	private int[] values = new int[5];
	//	private final String[] stringAnswers = new String[5];
	private final String[] stressValues = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
	private final String[] likertTextValues = new String[]{"totalmente en desacuerdo", "en desacuerdo", "neutral", "de acuerdo", "totalmente de acuerdo"};

	private final int LAST_VALID_HOUR = 5;
	private final int FIRST_VALID_HOUR = 8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questions);

		final SeekBar sbStress = (SeekBar) findViewById(R.id.seek_bar_question_stress);
		final SeekBar sbChallenge = (SeekBar) findViewById(R.id.seek_bar_question_challenge);
		final SeekBar sbSkill = (SeekBar) findViewById(R.id.seek_bar_question_skill);
		final SeekBar sbAvoidance = (SeekBar) findViewById(R.id.seek_bar_question_avoidance);
		final SeekBar sbEffort = (SeekBar) findViewById(R.id.seek_bar_question_effort);
		final TextView tvStress = (TextView) findViewById(R.id.question_stress_value);
		final TextView tvChallenge = (TextView) findViewById(R.id.question_challenge_value);
		final TextView tvSkill = (TextView) findViewById(R.id.question_skill_value);
		final TextView tvAvoidance = (TextView) findViewById(R.id.question_avoidance_value);
		final TextView tvEffort = (TextView) findViewById(R.id.question_effort_value);

		final Button btnSave = (Button) findViewById(R.id.btn_save);
		final Button btnCancel = (Button) findViewById(R.id.btn_cancel);
		final Button btnLater = (Button) findViewById(R.id.btn_later);

		textViews.add(tvStress);
		textViews.add(tvChallenge);
		textViews.add(tvSkill);
		textViews.add(tvAvoidance);
		textViews.add(tvEffort);

		seekBars.add(sbStress);
		seekBars.add(sbChallenge);
		seekBars.add(sbSkill);
		seekBars.add(sbAvoidance);
		seekBars.add(sbEffort);

//		int stressProgress = sbStress.getProgress();
//		tvStress.setText(stressValues[stressProgress]);
//		values[0] = stressProgress + 1;
//		for (int i = 1; i < values.length; i++) {
//			int progress = seekBars.get(i).getProgress();
//			textViews.get(i).setText(likertTextValues[progress]);
//			values[i] = progress + 1;
//		}

		setAnswers();

//		tvStress.setText(stressValues[sbStress.getProgress()]);
//		tvChallenge.setText(likertTextValues[sbChallenge.getProgress()]);
//		tvSkill.setText(likertTextValues[sbSkill.getProgress()]);
//		tvAvoidance.setText(likertTextValues[sbAvoidance.getProgress()]);
//		tvEffort.setText(likertTextValues[sbEffort.getProgress()]);

//		setControlsByAnswers();
		for (SeekBar bar : seekBars) {
			bar.setOnSeekBarChangeListener(this);
		}

		sbStress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//				valueStress = progress + 1;// seekbar start from 0 while Likert Scale from 1.
//				stringAnswers[0] = stressValues[progress];
				tvStress.setText(stressValues[progress]);
			}
		});

		sbChallenge.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {

//				valueChallenge = progress + 1;// seekbar start from 0 while Likert Scale from 1.
//				stringAnswers[1] = likertTextValues[progress];
				tvChallenge.setText(likertTextValues[progress]);
			}
		});

		sbSkill.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {

//				valueSkill = progress + 1;// seekbar start from 0 while Likert Scale from 1.
//				stringAnswers[2] = likertTextValues[progress];
				tvSkill.setText(likertTextValues[progress]);
			}
		});

		sbAvoidance.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {

//				valueAvoidance = progress + 1;// seekbar start from 0 while Likert Scale from 1.
//				stringAnswers[3] = likertTextValues[progress];
				tvAvoidance.setText(likertTextValues[progress]);
			}
		});

		sbEffort.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {

//				valueEffort = progress + 1;// seekbar start from 0 while Likert Scale from 1.
//				stringAnswers[4] = likertTextValues[progress];
				tvEffort.setText(likertTextValues[progress]);

			}
		});

		btnSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setAnswers();
				Log.d(TAG, "Yei!");
				for (int i = 0; i < values.length; i++) {
					Log.d(TAG, "Answer " + i + ": " + values[i]);
				}

				Calendar calendar = Calendar.getInstance();
				Date date = calendar.getTime();
				new DataStoreThread(SurveyActivity.this, date).start();

				onBackPressed();
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "I don't want to take your freaking survey");
//				resetAlarms();
				Calendar calendar = Calendar.getInstance();

				// check which day this survey belongs to
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				// double check that the survey was taken during a valid period
				if (hour < LAST_VALID_HOUR || hour >= FIRST_VALID_HOUR) {
					Log.d(TAG, "Valid survey");
					if (hour < LAST_VALID_HOUR) {
						// if the survey is still valid for yesterday, correct date
						calendar.add(Calendar.DAY_OF_MONTH, -1);
					}
					Date date = calendar.getTime();
					new DataStoreThread(SurveyActivity.this, date, new int[]{-1, -1, -1, -1, -1}).start();
				} else {
					// invalid survey, doesn't belong to anywhere
					// SurveyActivity should had been automatically closed at this point
					Log.d(TAG, "Invalid survey");
				}

				onBackPressed();
			}
		});

		btnLater.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Sigh, I'll take it later okay?... maybe");
				onBackPressed();
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SensitActions.ACTION_CLOSE_SURVEY);
		registerReceiver(closeActivityReceiver, intentFilter);
	}

	@Override
	public void onStop() {
		super.onStop();
		unregisterReceiver(closeActivityReceiver);
	}

	private void resetAlarms() {
		// cancel remaining alarms
		/*AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent alarmSurveyIntent = new Intent(this, OnSurveyAlarmReceiver.class);
		PendingIntent piSurvey = PendingIntent.getBroadcast(SurveyActivity.this, 0, alarmSurveyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.cancel(piSurvey);*/

		/*// set new alarms
		Calendar now = Calendar.getInstance();
		// check is this survey is not

		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		Calendar calendar3 = Calendar.getInstance();
		// 08:00 PM
		calendar1.set(Calendar.HOUR_OF_DAY, 20);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);

		long nextTimeInMillis = calendar1.getTimeInMillis();

//				long offset = 300000;
//				if ((System.currentTimeMillis() - offset) < nextTimeInMillis) {
//					// readjust
//					Calendar calendarNow = Calendar.getInstance();
//					calendar.get(Calendar.HOUR_OF_DAY)
//				}

		Intent alarmSurveyIntent = new Intent(SurveyActivity.this, OnSurveyAlarmReceiver.class);
		PendingIntent piSurvey = PendingIntent.getBroadcast(SurveyActivity.this, 0, alarmSurveyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), 20000, piSurvey);*/
	}

	/*private void setControlsByAnswers() {
		textViews.get(0).setText(String.valueOf(stressValues[0]));
		for (int i = 1; i < 5; i++) {
			textViews.get(i).setText(String.valueOf(likertTextValues[i]));
		}
	}*/

	private void setAnswers() {
		int stressProgress = seekBars.get(0).getProgress();
		textViews.get(0).setText(stressValues[stressProgress]);
		values[0] = stressProgress + 1;
		for (int i = 1; i < values.length; i++) {
			int progress = seekBars.get(i).getProgress();
			textViews.get(i).setText(likertTextValues[progress]);
			values[i] = progress + 1;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
	                              boolean fromUser) {
		// TODO Auto-generated method stub
	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}


	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	private class DataStoreThread extends Thread {
		private Date date;
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		private DBAdapter dbAdapter;
		private int[] values;

		public DataStoreThread(Context context, Date date) {
			this.date = date;
			dbAdapter = new DBAdapter(context);
			values = SurveyActivity.this.values;
		}

		public DataStoreThread(Context context, Date date, int[] values) {
			this(context, date);
			this.values = values;
		}

		@Override
		public void run() {
			Log.d(TAG, "Store survey data");
			dbAdapter.open();
			long inserted = dbAdapter.insertSurvey(values, dateFormat.format(date), 0);
			Log.d(TAG, "Inserted at row ID " + inserted);
			dbAdapter.close();
		}
	}

	public BroadcastReceiver closeActivityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// double-check
			switch (intent.getAction()) {
				case SensitActions.ACTION_CLOSE_SURVEY:
					Log.d(TAG, "Action ACTION_CLOSE_SURVEY received");
					Log.d(TAG, "Closing Survey. 'But why?' you wonder.. Because it took too freaking long.. and I'm tired");
					onBackPressed();
					break;
			}
		}
	};

	//TODO 'Later' needs to set new alarm (depending on the hour)?
}