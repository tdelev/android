package org.feit.jokesmk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

public class JokesProcessor {
	private static final String TAG = "JokesProcessor";

	private static final int DATE = 0;
	private static final int NAME = 1;
	private static final int SIZE = 2;
	private static final int RATING = 3;
	private static final int VIEWS = 4;

	private Context mContext;

	public JokesProcessor(Context context) {
		mContext = context;
	}

	public List<Joke> getJokes(int sort, int limit, int offset,
			long[] categories) {
		List<Joke> jokes = new ArrayList<Joke>(limit);
		// ContentResolver content = mContext.getContentResolver();
		String sortOrder = "_id desc";
		if (sort == NAME) {
			sortOrder = "name ASC";
		}
		if (sort == SIZE) {
			sortOrder = "length(text) DESC";
		}
		if (sort == RATING) {
			sortOrder = "rating DESC";
		}
		if (sort == VIEWS) {
			sortOrder = "views DESC";
		}
		// long t1 = System.currentTimeMillis();
		JokesProvider jokesProvider = new JokesProvider();
		jokesProvider.loadDb(mContext);
		String l = String.format("%d, %d", offset, limit);
		Cursor cursor = null;
		if (categories != null && categories.length > 0) {
			StringBuilder sb = new StringBuilder();
			String[] selectionArgs = new String[categories.length];
			for (int i = 0; i < categories.length; i++) {
				if (i != categories.length - 1) {
					sb.append("category_id = ? OR ");
				} else {
					sb.append("category_id = ?");
				}
				selectionArgs[i] = String.valueOf(categories[i]);
			}
			String where = sb.toString();
			cursor = jokesProvider.queryLimit(JokesProvider.CONTENT_URI, null,
					where, selectionArgs, sortOrder, l);
		} else {
			cursor = jokesProvider.queryLimit(JokesProvider.CONTENT_URI, null,
					null, null, sortOrder, l);
		}

		while (cursor.moveToNext()) {
			Joke joke = new Joke();
			joke.id = cursor.getLong(0);
			joke.name = cursor.getString(1);
			joke.text = cursor.getString(2);
			joke.category_id = cursor.getInt(3);
			joke.rating = cursor.getInt(4);
			joke.views = cursor.getInt(5);
			joke.favorite = cursor.getInt(6) != 0;
			jokes.add(joke);
		}
		cursor.close();
		jokesProvider.close();
		// Log.d(TAG, "TIME: " + (System.currentTimeMillis() - t1));
		return jokes;
	}

	public List<Joke> getJokes() {
		ContentResolver content = mContext.getContentResolver();
		Cursor cursor = content.query(JokesProvider.CONTENT_URI, null, null,
				null, null);
		List<Joke> jokes = new ArrayList<Joke>(cursor.getCount());
		while (cursor.moveToNext()) {
			Joke joke = new Joke();
			joke.id = cursor.getLong(0);
			joke.name = cursor.getString(1);
			joke.text = cursor.getString(2);
			joke.category_id = cursor.getInt(3);
			joke.rating = cursor.getInt(4);
			joke.views = cursor.getInt(5);
			joke.favorite = cursor.getInt(6) != 0;
			jokes.add(joke);
		}
		cursor.close();
		return jokes;
	}

	public List<Joke> getFavorites() {
		ContentResolver content = mContext.getContentResolver();
		String where = "favorite = ?";
		String[] selectionArgs = new String[] { "1" };
		Cursor cursor = content.query(JokesProvider.CONTENT_URI, null, where,
				selectionArgs, null);
		List<Joke> jokes = new ArrayList<Joke>(cursor.getCount());
		while (cursor.moveToNext()) {
			Joke joke = new Joke();
			joke.id = cursor.getLong(0);
			joke.name = cursor.getString(1);
			joke.text = cursor.getString(2);
			joke.category_id = cursor.getInt(3);
			joke.rating = cursor.getInt(4);
			joke.views = cursor.getInt(5);
			joke.favorite = cursor.getInt(6) != 0;
			jokes.add(joke);
		}
		cursor.close();
		return jokes;
	}

