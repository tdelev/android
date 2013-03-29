package org.feit.quizdroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.feit.quizdroid.http.Constants;
import org.feit.quizdroid.http.RestRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class QuizList extends Activity {
	private static final String TAG = "QuizList";

	static final int ENABLE_NETWORK_DIALOG = 0;
	static final int LOADING_DIALOG = 1;
	private AlertDialog mNetworkErrorDialog;
	private AlertDialog mPlayedDialog;
	private QuizListAdapter mListAdapter;
	private ListView mListView;
	private List<Quiz> mQuizes;
	private ImageView mImpressionImage;
	private Drawable mImageDrawable;
	private Player mPlayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.quiz_list);
		mPlayer = Player.getInstance(this);
		mListView = (ListView) findViewById(R.id.quiz_list);
		mListView.setEmptyView(findViewById(R.id.no_quizes));
		mListView.setOnItemClickListener(mOnItemClicked);
		mImpressionImage = (ImageView) findViewById(R.id.ivImpression);
		createDialogs();
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadQuizes();
		new LoadImageTask().execute();
	}

	private void createDialogs() {
		mNetworkErrorDialog = new AlertDialog.Builder(this).setTitle(
				R.string.network_error).setMessage(
				getString(R.string.check_internet)).setPositiveButton(
				R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						launchNetworkOptions();
					}
				}).setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						finish();
					}
				}).create();
	}

	private void loadQuizes() {
		if (isOnline(this)) {
			new LoadQuizesTask().execute();
		} else {
			showDialog(ENABLE_NETWORK_DIALOG);
		}
	}

	private class LoadQuizesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mQuizes = RestRequest.getQuizes(mPlayer);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mQuizes != null && mQuizes.size() > 0) {
				mListAdapter = new QuizListAdapter(QuizList.this, mQuizes);
				mListView.setAdapter(mListAdapter);
			} else {
				final TextView noQuizes = (TextView) findViewById(R.id.no_quizes);
				noQuizes.setText(R.string.no_quizes);
			}

			setProgressBarIndeterminateVisibility(false);
			mListView.focusableViewAvailable(mListView);
			super.onPostExecute(result);
		}

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

	final OnItemClickListener mOnItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {

			Quiz quiz = mListAdapter.getQuiz(position);
			if (quiz.played && quiz.score != 0) {
				mPlayedDialog = new AlertDialog.Builder(QuizList.this)
						.setTitle(R.string.played_title).setMessage(
								String.format(
										getString(R.string.played_message),
										quiz.score)).setCancelable(true)
						.create();
				mPlayedDialog.show();
			} else {
				Intent intent = new Intent(QuizList.this, QuestionsList.class);
				intent.putExtra("quiz", quiz);
				startActivity(intent);
			}

		}
	};

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

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == ENABLE_NETWORK_DIALOG) {
			return mNetworkErrorDialog;
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
}