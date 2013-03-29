package org.feit.jokesmk;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ToggleButton;

public class JokeView extends Activity {
	private static final int RATE_JOKE_DIALOG = 0;
	private static final String DEFAULT_FONT_SIZE = "18";
	private long mJokeId;
	private Joke mJoke;
	private long[] mJokesIds;
	private int mCurrentIndex;
	private TextSwitcher mName;
	private TextSwitcher mText;
	private RatingBar mJokeRating;
	private RatingBar mJokeRatingStatic;
	private ToggleButton mIsFavorite;
	private JokesProcessor mJokesProcessor;
	private AlertDialog mRateJokeDialog;
	private ProgressBar mPbTitle;
	private float mDefaultFont;
	private HashMap<Long, Category> mCategories;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joke_view);
		Typeface font = Typeface.createFromAsset(getAssets(),
				"fonts/idolwild.ttf");
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setTypeface(font);
		Button btn = (Button) findViewById(R.id.btnDecrease);
		btn.setTypeface(font);
		btn = (Button) findViewById(R.id.btnReset);
		btn.setTypeface(font);
		btn = (Button) findViewById(R.id.btnIncrease);
		btn.setTypeface(font);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		mDefaultFont = Float.parseFloat(prefs.getString("fontSize",
				DEFAULT_FONT_SIZE));
		final TextView t1 = (TextView) findViewById(R.id.t1);
		final TextView t2 = (TextView) findViewById(R.id.t2);
		t1.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDefaultFont);
		t2.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDefaultFont);
		mPbTitle = (ProgressBar) findViewById(R.id.pbTitle);
		mJokeId = getIntent().getLongExtra("joke_id", -1);
		mJokesIds = getIntent().getLongArrayExtra("joke_ids");
		if (mJokeId == -1) {
			// started from widget
			mJokeId = Settings.getInstance(this).getRandomJokeId();
			mJokesIds = new long[0];
			final Button nextButton = (Button) findViewById(R.id.btnNext);
			nextButton.setVisibility(View.GONE);
			final Button prevButton = (Button) findViewById(R.id.btnPrev);
			prevButton.setVisibility(View.GONE);
		}
		mCurrentIndex = 0;
		for (int i = 0; i < mJokesIds.length; i++) {
			if (mJokeId == mJokesIds[i]) {
				mCurrentIndex = i;
			}
		}
		mJokesProcessor = new JokesProcessor(this);
		mName = (TextSwitcher) findViewById(R.id.name);
		mText = (TextSwitcher) findViewById(R.id.text);
		mJokeRatingStatic = (RatingBar) findViewById(R.id.ratingbar_static);
		mIsFavorite = (ToggleButton) findViewById(R.id.favorite);
		mIsFavorite.setOnCheckedChangeListener(onFavoriteChanged);
		CategoriesProcessor categoriesProcessor = new CategoriesProcessor(this);
		mCategories = categoriesProcessor.getCategories();
		new LoadJokeTask().execute();
		createDialogs();
		
	}
	
	private final OnCheckedChangeListener onFavoriteChanged = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			setFavorite(isChecked);
		}
	};

	private void setFavorite(boolean favorite) {
		mJoke.favorite = favorite;
		new FavoriteJokeTask().execute();
	}

	public void onRateClick(View v) {
		showDialog(RATE_JOKE_DIALOG);
	}

	public void onShareClick(View v) {
		shareJoke();
	}

	private void shareJoke() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
		Category category = mCategories.get(mJoke.category_id);
		intent.putExtra(Intent.EXTRA_TEXT, String.format("%s - %s",
				getString(R.string.app_long_name),
				mJoke.getShareLink(category.slug)));
		intent.setType("text/plain");
		Intent i = Intent.createChooser(intent, "Share via...");
		startActivity(i);
	}

	public void onNextPrev(View v) {
		if (v.getId() == R.id.btnNext) {
			mCurrentIndex++;
			if (mCurrentIndex >= mJokesIds.length) {
				mCurrentIndex = 0;
			}
		}
		if (v.getId() == R.id.btnPrev) {
			mCurrentIndex--;
			if (mCurrentIndex < 0) {
				mCurrentIndex = mJokesIds.length - 1;
			}
		}
		mJokeId = mJokesIds[mCurrentIndex];
		new LoadJokeTask().execute();
	}

	public void onFontChange(View v) {
		final TextView t1 = (TextView) findViewById(R.id.t1);
		final TextView t2 = (TextView) findViewById(R.id.t2);
		float s1 = t1.getTextSize();
		float s2 = t2.getTextSize();
		float value = 0;
		if (v.getId() == R.id.btnIncrease) {
			value = 1;
		}
		if (v.getId() == R.id.btnDecrease) {
			value = -1;
		}
		if (v.getId() == R.id.btnReset) {
			s1 = mDefaultFont;
			s2 = mDefaultFont;
			t1.setTextSize(TypedValue.COMPLEX_UNIT_SP, s1);
			t2.setTextSize(TypedValue.COMPLEX_UNIT_SP, s2);
		} else {
			t1.setTextSize(TypedValue.COMPLEX_UNIT_PX, s1 + value);
			t2.setTextSize(TypedValue.COMPLEX_UNIT_PX, s2 + value);
		}
	}

	private class LoadJokeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mPbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mJoke = mJokesProcessor.getJoke(mJokeId);
			mJoke.text = mJoke.text.replaceAll("&quot;", "\"").replaceAll(
					"&nbsp", " ");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPbTitle.setVisibility(View.GONE);
			mName.setText(mJoke.name);
			mText.setText("");
			mText.setText(mJoke.text);
			mJokeRatingStatic.setRating(mJoke.rating);
			mIsFavorite.setChecked(mJoke.favorite);
			super.onPostExecute(result);
		}

	}

	private class RateJokeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mPbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mJoke.rating = (int) mJokeRating.getRating();
			mJokesProcessor.rateJoke(mJoke);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPbTitle.setVisibility(View.GONE);
			mJokeRatingStatic.setRating(mJoke.rating);
			super.onPostExecute(result);
		}

	}

	private class FavoriteJokeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mPbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mJokesProcessor.favoriteJoke(mJoke);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPbTitle.setVisibility(View.GONE);
			mIsFavorite.setChecked(mJoke.favorite);
			super.onPostExecute(result);
		}

	}

	private void createDialogs() {
		final View rateJokeView = LayoutInflater.from(this).inflate(
				R.layout.rate_joke, null);
		mJokeRating = (RatingBar) rateJokeView.findViewById(R.id.ratingBar);

		mRateJokeDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.rate_joke_dlg_title)
				.setView(rateJokeView)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								new RateJokeTask().execute();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == RATE_JOKE_DIALOG) {
			return mRateJokeDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.joke_menu, m);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.share) {
			shareJoke();
		}
		if (item.getItemId() == R.id.rate) {
			showDialog(RATE_JOKE_DIALOG);
		}
		return true;
	}

}
