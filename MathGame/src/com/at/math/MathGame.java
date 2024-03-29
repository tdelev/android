package com.at.math;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.at.math.data.ScoreProcessor;
import com.scoreloop.client.android.ui.LeaderboardsScreenActivity;
import com.scoreloop.client.android.ui.OnScoreSubmitObserver;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;
import com.scoreloop.client.android.ui.ShowResultOverlayActivity;

public class MathGame extends Activity implements OnScoreSubmitObserver {
	static final long NEXT_EXPRESSION_DELAY = 500;
	static final int HIGH_SCORE_DIALOG = 0;
	static final int NEW_GAME_DIALOG = 1;
	static final int DELAY = 2000;
	TextView mTvTime;
	TextSwitcher mTsExpression;
	TextSwitcher mTsSolution;
	TextSwitcher mTsCounter;
	TextSwitcher mTsPoints;
	TextSwitcher mTsCombo;
	TextSwitcher mTsBonusTime;
	TimeMode mGame;
	TimerPieView mTimerPieView;
	String mCurrentSolution;
	Drawable mDrawableOK;
	Drawable mDrawableWrong;
	ImageView mResultImage;
	Animation mFadeIn;
	Animation mFadeOut;
	Typeface mTfTiza;
	Typeface mTfErazer;
	Typeface mTfdoWild;

	Synchronizer mStartSynchronizer;
	Synchronizer mGameSynchronizer;
	StartingCounter mStartingCounter;
	ScoreProcessor mScoreProcessor;
	AlertDialog mHighScoreDialog;
	AlertDialog mNewGameDialog;

