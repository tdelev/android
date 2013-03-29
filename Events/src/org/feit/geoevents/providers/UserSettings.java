package org.feit.geoevents.providers;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class UserSettings {
	public static final String EVENTS = "events";
	public static final String TOKEN = "token";
	public static final String AUTHENTICATED = "a";
	public static final String USER_ID = "user_id";
	public static final String USERNAME = "username";
	public static final String NAME = "name";
	
	private static UserSettings mInstance;
	private Context mContext;

	private UserSettings(Context context) {
		mContext = context;
	}

	public static UserSettings getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new UserSettings(context);
		}
		return mInstance;
	}

	public boolean isAuthenticated() {
		return mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE)
				.getBoolean(AUTHENTICATED, false);
	}
	
	public void setAuthenticated() {
		Editor e = mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE).edit();
		e.putBoolean(AUTHENTICATED, true);
		e.commit();
	}
	
	public String getToken() {
		return mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE)
		.getString(TOKEN, "");
	}
	
	public void setToken(String token) {
		Editor e = mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE).edit();
		e.putString(TOKEN, token);
		e.commit();
	}
	
	public String getUsername() {
		return mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE)
		.getString(USERNAME, "");
	}
	
	public void setUsername(String username) {
		Editor e = mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE).edit();
		e.putString(USERNAME, username);
		e.commit();
	}
	
	public String getName() {
		return mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE)
		.getString(NAME, "");
	}
	
	public void setName(String name) {
		Editor e = mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE).edit();
		e.putString(NAME, name);
		e.commit();
	}
	
	public int getUserId() {
		return mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE)
		.getInt(USER_ID, -1);
	}
	
	public void setUserId(int user_id) {
		Editor e = mContext.getSharedPreferences(EVENTS, Context.MODE_PRIVATE).edit();
		e.putInt(USER_ID, user_id);
		e.commit();
	}
	
}
