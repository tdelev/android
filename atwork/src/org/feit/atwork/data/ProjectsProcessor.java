package org.feit.atwork.data;

import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ProjectsProcessor {
	private Context mContext;

	public ProjectsProcessor(Context context) {
		mContext = context;
	}

	public Project[] getProjects() {
		Project[] projects;
		ContentResolver content = mContext.getContentResolver();
		String sortOrder = "name ASC";
		Cursor cursor = content.query(ProjectsProvider.CONTENT_URI, null, null,
				null, sortOrder);
		projects = new Project[cursor.getCount()];
		int i = 0;
		while (cursor.moveToNext()) {
			Project p = new Project();
			p.Id = cursor.getLong(0);
			p.name = cursor.getString(1);
			p.progress = cursor.getInt(2) == 1;
			p.created = new Date(cursor.getLong(3));
			projects[i] = p;
			i++;
		}
		cursor.close();
		return projects;
	}
	
	public Project getProject(long projectId) {
		ContentResolver content = mContext.getContentResolver();
		String where = "_id = ?";
		String[] selectionArgs = new String[] { String.valueOf(projectId) }; 
		Cursor cursor = content.query(ProjectsProvider.CONTENT_URI, null, where,
				selectionArgs, null);
		Project project = null;
		if(cursor.moveToFirst()) {
			project = new Project();
			project.Id = cursor.getLong(0);
			project.name = cursor.getString(1);
			project.progress = cursor.getInt(2) == 1;
		}
		cursor.close();
		return project;
	}

	public void saveProject(String name) {
		ContentValues values = new ContentValues();
		values.put(ProjectsProvider.NAME, name);
		values.put(ProjectsProvider.PROGRESS, false);
		values.put(ProjectsProvider.CREATED, System.currentTimeMillis());
		ContentResolver content = mContext.getContentResolver();
		content.insert(ProjectsProvider.CONTENT_URI, values);
	}
	
	public void updateProject(long projectId, String name) {
		ContentValues values = new ContentValues();
		values.put(ProjectsProvider.NAME, name);
		ContentResolver content = mContext.getContentResolver();
		content.update(ProjectsProvider.CONTENT_URI, values, "_id = ?", new String[] { String.valueOf(projectId)});
	}

	public void deleteProject(long id) {
		ContentResolver content = mContext.getContentResolver();
		content.delete(SessionsProvider.CONTENT_URI, "project_id = ?",
				new String[] { String.valueOf(id) });
		content.delete(ProjectsProvider.CONTENT_URI, "_id = ?",
				new String[] { String.valueOf(id) });
	}

	public void deleteAll() {
		ContentResolver content = mContext.getContentResolver();
		content.delete(ProjectsProvider.CONTENT_URI, null, null);
	}
}
