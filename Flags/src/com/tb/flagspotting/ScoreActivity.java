package com.tb.flagspotting;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class ScoreActivity extends Activity {
	public static final String SCORE = "score";
	public static final String FACT_COUNTRY = "fc";
	private static final long DELAY = 1500;
	public static final int EASY = 0;
	public static final int MEDIUM = 1;
	public static final int HARD = 2;
	private int mFinalPoints;
	TextView mTvPoints;
	long mTimeEntered;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score);
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/gong.ttf");
		Bundle bundle = getIntent().getExtras();
		mFinalPoints = bundle.getInt(SCORE);
		TextView tv = (TextView) findViewById(R.id.score);
		tv.setText(String.format("%d", mFinalPoints));
		tv.setTypeface(tf);
		String factCountry = bundle.getString(FACT_COUNTRY);
		String[] list = getResources().getStringArray(R.array.hard);
		String fact = null;
		if (factCountry == null) {
			Random r = new Random();
			String[] fl = getResources().getStringArray(R.array.hard_facts);
			final int index = r.nextInt(list.length);
			fact = fl[index];
			fl = getResources().getStringArray(R.array.hard);
			factCountry = fl[index];
		} else {
			int found = HARD;
			int index = -1;
			index = findIndex(list, factCountry);
			if (index == -1) {
				list = getResources().getStringArray(R.array.medium);
				index = findIndex(list, factCountry);
				found = MEDIUM;
			}
			if (index == -1) {
				list = getResources().getStringArray(R.array.easy);
				index = findIndex(list, factCountry);
				found = EASY;
			}
			if (found == EASY) {
				fact = getResources().getStringArray(R.array.easy_facts)[index];
			}
			if (found == MEDIUM) {
				fact = getResources().getStringArray(R.array.medium_facts)[index];
			}
			if (found == HARD) {
				fact = getResources().getStringArray(R.array.hard_facts)[index];
			}
		}
		fact = fact.replace("%%", "%");
		tv = (TextView) findViewById(R.id.fact);
		tv.setText(fact);
		String name = factCountry.toLowerCase().replace(" ", "_");
		int d = getResources()
				.getIdentifier(name, "drawable", "com.tb.flags");
		Drawable drawable = getResources().getDrawable(d);
		ImageView iv = (ImageView) findViewById(R.id.ivFlag);
		iv.setImageDrawable(drawable);
		mTimeEntered = SystemClock.elapsedRealtime();
		
		//AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });

		//AdView view = (AdView) findViewById(R.id.ad);
		//view.requestFreshAd();
	}

	private int findIndex(String[] list, String key) {
		for (int i = 0; i < list.length; i++) {
			if (key.equals(list[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		long span = SystemClock.elapsedRealtime() - mTimeEntered;
		if(span < DELAY) {
			return true;
		}
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			finish();
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void finish() {
		super.finish();
		Bundle bun = new Bundle();
		bun.putInt(SCORE, mFinalPoints);
		Intent scoreIntent = new Intent(this, HighScores.class);
		scoreIntent.putExtras(bun);
		startActivity(scoreIntent);
	}

}
