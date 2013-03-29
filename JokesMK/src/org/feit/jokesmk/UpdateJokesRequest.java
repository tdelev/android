package org.feit.jokesmk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class UpdateJokesRequest {
	static final String TAG = "UpdateJokesRequest";
	static final String UPDATE_URL = "http://vicoteka.mk/android_api.php?api_pass=vicoteka_android&time=%s";
	private JSONArray mJokes;
	private ContentResolver mContentResolver;
	private Map<Integer, Long> mCat;
	private Map<Long, Integer> mCatUpdate;

	public UpdateJokesRequest(Context context) {
		mContentResolver = context.getContentResolver();
		mCat = new HashMap<Integer, Long>();
		mCatUpdate = new HashMap<Long, Integer>();
		Cursor cursor = mContentResolver.query(CategoriesProvider.CONTENT_URI,
				new String[] { "_id", "total", "vicoteka_id" }, null, null,
				null);
		while (cursor.moveToNext()) {
			mCat.put(cursor.getInt(2), cursor.getLong(0));
			mCatUpdate.put(cursor.getLong(0), cursor.getInt(1));
		}
		cursor.close();
	}

	public int update(long lastUpdate) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = String.format(UPDATE_URL, lastUpdate);
		Log.d(TAG, url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			if (json.equals("null")) {
				return 0;
			} else {
				mJokes = new JSONArray(json);
			}
			return mJokes.length();
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public boolean saveJoke(int i) {
		try {
			JSONObject joke = mJokes.getJSONObject(i);
			ContentValues values = new ContentValues();
			values.put(JokesProvider.NAME, joke.getString("title"));
			values.put(JokesProvider.TEXT, joke.getString("content"));
			long catId = mCat.get(joke.getInt("catid"));
			mCatUpdate.put(catId, mCatUpdate.get(catId) + 1);
			values.put(JokesProvider.CATEGORY_ID, catId);
			mContentResolver.insert(JokesProvider.CONTENT_URI, values);
			return true;
		} catch (JSONException e) {
			Log.d(TAG, "JSON exception: " + e.getMessage());
			return false;
		}
	}

	public void updateCategories() {
		ContentValues values = new ContentValues();
		String where = "_id = ?";
		String[] selectionArgs;
		for (long id : mCatUpdate.keySet()) {
			values.clear();
			values.put("total", mCatUpdate.get(id));
			selectionArgs = new String[] { String.valueOf(id) };
			mContentResolver.update(CategoriesProvider.CONTENT_URI, values,
					where, selectionArgs);
		}
	}

}
