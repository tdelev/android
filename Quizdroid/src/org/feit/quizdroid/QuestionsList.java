package org.feit.quizdroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.feit.quizdroid.http.Constants;
import org.feit.quizdroid.http.RestRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class QuestionsList extends Activity {
	private static final String TAG = "QuestionList";
	private static final int SAVE_NAME_DIALOG = 0;
	private static final int SCORE_DIALOG = 1;
	private AnswerListAdapter mListAdapter;
	private ListView mListView;
	private Quiz mQuiz;
	private Question mActiveQuestion;
	private TextView mQuestion;
	private TextView mTotal;
	private int mCurrentQuestionIndex;
	private int mTotalQuestions;
	private Set<Integer> mAnsweredQuestions;
	private Player mPlayer;
	private Score mScore;
	private ImageView mImpressionImage;
	private Drawable mImageDrawable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.questions_list);
		mQuiz = (Quiz) getIntent().getExtras().get("quiz");
		mQuestion = (TextView) findViewById(R.id.question_text);
		mTotal = (TextView) findViewById(R.id.questions_total);
		mListView = (ListView) findViewById(R.id.answers_list);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(mOnAnswerCheck);
		mImpressionImage = (ImageView) findViewById(R.id.ivImpression);
		mCurrentQuestionIndex = 0;
		mAnsweredQuestions = new HashSet<Integer>();
		mPlayer = Player.getInstance(QuestionsList.this);
		createDialogs();
		if (mPlayer.getName().equals("")) {
			showDialog(SAVE_NAME_DIALOG);
		} else {
			loadQuiz();
		}
		new LoadImageTask().execute();
	}

	private void loadQuiz() {
		if (isOnline(this)) {
			new LoadQuizTask().execute();
		} else {
			// showDialog(ENABLE_NETWORK_DIALOG);
		}
	}

	private class LoadQuizTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mQuiz = RestRequest.getQuiz(mQuiz, mPlayer);
			mTotalQuestions = mQuiz.questions.size();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			loadQuestion();

			setProgressBarIndeterminateVisibility(false);
			mListView.focusableViewAvailable(mListView);
			super.onPostExecute(result);
		}

	}

	private ProgressDialog mPostingProgress;

	private class PostAnswersTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mPostingProgress.setIndeterminate(true);
			mPostingProgress.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mScore = RestRequest.postAnswers(mQuiz, mPlayer);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPostingProgress.dismiss();
			mResult.setText(String.valueOf(mScore.score));
			mTime.setText(mScore.toReadableTime(QuestionsList.this));
			showDialog(SCORE_DIALOG);
			super.onPostExecute(result);
		}

	}

	private void launchNetworkOptions() {
		final ComponentName toLaunch = new ComponentName(
				"com.android.settings", "com.android.settings.WirelessSettings");
		final Intent intent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, 0);
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

	DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {

			dialog.dismiss();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == SAVE_NAME_DIALOG) {
			return mSaveNameDialog;
		}
		if (id == SCORE_DIALOG) {
			return mScoreDetails;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	final OnItemClickListener mOnAnswerCheck = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			for (Answer a : mActiveQuestion.answers) {
				if (a.id == id)
					a.selected = true;
				else
					a.selected = false;
			}
			mAnsweredQuestions.add(mActiveQuestion.id);
			mListAdapter.reloadData(mActiveQuestion.answers);
		}
	};

	private EditText mName;

	private AlertDialog mSaveNameDialog;

	private void loadQuestion() {
		mActiveQuestion = mQuiz.questions.get(mCurrentQuestionIndex);
		mQuestion.setText(String.format("%d. %s", mCurrentQuestionIndex + 1,
				mActiveQuestion.text));
		mTotal.setText(String.format("%d/%d", mAnsweredQuestions.size(), mTotalQuestions));
		mListAdapter = new AnswerListAdapter(QuestionsList.this,
				mActiveQuestion.answers);
		mListView.setAdapter(mListAdapter);
	}

	public void onButtonClick(View v) {
		new LoadImageTask().execute();
		if (v.getId() == R.id.btnPrev) {
			mCurrentQuestionIndex--;
			if (mCurrentQuestionIndex == 0) {
				findViewById(R.id.btnPrev).setVisibility(View.GONE);
			}
			if (mCurrentQuestionIndex < mTotalQuestions - 1) {
				findViewById(R.id.btnEndQuiz).setVisibility(View.GONE);
				findViewById(R.id.btnNext).setVisibility(View.VISIBLE);
			}
			loadQuestion();
		}
		if (v.getId() == R.id.btnNext) {
			mCurrentQuestionIndex++;
			if (mCurrentQuestionIndex == mTotalQuestions - 1) {
				findViewById(R.id.btnNext).setVisibility(View.GONE);
				findViewById(R.id.btnEndQuiz).setVisibility(View.VISIBLE);
			}
			findViewById(R.id.btnPrev).setVisibility(View.VISIBLE);
			loadQuestion();
		}
		if (v.getId() == R.id.btnEndQuiz) {
			if(mAnsweredQuestions.size() == mTotalQuestions) {
				new PostAnswersTask().execute();
			} else {
				Toast.makeText(QuestionsList.this, R.string.more_questions, Toast.LENGTH_LONG).show();
			}
		}
	}
	private AlertDialog mScoreDetails;
	private TextView mResult;
	private TextView mTime;

	private void createDialogs() {
		final View highScoreView = LayoutInflater.from(this).inflate(
				R.layout.enter_name, null);
		mName = (EditText) highScoreView.findViewById(R.id.name);

		mSaveNameDialog = new AlertDialog.Builder(this).setTitle(
				R.string.enter_your_name).setView(highScoreView)
				.setPositiveButton(R.string.save,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								String name = mName.getText().toString().trim();
								Player.getInstance(QuestionsList.this).setName(
										name);
								loadQuiz();
							}
						}).setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								finish();
							}
						}).create();

		mPostingProgress = new ProgressDialog(this);
		mPostingProgress.setTitle(R.string.posting);
		mPostingProgress.setMessage(getString(R.string.loading_msg));
		mPostingProgress.setIndeterminate(true);
		
		
		final View scoreDetailsView = LayoutInflater.from(this).inflate(
				R.layout.score_details, null);
		
		mResult = (TextView) scoreDetailsView.findViewById(R.id.result);
		mTime = (TextView) scoreDetailsView.findViewById(R.id.time);

		mScoreDetails = new AlertDialog.Builder(this).setTitle(
				R.string.results).setView(scoreDetailsView).setIcon(
				android.R.drawable.ic_dialog_info).setPositiveButton(R.string.save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				}).create();
	}
	
	private class LoadImageTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			URL url = null;
			try {
				url = new URL(Constants.IMP_URL);
				InputStream is = (InputStream) url.getContent();
				mImageDrawable = Drawable.createFromStream(is, "");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setProgressBarIndeterminateVisibility(false);
			mImpressionImage.setImageDrawable(mImageDrawable);
			super.onPostExecute(result);
		}
	}
}