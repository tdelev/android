package com.at.math;

import android.content.Context;
import android.content.SharedPreferences;

public class GamePrefs {
	public static final String GAME_PREFS = "game_prefs";
	public static final String LAST_SAVED_NAME = "name";
	public static final String MARKET_LINK = "market://details?id=com.at.math";
	public static final String RATED_PREF_KEY = "rated";
	public static final String RATED_APP_USE_COUNT_KEY = "times_used";


	private Context mContext;
	private static GamePrefs mInstance;
	private GamePrefs(Context context) {
		mContext = context;
	}
	
	public static GamePrefs getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new GamePrefs(context);
		}
		return mInstance;
	}
	
	
	public String getLastSavedName() {
		SharedPreferences prefs = mContext.getSharedPreferences(GAME_PREFS,
				Context.MODE_PRIVATE);
		return prefs.getString(LAST_SAVED_NAME, "");	
	}
	
	public void setName(String name) {
		SharedPreferences prefs = mContext.getSharedPreferences(GAME_PREFS,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(LAST_SAVED_NAME, name);
		editor.commit();
	}
	
	public boolean isRated() {
		SharedPreferences prefs = mContext.getSharedPreferences(GAME_PREFS,
				Context.MODE_PRIVATE);
		return prefs.getBoolean(RATED_PREF_KEY, false);
	}
	
	public void setRated() {
		SharedPreferences prefs = mContext.getSharedPreferences(GAME_PREFS,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(RATED_PREF_KEY, true);
		editor.commit();
	}
	
	public int checkUsege() {
		SharedPreferences prefs = mContext.getSharedPreferences(GAME_PREFS,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		int used = prefs.getInt(RATED_APP_USE_COUNT_KEY, 0);
		editor.putInt(RATED_APP_USE_COUNT_KEY, used + 1);
		editor.commit();
		return used;
	}
	
	public void resetUsage() {
		SharedPreferences prefs = mContext.getSharedPreferences(GAME_PREFS,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(RATED_APP_USE_COUNT_KEY, 0);
		editor.commit();
	}
	
	public SharedPreferences getPrefs() {
		return mContext.getSharedPreferences(GAME_PREFS, Context.MODE_PRIVATE);
	}
}