	boolean mIsHighScore;
	long mEndGameTime;
	private ProgressDialog mScoreSubmitProgress;
	int gameMode = 0;
	int mCurrentLevel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.counter_view);
		mTfTiza = Typeface.createFromAsset(getAssets(), "fonts/tiza.ttf");
		mTfdoWild = Typeface.createFromAsset(getAssets(), "fonts/idolwild.ttf");
		mTfErazer = Typeface.createFromAsset(getAssets(),
				"fonts/EraserRegular.ttf");
		mTsCounter = (TextSwitcher) findViewById(R.id.tsCounter);
		mCurrentLevel = 1;
		mGame = new TimeMode(mCurrentLevel);
		setCounterFont();
		mStartSynchronizer = new Synchronizer(1000);
		mStartingCounter = new StartingCounter();
		mStartSynchronizer.setCounter(mStartingCounter);
		mStartSynchronizer.addEvent(mStartingEvent);
		mStartSynchronizer.setFinalizer(mStartingFinalizer);

		mGameSynchronizer = new Synchronizer(100);
		mGameSynchronizer.setCounter(mGame);
		mGameSynchronizer.addEvent(mGameEvent);
		mGameSynchronizer.setFinalizer(mGameFinalizer);

		mCurrentSolution = "";
		mDrawableOK = getResources().getDrawable(R.drawable.ok);
		mDrawableWrong = getResources().getDrawable(R.drawable.wrong);

		mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeIn.setFillAfter(true);
		mFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeOut.setFillAfter(true);
		mStartSynchronizer.start();
		mScoreProcessor = new ScoreProcessor(this);
		createDialogs();

		// Set the observer
		ScoreloopManagerSingleton.get().setOnScoreSubmitObserver(this);
		mScoreSubmitProgress = new ProgressDialog(MathGame.this);
		mScoreSubmitProgress.setCancelable(true);
		mScoreSubmitProgress.setMessage("Submitting score to server....");

	}

	void setCounterFont() {
		TextView tv = (TextView) findViewById(R.id.tvCounter1);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvLevel);
		tv.setTypeface(mTfErazer);
		tv.setText(String.format("Level %d", mCurrentLevel));
		tv = (TextView) findViewById(R.id.tvCounter2);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvMessage);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvWorldChallenge);
		tv.setTypeface(mTfTiza);
	}

	void increaseCounterText() {
		TextView tv = (TextView) findViewById(R.id.tvCounter1);
		tv.setTextSize(tv.getTextSize() + 20);
		tv = (TextView) findViewById(R.id.tvCounter2);
		tv.setTextSize(tv.getTextSize() + 20);
	}

	void startLevel() {
		setContentView(R.layout.counter_view);
		mTsCounter = (TextSwitcher) findViewById(R.id.tsCounter);
		setCounterFont();
		mStartingCounter.reset();
		mStartSynchronizer.start();
		mGame = new TimeMode(mCurrentLevel);
		mGameSynchronizer = new Synchronizer(100);
		mGameSynchronizer.setCounter(mGame);
		mGameSynchronizer.addEvent(mGameEvent);
		mGameSynchronizer.setFinalizer(mGameFinalizer);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	void nextExpression() {
		mTsExpression.setText(mGame.nextExpression());
	}

	class StartingCounter implements Synchronizer.Counter {
		int mStartSeconds = 3;

		@Override
		public int tick() {
			return mStartSeconds--;
		}

		public void reset() {
			mStartSeconds = 3;
		}
	}

	private final Synchronizer.Event mStartingEvent = new Synchronizer.Event() {

		@Override
		public void tick(int time) {
			// increaseCounterText();
			mTsCounter.setText(String.format("%d", time));
		}
	};
	private final Synchronizer.Finalizer mStartingFinalizer = new Synchronizer.Finalizer() {
		@Override
		public void doFinalEvent() {
			mStartSynchronizer.abort();
			if (mGame.getLevel() == 1) {
				setContentView(R.layout.game_view);
				initGameView();
				mGameSynchronizer.setCounter(mGame);
				mGameSynchronizer.addEvent(mTimerPieView);
				nextExpression();
				mGameSynchronizer.start();
			}
			if (mGame.getLevel() == 2) {
				setContentView(R.layout.level2);
				initLevel2();
				nextExpression();
				mGameSynchronizer.start();
			}

		}
	};

	void initGameView() {
		setNumPadFont();

		TextView tv = (TextView) findViewById(R.id.tvExp1);
		tv.setTypeface(mTfdoWild);
		tv = (TextView) findViewById(R.id.tvExp2);
		tv.setTypeface(mTfdoWild);
		tv = (TextView) findViewById(R.id.tvSol1);
		tv.setTypeface(mTfdoWild);
		tv = (TextView) findViewById(R.id.tvSol2);
		tv.setTypeface(mTfdoWild);

		tv = (TextView) findViewById(R.id.tvP1);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvP2);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvC1);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvC2);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvBT1);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvBT2);
		tv.setTypeface(mTfErazer);
		mTvTime = (TextView) findViewById(R.id.tvTime);
		mTvTime.setTypeface(mTfErazer);
		mTsExpression = (TextSwitcher) findViewById(R.id.tsExpression);
		mTsSolution = (TextSwitcher) findViewById(R.id.tsSolution);
		mResultImage = (ImageView) findViewById(R.id.ivResult);
		mTsPoints = (TextSwitcher) findViewById(R.id.tsPoints);
		mTsCombo = (TextSwitcher) findViewById(R.id.tsCombo);
		mTsBonusTime = (TextSwitcher) findViewById(R.id.tsBonusTime);
		mTimerPieView = (TimerPieView) findViewById(R.id.timerPie);
		mTimerPieView.setGame(mGame);
	}

	void initLevel2() {
		setNumPadFontLevel2();

		TextView tv = (TextView) findViewById(R.id.tvExp1);
		tv.setTypeface(mTfdoWild);
		tv = (TextView) findViewById(R.id.tvExp2);
		tv.setTypeface(mTfdoWild);

		tv = (TextView) findViewById(R.id.tvP1);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvP2);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvC1);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvC2);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvBT1);
		tv.setTypeface(mTfErazer);
		tv = (TextView) findViewById(R.id.tvBT2);
		tv.setTypeface(mTfErazer);
		mTvTime = (TextView) findViewById(R.id.tvTime);
		mTvTime.setTypeface(mTfErazer);
		mTsExpression = (TextSwitcher) findViewById(R.id.tsExpression);
		mResultImage = (ImageView) findViewById(R.id.ivResult);
		mTsPoints = (TextSwitcher) findViewById(R.id.tsPoints);
		mTsCombo = (TextSwitcher) findViewById(R.id.tsCombo);
		mTsBonusTime = (TextSwitcher) findViewById(R.id.tsBonusTime);
		mTimerPieView = (TimerPieView) findViewById(R.id.timerPie);
		mTimerPieView.setGame(mGame);
	}

	private final Synchronizer.Event mGameEvent = new Synchronizer.Event() {

		@Override
		public void tick(int time) {
			mTvTime.setText(String.format("%.1f", 1.0f * time / 10));
		}
	};
	private final Synchronizer.Finalizer mGameFinalizer = new Synchronizer.Finalizer() {
		@Override
		public void doFinalEvent() {
			mGameSynchronizer.abort();
			mEndGameTime = SystemClock.elapsedRealtime();
			if (mGame.getLevel() == 1) {
				mCurrentLevel++;
				startLevel();
			} else {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						new EndGameTask().execute();
					}
				}, DELAY);
			}
		}
	};

	void initEndView() {
		TextView tv = (TextView) findViewById(R.id.tvTotalPoits);
		tv.setText(String.format("%d", mGame.getPoints()));
		tv.setTypeface(mTfErazer);
		if (mIsHighScore) {
			tv.setTextColor(Color.rgb(0x50, 0xAD, 0x2D));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 72);
		} else {
			tv.setTextColor(Color.rgb(0x33, 0x33, 0x33));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52);
		}
		tv = (TextView) findViewById(R.id.tvGameOver);
		tv.setTypeface(mTfTiza);
		Button btn = (Button) findViewById(R.id.btnSubmit);
		btn.setTypeface(mTfErazer);
		btn = (Button) findViewById(R.id.btnHighScores);
		btn.setTypeface(mTfErazer);
		btn = (Button) findViewById(R.id.btnRetry);
		btn.setTypeface(mTfErazer);
	}

	public void onNumPadClick(View v) {
		final Button b = (Button) v;
		String result = b.getText().toString();
		if (mCurrentLevel == 1) {
			if (result.equals("C")) {
				mCurrentSolution = "";
				mTsSolution.setText(mCurrentSolution);
				return;
			}
			if (result.equals("N")) {
				mCurrentSolution = "";
				mGameSynchronizer.abort();
				showDialog(NEW_GAME_DIALOG);
				return;
			}
			mCurrentSolution += result;
			String correct = mGame.getCorrect();
			if (mCurrentSolution.length() < correct.length()) {
				mTsSolution.setText(mCurrentSolution);
			}
			if (mCurrentSolution.length() == correct.length()) {
				mTsSolution.setText(mCurrentSolution);
				score(mCurrentSolution.equals(correct));
				mCurrentSolution = "";
				mResultImage.startAnimation(mFadeIn);
				mNextExpressionHandler.postDelayed(mNextExpression,
						NEXT_EXPRESSION_DELAY);
			}
		}
		if (mCurrentLevel == 2) {
			mTsExpression.setText(mGame.getExpression().replace("?", result));
			score(result.equals(mGame.getCorrect()));
			mResultImage.startAnimation(mFadeIn);
			mNextExpressionHandler.postDelayed(mNextExpression,
					NEXT_EXPRESSION_DELAY);
		}

	}

	private void score(boolean isCorrect) {
		if (isCorrect) {
			mResultImage.setImageDrawable(mDrawableOK);
			mGame.setCorrect();
			mTsPoints.setText(String.format("%d", mGame.getPoints()));
			if (mGame.getBonusPoints() > 0) {
				mTsCombo.setText(String.format("+%d", mGame.getBonusPoints()));
			} else {
				mTsCombo.setText("");
			}
			if (mGame.getBonusTime() > 0) {
				mTsBonusTime
						.setText(String.format("+%d", mGame.getBonusTime()));
			} else {
				mTsBonusTime.setText("");
			}
		} else {
			mResultImage.setImageDrawable(mDrawableWrong);
			mGame.setWrong();
		}
	}

	final Handler mNextExpressionHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			nextExpression();
			mResultImage.startAnimation(mFadeOut);
			mTsSolution.setText("");
			mCurrentSolution = "";
		}

	};

	final Runnable mNextExpression = new Runnable() {
		@Override
		public void run() {
			mNextExpressionHandler.sendEmptyMessage(0);
		}
	};

	@Override
	public void finish() {
		super.finish();
		if (mStartSynchronizer != null) {
			mStartSynchronizer.abort();
		}
		if (mGameSynchronizer != null) {
			mGameSynchronizer.abort();
		}
	}

	void setNumPadFont() {
		Button btn = (Button) findViewById(R.id.btn0);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn1);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn2);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn3);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn4);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn5);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn6);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn7);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn8);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btn9);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btnC);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btnN);
		btn.setTypeface(mTfTiza);
	}

	void setNumPadFontLevel2() {
		Button btn = (Button) findViewById(R.id.btnPlus);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btnMinus);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btnTimes);
		btn.setTypeface(mTfTiza);
		btn = (Button) findViewById(R.id.btnDivide);
		btn.setTypeface(mTfTiza);
	}

	public void onBtnClick(View v) {
		long span = SystemClock.elapsedRealtime() - mEndGameTime;
		if (span < DELAY) {
			return;
		}
		if (v.getId() == R.id.btnRetry) {
			startLevel();
		}
		if (v.getId() == R.id.btnHighScores) {
			Intent intent = new Intent(this, HighScores.class);
			startActivity(intent);
			finish();
		}
		if (v.getId() == R.id.btnSubmit) {
			mScoreSubmitProgress.show();
			ScoreloopManagerSingleton.get().onGamePlayEnded(
					(double) mGame.getPoints(), gameMode);
		}
	}

	private class EndGameTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mIsHighScore = mScoreProcessor.isHighScore(mGame.getPoints());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setContentView(R.layout.endgame_view);
			initEndView();
			if (mIsHighScore) {
				String name = GamePrefs.getInstance(MathGame.this)
						.getLastSavedName();
				if (!name.equals("")) {
					mName.setText(name);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				showDialog(HIGH_SCORE_DIALOG);
			}
			super.onPostExecute(result);

		}

	}

	private class SaveScoreTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String name = mName.getText().toString();
			if (name.length() <= 0) {
				name = getString(R.string.unknown);
			}
			if (name.length() > 20) {
				name = name.substring(0, 20);
			}
			mScoreProcessor.addHighScore(name, mGame.getPoints());
			GamePrefs.getInstance(MathGame.this).setName(name);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setContentView(R.layout.endgame_view);
			initEndView();
			super.onPostExecute(result);

		}
	}

	EditText mName;

	private void createDialogs() {
		final View highScoreView = LayoutInflater.from(this).inflate(
				R.layout.new_high_score, null);
		mName = (EditText) highScoreView.findViewById(R.id.name);

		mHighScoreDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.new_high_score)
				.setView(highScoreView)
				.setPositiveButton(R.string.save,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								new SaveScoreTask().execute();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		mNewGameDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.start_new_game)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								startLevel();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								mGameSynchronizer.start();
							}
						}).create();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == HIGH_SCORE_DIALOG) {
			return mHighScoreDialog;
		}
		if (id == NEW_GAME_DIALOG) {
			return mNewGameDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onScoreSubmit(int status, Exception error) {
		if (null != mScoreSubmitProgress && mScoreSubmitProgress.isShowing())
			mScoreSubmitProgress.dismiss();
		Intent leaderBoardIntent = new Intent(this,
				LeaderboardsScreenActivity.class);
		leaderBoardIntent.putExtra(LeaderboardsScreenActivity.LEADERBOARD,
				LeaderboardsScreenActivity.LEADERBOARD_24h);
		// startActivity(new Intent(this, ShowResultOverlayActivity.class));
		startActivity(leaderBoardIntent);
		finish();

	}

}