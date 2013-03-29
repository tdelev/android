package org.feit.sharelater;

import android.content.Context;
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
public class IntentsDbAdapter {

	private static final String TAG = "IntentsDbAdapter";

	public static final String INTENTS_TABLE = "intents";

	private static final String DATABASE_CREATE_INTENTS = "create table "
			+ INTENTS_TABLE + "(_id integer primary key autoincrement, "
			+ "name text not null, " 
			+ "action text not null, " 
			+ "data text not null, "
			+ "categories text null, " 
			+ "type text not null, "
			+ "component text not null, " 
			+ "extras text not null, " 
			+ "date_added integer not null, "
			+ "date_sended integer null, "
			+ "is_sended integer not null);";

	private static final String DATABASE_NAME = "intentsdb";
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
			db.execSQL(DATABASE_CREATE_INTENTS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + INTENTS_TABLE);
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
	public IntentsDbAdapter(Context ctx) {
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
	public IntentsDbAdapter open() throws SQLException {
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
