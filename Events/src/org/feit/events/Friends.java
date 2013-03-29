package org.feit.events;

import java.util.ArrayList;
import java.util.List;

import org.feit.geoevents.http.Constants;
import org.feit.geoevents.providers.UserSettings;
import org.feit.geoevents.providers.Event;
import org.feit.geoevents.providers.EventsProcessor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

public class Friends extends Activity {
	private List<Event> mEvents;
	private EventsProcessor mEventsProcessor;
	private EventsListAdapter mEventsListAdapter;
	private ListView mListView;
	private ProgressDialog mLoadingProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);
		
		if(!UserSettings.getInstance(this).isAuthenticated()) {
			authenticate();
			return;
		}
		
		mEvents = new ArrayList<Event>();
		mEventsProcessor = EventsProcessor.getInstance(this);
		mEventsListAdapter = new EventsListAdapter(this, mEvents, null);
		mListView = (ListView) findViewById(R.id.events_list);
		mListView.setEmptyView(findViewById(R.id.no_events));
		mListView.setAdapter(mEventsListAdapter);
		createDialogs();
		new LoadFriendEventsTask().execute();
	}
	
	private void authenticate() {	
		String url = String.format("%s?api_key=%s", Constants.AUTH_URL, Constants.API_KEY);
		Intent browserIntent = new Intent(
				"android.intent.action.VIEW",
				Uri.parse(url));
		startActivity(browserIntent);
	}
	private void createDialogs() {
		mLoadingProgress = new ProgressDialog(this);
		mLoadingProgress.setTitle(R.string.loading);
		mLoadingProgress.setMessage(getString(R.string.loading_msg));
		mLoadingProgress.setIndeterminate(true);
	}
	
	private class LoadFriendEventsTask extends AsyncTask<Void, Void, Void> {
		 
		@Override
		protected void onPreExecute() {
			mLoadingProgress.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mEvents = mEventsProcessor.friendsEvents();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mEvents != null) {
				mEventsListAdapter.refreshDataSet(mEvents, null);				
			} else {
				Toast.makeText(Friends.this, R.string.no_events, Toast.LENGTH_SHORT).show();
			}
			mLoadingProgress.dismiss();
			mListView.focusableViewAvailable(mListView);
			super.onPostExecute(result);
		}

	}
}
