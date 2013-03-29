package com.at.math;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.at.math.data.Score;
import com.at.math.data.ScoreProcessor;

public class HighScores extends Activity {
	static final int HIGH_SCORE_DIALOG = 0;
	static final int SEND_SCORE_DIALOG = 1;
	static final int CLEAR_SCORES_DIALOG = 2;

	private ListView mScoresList;
	private ScoresListAdapter mListAdapter;

	private ScoreProcessor mScoreProcessor;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.high_scores);
		Typeface tf = Typeface
		.createFromAsset(getAssets(), "fonts/tiza.ttf");
		TextView tv = (TextView)findViewById(R.id.tvHighScores);
		tv.setTypeface(tf);		
		mScoreProcessor = new ScoreProcessor(this);
		mScoresList = (ListView) findViewById(R.id.scores_list);
		mScoresList.setEmptyView(findViewById(R.id.no_high_scores));
		List<Score> scores = mScoreProcessor.getHighScores();
		mListAdapter = new ScoresListAdapter(this, scores);
		mScoresList.setAdapter(mListAdapter);
		//ScoreloopManagerSingleton.get().setOnScoreSubmitObserver(this);
	}
	
	public static boolean isOnline(Context context) {
		// ConnectivityManager is used to check available network(s)
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null) {
			// no network is available
			return false;
		} else {
			// at least one type of network is available
			return true;
		}
	}
	
}
