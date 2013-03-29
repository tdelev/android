package com.at.math;

public class TimeMode implements Synchronizer.Counter {
	private static final int GAME_TIME = 60; // 60 seconds
	private static final int POINTS = 100;
	private static final int BONUS_POINTS = 50;
	private static final int BONUS_TIME = 50; // 5 seconds
	private static final int POINTS_HITS = 5;
	private static final int TIME_HITS = 20;
	private static final int TIME = 30;

	private int mLevel;
	private int mTotalTime;
	private int mRemainingTime;
	private int mTimeGenerated;
	private int mPoints;
	private int mConsecutiveHits;
	private int mBonusPoints;
	private int mBonusTime;

	private MathExpression mExpression;
	private String mExpressionString;

	public TimeMode(int level) {
		mTotalTime = GAME_TIME;
		mLevel = level;
		mRemainingTime = mTotalTime;
		mPoints = 0;
		mConsecutiveHits = 0;
		mExpression = new MathExpression();
	}

	public String nextExpression() {
		mTimeGenerated = mRemainingTime;
		mExpressionString = mExpression.generateRandom(mLevel);
		return mExpressionString;
	}
	
	public String getExpression() {
		return mExpressionString;
	}
	
	public int getLevel() {
		return mLevel;
	}
	
	public String getCorrect() {
		if(mLevel == 1) {
			return String.valueOf(mExpression.getResult());
		}
		if(mLevel == 2) {
			return mExpression.getOperator();
		}
		return null;
	}
	
	
	public void setCorrect() {
		mConsecutiveHits++;
		// As fast you solve the problem more points you get
		int points = POINTS - (mTimeGenerated - mRemainingTime);
		if (points <= POINTS / 4) {
			mPoints += POINTS / 4;
		} else {
			mPoints += points;
		}

		if (mConsecutiveHits > 0 && mConsecutiveHits % POINTS_HITS == 0) {
			int m = mConsecutiveHits / POINTS_HITS;
			mBonusPoints =m * m * BONUS_POINTS; 
			mPoints += mBonusPoints;
		}else {
			mBonusPoints = 0;
		}
		if (mConsecutiveHits > 0 && mConsecutiveHits % TIME_HITS == 0) {
			int m = mConsecutiveHits / TIME_HITS;
			// To stop theoretically unlimited game time
			if (m <= 3) {
				mBonusTime = m * BONUS_TIME;
				mRemainingTime += mBonusTime;
			}
		}else {
			mBonusTime = 0;
		}
	}

	public void setWrong() {
		mConsecutiveHits = 0;
	}

	public int getPoints() {
		return mPoints;
	}
	
	public int getBonusPoints() {
		return mBonusPoints;
	}

	public int getBonusTime() {
		return mBonusTime / 10;
	}
	
	public float getTimeRatio() {
		return mRemainingTime * 1.0f / mTotalTime;
	}

	@Override
	public int tick() {
		mRemainingTime--;
		return mRemainingTime;
	}

}
