package com.at.math;

import android.app.Application;

import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

public class Scoreloop extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
	       ScoreloopManagerSingleton.init(this, "ncCnN4Bs/OjdI0QXWOVi/rWX9TEC7OTex7G5k6FfonwQHV+NBYIbFg==");
    }

    @Override
    public void onTerminate() {
            super.onTerminate();
            ScoreloopManagerSingleton.destroy();
    }
}