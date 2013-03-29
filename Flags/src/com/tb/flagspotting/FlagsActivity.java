package com.tb.flagspotting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.tb.flagspotting.FlagsGame.GameStage;
import com.tb.flagspotting.FlagsGame.GameStatus;

public class FlagsActivity extends Activity implements Synchronizer.Finalizer,
		Synchronizer.Event {
	private static final String TAG = "FlagsActivity";
	static final int CLICK_SPAN = 1500;

	ImageView[] mFlags;
	Drawable mDrawables[];
	String[] mCountries;

	boolean mSoundOn;
	SoundPool mSoundPool;
	MediaPlayer mMediaPlayer;
	int mSoundOk;
	int mSoundWrong;
	AudioManager mAudioManager;
	Animation mFadeIn;
	Animation mFadeOut;
	Animation mOkFadeIn;
	Animation mOkFadeOut;
	Animation mBonusTimeFadeIn;
	TimerBarView mTimerBarView;
	FlagsGame mGame;
	Synchronizer mSynch;
	Synchronizer mSynchStart;
	TextView mTvName;
	TextSwitcher mTvPoints;
	TextSwitcher mTvCombo;

	TextSwitcher mTvCenterText;
	TextView mTvTime;
	int mClicked;
	long mPreviousClick;
	GameStage mCurrentStage;
	boolean mFinished;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flags);
		Typeface tfGong = Typeface.createFromAsset(getAssets(),
				"fonts/gong.ttf");
		Typeface tfAnudrg = Typeface.createFromAsset(getAssets(),
				"fonts/anudrg.ttf");

		mFlags = new ImageView[4];
		mFlags[0] = (ImageView) findViewById(R.id.iv1);
		mFlags[1] = (ImageView) findViewById(R.id.iv2);
		mFlags[2] = (ImageView) findViewById(R.id.iv3);
		mFlags[3] = (ImageView) findViewById(R.id.iv4);
		mTvName = (TextView) findViewById(R.id.tvName);
		mTvPoints = (TextSwitcher) findViewById(R.id.tsPoints);
		mTvCombo = (TextSwitcher) findViewById(R.id.tsBonus);
		mTvCenterText = (TextSwitcher) findViewById(R.id.tsCenter);
		mTvTime = (TextView) findViewById(R.id.tvTime);
		mTimerBarView = (TimerBarView) findViewById(R.id.timer_view);
		TextView tv = (TextView) findViewById(R.id.p1);
		tv.setTypeface(tfGong);
		tv = (TextView) findViewById(R.id.p2);
		tv.setTypeface(tfGong);
		tv = (TextView) findViewById(R.id.b1);
		tv.setTypeface(tfGong);
		tv = (TextView) findViewById(R.id.b2);
		tv.setTypeface(tfGong);
		tv = (TextView) findViewById(R.id.bs1);
		tv.setTypeface(tfGong);
		tv = (TextView) findViewById(R.id.bs2);
		tv.setTypeface(tfGong);
		mTvName.setTypeface(tfAnudrg);
		mTvTime.setTypeface(tfGong);
		mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mBonusTimeFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeIn.setFillAfter(true);
		mFadeIn.setAnimationListener(mOnFadeIn);
		mFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeOut.setFillAfter(true);
		mFadeOut.setAnimationListener(mOnFadeOut);

		mOkFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mOkFadeOut.setFillAfter(true);
		mOkFadeOut.setAnimationListener(mOnOkFadeOut);
		mOkFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mOkFadeIn.setFillAfter(true);
		mOkFadeIn.setAnimationListener(mOnOkFadeIn);
		mDrawables = new Drawable[4];
		

		mSoundOn = GamePrefs.getInstance(this).getSoundOn();
		Log.d(TAG, "onCreate");
		if (savedInstanceState != null) {
			Log.d(TAG, "restoring instance state");
			try {
				restoreGame(savedInstanceState);
			} catch (Exception e) {
				Log.e(TAG, "error restoring state", e);
			}
			return;
		}
		String action = getIntent().getAction();
		if (action.equals("com.tb.flags.action.CONTINUE")) {
			Log.d(TAG, "resuming game");
			restoreGame();
		} else if (action.equals("com.tb.flags.action.NEW_GAME")) {
			Log.d(TAG, "starting new game");
			prepareNewGame();
		} else {
			Log.d(TAG, "Whoa there, friend!");
		}

		if (mSoundOn) {
			initSoundPool(this);
			prepareLoopMusic();
		}
	}

	private void prepareLoopMusic() {
		mMediaPlayer = MediaPlayer.create(this, R.raw.loop_sound);
		mMediaPlayer.setLooping(true);
		float streamVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (streamVolume != 0) {
			mMediaPlayer.setVolume(0.6f, 0.6f);
		} else {
			mMediaPlayer.setVolume(streamVolume, streamVolume);
		}

	}

	private void initSoundPool(Context c) {
		mAudioManager = (AudioManager) c
				.getSystemService(Context.AUDIO_SERVICE);
		mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);
		mSoundOk = mSoundPool.load(c, R.raw.ok, 1);
		mSoundWrong = mSoundPool.load(c, R.raw.wrong, 1);
	}

	private void prepareNewGame() {
		mPreviousClick = -1;
		mGame = new FlagsGame(this);
		mTimerBarView.setGame(mGame);
		mTvPoints.setVisibility(View.INVISIBLE);
		mTvPoints.setText(String.valueOf(mGame.getPoints()));
		mTvCombo.setText("");
		setStartCounter();
		loadStage();
	}

	private void setStartCounter() {
		mSynchStart = new Synchronizer(1000);
		mSynchStart.setCounter(mStartingCounter);
		mSynchStart.addEvent(mStartingEvent);
		mSynchStart.setFinalizer(mStartingFinalizer);
	}

	private void startNewGame() {
		mGame.start();
		mTvCenterText.setText("");
		mSynchStart.abort();
		if (mSoundOn) {
			mMediaPlayer.start();
		}
		mTvPoints.setVisibility(View.VISIBLE);
		mSynch = new Synchronizer();
		mSynch.setCounter(mGame);
		mSynch.addEvent(mTimerBarView);
		mSynch.addEvent(this);
		mSynch.setFinalizer(this);
		mSynch.start();
		setCountryName();
		setFlags();
	}

	private void restoreGame() {
		SharedPreferences prefs = GamePrefs.getInstance(this).getPrefs();
		mGame = new FlagsGame(this);
		mGame.load(prefs);
		mTimerBarView.setGame(mGame);
		resumeGame();
	}

	private void resumeGame() {
		mSynch = new Synchronizer();
		mSynch.setCounter(mGame);
		mSynch.addEvent(mTimerBarView);
		mSynch.addEvent(this);
		mSynch.setFinalizer(this);
		mSynch.start();
		loadStage();
		setFlags();
		setCountryName();
		showPoints();
	}

	private void restoreGame(Bundle bun) {
		mGame = new FlagsGame(this);
		mGame.load(bun);
		mTimerBarView.setGame(mGame);
		resumeGame();
	}

	private void playLoop(int soundId) {
		if (mSoundPool != null && mSoundOn) {
			float streamVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (streamVolume != 0) {
				if (soundId == mSoundOk) {
					mSoundPool.play(soundId, 0.8f, 0.8f, 1, 0, 1f);
				} else {
					mSoundPool.play(soundId, 1f, 1f, 1, 0, 1f);
				}
			} else {
				if (soundId == mSoundOk) {
					mSoundPool.play(soundId, streamVolume - 0.2f,
							streamVolume - 0.2f, 1, 0, 1f);
				} else {
					mSoundPool.play(soundId, streamVolume, streamVolume, 1, 0,
							1f);
				}
			}
		}
	}

	private void setFlags() {
		for (int i = 0; i < 4; i++) {
			mFlags[i].setImageDrawable(mDrawables[i]);
		}
	}

	private void setCountryName() {
		mTvName.setText(mCurrentStage.getCorrectCountry());
	}

	private final AnimationListener mOnFadeOut = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (mClicked == mCurrentStage.correctIndex) {
				playLoop(mSoundOk);
				mFlags[mClicked].setImageResource(R.drawable.ok);
			} else {
				playLoop(mSoundWrong);
				mFlags[mClicked].setImageResource(R.drawable.wrong);
			}
			loadStage();
			setCountryName();
			mFlags[mClicked].startAnimation(mOkFadeIn);
		}
	};

	private final AnimationListener mOnFadeIn = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			setFlags();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
		}
	};

	private final AnimationListener mOnOkFadeOut = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			setFlags();
			for (int i = 0; i < 4; i++) {
				mFlags[i].startAnimation(mFadeIn);
			}
		}
	};

	private final AnimationListener mOnOkFadeIn = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mFlags[mClicked].startAnimation(mOkFadeOut);
			mTvCenterText.setText("");
		}
	};

	public void onFlagClick(View v) {
		long time = SystemClock.elapsedRealtime();
		if (mPreviousClick > 0 && (time - mPreviousClick) < CLICK_SPAN) {
			return;
		}
		mPreviousClick = time;
		for (int i = 0; i < 4; i++) {
			if (v.getId() == mFlags[i].getId()) {
				mClicked = i;
				mGame.setChoice(mClicked);
				showPoints();
			}

		}
		for (int i = 0; i < 4; i++) {
			mFlags[i].startAnimation(mFadeOut);
		}
	}

	private void showPoints() {
		mTvPoints.setText(String.valueOf(mGame.getPoints()));
		if (mGame.getCombo() > 0) {
			mTvCombo.setText(String.format("+%d", mGame.getCombo()));
		} else {
			mTvCombo.setText("");
		}
		if (mGame.getBonusSeconds() > 0) {
			mTvCenterText.setText(String.format("+%d sec", mGame
					.getBonusSeconds()));
		}
	}

	private void loadStage() {
		mCurrentStage = mGame.getStage();
		loadFlags();
	}

	private void loadFlags() {
		for (int i = 0; i < mCurrentStage.countries.length; i++) {
			String name = mCurrentStage.countries[i].toLowerCase().replace(" ",
					"_");
			int d = getResources().getIdentifier(name, "drawable",
					"com.tb.flags");
			mDrawables[i] = getResources().getDrawable(d);
			
		}
	}

	@Override
	public void doFinalEvent() {
		mSynch.abort();
		// mFinalPoints.setText(String.valueOf(mGame.getPoints()));
		// mEndGameDialog.show();
		GamePrefs.getInstance(this).clearSavedGame();
		mGame.start();
		Bundle bun = new Bundle();
		bun.putInt(ScoreActivity.SCORE, mGame.getPoints());
		bun.putString(ScoreActivity.FACT_COUNTRY, mGame.getLastWrong());
		Intent scoreIntent = new Intent(this, ScoreActivity.class);
		scoreIntent.putExtras(bun);
		startActivity(scoreIntent);
		finish();
	}

	private void saveGame() {
		if (mGame.getStatus() == GameStatus.GAME_RUNNING) {
			SharedPreferences prefs = GamePrefs.getInstance(this).getPrefs();
			mGame.pause();
			mGame.save(prefs.edit());
		}
	}

	private void saveGame(Bundle state) {
		if (mGame.getStatus() == GameStatus.GAME_RUNNING) {
			mGame.pause();
			mGame.save(state);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		if (mSynch != null && !mFinished) {
			mSynch.abort();
			if (mSoundOn) {
				mMediaPlayer.pause();
			}
		}
		saveGame();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if (mGame.getStatus() == GameStatus.GAME_STARTING) {
			mSynchStart.start();
		}
		if (mGame.getStatus() == GameStatus.GAME_PAUSED) {
			if(!mSynch.isRunning()) {
				mSynch.start();				
			}
			mGame.resume();
			if (mSoundOn) {
				mMediaPlayer.start();
			}
		}
	}

	@Override
	public void finish() {
		super.finish();
		Log.d(TAG, "onFinish");
		if (mSynch != null) {
			mSynch.abort();
		}
		if (mSynchStart != null) {
			mSynchStart.abort();
		}
		if (mSoundOn) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
		}
		mFinished = true;
	}

	@Override
	public void tick(int time) {
		mTvTime.setText(String.format("%.1f", time * 1.0 / 10));
	}

	private final Synchronizer.Counter mStartingCounter = new Synchronizer.Counter() {
		int mTimeToStart = 3;

		@Override
		public int tick() {
			return mTimeToStart--;
		}
	};
	private final Synchronizer.Event mStartingEvent = new Synchronizer.Event() {

		@Override
		public void tick(int time) {
			mTvCenterText.setText(String.format("%d", time));
		}
	};
	private final Synchronizer.Finalizer mStartingFinalizer = new Synchronizer.Finalizer() {
		@Override
		public void doFinalEvent() {
			startNewGame();
		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
		saveGame(outState);
	}

}