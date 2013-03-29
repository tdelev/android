package org.feit.atwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Settings {
	public static final String PROJECTS = "projects";
	public static final String LAST_ACTIVE = "last";
	public static final String HOME_PROJECT = "home";
	public static final String USER_SELECTED = "us";
	private static final String TAG = "Settings";
	private static Settings mInstance;
	private Context mContext;

	private Settings(Context context) {
		mContext = context;
	}

	public static Settings getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Settings(context);
		}
		return mInstance;
	}

	public long getLastActiveProjectId() {
		return mContext.getSharedPreferences(PROJECTS, Context.MODE_PRIVATE)
				.getLong(LAST_ACTIVE, -1);
	}

	public void setLastActiveProjectId(long projectId) {
		Editor e = mContext
				.getSharedPreferences(PROJECTS, Context.MODE_PRIVATE).edit();
		e.putLong(LAST_ACTIVE, projectId);
		e.commit();
		AtworkWidgetProvider.updateWidget(mContext);
	}
	
	public long getHomeProjectId() {
		return mContext.getSharedPreferences(PROJECTS, Context.MODE_PRIVATE)
				.getLong(HOME_PROJECT, -1);
	}

	public void setHomeProjectId(long projectId) {
		Editor e = mContext
				.getSharedPreferences(PROJECTS, Context.MODE_PRIVATE).edit();
		e.putLong(HOME_PROJECT, projectId);
		e.commit();
		AtworkWidgetProvider.updateWidget(mContext);
	} 
	
	public boolean isUserSelected() {
		return mContext.getSharedPreferences(PROJECTS, Context.MODE_PRIVATE)
				.getBoolean(USER_SELECTED, false);
	}

	public void setUserSelected(boolean selected) {
		Editor e = mContext
				.getSharedPreferences(PROJECTS, Context.MODE_PRIVATE).edit();
		e.putBoolean(USER_SELECTED, selected);
		e.commit();
	} 
	
	
}
