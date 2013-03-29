package org.feit.jokesmk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/***
 * Adapter class to sqlite database
 * 
 * @author Tomche
 * 
 */
public class JokesDbAdapter {

	private static final String TAG = "JokesDbAdapter";

	// The Android's default system path of your application database.
	private static String DATABASE_PATH = "/data/data/org.feit.jokesmk/databases/";

	public static final String JOKES_TABLE = "jokes";
	public static final String CATEGORIES_TABLE = "categories";
	private static final String DATABASE_NAME = "vicotekadb.sqlite";
	private static final int DATABASE_VERSION = 1;
	private final Context mContext;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Creates a empty database on the system and rewrites it with your own
		 * database.
		 * */
		public void createDataBase() throws IOException {
			boolean dbExist = checkDataBase();
			int dbVersion = Settings.getInstance(mContext).getDBVersion();
			if (!dbExist || dbVersion == Settings.CURRENT_DB_VERSION) {
				// By calling this method and empty database will be created into
				// the default system path of your application so we are going
				// be able to overwrite that database with our database.
				this.getWritableDatabase();
				try {
					copyDataBase();
				} catch (IOException e) {
					throw e;
					//throw new Error("Error copying database");
				}
				Settings.getInstance(mContext).incrementDBVersion();
			}

		}

		/**
		 * Check if the database already exist to avoid re-copying the file each
		 * time you open the application.
		 * 
		 * @return true if it exists, false if it doesn't
		 */
		private boolean checkDataBase() {
			SQLiteDatabase checkDB = null;
			try {
				String myPath = DATABASE_PATH + DATABASE_NAME;
				checkDB = SQLiteDatabase.openDatabase(myPath, null,
						SQLiteDatabase.OPEN_READONLY);
			} catch (SQLiteException e) {
				// database does't exist yet.
			}
			if (checkDB != null) {
				checkDB.close();
			}
			return checkDB != null ? true : false;
		}

		/**
		 * Copies your database from your local assets-folder to the just
		 * created empty database in the system folder, from where it can be
		 * accessed and handled. This is done by transfering bytestream.
		 * */
		private void copyDataBase() throws IOException {
			InputStream databaseInput = null;
		    String outFileName = DATABASE_PATH + DATABASE_NAME;
		    OutputStream databaseOutput = new FileOutputStream(outFileName);

		    byte[] buffer = new byte[1024];
		    int length;

		    databaseInput = mContext.getAssets().open("x00");
		    while((length = databaseInput.read(buffer)) > 0) {
		        databaseOutput.write(buffer, 0, length);
		        databaseOutput.flush();
		    }
		    databaseInput.close();

		    databaseInput = mContext.getAssets().open("x01");
		    while((length = databaseInput.read(buffer)) > 0) {
		        databaseOutput.write(buffer);
		        databaseOutput.flush();
		    }
		    databaseInput.close();
		    databaseInput = mContext.getAssets().open("x02");
		    while((length = databaseInput.read(buffer)) > 0) {
		        databaseOutput.write(buffer);
		        databaseOutput.flush();
		    }
		    databaseInput.close();
		    databaseOutput.flush();
		    databaseOutput.close();

		}

		public void openDataBase() throws SQLException {
			// Open the database
			String myPath = DATABASE_PATH + DATABASE_NAME;
			mDb = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public JokesDbAdapter(Context ctx) {
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
	public JokesDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		try {
			mDbHelper.createDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mDbHelper.openDataBase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}

	public SQLiteDatabase getWritableDatabase() {
		mDbHelper = new DatabaseHelper(mContext);
		try {
			mDbHelper.createDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mDbHelper.openDataBase();
		return mDb;
	}

}
