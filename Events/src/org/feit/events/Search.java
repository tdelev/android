package org.feit.events;

import java.util.ArrayList;
import java.util.List;

import org.feit.geoevents.providers.Event;
import org.feit.geoevents.providers.EventsProcessor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Search extends Activity {
	private List<Event> mEvents;
	private EventsProcessor mEventsProcessor;
	private EventsListAdapter mEventsListAdapter;
	private ListView mListView;
	private ProgressDialog mLoadingProgress;
	private EditText mSearchBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
		mEvents = new ArrayList<Event>();
		mEventsProcessor = EventsProcessor.getInstance(this);
		mEventsListAdapter = new EventsListAdapter(this, mEvents, null);
		mListView = (ListView) findViewById(R.id.events_list);
		mListView.setEmptyView(findViewById(R.id.no_events));
		mListView.setAdapter(mEventsListAdapter);
		mSearchBox = (EditText)findViewById(R.id.keywords);
		createDialogs();
	}
	
	private void createDialogs() {
		mLoadingProgress = new ProgressDialog(this);
		mLoadingProgress.setTitle(R.string.loading);
		mLoadingProgress.setMessage(getString(R.string.loading_msg));
		mLoadingProgress.setIndeterminate(true);
	}
	
	public void onSearchClick(View v) {
		if(v.getId() == R.id.btnSearch) {
			new SearchEventsTask().execute();
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
			if(mEvents != null) {
				mEventsListAdapter.refreshDataSet(mEvents, null);				
			} else {
				Toast.makeText(Search.this, R.string.no_events, Toast.LENGTH_SHORT).show();
			}
			mLoadingProgress.dismiss();
			mListView.focusableViewAvailable(mListView);
			super.onPostExecute(result);
		}

	}

}
