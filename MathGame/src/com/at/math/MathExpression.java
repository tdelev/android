package com.at.math;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class MathExpression {	
	
	private int mNumA;
	private int mNumB;
	private Operator mOperator;
	private int mResult;
	private Set<String> mGenerated;
	private Random mRandom;

	public enum Operator {
		ADDITION, SUBSTRACTION, MULTIPLICATION, DIVISION,
	}

	public MathExpression() {
		mNumA = 0;
		mNumB = 0;
		mResult = 0;
		mRandom = new Random();
		mGenerated = new HashSet<String>();
	}
	
	public String generateRandom(int level) {
		int op = mRandom.nextInt(100);
		Operator operator = Operator.values()[op / 25];
		generate(operator, 10);
		String exp = null;
		if(level == 1) {
			if(operator == Operator.ADDITION) {
				exp = String.format("%d + %d = ", mNumA, mNumB);				
			}
			if(operator == Operator.SUBSTRACTION) {
				exp = String.format("%d + %d = ", mNumA, mNumB);				
			}
			if(operator == Operator.MULTIPLICATION) {
				exp = String.format("%d x %d = ", mNumA, mNumB);
			}
			if(operator == Operator.DIVISION) {
				exp = String.format("%d / %d = ", mNumA, mNumB);
			}
		}
		if(level == 2) {
			exp = String.format("%d ? %d = %d", mNumA, mNumB, mResult);				
		}
		return exp;
	}

	public void generate(Operator operator, int maxNumber) {
		
		mNumA = mRandom.nextInt(maxNumber) + 1;
		mNumB = mRandom.nextInt(maxNumber) + 1;
		String exp = null;
		if (mNumB > mNumA) {
			mNumA = mNumB + mNumA;
			mNumB = mNumA - mNumB;
			mNumA = mNumA - mNumB;
		}
		if(operator == Operator.ADDITION) {
			mOperator = Operator.ADDITION;
			mResult = mNumA + mNumB;
			exp = String.format("%d + %d = ", mNumA, mNumB);
		}
		if(operator == Operator.SUBSTRACTION) {
			mOperator = Operator.SUBSTRACTION;
			mResult = mNumA - mNumB;
			exp = String.format("%d - %d = ", mNumA, mNumB);
		}
		if(operator == Operator.MULTIPLICATION) {
			mOperator = Operator.MULTIPLICATION;
			if (mNumB == 1) {
				mNumB = 2 + mRandom.nextInt(8);
			}
			mResult = mNumA * mNumB;
			exp = String.format("%d x %d = ", mNumA, mNumB);
		}
		if(operator == Operator.DIVISION) {
			mOperator = Operator.DIVISION;
			if (mNumB == 1) {
				mNumB = 2 + mRandom.nextInt(8);
				if (mNumB > mNumA) {
					mNumA = mNumB + mNumA;
					mNumB = mNumA - mNumB;
					mNumA = mNumA - mNumB;
				}
			}
			mResult = mNumA / mNumB;
			int mode = mNumA % mNumB;
			if (mode > 0) {
				mNumA -= mode;
			}
			exp = String.format("%d / %d = ", mNumA, mNumB);
		}
		if(mGenerated.contains(exp)) {
			generate(operator, maxNumber);
		}else {
			mGenerated.add(exp);
		}
	}

	public int[] getOption(int optNum) {
		// int option[] = {1,2,3,4};

		int option[] = new int[optNum];
		Set<Integer> set = new HashSet<Integer>();

		int rightOption = mRandom.nextInt(optNum);

		while (set.size() < optNum) {
			int diff = mRandom.nextInt(9) + 1;

			if (mRandom.nextBoolean() && diff < mResult) {
				set.add(mResult - diff);
			} else {
				set.add(mResult + diff);
			}
		}

		Iterator<Integer> it = set.iterator();
		int i = 0;
		while (it.hasNext()) {
			int val = it.next();
			if (i == rightOption) {
				option[i] = mResult;
			} else {
				option[i] = val;
			}
			i++;
		}

		return option;
	}

	public boolean isCorrect(int choice) {
		if (choice == mResult) {
			return true;
		}
		return false;
	}
	
	public int getResult() {
		return mResult;
	}
	
	public String getOperator() {
		if(mOperator == Operator.ADDITION) {
			return "+";
		}
		if(mOperator == Operator.SUBSTRACTION) {
			return "-";
		}
		if(mOperator == Operator.MULTIPLICATION) {
			return "x";
		}
		if(mOperator == Operator.DIVISION) {
			return "/";
		}
		return null;
	}
}
