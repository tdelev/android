package org.feit.jokesmk;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class JokesMK extends Activity {
	private static final String TAG = "JokesMK";
	private List<Joke> mJokes;
	private JokesProcessor mJokesProcessor;
	private TextView mJokeId;
	private TextView mName;
	private TextView mText;
	private int mTotal;
	private int mCurrentIndex;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		Log.d(TAG, "before jokes precessor");
		mJokesProcessor = new JokesProcessor(this);
		mJokeId = (TextView) findViewById(R.id.jokes_id);
		mName = (TextView) findViewById(R.id.name);
		mText = (TextView) findViewById(R.id.text);
		mCurrentIndex = 0;
	}

	
	private void loadJoke() {
		Joke joke = mJokes.get(mCurrentIndex);
		mJokeId.setText("ID: " + joke.id);
		mName.setText(joke.name);
		mText.setText(joke.text);
	}

	public void onNextPrev(View v) {
		if (v.getId() == R.id.btnNext) {
			mCurrentIndex++;
			if (mCurrentIndex >= mTotal) {
				mCurrentIndex = 0;
			}
		}
		if (v.getId() == R.id.btnPrev) {
			mCurrentIndex--;
			if (mCurrentIndex < 0) {
				mCurrentIndex = mTotal - 1;
			}
		}
		loadJoke();
	}
}