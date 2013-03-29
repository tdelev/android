package org.feit.atwork.data;

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
public class WorkDbAdapter {

	private static final String TAG = "IntentsDbAdapter";

	static final String DATABASE_NAME = "atwork";
	static final int DATABASE_VERSION = 1;
	static final String REMINDERS_TABLE = "reminders";
	static final String PROJECTS_TABLE = "projects";
	static final String SESSIONS_TABLE = "sessions";

	static final String ID = "_id";
	static final String ENABLED = "enabled";
	static final String START_TIME = "start_time";
	static final String PERIOD_MINUTES = "period_minutes";
	static final String PERIOD_HOURS = "period_hours";
	static final String MESSAGE = "message";
	static final String ALARM = "alarm";

	static final String NAME = "name";
	static final String PROGRESS = "progress";
	static final String CREATED = "created";

	private final Context mContext;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String REMINDERS_TABLE_CREATE = "CREATE  TABLE "
				+ REMINDERS_TABLE
				+ "(_id INTEGER PRIMARY KEY  AUTOINCREMENT , "
				+ "enabled BOOL NOT NULL , "
				+ "start_time DATETIME NOT NULL , "
				+ "period_minutes INTEGER NOT NULL , "
				+ "period_hours INTEGER NOT NULL , "
				+ "repeating BOOL NOT NULL , " + "message TEXT NOT NULL ,"
				+ "alarm VARCHAR NOT NULL );";

		private static final String PROJECTS_TABLE_CREATE = "CREATE TABLE "
				+ PROJECTS_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "name VARCHAR NOT NULL ," + "progress BOOL NOT NULL ,"
				+ "created DATETIME NOT NULL);";

		private static final String SESSIONS_TABLE_CREATE = "CREATE TABLE "
				+ SESSIONS_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "project_id INTEGER NOT NULL ,"
				+ "start_time DATETIME NOT NULL ,"
				+ "end_time DATETIME NOT NULL, comment TEXT);";

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(REMINDERS_TABLE_CREATE);
			db.execSQL(PROJECTS_TABLE_CREATE);
			db.execSQL(SESSIONS_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + REMINDERS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + PROJECTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SESSIONS_TABLE);
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
	public WorkDbAdapter(Context ctx) {
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
	public WorkDbAdapter open() throws SQLException {
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
