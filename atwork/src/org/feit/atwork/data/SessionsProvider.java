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

public class SessionsProvider extends ContentProvider {

	private WorkDbAdapter mSessionsProvider;
	private SQLiteDatabase mDb;

	public static final String PROVIDER_NAME = "org.feit.atwork.data.SESSIONS";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/sessions");

	public static final String ID = "_id";
	public static final String PROJECT_ID = "project_id";
	public static final String START_TIME = "start_time";
	public static final String END_TIME = "end_time";
	public static final String COMMENT = "comment";


	public static final String[] ALL = { ID, PROJECT_ID, START_TIME, END_TIME, COMMENT };

	public static final int SESSIONS = 1;
	public static final int SESSION_ID = 2;
	private static final UriMatcher mUriMatcher;

	private static final String TAG = "ProjectsProvider";
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(PROVIDER_NAME, "intents", SESSIONS);
		mUriMatcher.addURI(PROVIDER_NAME, "intents/#", SESSION_ID);
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		// ---get all events---
		case SESSIONS:
			return "vnd.android.cursor.dir/org.feit.atwork.data.SESSIONS";
			// ---get a particular event---
		case SESSION_ID:
			return "vnd.android.cursor.item/org.feit.atwork.data.SESSIONS";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = mDb
				.insert(WorkDbAdapter.SESSIONS_TABLE, "session", values);
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
		mSessionsProvider = new WorkDbAdapter(getContext());
		mDb = mSessionsProvider.getWritableDatabase();
		return (mDb == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(WorkDbAdapter.SESSIONS_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs,
				null, null, sortOrder);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = mDb.update(WorkDbAdapter.SESSIONS_TABLE, values, selection,
				selectionArgs);
		if (res > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return res;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int n = mDb.delete(WorkDbAdapter.SESSIONS_TABLE, selection,
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
