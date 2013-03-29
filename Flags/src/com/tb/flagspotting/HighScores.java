package com.tb.flagspotting;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scoreloop.client.android.ui.OnScoreSubmitObserver;
import com.scoreloop.client.android.ui.PostScoreOverlayActivity;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;
import com.scoreloop.client.android.ui.ShowResultOverlayActivity;
import com.tb.flagspotting.data.Score;
import com.tb.flagspotting.data.ScoreProcessor;

public class HighScores extends Activity implements OnScoreSubmitObserver  {
	static final int HIGH_SCORE_DIALOG = 0;
	static final int SEND_SCORE_DIALOG = 1;
	static final int CLEAR_SCORES_DIALOG = 2;
	private static final int SHOW_RESULT = 0;
	private static final int POST_SCORE = 1;

	private ListView mScoresList;
	private ScoresListAdapter mListAdapter;
	private AlertDialog mHighScoreDialog;
	private AlertDialog mResetHighScores;
	private AlertDialog mSendWorldwide;
	private ProgressDialog mSendScoreProgress;
	private EditText mName;
	private ScoreProcessor mScoreProcessor;
	private int mScore;
	private Bundle mGameBundle;
	private boolean mIsHighScore;
	private int mSubmitStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.high_scores);
		Typeface tf = Typeface
		.createFromAsset(getAssets(), "fonts/GoodDog.otf");
		TextView tv = (TextView)findViewById(R.id.lblHighScores);
		tv.setTypeface(tf);		
		createDialogs();
		mScoreProcessor = new ScoreProcessor(this);
		Button btn = (Button) findViewById(R.id.btnNewGame);
		mGameBundle = getIntent().getExtras();
		if (mGameBundle != null) {
			btn.setTypeface(tf);
			mScore = mGameBundle.getInt(ScoreActivity.SCORE);
			checkHighScores();
		}else {
			btn.setVisibility(View.GONE);
		}
		mScoresList = (ListView) findViewById(R.id.scores_list);
		mScoresList.setEmptyView(findViewById(R.id.no_high_scores));
		List<Score> scores = mScoreProcessor.getHighScores();
		mListAdapter = new ScoresListAdapter(this, scores);
		mScoresList.setAdapter(mListAdapter);
		ScoreloopManagerSingleton.get().setOnScoreSubmitObserver(this);
	}

	private void checkHighScores() {
		setProgressBarIndeterminateVisibility(true);
		Thread t = new Thread(mHighScoreRunnable);
		t.start();
	}

	final Runnable mHighScoreRunnable = new Runnable() {

		@Override
		public void run() {
			mIsHighScore = mScoreProcessor.isHighScore(mScore);
			mHighScoreHandler.sendEmptyMessage(0);
		}
	};

	final Handler mHighScoreHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			if (mIsHighScore) {
				String name = GamePrefs.getInstance(HighScores.this).getLastSavedName();
				mName.setText(name);
				showDialog(HIGH_SCORE_DIALOG);
			}
		}

	};

	private void saveScore() {
		setProgressBarIndeterminateVisibility(true);
		Thread t = new Thread(mSaveScoreRunnable);
		t.start();
	}

	final Runnable mSaveScoreRunnable = new Runnable() {

		@Override
		public void run() {
			String name = mName.getText().toString();
			if (name.length() <= 0) {
				name = getString(R.string.unknown);
			}
			if (name.length() > 20) {
				name = name.substring(0, 20);
			}
			mScoreProcessor.addHighScore(name, mScore);
			GamePrefs.getInstance(HighScores.this).setName(name);
			mSaveScoreHandler.sendEmptyMessage(0);
		}
	};

	final Handler mSaveScoreHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			reloadScores();
			if(isOnline(HighScores.this)){
				showDialog(SEND_SCORE_DIALOG);				
			}
		}

	};

	private void reloadScores() {
		List<Score> scores = mScoreProcessor.getHighScores();
		mListAdapter.refreshDataSet(this, scores);
	}

	private void createDialogs() {
		final View highScoreView = LayoutInflater.from(this).inflate(
				R.layout.new_high_score, null);
		mName = (EditText) highScoreView.findViewById(R.id.name);

		mHighScoreDialog = new AlertDialog.Builder(this).setTitle(
				R.string.new_high_score).setView(highScoreView)
				.setPositiveButton(R.string.save,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								saveScore();
							}
						}).setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		
		mSendWorldwide = new AlertDialog.Builder(this).setTitle(
				R.string.send_worldwide)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								ScoreloopManagerSingleton.get().onGamePlayEnded((double)mScore, null); 
								mSendScoreProgress.show();
							}
						}).setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		mResetHighScores = new AlertDialog.Builder(this).setTitle(
				R.string.reset)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								new ClearScoresTask().execute();
							}
						}).setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		
		mSendScoreProgress = new ProgressDialog(this);
		mSendScoreProgress.setIndeterminate(true);
		mSendScoreProgress.setTitle(R.string.please_wait);
		mSendScoreProgress.setMessage(getResources().getString(
				R.string.submitting));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == HIGH_SCORE_DIALOG) {
			return mHighScoreDialog;
		}
		if (id == SEND_SCORE_DIALOG) {
			return mSendWorldwide;
		}
		if (id == CLEAR_SCORES_DIALOG) {
			return mResetHighScores;
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	public void onScoreSubmit(int status, Exception error) {
		mSendScoreProgress.dismiss();
		mSubmitStatus = status;
		startActivityForResult(new Intent(this, ShowResultOverlayActivity.class), SHOW_RESULT);
	}
	
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

	      switch (requestCode) {

	              case SHOW_RESULT:
	                     if (mSubmitStatus != OnScoreSubmitObserver.STATUS_ERROR_NETWORK) {
	                        // Show the post-score activity unless there has been a network error.
	                        startActivityForResult(new Intent(this, PostScoreOverlayActivity.class), POST_SCORE);
	                     } else { 
	   
	                        //finish();
	                     }
	                     break;

	             case POST_SCORE:
	                        // Here we get notified that the PostScoreOverlay has finished.
	                        // in this example this simply means that we're ready to return to the main activity
	                        //finish();
	                        break;
	             default:
	                        break;
	     }
	  }
	
	public void onBtnClick(View v) {
		if(v.getId() == R.id.btnSendHighScore) {
			mScore = mListAdapter.getHighScore();
			if(mScore != -1) {
				if(isOnline(this)){
					showDialog(SEND_SCORE_DIALOG);					
				} else {
					Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
				}
			} else {
				// TODO: no high score msg
			}
		}
		if(v.getId() == R.id.btnResetHighScore) {
			showDialog(CLEAR_SCORES_DIALOG);
		}
		if (v.getId() == R.id.btnNewGame) {
			GamePrefs.getInstance(this).clearSavedGame();
			Intent intent = new Intent("com.tb.flags.action.NEW_GAME");
			startActivity(intent);
			finish();
		}
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
	
	private class ClearScoresTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mScoreProcessor.clearHighScores();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			reloadScores();
		}
		
	}
}
