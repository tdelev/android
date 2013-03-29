package org.feit.jokesmk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class JokesList extends Activity {
	static final String TAG = "JokesList";

	private static final int SORT_DIALOG = 0;
	static final int ENABLE_NETWORK_DIALOG = 1;
	static final int RATE_DIALOG = 2;
	private static final String PAGE_SIZE = "20";

	private ListView mListView;
	private ListView mFavoritesListView;
	private ListView mCategoriesList;
	private JokesListAdapter mListAdapter;
	private JokesListAdapter mFavoritesListAdapter;
	private CategoriesListAdapter mCategoriesListAdapter;
	private JokesProcessor mJokesProcessor;
	private Joke mSelectedJoke;
	private List<Joke> mJokes;
	private List<Joke> mFavoriteJokes;
	private long[] mJokesIds;
	private int mSortOrder;
	private int mOffset;
	private boolean mScrolledDown;
	private ToggleButton btnJokes;
	private ToggleButton btnCategories;
	private ToggleButton btnFavorites;
	private long[] categories;
	private HashMap<Long, Category> mCategories;
	private int mTotal;
	private ProgressBar pbTitle;
	private int mPageSize;
	private TextView mTitleInfo;
	private Animation mTabsInAnimation;
	private int mLastSelectedJoke;
	private int mLoadedPageSize;
	private int mLoadedPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jokes_list);
		Typeface font = Typeface.createFromAsset(getAssets(),
				"fonts/idolwild.ttf");
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setTypeface(font);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		mPageSize = Integer.parseInt(prefs.getString("pageSize", PAGE_SIZE));
		mLoadedPageSize = mPageSize;
		pbTitle = (ProgressBar) findViewById(R.id.pbTitle);
		mTitleInfo = (TextView) findViewById(R.id.title_info);
		mTitleInfo.setTypeface(font);
		mJokesProcessor = new JokesProcessor(this);
		mListView = (ListView) findViewById(R.id.jokes_list);
		mFavoritesListView = (ListView) findViewById(R.id.favorites_list);
		mCategoriesList = (ListView) findViewById(R.id.categories_list);
		CategoriesProcessor categoriesProcessor = new CategoriesProcessor(this);
		mCategories = categoriesProcessor.getCategories();
		mCategoriesListAdapter = new CategoriesListAdapter(this, mCategories);
		mCategoriesList.setAdapter(mCategoriesListAdapter);
		mCategoriesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mCategoriesList.setOnItemClickListener(mOnCategoryClicked);
		mCategoriesList.setVisibility(View.GONE);
		mListView.setOnItemClickListener(mOnItemClicked);
		mListView.setOnScrollListener(onListScrolledDown);
		mFavoritesListView.setOnItemClickListener(mOnFavoriteItemClicked);
		mJokes = new ArrayList<Joke>();
		mListAdapter = new JokesListAdapter(JokesList.this, mJokes);
		mListView.setAdapter(mListAdapter);
		mFavoritesListAdapter = new JokesListAdapter(this, mJokes);
		mFavoritesListView.setAdapter(mFavoritesListAdapter);
		getContentResolver().registerContentObserver(JokesProvider.CONTENT_URI,
				true, mJokesObserver);
		mOffset = 0;
		mScrolledDown = false;
		btnJokes = (ToggleButton) findViewById(R.id.tbJokes);
		btnCategories = (ToggleButton) findViewById(R.id.tbCategories);
		btnFavorites = (ToggleButton) findViewById(R.id.tbFavorites);
		mTabsInAnimation = AnimationUtils.loadAnimation(this,
				R.anim.push_bottom_in);
		calculateTotal();
		loadState();
	}

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int ps = Integer.parseInt(prefs.getString("pageSize", PAGE_SIZE));
		if (ps != mPageSize) {
			refreshJokes();
		}
		findViewById(R.id.tabs).startAnimation(mTabsInAnimation);
	}

	private void calculateTotal() {
		mTotal = 0;
		for (Category c : mCategories.values()) {
			mTotal += c.total;
		}
	}

	final Handler mObserverHandler = new Handler() {

	};
	final ContentObserver mJokesObserver = new ContentObserver(mObserverHandler) {

		@Override
		public void onChange(boolean selfChange) {
			// new LoadJokesTask().execute();
			super.onChange(selfChange);
		}

	};

	final OnItemClickListener mOnItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			mLastSelectedJoke = position;
			mSelectedJoke = mListAdapter.getJoke(position);
			mJokes.get(position).views++;
			mListAdapter.reloadData(mJokes);
			Intent intent = new Intent(JokesList.this, JokeView.class);
			intent.putExtra("joke_id", id);
			intent.putExtra("joke_ids", mJokesIds);
			startActivity(intent);

		}
	};

	final OnItemClickListener mOnFavoriteItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {

			mSelectedJoke = mFavoritesListAdapter.getJoke(position);
			mFavoriteJokes.get(position).views++;
			Intent intent = new Intent(JokesList.this, JokeView.class);
			intent.putExtra("joke_id", id);
			intent.putExtra("joke_ids", mFavoriteJokesIds);
			startActivity(intent);

		}
	};

	final OnItemClickListener mOnCategoryClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			mCategoriesListAdapter.checkCategory(position);
		}
	};

	final OnScrollListener onListScrolledDown = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (firstVisibleItem + visibleItemCount == totalItemCount
					&& totalItemCount > 0) {
				if (mOffset + mLoadedPageSize < mTotal) {
					if (!mScrolledDown) {
						mScrolledDown = true;
						mOffset += mPageSize;
						new LoadJokesTask().execute();
					}
				}
			}
		}
	};

	private class LoadJokesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			pbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<Joke> jokes = mJokesProcessor.getJokes(mSortOrder,
					mLoadedPageSize, mOffset, categories);
			List<Joke> originalJokes = new ArrayList<Joke>(mJokes);
			int size = originalJokes.size() + jokes.size();
			mJokes = new ArrayList<Joke>(size);
			mJokesIds = new long[size];
			int i = 0;
			for (Joke j : originalJokes) {
				mJokes.add(j);
				mJokesIds[i++] = j.id;
			}
			for (Joke j : jokes) {
				mJokes.add(j);
				mJokesIds[i++] = j.id;
			}
			mLoadedPageSize = mPageSize;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mListAdapter.reloadData(mJokes);
			mTitleInfo.setText(String.format("(%d/%d)",
					mListAdapter.getCount(), mTotal));
			pbTitle.setVisibility(View.GONE);
			mListView.focusableViewAvailable(mListView);
			mScrolledDown = false;
			if (mLoadedPosition >= 0) {
				mListView.setSelection(mLoadedPosition);
				mLoadedPosition = -1;
			}
			super.onPostExecute(result);
		}

	}

	private long[] mFavoriteJokesIds;

	private class LoadFavoritesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			pbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mFavoriteJokes = mJokesProcessor.getFavorites();
			int size = mFavoriteJokes.size();
			mFavoriteJokesIds = new long[size];
			int i = 0;
			for (Joke j : mFavoriteJokes) {
				mFavoriteJokesIds[i++] = j.id;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mFavoritesListAdapter.reloadData(mFavoriteJokes);
			if (mFavoriteJokes.size() == 0) {
				Toast.makeText(JokesList.this,
						getString(R.string.no_favorites), Toast.LENGTH_LONG)
						.show();
			}
			mTitleInfo.setText(String.format("(%d)", mFavoriteJokes.size()));
			pbTitle.setVisibility(View.GONE);
			mFavoritesListView.focusableViewAvailable(mFavoritesListView);
			super.onPostExecute(result);
		}

	}

	protected Dialog onCreateDialog(int id) {
		if (id == SORT_DIALOG) {
			return new AlertDialog.Builder(JokesList.this)
					.setSingleChoiceItems(R.array.sort_type, id,
							sortDialogClick).setTitle(R.string.sort_by)
					.create();
		}
		if (id == ENABLE_NETWORK_DIALOG) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.network_error)
					.setMessage(getString(R.string.check_internet))
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									launchNetworkOptions();
								}
							})
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							}).create();
		}
		if (id == RATE_DIALOG) {
			AlertDialog.Builder alert = new AlertDialog.Builder(JokesList.this);
			alert.setTitle(R.string.rate_app_title);
			alert.setMessage(R.string.rate_app_msg);
			alert.setPositiveButton(R.string.rate_app_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							Settings.getInstance(JokesList.this).setRated();
							Intent marketIntent = new Intent(
									Intent.ACTION_VIEW, Uri
											.parse(Settings.MARKET_LINK));
							startActivity(marketIntent);
						}
					});
			alert.setNegativeButton(R.string.rate_app_later,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							finish();
						}
					});
			return alert.create();
		}
		return super.onCreateDialog(id);
	}

	private void launchNetworkOptions() {
		final ComponentName toLaunch = new ComponentName(
				"com.android.settings", "com.android.settings.WirelessSettings");
		final Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, 0);
	}

	final DialogInterface.OnClickListener sortDialogClick = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mLastSelectedJoke = -1;
			mSortOrder = which;
			mOffset = 0;
			mJokes = new ArrayList<Joke>();
			new LoadJokesTask().execute();
			dialog.dismiss();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_menu, m);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.sort) {
			showDialog(SORT_DIALOG);
		}
		if (item.getItemId() == R.id.update) {
			updateJokes();
		}
		if (item.getItemId() == R.id.settings) {
			Intent intent = new Intent(this, JokesPreferences.class);
			startActivity(intent);
		}
		return true;
	}

	public void onChangeView(View v) {
		if (v.getId() == R.id.tbJokes) {
			btnCategories.setChecked(false);
			btnJokes.setChecked(true);
			btnFavorites.setChecked(false);
			mListView.setVisibility(View.VISIBLE);
			mFavoritesListView.setVisibility(View.GONE);
			mCategoriesList.setVisibility(View.GONE);
			refreshJokes();
		}
		if (v.getId() == R.id.tbCategories) {
			btnCategories.setChecked(true);
			btnJokes.setChecked(false);
			btnFavorites.setChecked(false);
			mListView.setVisibility(View.GONE);
			mFavoritesListView.setVisibility(View.GONE);
			mCategoriesList.setVisibility(View.VISIBLE);

			mTitleInfo.setText(String.format("(%d)",
					mCategoriesListAdapter.getCount()));
		}
		if (v.getId() == R.id.tbFavorites) {
			btnCategories.setChecked(false);
			btnJokes.setChecked(false);
			btnFavorites.setChecked(true);
			mListView.setVisibility(View.GONE);
			mCategoriesList.setVisibility(View.GONE);
			mFavoritesListView.setVisibility(View.VISIBLE);
			loadFavorites();
		}
	}

	private void updateJokes() {
		if (isOnline(this)) {
			new UpdateJokesTask().execute();
		} else {
			showDialog(ENABLE_NETWORK_DIALOG);
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

	public void refreshJokes() {
		long[] checked = mCategoriesListAdapter.getCheckedIds();
		if (categories == null) {
			if (checked != null && checked.length > 0) {
				loadFromCategories(checked);
			}
		} else {
			if (categories.length == checked.length) {
				Arrays.sort(categories);
				Arrays.sort(checked);
				boolean same = true;
				for (int i = 0; i < categories.length; i++) {
					if (checked[i] != categories[i]) {
						same = false;
						break;
					}
				}
				if (!same) {
					loadFromCategories(checked);
				}
			} else {
				if (checked.length > 0) {
					loadFromCategories(checked);
				} else {
					calculateTotal();
					categories = checked;
					mOffset = 0;
					mJokes = new ArrayList<Joke>();
					new LoadJokesTask().execute();
				}
			}
		}
		mTitleInfo.setText(String.format("(%d/%d)", mListAdapter.getCount(),
				mTotal));
	}

	void loadFavorites() {
		new LoadFavoritesTask().execute();
	}

	private void loadFromCategories(long[] checked) {
		if (checked.length > 0) {
			mTotal = 0;
		}
		for (long l : checked) {
			Category cat = mCategories.get(l);
			if(cat != null) {
				mTotal += cat.total;
			}
		}
		categories = checked;
		mOffset = 0;
		mJokes = new ArrayList<Joke>();
		new LoadJokesTask().execute();
	}

	private class UpdateJokesTask extends AsyncTask<Void, Integer, Integer> {
		private final ProgressDialog mUpdatingProgress = new ProgressDialog(
				JokesList.this);
		private UpdateJokesRequest mUpdateJokesRequest = new UpdateJokesRequest(
				JokesList.this);
		private boolean mFirstPhase = true;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mUpdatingProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mUpdatingProgress.setMax(100);
			mUpdatingProgress.setTitle(R.string.update_progress_dlg_title);
			mUpdatingProgress
					.setMessage(getString(R.string.update_progress_dlg_msg_update));
			mUpdatingProgress.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			long updateTime = Settings.getInstance(JokesList.this)
					.getLastUpdateTime();
			Log.d(TAG, "last update: " + updateTime);
			int total = mUpdateJokesRequest.update(updateTime);

			Log.d(TAG, "total: " + total);
			if (total > 0) {
				publishProgress(50);
				for (int i = 0; i < total; i++) {
					mUpdateJokesRequest.saveJoke(i);
					publishProgress(50 + (int) (((i + 1) * 1.0 / total) * 50));
				}
				mUpdateJokesRequest.updateCategories();
				updateTime = System.currentTimeMillis() / 1000;
				Log.d(TAG, "next update: " + updateTime);

				Settings.getInstance(JokesList.this).setLastUpdateTime(
						updateTime);
			}
			return total;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (mFirstPhase) {
				mUpdatingProgress
						.setMessage(getString(R.string.update_progress_dlg_msg_save));
				mFirstPhase = false;
			}
			mUpdatingProgress.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			mUpdatingProgress.dismiss();
			if (result > 0) {
				Toast.makeText(
						JokesList.this,
						String.format(getString(R.string.update_success),
								result), Toast.LENGTH_LONG).show();
				mTotal += result;
				mLastSelectedJoke = -1;
				mSortOrder = 0;
				mOffset = 0;
				mJokes = new ArrayList<Joke>();
				new LoadJokesTask().execute();
			} else {
				Toast.makeText(JokesList.this, getString(R.string.update_fail),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	private void saveState() {
		Settings settings = Settings.getInstance(this);
		settings.setSortOrder(mSortOrder);
		settings.setLastSelected(mLastSelectedJoke);
		settings.setCategories(categories);
	}

	private void loadState() {
		Settings settings = Settings.getInstance(this);
		mSortOrder = settings.getSortOrder();
		int position = settings.getLastSelected();
		mLastSelectedJoke = position;
		mLoadedPageSize = mPageSize;
		while (position > mLoadedPageSize) {
			mLoadedPageSize += mPageSize;
		}
		mLoadedPosition = position;
		categories = settings.getCategories();
		if (categories.length > 0) {
			loadFromCategories(categories);
		} else {
			new LoadJokesTask().execute();
		}
	}

	@Override
	public void finish() {
		super.finish();
		saveState();
	}

	/*
	 * Override back key press for rating
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			boolean rated = Settings.getInstance(this).isRated();
			if (!rated) {
				int useCount = Settings.getInstance(this).checkUsege();

				if (useCount == 3) {
					Settings.getInstance(this).resetUsage();
					showDialog(RATE_DIALOG);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
