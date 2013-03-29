package org.feit.sharelater;

import org.feit.sharelater.quickaction.ActionItem;
import org.feit.sharelater.quickaction.QuickAction;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SharedList extends Activity {
	static final String TAG = "SharedList";
	static final int LONG_CLICK_DIALOG = 0;

	private ListView mListView;
	private IntentsListAdapter mListAdapter;
	private IntentsProcessor mIntentsProcessor;
	private IntentData mSelectedIntent;
	private ImageView ivWifi;
	private QuickAction mQuickAction;
	private ActionItem mActionDelete;
	private ActionItem mActionShare;
	private ActionItem mActionView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shared_list);
		setTitle(String.format("%s - %s", getString(R.string.app_name),
				getString(R.string.shared)));
		mIntentsProcessor = new IntentsProcessor(this);
		mIntents = mIntentsProcessor.getIntents(false);
		mListAdapter = new IntentsListAdapter(this, mIntents);
		mListView = (ListView) findViewById(R.id.intents_list);
		mListView.setEmptyView(findViewById(R.id.no_intents));
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(mOnItemClicked);

		boolean connected = getIntent().getBooleanExtra(
				ShareLaterTabs.CONNECTED, false);
		ivWifi = (ImageView) findViewById(R.id.ivWifi);
		if (connected) {
			ivWifi.setImageDrawable(getResources().getDrawable(
					R.drawable.wifi_on));
		} else {
			ivWifi.setImageDrawable(getResources().getDrawable(
					R.drawable.wifi_off));
		}
		createActionButtons();
		getContentResolver().registerContentObserver(IntentsProvider.CONTENT_URI, true, mIntentsObserver);
	}
	
	final Handler mObserverHandler = new Handler() {

	};
	final ContentObserver mIntentsObserver = new ContentObserver(
			mObserverHandler) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			reloadList();
		}

	};

	private void createActionButtons() {
		mActionShare = new ActionItem();
		
		mActionShare.setTitle(getString(R.string.share));
		mActionShare.setIcon(getResources().getDrawable(R.drawable.share));
		mActionShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareIntent(mSelectedIntent.getIntent());
				mQuickAction.dismiss();
			}
		});
		
		mActionView = new ActionItem();
		
		mActionView.setTitle(getString(R.string.view));
		mActionView.setIcon(getResources().getDrawable(R.drawable.view));
		mActionView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri uri = mSelectedIntent.getUri();
				if (uri != null) {
					intent.setDataAndType(uri, mSelectedIntent.type);
					intent.putExtras(mSelectedIntent.extras);
					startActivity(intent);
				}
				mQuickAction.dismiss();
			}
		});

		mActionDelete = new ActionItem();
		mActionDelete.setTitle(getString(R.string.delete));
		mActionDelete.setIcon(getResources().getDrawable(R.drawable.delete));
		mActionDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIntentsProcessor.deleteIntent(mSelectedIntent.Id);
				reloadList();
				mQuickAction.dismiss();
			}
		});

	}

	private void shareIntent(Intent intent) {
		Intent i = Intent.createChooser(intent, getString(R.string.share_via));
		startActivity(i);
	}

	final OnItemClickListener mOnItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {
			Log.d(TAG, "id: " + position);
			mSelectedIntent = mListAdapter.getIntentData(position);
			mQuickAction = new QuickAction(v);
			mQuickAction.addActionItem(mActionShare);
			mQuickAction.addActionItem(mActionView);
			mQuickAction.addActionItem(mActionDelete);
			mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
			
			mQuickAction.show();

		}
	};

	protected IntentData[] mIntents;

	private void reloadList() {
		Thread t = new Thread(mReloadRunnable);
		t.start();
	}

	final Runnable mReloadRunnable = new Runnable() {

		@Override
		public void run() {
			mIntents = mIntentsProcessor.getIntents(false);
			mReloadHandler.sendEmptyMessage(0);
		}
	};

	final Handler mReloadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mListAdapter.refreshData(mIntents);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, m);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.settings) {
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
		}
		if (item.getItemId() == R.id.clear_all) {
			new DeleteAllTask().execute();
		}
		return true;
	}

	private class DeleteAllTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mIntentsProcessor.deleteShared();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

	}
}
