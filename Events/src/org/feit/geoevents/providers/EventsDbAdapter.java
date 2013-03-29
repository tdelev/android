package org.feit.geoevents.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/***
 * Adapter class to sqlite database
 * 
 * @author Tomche
 * 
 */
public class EventsDbAdapter {

	private static final String TAG = "";

	public static final String EVENTS_TABLE = "events";
	public static final String VENUES_TABLE = "venues";
	public static final String CATEGORIES_TABLE = "categories";
	public static final String METROS_TABLE = "metros";

	private static final String DATABASE_CREATE_EVENTS = "create table "
			+ EVENTS_TABLE + "(_id integer primary key autoincrement, "
			+ "name varchar not null, "
			+ "venue_id integer not null, "
			+ "category_id varchar not null, "
			+ "start_date integer not null, "
			+ "end_date integer, "
			+ "start_time integer, "
			+ "end_time integer, "
			+ "description text, " 
			+ "url varchar, " 
			+ "personal integer default 0, "
			+ "selfpromotion integer default 0, "
			+ "ticket_url varchar, "
			+ "ticket_price varchar, "
			+ "ticket_free integer default 0);";
	
	
	private static final String DATABASE_CREATE_VENUES = "create table "
		+ VENUES_TABLE + "(_id integer primary key autoincrement, "
		+ "venuename varchar not null, "
		+ "venueaddress varchar not null, "
		+ "venuecity varchar not null, "
		+ "metro_id integer not null, "
		+ "location varchar not null, "
		+ "venuezip varchar, "
		+ "venuephone varchar, "
		+ "venueurl varchar, "
		+ "venuedescription text, " 
		+ "private integer default 0);";
	
	private static final String DATABASE_CREATE_CATEGORIES= "create table "
		+ CATEGORIES_TABLE 
		+ "(_id integer primary key autoincrement, "
		+ "name varchar not null, "
		+ "description varchar not null);";
	
	private static final String DATABASE_CREATE_METROS= "create table "
		+ METROS_TABLE 
		+ "(_id integer primary key autoincrement, "
		+ "name varchar not null, "
		+ "code varchar not null, "
		+ "state_id integer not null, "
		+ "state_name varchar not null, "
		+ "state_code varchar not null, "
		+ "country_id integer not null, "
		+ "country_name varchar not null, "
		+ "country_code varchar not null);";

	private static final String DATABASE_NAME = "eventsdb";
	private static final int DATABASE_VERSION = 1;
	private final Context mContext;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_EVENTS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public EventsDbAdapter(Context ctx) {
		this.mContext = ctx;
	}

	/**
	 * Open the events database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public EventsDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public SQLiteDatabase getWritableDatabase() {
		mDbHelper = new DatabaseHelper(mContext);
		return mDbHelper.getWritableDatabase();
	}

}