	public Joke getJoke(long jokeId) {
		ContentResolver content = mContext.getContentResolver();
		String selection = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(jokeId) };
		Cursor cursor = content.query(JokesProvider.CONTENT_URI, null,
				selection, selectionArgs, null);
		Joke joke = null;
		while (cursor.moveToNext()) {
			joke = new Joke();
			joke.id = cursor.getLong(0);
			joke.name = cursor.getString(1);
			joke.text = cursor.getString(2);
			joke.category_id = cursor.getLong(3);
			joke.rating = cursor.getInt(4);
			joke.views = cursor.getInt(5);
			joke.favorite = cursor.getInt(6) != 0;
		}
		cursor.close();
		String where = "_id = ?";
		selectionArgs = new String[] { String.valueOf(joke.id) };
		ContentValues values = new ContentValues();
		values.put("views", joke.views + 1);
		content.update(JokesProvider.CONTENT_URI, values, where, selectionArgs);
		return joke;
	}

	public Joke getRandomJoke() {
		ContentResolver content = mContext.getContentResolver();
		Cursor cursor = content.query(JokesProvider.CONTENT_URI, null, null,
				null, null);
		int total = cursor.getCount();
		total = (int) (Math.random() * total);
		cursor.moveToPosition(total);
		Joke joke = new Joke();
		joke.id = cursor.getLong(0);
		joke.name = cursor.getString(1);
		joke.text = cursor.getString(2);
		joke.category_id = cursor.getInt(3);
		joke.rating = cursor.getInt(4);
		joke.views = cursor.getInt(5);
		joke.favorite = cursor.getInt(6) != 0;
		cursor.close();
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(joke.id) };
		ContentValues values = new ContentValues();
		values.put("views", joke.views + 1);
		content.update(JokesProvider.CONTENT_URI, values, where, selectionArgs);
		Settings.getInstance(mContext).setRandomJokeId(joke.id);
		return joke;
	}

	public void rateJoke(Joke joke) {
		ContentResolver content = mContext.getContentResolver();
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(joke.id) };
		ContentValues values = new ContentValues();
		values.put("rating", joke.rating);
		content.update(JokesProvider.CONTENT_URI, values, where, selectionArgs);
	}

	public void favoriteJoke(Joke joke) {
		ContentResolver content = mContext.getContentResolver();
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(joke.id) };
		ContentValues values = new ContentValues();
		values.put("favorite", joke.favorite);
		content.update(JokesProvider.CONTENT_URI, values, where, selectionArgs);
	}

	public List<String> getWords() {
		List<Joke> jokes = getJokes();
		HashMap<String, Integer> words = new HashMap<String, Integer>();
		HashMap<String, Integer> wv = new HashMap<String, Integer>();
		Pattern p = Pattern.compile("\\s+|\\.\\s+|-\\s+|\\?\\s+|:\\s+");
		for (Joke j : jokes) {
			String[] wl = p.split(j.text);
			for (String w : wl) {
				boolean wj = false;
				w = w.toLowerCase();
				// Log.d(TAG, "word: " + w);
				if (words.containsKey(w)) {
					int n = words.get(w);
					words.put(w, n + 1);
				} else {
					words.put(w, 1);
				}
				if (wv.containsKey(w) && !wj) {
					int n = wv.get(w);
					wv.put(w, n + 1);
					wj = true;
				} else {
					wv.put(w, 1);
					wj = true;
				}
			}
		}
		int n = jokes.size();
		List<String> result = new ArrayList<String>();
		for (String w : wv.keySet()) {
			Log.d(TAG, "word: " + w);
			float f = (1.0f * wv.get(w)) / n;
			Log.d(TAG, "f: " + f);
			if (f > 0.1 && f < 0.5) {
				result.add(w);
			}
		}
		return result;
	}

	public static class JokesComparator implements Comparator<Joke> {

		private int mCompareType;

		public JokesComparator(int compareType) {
			mCompareType = compareType;
		}

		@Override
		public int compare(Joke joke1, Joke joke2) {
			if (mCompareType == NAME) {
				return joke1.name.compareTo(joke2.name);
			}
			if (mCompareType == SIZE) {
				if (joke1.text.length() < joke2.text.length()) {
					return 1;
				} else if (joke1.text.length() > joke2.text.length()) {
					return -1;
				} else
					return 0;
			}
			if (mCompareType == RATING) {
				if (joke1.rating < joke2.rating) {
					return 1;
				} else if (joke1.rating > joke2.rating) {
					return -1;
				} else
					return 0;
			}
			if (mCompareType == VIEWS) {
				if (joke1.views < joke2.views) {
					return 1;
				} else if (joke1.views > joke2.views) {
					return -1;
				} else
					return 0;
			}
			return 0;
		}

	}
}
