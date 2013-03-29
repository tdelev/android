package com.at.math.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ScoresProvider extends ContentProvider {

	private GameDbAdapter mGameDb;
	private SQLiteDatabase mDb;
	
	public static final String PROVIDER_NAME = 
		"com.at.math.data.SCORES";

	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + PROVIDER_NAME + "/scores");
	
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String SCORE = "score";
	public static final String DATE = "date";
	
	public static final String[] ALL = {
		ID,
		NAME,
		SCORE,
		DATE
	};
	
	public static final int SCORES = 1;
	public static final int SCORE_ID = 2;
	private static final UriMatcher mUriMatcher;

	private static final String TAG = "ScoresProvider";
    static{
       mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
       mUriMatcher.addURI(PROVIDER_NAME, "scores", SCORES);
       mUriMatcher.addURI(PROVIDER_NAME, "scores/#", SCORE_ID);      
    }	

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)){
        //---get all events---
        case SCORES:
           return "vnd.android.cursor.dir/com.at.math.data.scores";
        //---get a particular event---
        case SCORE_ID:                
           return "vnd.android.cursor.item/com.at.math.data.scores";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);        
     }   

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		mDb = mGameDb.getWritableDatabase();
		long rowId = mDb.insert(GameDbAdapter.SCORES_TABLE, "scores", values);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);		
	}

	@Override
	public boolean onCreate() {
		mGameDb = new GameDbAdapter(getContext());
		return true;
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(GameDbAdapter.SCORES_TABLE);
		mDb = mGameDb.getReadableDatabase();
		Cursor result = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		mDb = mGameDb.getWritableDatabase();
		int res = mDb.update(GameDbAdapter.SCORES_TABLE, values, selection, selectionArgs);
		if(res > 0) {
			getContext().getContentResolver().notifyChange(uri, null);			
		}
		return res;
	}
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		mDb = mGameDb.getWritableDatabase();
		int n = mDb.delete(GameDbAdapter.SCORES_TABLE, selection, selectionArgs);
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
