package org.feit.atwork;

import org.feit.atwork.data.Project;
import org.feit.atwork.data.ProjectsProcessor;
import org.feit.atwork.data.Session;
import org.feit.atwork.data.SessionsProcessor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateSessionReceiver extends BroadcastReceiver {
	public static final String ACTION = "org.feit.atwork.action.UPDATE_SESSION";

	@Override
	public void onReceive(Context context, Intent intent) {
		long lastProjectId = Settings.getInstance(context)
				.getLastActiveProjectId();
		ProjectsProcessor projectsProcessor = new ProjectsProcessor(context);
		SessionsProcessor sessionsProcessor = new SessionsProcessor(context);
		Project project = projectsProcessor.getProject(lastProjectId);
		if(project.progress) {
			Session session = sessionsProcessor.runningSession(lastProjectId);
			sessionsProcessor.endSession(session);
		} else {
			sessionsProcessor.startSession(lastProjectId);
		}
		AtworkWidgetProvider.updateWidget(context);		
	}

}
