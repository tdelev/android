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

public class CategoriesProvider extends ContentProvider {
	private static final String TAG = "CategoriesProvider";

	private JokesDbAdapter mJokesAdapter;
	private SQLiteDatabase mDb;
	public static final String PROVIDER = "/categories";
	public static final String PROVIDER_NAME = 
		"org.feit.jokesmk.CATEGORIES";

	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + PROVIDER_NAME + PROVIDER);
	
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String TOTAL = "total";
	
	public static final String[] ALL = {
		ID,
		NAME,
		TOTAL
	};
	
	public static final int CATEGORIES = 1;
	public static final int CATEGORY_ID = 2;
	private static final UriMatcher mUriMatcher;

    static{
       mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
       mUriMatcher.addURI(PROVIDER_NAME, "categories", CATEGORIES);
       mUriMatcher.addURI(PROVIDER_NAME, "categories/#", CATEGORY_ID);      
    }	

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)){
        //---get all events---
        case CATEGORIES:
           return "vnd.android.cursor.dir/org.feit.jokesmk.categories";
        //---get a particular event---
        case CATEGORY_ID:                
           return "vnd.android.cursor.item/org.feit.jokesmk.categories";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);        
     }   

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = mDb.insert(JokesDbAdapter.CATEGORIES_TABLE, "categories", values);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(CONTENT_URI.buildUpon(), rowId).build();
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
		qb.setTables(JokesDbAdapter.CATEGORIES_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
		return result;
	}
	
	public void loadDb(Context context) {
		mJokesAdapter = new JokesDbAdapter(context);
		mDb = mJokesAdapter.getWritableDatabase();
	}
	
	public Cursor queryLimit(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder, String limit) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(JokesDbAdapter.CATEGORIES_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder, limit);
		return result;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = mDb.update(JokesDbAdapter.CATEGORIES_TABLE, values, selection, selectionArgs);
		if(res > 0) {
			getContext().getContentResolver().notifyChange(uri, null);			
		}
		return res;
	}
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int n = mDb.delete(JokesDbAdapter.CATEGORIES_TABLE, selection, selectionArgs);
		if(n > 0) {
			getContext().getContentResolver().notifyChange(uri, null);			
		}
		return n;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {			
		int n = super.bulkInsert(uri, values);
		if(n > 0){
			getContext().getContentResolver().notifyChange(uri, null);			
		}
		return n;
	}
	
	

}
