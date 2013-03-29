package org.feit.jokesmk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {
	public static final String JOKES = "jokes";
	public static final String FIRST_TIME = "ft";
	public static final String RATED = "rated";
	public static final String LAST_UPDATE_TIME = "lut";
	public static final String ORDER = "order";
	public static final String OFFSET = "offset";
	public static final String DB_VERSION = "db_version";
	public static final String CATEGORIES = "categories";
	public static final String LIST_MARKER = "lm";
	private static final String RATED_APP_USE_COUNT_KEY = "use_count";
	public static final long LAST_JOKE_TIMESTAMP = 1328832000;// TIMESTAMP of
																// the last joke
																// record in the
																// DB
	public static final int CURRENT_DB_VERSION = 0;
	public static final String RANDOM_JOKE_ID = "rjid";
	private static Settings mInstance;
	private Context mContext;
	public static final String MARKET_LINK = "market://details?id=org.feit.jokesmk";

	private Settings(Context context) {
		mContext = context;
	}

	public static Settings getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Settings(context);
		}
		return mInstance;
	}

	public boolean isStarted() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getBoolean(FIRST_TIME, false);
	}

	public void setFirstTimeStarted() {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		e.putBoolean(FIRST_TIME, true);
		e.commit();
	}

	public boolean isRated() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getBoolean(RATED, false);
	}

	public void setRated() {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		e.putBoolean(RATED, true);
		e.commit();
	}

	public int checkUsege() {
		SharedPreferences prefs = mContext.getSharedPreferences(JOKES,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		int used = prefs.getInt(RATED_APP_USE_COUNT_KEY, 0);
		editor.putInt(RATED_APP_USE_COUNT_KEY, used + 1);
		editor.commit();
		return used;
	}

	public void resetUsage() {
		SharedPreferences prefs = mContext.getSharedPreferences(JOKES,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(RATED_APP_USE_COUNT_KEY, 0);
		editor.commit();
	}

	public long getLastUpdateTime() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getLong(LAST_UPDATE_TIME, LAST_JOKE_TIMESTAMP);
	}

	public void setLastUpdateTime(long time) {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		e.putLong(LAST_UPDATE_TIME, time);
		e.commit();
	}

	public void setSortOrder(int sortOrder) {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		e.putInt(ORDER, sortOrder);
		e.commit();
	}

	public int getSortOrder() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getInt(ORDER, 0);
	}

	public void setLimit(int offset) {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		e.putInt(OFFSET, offset);
		e.commit();
	}

	public int getLimit() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getInt(OFFSET, 0);
	}

	public void setLastSelected(int listMarker) {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		e.putInt(LIST_MARKER, listMarker);
		e.commit();
	}

	public int getLastSelected() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getInt(LIST_MARKER, 0);
	}

	public int getDBVersion() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getInt(DB_VERSION, 0);
	}

	public void incrementDBVersion() {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		int version = mContext
				.getSharedPreferences(JOKES, Context.MODE_PRIVATE).getInt(
						DB_VERSION, 0);
		e.putInt(DB_VERSION, version + 1);
		e.commit();
	}

	public void setCategories(long[] categories) {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		StringBuffer c = new StringBuffer();
		for (int i = 0; i < categories.length; i++) {
			c.append(categories[i]);
			if (i != categories.length - 1) {
				c.append(",");
			}
		}
		e.putString(CATEGORIES, c.toString());
		e.commit();
	}

	public long[] getCategories() {
		String c = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getString(CATEGORIES, "");
		if (c.contains(",")) {
			String[] nums = c.split(",");
			long[] res = new long[nums.length - 1];
			for (int i = 1; i < nums.length - 1; i++) {
				res[i] = Integer.parseInt(nums[i]);
			}
			return res;
		} else {
			if (c.length() > 0) {
				long[] res = new long[1];
				res[0] = Integer.parseInt(c);
				return res;
			} else {
				return new long[0];
			}
		}
	}

	public void setRandomJokeId(long jokeId) {
		Editor e = mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.edit();
		e.putLong(RANDOM_JOKE_ID, jokeId);
		e.commit();
	}

	public long getRandomJokeId() {
		return mContext.getSharedPreferences(JOKES, Context.MODE_PRIVATE)
				.getLong(RANDOM_JOKE_ID, 0);
	}

}
