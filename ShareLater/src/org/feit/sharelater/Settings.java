package org.feit.sharelater;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setTitle(String.format("%s - %s", getString(R.string.app_name), getString(R.string.settings)));
	}
}
