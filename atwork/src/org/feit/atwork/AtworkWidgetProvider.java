package org.feit.atwork;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.feit.atwork.data.Project;
import org.feit.atwork.data.ProjectsProcessor;
import org.feit.atwork.data.Session;
import org.feit.atwork.data.SessionsProcessor;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class AtworkWidgetProvider extends AppWidgetProvider {
	public static final String ACTION = "android.appwidget.action.APPWIDGET_UPDATE";
	private static final String TAG = "AtworkWidgetProveder";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		updateWidget(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (intent.getAction().equals(ACTION)) {
			updateWidget(context);
		}
	}

	public static void updateWidget(Context context) {
		context.startService(new Intent(context, UpdateService.class));
	}
	
	public static class UpdateService extends Service {
        @Override
        public void onStart(Intent intent, int startId) {
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, AtworkWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            
    		long lastProjectId;
    		if (Settings.getInstance(this).isUserSelected()) {
    			lastProjectId = Settings.getInstance(this).getHomeProjectId();
    		} else {
    			lastProjectId = Settings.getInstance(this)
    					.getLastActiveProjectId();
    		}
    		int[] appWidgetIds = manager.getAppWidgetIds(thisWidget);
    		ProjectsProcessor projectsProcessor = new ProjectsProcessor(this);
    		Project project = projectsProcessor.getProject(lastProjectId);
    		SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    		SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm");
    		final int N = appWidgetIds.length;

    		// Perform this loop procedure for each App Widget that belongs to this
    		// provider
    		for (int i = 0; i < N; i++) {
    			int appWidgetId = appWidgetIds[i];

    			// Create an Intent to launch ExampleActivity
    			Intent pi = new Intent(this, UpdateSessionReceiver.class);
    			PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
    					0, pi, 0);

    			// Get the layout for the App Widget and attach an on-click listener
    			// to the button
    			RemoteViews views = new RemoteViews(this.getPackageName(),
    					R.layout.widget_item);
    			views.setOnClickPendingIntent(R.id.btnStartStop, pendingIntent);
    			if (project != null) {
    				views.setTextViewText(R.id.name, project.name);
    				SessionsProcessor sessionsProcessor = new SessionsProcessor(
    						this);
    				Session lastSession = sessionsProcessor
    						.getLastSession(lastProjectId);
    				views.setTextViewText(R.id.start_time, mTimeFormat
    						.format(lastSession.start_time));
    				views.setTextViewText(R.id.start_date, mDateFormat
    						.format(lastSession.start_time));
    				if (lastSession.inProgress()) {
    					views.setTextViewText(R.id.end_time, this
    							.getString(R.string.in_progress));
    					views.setTextViewText(R.id.end_date, "");
    					views.setTextViewText(R.id.total, lastSession
    							.getTimeString(new Date()));
    					views.setCharSequence(R.id.btnStartStop, "setText", this
    							.getString(R.string.stop));
    				} else {
    					views.setTextViewText(R.id.end_time, mTimeFormat
    							.format(lastSession.end_time));
    					views.setTextViewText(R.id.end_date, mDateFormat
    							.format(lastSession.end_time));
    					views.setTextViewText(R.id.total, lastSession
    							.getTimeString());
    					views.setCharSequence(R.id.btnStartStop, "setText", this
    							.getString(R.string.start));
    				}
    			} else {
    				// still no project started
    				views.setTextViewText(R.id.name, "No projects or sesions");
    			}
    			// Tell the AppWidgetManager to perform an update on the current App
    			// Widget
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
