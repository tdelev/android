package org.feit.atwork.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class ProjectsProvider extends ContentProvider {

	private WorkDbAdapter mProjectsAdapter;
	private SQLiteDatabase mDb;

	public static final String PROVIDER_NAME = "org.feit.atwork.data.PROJECTS";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/projects");

	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String PROGRESS = "progress";
	public static final String CREATED = "created";

	public static final String[] ALL = { ID, NAME, PROGRESS, CREATED };

	public static final int PROJECTS = 1;
	public static final int PROJECT_ID = 2;
	private static final UriMatcher mUriMatcher;

	private static final String TAG = "ProjectsProvider";
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(PROVIDER_NAME, "intents", PROJECTS);
		mUriMatcher.addURI(PROVIDER_NAME, "intents/#", PROJECT_ID);
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		// ---get all events---
		case PROJECTS:
			return "vnd.android.cursor.dir/org.feit.atwork.data.PROJECTS";
			// ---get a particular event---
		case PROJECT_ID:
			return "vnd.android.cursor.item/org.feit.atwork.data.PROJECTS";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = mDb
				.insert(WorkDbAdapter.PROJECTS_TABLE, "project", values);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(CONTENT_URI.buildUpon(), rowId)
					.build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mProjectsAdapter = new WorkDbAdapter(getContext());
		mDb = mProjectsAdapter.getWritableDatabase();
		return (mDb == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(WorkDbAdapter.PROJECTS_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs,
				null, null, sortOrder);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = mDb.update(WorkDbAdapter.PROJECTS_TABLE, values, selection,
				selectionArgs);
		if (res > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return res;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int n = mDb.delete(WorkDbAdapter.PROJECTS_TABLE, selection,
				selectionArgs);
		if (n > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return n;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int n = super.bulkInsert(uri, values);
		if (n > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return n;
	}

}
