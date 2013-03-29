package org.feit.jokesmk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RandomJokeReceiver extends BroadcastReceiver {
	public static final String ACTION = "org.feit.mkjokes.action.RANDOM_JOKE";

	@Override
	public void onReceive(Context context, Intent intent) {
		MkJokesWidgetProvider.updateWidget(context);		
	}

}
