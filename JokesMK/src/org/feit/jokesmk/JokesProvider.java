package org.feit.jokesmk;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class JokesProvider extends ContentProvider {
	private static final String TAG = "JokesProvider";

	private JokesDbAdapter mJokesAdapter;
	private SQLiteDatabase mDb;
	public static final String PROVIDER = "/jokes";
	public static final String PROVIDER_NAME = "org.feit.jokesmk.JOKES";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + PROVIDER);

	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String TEXT = "text";
	public static final String CATEGORY_ID = "category_id";
	public static final String RATING = "rating";
	public static final String FAVORITE = "favorite";
	public static final String VIEWS = "views";

	public static final String[] ALL = { ID, NAME, TEXT, CATEGORY_ID, RATING,
			VIEWS, FAVORITE };

	public static final int JOKES = 1;
	public static final int JOKE_ID = 2;
	private static final UriMatcher mUriMatcher;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(PROVIDER_NAME, "jokes", JOKES);
		mUriMatcher.addURI(PROVIDER_NAME, "jokes/#", JOKE_ID);
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		// ---get all events---
		case JOKES:
			return "vnd.android.cursor.dir/org.feit.jokesmk.jokes";
			// ---get a particular event---
		case JOKE_ID:
			return "vnd.android.cursor.item/org.feit.jokesmk.jokes";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = mDb.insert(JokesDbAdapter.JOKES_TABLE, "jokes", values);
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
		mJokesAdapter = new JokesDbAdapter(getContext());
		mDb = mJokesAdapter.getWritableDatabase();
		return (mDb == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(JokesDbAdapter.JOKES_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs,
				null, null, sortOrder);
		return result;
	}

	public void loadDb(Context context) {
		mJokesAdapter = new JokesDbAdapter(context);
		mDb = mJokesAdapter.getWritableDatabase();
	}

	public Cursor queryLimit(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder, String limit) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(JokesDbAdapter.JOKES_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs,
				null, null, sortOrder, limit);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = mDb.update(JokesDbAdapter.JOKES_TABLE, values, selection,
				selectionArgs);
		if (res > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return res;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int n = mDb
				.delete(JokesDbAdapter.JOKES_TABLE, selection, selectionArgs);
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

	public void close() {
		mDb.close();
		mJokesAdapter.close();
	}

}
