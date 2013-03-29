package com.tb.flagspotting;

import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

import android.app.Application;

public class ScoresApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ScoreloopManagerSingleton.init(this);
	}
}
