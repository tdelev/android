package org.feit.jokesmk;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class MkJokesWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "MkJokesWidgetProveder";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		updateWidget(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	public static void updateWidget(Context context) {
		context.startService(new Intent(context, UpdateService.class));
	}

	public static class UpdateService extends Service {
		@Override
		public void onStart(Intent intent, int startId) {
			ComponentName thisWidget = new ComponentName(this,
					MkJokesWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);

			int[] appWidgetIds = manager.getAppWidgetIds(thisWidget);
			JokesProcessor jokesProcessor = new JokesProcessor(this);
			final int N = appWidgetIds.length;
			Joke joke = jokesProcessor.getRandomJoke();
			// Perform this loop procedure for each App Widget that belongs to
			// this
			// provider
			for (int i = 0; i < N; i++) {
				int appWidgetId = appWidgetIds[i];
				// Create an Intent to launch ExampleActivity
				Intent pi = new Intent(this, RandomJokeReceiver.class);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
						0, pi, 0);
				Intent jokeIntent = new Intent(this, JokeView.class);
				PendingIntent pendingJokeIntent = PendingIntent.getActivity(
						this, 0, jokeIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
				// Get the layout for the App Widget and attach an on-click
				// listener
				// to the button
				RemoteViews views = new RemoteViews(this.getPackageName(),
						R.layout.widget_item);
				views
						.setOnClickPendingIntent(R.id.btnNextRandom,
								pendingIntent);
				views.setTextViewText(R.id.name, joke.name);
				views.setTextViewText(R.id.text, joke.text);
				views.setOnClickPendingIntent(R.id.text, pendingJokeIntent);
				manager.updateAppWidget(appWidgetId, views);
			}
			stopSelf();
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

	}
}
