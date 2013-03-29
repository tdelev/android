package org.feit.sharelater;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

public class IntentsProcessor {
	static final String TAG = "IntentsProcessor";
	static final String EXTRAS_SEPARATOR = "@;@";
	static final String EXTRAS_KEY_VALUE = "@:@";
	static final String SHARE_LATER_PREFS = "slp";
	static final String NEW_INTENTS = "new_intents";
	private Context mContext;

	public IntentsProcessor(Context context) {
		mContext = context;
	}

	public long saveIntent(String name, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "Action: " + action);
		String data = "";
		if (intent.getDataString() != null) {
			data = intent.getDataString();
		}
		Log.d(TAG, "Intent data string: " + data);
		String categories = "";
		Set<String> cs = intent.getCategories();
		if (cs != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<String> iterator = cs.iterator();
			while (iterator.hasNext()) {
				sb.append(iterator.next());
				if (iterator.hasNext()) {
					sb.append(",");
				}
			}
			categories = sb.toString();
		}
		Log.d(TAG, "Categories: " + categories);
		String type = intent.getType();
		Log.d(TAG, "Type: " + type);
		String component = "";
		if (intent.getComponent() != null) {
			component = intent.getComponent().flattenToString();
		}
		Log.d(TAG, "Component: " + component);
		ContentResolver content = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(IntentsProvider.NAME, name);
		values.put(IntentsProvider.ACTION, action);
		values.put(IntentsProvider.DATA, data);
		values.put(IntentsProvider.CATEGORIES, categories);
		values.put(IntentsProvider.TYPE, type);
		values.put(IntentsProvider.COMPONENT, component);
		values.put(IntentsProvider.DATE_ADDED, System.currentTimeMillis());
		values.put(IntentsProvider.IS_SENDED, false);
		Bundle bun = intent.getExtras();
		if (bun != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<String> iterator = bun.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Log.d(TAG, "key: " + key + " : value: " + bun.get(key));
				sb.append(String.format("%s%s%s", key, EXTRAS_KEY_VALUE, String
						.valueOf(bun.get(key))));
				if (iterator.hasNext()) {
					sb.append(EXTRAS_SEPARATOR);
				}
			}
			values.put(IntentsProvider.EXTRAS, sb.toString());
		}
		Uri uri = content.insert(IntentsProvider.CONTENT_URI, values);
		long intentId = Long.parseLong(uri.getLastPathSegment());
		incrementPending();
		return intentId;
	}

	public void deleteIntent(long intentId) {
		ContentResolver content = mContext.getContentResolver();
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(intentId) };
		content.delete(IntentsProvider.CONTENT_URI, where, selectionArgs);
	}
	
	public void deletePending() {
		ContentResolver content = mContext.getContentResolver();
		String where = "is_sended = ?";
		String[] selectionArgs = new String[] { "0" };
		content.delete(IntentsProvider.CONTENT_URI, where, selectionArgs);
		resetPending();
	}
	
	public void deleteShared() {
		ContentResolver content = mContext.getContentResolver();
		String where = "is_sended = ?";
		String[] selectionArgs = new String[] { "1" };
		content.delete(IntentsProvider.CONTENT_URI, where, selectionArgs);
	}

	public IntentData[] getIntents(boolean pending) {
		ContentResolver content = mContext.getContentResolver();
		String selection = "is_sended = ?";
		String[] selectionArgs = new String[] { pending ? "0" : "1" };
		String sortOrder = pending ? "date_added DESC" : "date_sended DESC";
		Cursor cursor = content.query(IntentsProvider.CONTENT_URI,
				IntentsProvider.ALL, selection, selectionArgs, sortOrder);
		IntentData[] result = new IntentData[cursor.getCount()];
		int i = 0;
		while (cursor.moveToNext()) {
			IntentData data = new IntentData();
			data.Id = cursor.getLong(0);
			data.name = cursor.getString(1);
			data.action = cursor.getString(2);
			data.data = cursor.getString(3);
			String cs = cursor.getString(4);
			Set<String> set = new TreeSet<String>();
			if (cs != null && !cs.equals("")) {
				String[] ca = cs.split(",");
				for (String s : ca) {
					set.add(s);
				}
			}
			data.categories = set;
			data.type = cursor.getString(5);
			data.component = cursor.getString(6);
			Bundle extras = stringToBundle(cursor.getString(7));
			data.extras = extras;
			data.dateAdded = new Date(cursor.getLong(8));
			data.dateSended = new Date(cursor.getLong(9));
			data.isSended = cursor.getInt(10) == 0 ? false : true;
			result[i++] = data;
		}
		cursor.close();
		return result;
	}

	public Bundle stringToBundle(String s) {
		if (s == null || s.length() == 0) {
			return null;
		} else {
			Bundle result = new Bundle();
			String[] exs = s.split(EXTRAS_SEPARATOR);
			for (String ss : exs) {
				String[] kv = ss.split(EXTRAS_KEY_VALUE);
				Uri uri = Uri.parse(kv[1]);
				result.putParcelable(kv[0], uri);
			}
			return result;
		}
	}

	public void updateIntent(long intentId) {
		ContentResolver content = mContext.getContentResolver();
		String where = "_id = ?";
		String[] sel = new String[] { String.valueOf(intentId) };
		ContentValues values = new ContentValues();
		values.put(IntentsProvider.DATE_SENDED, System.currentTimeMillis());
		values.put(IntentsProvider.IS_SENDED, true);
		content.update(IntentsProvider.CONTENT_URI, values, where, sel);
	}

	public int getNewIntents() {
		return mContext.getSharedPreferences(SHARE_LATER_PREFS,
				Context.MODE_PRIVATE).getInt(NEW_INTENTS, 0);

	}

	public void setPendingIntents(int i) {
		Editor e = mContext.getSharedPreferences(SHARE_LATER_PREFS,
				Context.MODE_PRIVATE).edit();
		e.putInt(NEW_INTENTS, i);
		e.commit();
	}
	
	public void incrementPending() {
		int i = mContext.getSharedPreferences(SHARE_LATER_PREFS,
				Context.MODE_PRIVATE).getInt(NEW_INTENTS, 0);
		i++;
		setPendingIntents(i);
	}
	public void decrementPending() {
		int i = mContext.getSharedPreferences(SHARE_LATER_PREFS,
				Context.MODE_PRIVATE).getInt(NEW_INTENTS, 0);
		i--;
		setPendingIntents(i);
	}
	
	public void resetPending() {
		Editor e = mContext.getSharedPreferences(SHARE_LATER_PREFS,
				Context.MODE_PRIVATE).edit();
		e.clear();
		e.commit();
	}

}
