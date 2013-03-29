package org.feit.jokesmk;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class JokesPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
