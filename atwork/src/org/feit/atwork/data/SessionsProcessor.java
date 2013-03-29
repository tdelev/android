package org.feit.atwork.data;

import java.util.Date;

import org.feit.atwork.AtworkWidgetProvider;
import org.feit.atwork.Settings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SessionsProcessor {
	private Context mContext;

	public SessionsProcessor(Context context) {
		mContext = context;
	}

	public Session[] getSessions(long projectId) {
		Session[] sessions;
		ContentResolver content = mContext.getContentResolver();
		String where = "project_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(projectId) }; 
		String sortOrder = "start_time DESC";
		Cursor cursor = content.query(SessionsProvider.CONTENT_URI, null, where,
				selectionArgs, sortOrder);
		sessions = new Session[cursor.getCount()];
		int i = 0;
		while (cursor.moveToNext()) {
			Session session = new Session();
			session.id = cursor.getLong(0);
			session.project_id = cursor.getInt(1);
			session.start_time = new Date(cursor.getLong(2));
			session.end_time = new Date(cursor.getLong(3));
			session.comment = cursor.getString(4);
			sessions[i] = session;
			i++;
		}
		cursor.close();
		return sessions;
	}

	public Session startSession(long projectId) {
		Session session = new Session();
		session.project_id = projectId;
		long start = System.currentTimeMillis();
		session.start_time = new Date(start);
		session.end_time = session.start_time;
		ContentValues values = new ContentValues();
		values.put(SessionsProvider.PROJECT_ID, projectId);
		values.put(SessionsProvider.START_TIME, start);
		values.put(SessionsProvider.END_TIME, start);
		ContentResolver content = mContext.getContentResolver();
		Uri uri = content.insert(SessionsProvider.CONTENT_URI, values);
		long sessionId = Long.parseLong(uri.getLastPathSegment());
		session.id = sessionId;
		values.clear();
		values.put(ProjectsProvider.PROGRESS, true);
		content.update(ProjectsProvider.CONTENT_URI, values, "_id = ?", new String[] { String.valueOf(projectId)});
		Settings.getInstance(mContext).setLastActiveProjectId(projectId);
		return session;
	}
	
	public Session runningSession(long projectId) {
		ContentResolver content = mContext.getContentResolver();
		String where = "project_id = ? AND start_time = end_time";
		String[] selectionArgs = new String[] { String.valueOf(projectId) }; 
		Cursor cursor = content.query(SessionsProvider.CONTENT_URI, null, where,
				selectionArgs, null);
		Session session = null;
		if(cursor.moveToFirst()) {
			session = new Session();
			session.id = cursor.getLong(0);
			session.project_id = cursor.getInt(1);
			session.start_time = new Date(cursor.getLong(2));
			session.end_time = new Date(cursor.getLong(3));
			session.comment = cursor.getString(4);
		}
		cursor.close();
		return session;
	}
	
	public Session getLastSession(long projectId) {
		ContentResolver content = mContext.getContentResolver();
		String where = "project_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(projectId) };
		String sortOrder = "_id DESC";
		Cursor cursor = content.query(SessionsProvider.CONTENT_URI, null, where,
				selectionArgs, sortOrder);
		Session session = null;
		if(cursor.moveToFirst()) {
			session = new Session();
			session.id = cursor.getLong(0);
			session.project_id = cursor.getInt(1);
			session.start_time = new Date(cursor.getLong(2));
			session.end_time = new Date(cursor.getLong(3));
			session.comment = cursor.getString(4);
		}
		cursor.close();
		return session;
	}
	
	public Session getSession(long sessionId) {
		ContentResolver content = mContext.getContentResolver();
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(sessionId) }; 
		Cursor cursor = content.query(SessionsProvider.CONTENT_URI, null, where,
				selectionArgs, null);
		Session session = null;
		if(cursor.moveToFirst()) {
			session = new Session();
			session.id = cursor.getLong(0);
			session.project_id = cursor.getInt(1);
			session.start_time = new Date(cursor.getLong(2));
			session.end_time = new Date(cursor.getLong(3));
			session.comment = cursor.getString(4);
		}
		cursor.close();
		return session;
	}
	
	public void endSession(Session session) {
		ContentResolver content = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(SessionsProvider.END_TIME, System.currentTimeMillis());
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(session.id) };
		content.update(SessionsProvider.CONTENT_URI, values, where, selectionArgs);
		values.clear();
		values.put(ProjectsProvider.PROGRESS, false);
		content.update(ProjectsProvider.CONTENT_URI, values, "_id = ?", new String[] { String.valueOf(session.project_id)});
		Settings.getInstance(mContext).setLastActiveProjectId(session.project_id);
	}
	
	public void updateSession(Session session) {
		ContentValues values = new ContentValues();
		values.put(SessionsProvider.START_TIME, session.start_time.getTime());
		values.put(SessionsProvider.END_TIME, session.end_time.getTime());
		values.put(SessionsProvider.COMMENT, session.comment);
		ContentResolver content = mContext.getContentResolver();
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(session.id) };
		content.update(SessionsProvider.CONTENT_URI, values, where, selectionArgs);		
	}

	public void deleteSession(long id) {
		ContentResolver content = mContext.getContentResolver();
		content.delete(SessionsProvider.CONTENT_URI, "_id = ?",
				new String[] { String.valueOf(id) });
	}

	public void deleteAll() {
		ContentResolver content = mContext.getContentResolver();
		content.delete(SessionsProvider.CONTENT_URI, null, null);
	}
}
