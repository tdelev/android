package com.tb.flagspotting;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class FlagsGame implements Synchronizer.Counter {
	private static final int STAGE_COUNTRIES = 4;
	private static final int TIME = 60; // seconds
	private static final int HIT_POINTS = 100;
	private static final int COMBO = 100;
	private static final int COMBO_TIME = 30;

	private static final String ROUND = "round";
	private static final String CONSECUTIVE_HITS = "hits";
	private static final String POINTS = "points";
	private static final String TIME_REMAINING = "time";
	private static final String GUESSED = "guessed";
	public static final String ACTIVE = "active";
	private String[] mEasy;
	private String[] mMedium;
	private String[] mHard;
	private Random mRandom;
	private Set<String> mGuessed;
	private String[] mCountriesList;
	private int mRound;
	private int mPoints;
	private int mConsecutiveHits;
	private int mCombo;
	private int mBonusSeconds;
	private int mMaxTimeRemaining;
	private int mTimeRemaining;
	private int mTimeNew;
	private GameStatus mStatus;
	private GameStage mCurrentStage;
	private String mLastWrong;

	public enum GameStatus {
		GAME_STARTING, GAME_RUNNING, GAME_PAUSED, GAME_FINISHED
	};

	public FlagsGame(Context context) {
		mStatus = GameStatus.GAME_STARTING;
		mEasy = context.getResources().getStringArray(R.array.easy);
		mMedium = context.getResources().getStringArray(R.array.medium);
		mHard = context.getResources().getStringArray(R.array.hard);
		mRandom = new Random(System.currentTimeMillis());
		mGuessed = new HashSet<String>();
		mRound = 1;
		mPoints = 0;
		mConsecutiveHits = 0;
		mMaxTimeRemaining = TIME * 10;
		mTimeRemaining = mMaxTimeRemaining;
		mCountriesList = new String[STAGE_COUNTRIES];
	}

	public void save(Editor editor) {
		editor.putInt(ROUND, mRound);
		editor.putInt(POINTS, mPoints);
		editor.putInt(CONSECUTIVE_HITS, mConsecutiveHits);
		editor.putInt(TIME_REMAINING, mTimeRemaining);
		editor.putString(GUESSED, getGuessed());
		editor.putBoolean(ACTIVE, true);
		editor.commit();
	}
	
	private String getGuessed() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = mGuessed.iterator();
		while(iter.hasNext()) {
			sb.append(iter.next());
			if(iter.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	private void loadGuessed(String guessed) {
		String[] list = guessed.split(",");
		for(String s : list) {
			mGuessed.add(s);
		}
	}
	
	public void save(Bundle bundle) {
		bundle.putInt(ROUND, mRound);
		bundle.putInt(POINTS, mPoints);
		bundle.putInt(CONSECUTIVE_HITS, mConsecutiveHits);
		bundle.putInt(TIME_REMAINING, mTimeRemaining);
		bundle.putString(GUESSED, getGuessed());
		bundle.putBoolean(ACTIVE, true);
	}

	public void load(SharedPreferences prefs) {
		mRound = prefs.getInt(ROUND, 0);
		mPoints = prefs.getInt(POINTS, 0);
		mConsecutiveHits = prefs.getInt(CONSECUTIVE_HITS, 0);
		mTimeRemaining = prefs.getInt(TIME_REMAINING, 0);
		mTimeNew = mTimeRemaining;
		loadGuessed(prefs.getString(GUESSED, ""));
		mStatus = GameStatus.GAME_PAUSED;
	}
	
	public void load(Bundle bundle) {
		mRound = bundle.getInt(ROUND, 0);
		mPoints = bundle.getInt(POINTS, 0);
		mConsecutiveHits = bundle.getInt(CONSECUTIVE_HITS, 0);
		mTimeRemaining = bundle.getInt(TIME_REMAINING, 0);
		mTimeNew = mTimeRemaining;
		loadGuessed(bundle.getString(GUESSED));
		mStatus = GameStatus.GAME_PAUSED;
	}	

	public boolean start() {
		if (mStatus != GameStatus.GAME_STARTING) {
			return true;
		}
		mStatus = GameStatus.GAME_RUNNING;
		return true;
	}

	public void pause() {
		if (mStatus == GameStatus.GAME_RUNNING)
			mStatus = GameStatus.GAME_PAUSED;
	}

	public void resume() {
		mStatus = GameStatus.GAME_RUNNING;
	}

	private String[] getNext() {

		int i = 0;
		Set<String> used = new HashSet<String>();
		boolean notGuessed = false;
		int left = STAGE_COUNTRIES;
		while (left > 0) {
			String country = null;
			if (mRound <= 5) {
				country = getCountry(mEasy);
			}
			if (mRound > 5 && mRound <= 10) {
				if (left > 2) {
					country = getCountry(mEasy);
				} else {
					country = getCountry(mMedium);
				}
			}
			if (mRound > 10 && mRound <= 15) {
				if (left > 1) {
					country = getCountry(mEasy);
				} else {
					country = getCountry(mMedium);
				}
			}
			if (mRound > 15 && mRound <= 20) {
				country = getCountry(mMedium);
			}
			if (mRound > 20 && mRound <= 25) {
				if (left > 1) {
					country = getCountry(mMedium);
				} else {
					country = getCountry(mHard);
				}
			}
			if (mRound > 25) {
				country = getCountry(mHard);

			}
			if (used.contains(country)) {
				continue;
			}
			if (!notGuessed) {
				if (mGuessed.contains(country)) {
					continue;
				} else {
					notGuessed = true;
				}
			}
			used.add(country);
			mCountriesList[i++] = country;
			left--;
		}
		mRound++;
		mTimeNew = mTimeRemaining;
		return mCountriesList;
	}

	private int getCorrect() {
		int correct = mRandom.nextInt(4);
		String currentCountry = mCountriesList[correct];
		if (!mGuessed.contains(currentCountry)) {
			mGuessed.add(currentCountry);
			return correct;
		}
		return getCorrect();
	}

	public void setChoice(int choice) {
		if (choice == mCurrentStage.correctIndex) {
			mConsecutiveHits++;
			int points = HIT_POINTS - (mTimeNew - mTimeRemaining);
			if (points < HIT_POINTS / 5) {
				points = HIT_POINTS / 5;
			}
			mPoints += points;
		} else {
			mLastWrong = mCurrentStage.getCorrectCountry();
			mConsecutiveHits = 0;
		}
		if (mConsecutiveHits > 0 && mConsecutiveHits % 5 == 0) {
			int m = mConsecutiveHits / 5;
			mCombo = m * m * COMBO;
		} else {
			mCombo = 0;
		}
		if (mConsecutiveHits > 0 && mConsecutiveHits % 10 == 0) {
			int m = mConsecutiveHits / 10;
			mBonusSeconds = m * COMBO_TIME;
			if(m > 5) {
				m = 0;
			}
			mTimeRemaining += m * COMBO_TIME;
		} else {
			mBonusSeconds = 0;
		}
		mPoints += mCombo;
	}

	public int getPoints() {
		return mPoints;
	}

	public int getCombo() {
		return mCombo;
	}

	public int getBonusSeconds() {
		return mBonusSeconds / 10;
	}

	private String getCountry(String[] list) {
		return list[mRandom.nextInt(list.length)];
	}

	public int getMaxTimeRemaining() {
		return mMaxTimeRemaining;
	}
	
	public String getLastWrong() {
		return mLastWrong;
	}

	@Override
	public int tick() {
		mTimeRemaining--;
		if(mTimeRemaining <=0) {
			mStatus = GameStatus.GAME_FINISHED;
		}
		return mTimeRemaining;
	}

	public GameStatus getStatus() {
		return mStatus;
	}
	
	public GameStage getStage() {
		mCurrentStage = new GameStage();
		mCurrentStage.countries = getNext();
		mCurrentStage.correctIndex = getCorrect();
		return mCurrentStage;
	}
	
	static final class GameStage { 
		public GameStage() {
			countries = new String[STAGE_COUNTRIES];
		}
		public int correctIndex;
		public String[] countries;
		public String getCorrectCountry() {
			return countries[correctIndex];		
		}
	}
}
