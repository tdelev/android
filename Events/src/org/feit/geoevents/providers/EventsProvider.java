package org.feit.geoevents.providers;

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

public class EventsProvider extends ContentProvider {

	private EventsDbAdapter mEventsDb;
	private SQLiteDatabase mDb;
	
	public static final String PROVIDER_NAME = 
		"org.feit.geoevents.providers.EVENTS";

	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + PROVIDER_NAME + "/events");
	
	public static final String ID = "_id";
	public static final String USER_ID = "user_id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "date";
	public static final String START_TIME = "start_time";
	public static final String END_TIME = "end_time";
	public static final String LATITUDE = "lat";
	public static final String LONGITUDE = "lng";
	public static final String ADDRESS = "address";
	public static final String LOCATION_TYPE = "loc_type";
	public static final String PUBLIC = "public";
	public static final String STATE = "state";
	
	public static final String[] ALL = {
		ID,
		USER_ID,
		NAME,
		DESCRIPTION,
		START_TIME,
		END_TIME,
		LATITUDE,
		LONGITUDE,
		ADDRESS,
		LOCATION_TYPE,
		PUBLIC
	};
	
	public static final int EVENTS = 1;
	public static final int EVENTS_ID = 2;
	private static final UriMatcher mUriMatcher;

	private static final String TAG = "EventsProvider";
    static{
       mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
       mUriMatcher.addURI(PROVIDER_NAME, "events", EVENTS);
       mUriMatcher.addURI(PROVIDER_NAME, "events/#", EVENTS_ID);      
    }	

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)){
        //---get all events---
        case EVENTS:
           return "vnd.android.cursor.dir/org.feit.geoevents.providers.events";
        //---get a particular event---
        case EVENTS_ID:                
           return "vnd.android.cursor.item/org.feit.geoevents.providers.events";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);        
     }   

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = mDb.insert(EventsDbAdapter.EVENTS_TABLE, "event", values);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);		
	}

	@Override
	public boolean onCreate() {
		mEventsDb = new EventsDbAdapter(getContext());
		mDb = mEventsDb.getWritableDatabase();
		return (mDb == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EventsDbAdapter.EVENTS_TABLE);
		Cursor result = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = mDb.update(EventsDbAdapter.EVENTS_TABLE, values, selection, selectionArgs);
		if(res > 0) {
			getContext().getContentResolver().notifyChange(uri, null);			
		}
		return res;
	}
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int n = mDb.delete(EventsDbAdapter.EVENTS_TABLE, selection, selectionArgs);
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
