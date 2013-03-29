package org.feit.atwork;

import org.feit.atwork.data.Project;
import org.feit.atwork.data.ProjectsProcessor;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConfigureWidget extends Activity {
	private ListView mProjectsList;
	private ProjectsListAdapter mProjectsListAdapter;
	private Project[] mProjects;
	private ProjectsProcessor mProjectsProcessor;
	private ProgressBar mPbTitle;

	private CheckBox mCbLastActive;
	private int mAppWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

		setContentView(R.layout.configure_widget);
		mPbTitle = (ProgressBar) findViewById(R.id.pbTitle);
		mProjectsProcessor = new ProjectsProcessor(this);
		mProjects = new Project[0];
		mProjectsListAdapter = new ProjectsListAdapter(this, mProjects);
		mProjectsList = (ListView) findViewById(R.id.projects_list);
		mProjectsList.setEmptyView(findViewById(R.id.no_projects));
		mProjectsList.setAdapter(mProjectsListAdapter);
		mProjectsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mProjectsList.setOnItemClickListener(mOnProjectListClicked);

		mCbLastActive = (CheckBox) findViewById(R.id.cbLastActive);
		mCbLastActive.setOnCheckedChangeListener(mOnLastActiveChanged);
		new LoadProjectsTask().execute();
	}
	
	final OnCheckedChangeListener mOnLastActiveChanged = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked) {
				Settings.getInstance(ConfigureWidget.this).setUserSelected(false);
				finish();
			}else {
				Settings.getInstance(ConfigureWidget.this).setUserSelected(true);
			}
		}
	};

	private class LoadProjectsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mPbTitle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			mProjects = mProjectsProcessor.getProjects();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mProjectsListAdapter.refreshData(mProjects);
			mPbTitle.setVisibility(View.GONE);
		}

	}

	final OnItemClickListener mOnProjectListClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View v,
				int position, long id) {
			Settings.getInstance(ConfigureWidget.this).setUserSelected(true);
			Settings.getInstance(ConfigureWidget.this).setHomeProjectId(id);
			finish();
		}

	};

	@Override
	public void finish() {
		AtworkWidgetProvider.updateWidget(this);
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		super.finish();
	};
}
