package org.feit.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.feit.geoevents.providers.Event;
import org.feit.geoevents.providers.EventsProcessor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	ToggleButton mTbNearby;
	ToggleButton mTbBrowse;
	ToggleButton mTbSearch;

	static final int MIN_TIME = 15000; // 15 seconds
	static final int MIN_DISTANCE = 10; // 10 meters
	static final float DEFAULT_RADIUS = 50;// 50 mi
	static final int ENABLE_LOCATION_PROVIDER = 1;
	static final int SET_RADIUS_DIALOG = 2;
	static final int ENABLE_NETWORK_DIALOG = 3;
	private EventsProcessor mEventsProcessor;
	private ListView mListView;
	private ListView mCategoriesListView;
	private CategoriesListAdapter mCategoriesAdapter;
	private EventsListAdapter mEventsListAdapter;
	private List<Event> mEvents;
	private LocationManager mLocationManager;
	private float mRadius;
	private long mCategoryId;
	private SeekBar mSbRadius;
	private TextView mEventsLocation;
	private boolean mAddressFound;
	private ProgressBar mAddressProgress;
	private Location mCurrentLocation;
	private EditText mSearchBox;

	private AlertDialog mSetDistanceDialog;
	private AlertDialog mEnableLocationDialog;
	private AlertDialog mNetworkErrorDialog;
	private ProgressDialog mLoadingProgress;
	private SharedPreferences mPreferences;
	private LinearLayout mLLSearchBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/Red October-Regular.otf");
		TextView tv = (TextView) findViewById(R.id.title_info);
		tv.setTypeface(tf);
		tf = Typeface.createFromAsset(getAssets(), "fonts/ArmWrestler.ttf");

		mTbNearby = (ToggleButton) findViewById(R.id.tbNearby);
		//mTbNearby.setTypeface(tf);
		mTbBrowse = (ToggleButton) findViewById(R.id.tbBrowse);
		//mTbBrowse.setTypeface(tf);
		mTbSearch = (ToggleButton) findViewById(R.id.tbSearch);
		//mTbSearch.setTypeface(tf);
		mSearchBox = (EditText) findViewById(R.id.keywords);
		mEvents = new ArrayList<Event>();
		mEventsProcessor = EventsProcessor.getInstance(this);
		mEventsListAdapter = new EventsListAdapter(this, mEvents, null);
		mListView = (ListView) findViewById(R.id.events_list);
		mListView.setAdapter(mEventsListAdapter);
		mListView.setOnItemClickListener(mOnEventClickListener);

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mRadius = DEFAULT_RADIUS;
		mCategoryId = -1;
		mAddressProgress = (ProgressBar) findViewById(R.id.pbFindLocation);
		mEventsLocation = (TextView) findViewById(R.id.events_location);
		mLLSearchBox = (LinearLayout) findViewById(R.id.search_box);
		mLLSearchBox.setVisibility(View.GONE);
		createDialogs();
		mCategoriesListView = (ListView) findViewById(R.id.categories_list);
		mCategoriesListView.setOnItemClickListener(mOnCategoryClickListener);
		if (isOnline(this)) {
			new LoadCategoriesTask().execute();
		} else {
			showDialog(ENABLE_NETWORK_DIALOG);
		}
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		requestLocation();
	}

	public void onChangeView(View v) {
		if (v.getId() == R.id.tbNearby) {
			mCategoryId = -1;
			mTbNearby.setChecked(true);
			mTbBrowse.setChecked(false);
			mTbSearch.setChecked(false);
			mListView.setVisibility(View.VISIBLE);
			mLLSearchBox.setVisibility(View.GONE);
			mCategoriesListView.setVisibility(View.GONE);
			requestLocation();
		}
		if (v.getId() == R.id.tbBrowse) {
			mTbNearby.setChecked(false);
			mTbBrowse.setChecked(true);
			mTbSearch.setChecked(false);
			mLLSearchBox.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mCategoriesListView.setVisibility(View.VISIBLE);
		}
		if (v.getId() == R.id.tbSearch) {
			mTbNearby.setChecked(false);
			mTbBrowse.setChecked(false);
			mTbSearch.setChecked(true);
			mLLSearchBox.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			mCategoriesListView.setVisibility(View.GONE);
		}
	}

	public void onSearchClick(View v) {
		if (v.getId() == R.id.btnSearch) {
			new SearchEventsTask().execute();
		}
	}

	final OnItemClickListener mOnEventClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			Intent intent = new Intent(MainActivity.this, EventDetails.class);
			intent.putExtra("event_id", id);
			startActivity(intent);

		}

	};
	final OnItemClickListener mOnCategoryClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			mCategoryId = id;
			requestLocation();
		}

	};

	private class LoadEventsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mLoadingProgress.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mEvents = mEventsProcessor.getNearByEvents(mCurrentLocation
					.getLatitude(), mCurrentLocation.getLongitude(), mRadius,
					mCategoryId);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mEvents != null) {
				mEventsListAdapter.refreshDataSet(mEvents, null);
				mCategoriesListView.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(MainActivity.this, R.string.no_events,
						Toast.LENGTH_SHORT).show();
			}
			mLoadingProgress.dismiss();
			mListView.focusableViewAvailable(mListView);
			super.onPostExecute(result);
		}

	}

	private class LoadCategoriesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mCategoriesAdapter = new CategoriesListAdapter(MainActivity.this,
					mEventsProcessor.getCategories());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mCategoriesListView.setAdapter(mCategoriesAdapter);
			mCategoriesListView.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}

	private class SearchEventsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mLoadingProgress.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			String keywords = mSearchBox.getText().toString();
			mEvents = mEventsProcessor.searchEvents(keywords);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mEvents != null) {
				mEventsListAdapter.refreshDataSet(mEvents, null);
				mListView.setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(MainActivity.this, R.string.no_events,
						Toast.LENGTH_SHORT).show();
			}
			mLoadingProgress.dismiss();
			mListView.focusableViewAvailable(mListView);
			super.onPostExecute(result);
		}

	}

	private void requestLocation() {
		boolean hasLocationProvider = false;
		if (mPreferences.getBoolean("gps_enabled", false)) {
			if (mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				mLocationManager.removeUpdates(mLocationListener);
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE,
						mLocationListener);
			}
		} else {
			if (mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, MIN_TIME,
						MIN_DISTANCE, mLocationListener);
				hasLocationProvider = true;
			}
		}

		if (!hasLocationProvider) {
			showDialog(ENABLE_LOCATION_PROVIDER);
		}
	}

	final LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			mCurrentLocation = location;
			mLocationManager.removeUpdates(mLocationListener);
			geocodeLocation();
			loadNearbyEvents();
		}
	};

	private void loadNearbyEvents() {
		if (isOnline(this)) {
			new LoadEventsTask().execute();
		} else {
			showDialog(ENABLE_NETWORK_DIALOG);
		}
	}

	final OnSeekBarChangeListener mOnSeekBarChanged = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			setRadiusDialogTitle(progress);
		}
	};

	void setRadiusDialogTitle(int radius) {
		String title = getResources().getString(R.string.radius);
		title = String.format("%s (%d)", title, radius);
		mSetDistanceDialog.setTitle(title);
	}

	private void createDialogs() {
		final View distanceView = LayoutInflater.from(MainActivity.this)
				.inflate(R.layout.set_radius, null);
		mSbRadius = (SeekBar) distanceView.findViewById(R.id.sbRadius);
		mSbRadius.setOnSeekBarChangeListener(mOnSeekBarChanged);
		mSetDistanceDialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.set_radius).setView(distanceView)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								mRadius = mSbRadius.getProgress();
								if (mRadius <= 1) {
									mRadius = 1;
								}
								requestLocation();
							}
						}).create();
		setRadiusDialogTitle(mSbRadius.getProgress());
		mEnableLocationDialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.enable_location_provider).setPositiveButton(
						R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								launchGPSOptions();
							}
						}).setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						}).create();

		mNetworkErrorDialog = new AlertDialog.Builder(this).setTitle(
				R.string.network_error).setMessage(
				getString(R.string.check_internet)).setPositiveButton(
				R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						launchNetworkOptions();
					}
				}).setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						finish();
					}
				}).create();

		mLoadingProgress = new ProgressDialog(this);
		mLoadingProgress.setTitle(R.string.loading);
		mLoadingProgress.setMessage(getString(R.string.loading_msg));
		mLoadingProgress.setIndeterminate(true);
	}

	private void launchGPSOptions() {
		final ComponentName toLaunch = new ComponentName(
				"com.android.settings", "com.android.settings.SecuritySettings");
		final Intent intent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, 0);
	}

	private void launchNetworkOptions() {
		final ComponentName toLaunch = new ComponentName(
				"com.android.settings", "com.android.settings.WirelessSettings");
		final Intent intent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, 0);
	}

	protected Geocoder mGeocoder;
	protected String mAddressString;

	private void geocodeLocation() {
		mAddressProgress.setVisibility(View.VISIBLE);
		Thread geocodeThread = new Thread(mGeocodeRunnable);
		geocodeThread.start();
	}

	final Runnable mGeocodeRunnable = new Runnable() {

		@Override
		public void run() {
			mAddressString = "No address found";
			mAddressFound = false;
			try {
				mGeocoder = new Geocoder(MainActivity.this);
				List<Address> addresses = mGeocoder.getFromLocation(
						mCurrentLocation.getLatitude(), mCurrentLocation
								.getLongitude(), 1);
				StringBuilder sb = new StringBuilder();
				if (addresses.size() > 0) {
					Address address = addresses.get(0);
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
						sb.append(address.getAddressLine(i)).append(" ");
					}
					if (sb.length() <= 0) {
						String loc = address.getLocality();
						if (loc != null) {
							sb.append(loc);
							sb.append(", ");
						}
						sb.append(address.getCountryName());
					}
					mAddressString = sb.toString();
					mAddressFound = true;
				}
			} catch (IOException e) {
				mGeocodeHandler.sendEmptyMessage(0);
			}
			mGeocodeHandler.sendEmptyMessage(0);
		}
	};
	final Handler mGeocodeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mAddressFound) {
				mEventsLocation.setText(mAddressString);
			} else {
				mEventsLocation.setText(String.format("Lat: %.2f, Lng: %.2f",
						mCurrentLocation.getLatitude(), mCurrentLocation
								.getLongitude()));
			}
			mAddressProgress.setVisibility(View.GONE);
		}

	};

	@Override
	public void finish() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(mLocationListener);
		}
		super.finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == ENABLE_LOCATION_PROVIDER) {
			return mEnableLocationDialog;
		}
		if (id == ENABLE_NETWORK_DIALOG) {
			return mNetworkErrorDialog;
		}
		if (id == SET_RADIUS_DIALOG) {
			return mSetDistanceDialog;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.nearby_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.set_radius) {
			showDialog(SET_RADIUS_DIALOG);
		}
		if (item.getItemId() == R.id.refresh) {
			requestLocation();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public static boolean isOnline(Context context) {
		// ConnectivityManager is used to check available network(s)
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null) {
			// no network is available
			return false;
		} else {
			// at least one type of network is available
			return true;
		}
	}
}
