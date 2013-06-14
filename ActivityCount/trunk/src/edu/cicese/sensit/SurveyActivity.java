package edu.cicese.sensit;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.ArrayList;

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

		int stressProgress = sbStress.getProgress();
		tvStress.setText(stressValues[stressProgress]);
		values[0] = stressProgress + 1;
		for (int i = 1; i < values.length; i++) {
			int progress = seekBars.get(i).getProgress();
			textViews.get(i).setText(likertTextValues[progress]);
			values[i] = progress + 1;
		}

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
			public void onStopTrackingTouch(SeekBar seekBar) {}

			public void onStartTrackingTouch(SeekBar seekBar) {}

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

			public void onStopTrackingTouch(SeekBar seekBar) {}

			public void onStartTrackingTouch(SeekBar seekBar) {}

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
				Log.d(TAG, "Yei!");
				for (int i = 0; i < values.length; i++) {
//					int answer = stringAnswers[i];
					Log.d(TAG, "Answer " + i + ": " + values[i]);
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "I don't want to take your freaking survey");
				SurveyActivity.super.onBackPressed();
			}
		});

		btnLater.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Sigh, I'll take it later... maybe");
				SurveyActivity.super.onBackPressed();
			}
		});

	}

	private void setControlsByAnswers() {
		textViews.get(0).setText(String.valueOf(stressValues[0]));
		for (int i = 1; i < 5; i++) {
			textViews.get(i).setText(String.valueOf(likertTextValues[i]));
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
}