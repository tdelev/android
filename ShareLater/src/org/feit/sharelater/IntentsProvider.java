package org.feit.sharelater;

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

public class IntentsProvider extends ContentProvider {

	private IntentsDbAdapter mIntentsAdapter;
	private SQLiteDatabase mDb;
	
	public static final String PROVIDER_NAME = 
		"org.feit.sharelater.INTENTS";

	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + PROVIDER_NAME + "/intents");
	
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String ACTION = "action";
	public static final String DATA = "data";
	public static final String CATEGORIES = "categories";
	public static final String TYPE = "type";
	public static final String COMPONENT = "component";
	public static final String EXTRAS = "extras";
	public static final String DATE_ADDED = "date_added";
	public static final String DATE_SENDED = "date_sended";
	public static final String IS_SENDED = "is_sended";
	
	public static final String[] ALL = {
		ID,
		NAME,
		ACTION,
		DATA,
		CATEGORIES,
		TYPE,
		COMPONENT,
		EXTRAS,
		DATE_ADDED,
		DATE_SENDED,
		IS_SENDED
	};
	
	public static final int INTENTS = 1;
	public static final int INTENT_ID = 2;
	private static final UriMatcher mUriMatcher;

	private static final String TAG = "IntentsProvider";
    static{
       mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
       mUriMatcher.addURI(PROVIDER_NAME, "intents", INTENTS);
       mUriMatcher.addURI(PROVIDER_NAME, "intents/#", INTENT_ID);      
    }	

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)){
        //---get all events---
        case INTENTS:
           return "vnd.android.cursor.dir/org.feit.sharelater.intents";
        //---get a particular event---
        case INTENT_ID:                
           return "vnd.android.cursor.item/org.feit.sharelater.intents";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);        
     }   

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = mDb.insert(IntentsDbAdapter.INTENTS_TABLE, "event", values);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);		
	}

	@Override
	public boolean onCreate() {
		mIntentsAdapter = new IntentsDbAdapter(getContext());
		mDb = mIntentsAdapter.getWritableDatabase();
		return (mDb == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(IntentsDbAdapter.INTENTS_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = mDb.update(IntentsDbAdapter.INTENTS_TABLE, values, selection, selectionArgs);
		if(res > 0) {
			getContext().getContentResolver().notifyChange(uri, null);			
		}
		return res;
	}
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int n = mDb.delete(IntentsDbAdapter.INTENTS_TABLE, selection, selectionArgs);
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
