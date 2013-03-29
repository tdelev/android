package org.feit.atwork;

import org.feit.atwork.data.Project;
import org.feit.atwork.data.ProjectsProcessor;
import org.feit.atwork.data.ProjectsProvider;
import org.feit.atwork.quickaction.ActionItem;
import org.feit.atwork.quickaction.QuickAction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ProjectsList extends Activity {
	private ListView mProjectsList;
	private ProjectsListAdapter mProjectsListAdapter;
	private Project[] mProjects;
	private ProjectsProcessor mProjectsProcessor;
	private AlertDialog mSaveProjectDialog;
	private AlertDialog mEditProjectDialog;
	private AlertDialog mDeleteProjectDialog;
	private EditText mName;
	private EditText mEditName;

	private QuickAction mQuickAction;
	private ActionItem mActionDelete;
	private ActionItem mActionEdit;

	private long mSelectedProjectId;
	private Project mSelectedProject;
	private ProgressBar mPbTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.projects_list);
		mPbTitle = (ProgressBar) findViewById(R.id.pbTitle);
		mProjectsProcessor = new ProjectsProcessor(this);
		mProjects = new Project[0];
		mProjectsListAdapter = new ProjectsListAdapter(this, mProjects);
		mProjectsList = (ListView) findViewById(R.id.projects_list);
		mProjectsList.setEmptyView(findViewById(R.id.no_projects));
		mProjectsList.setAdapter(mProjectsListAdapter);
		mProjectsList.setOnItemClickListener(mOnProjectListClicked);
		mProjectsList.setOnItemLongClickListener(mOnProjectLongClick);
		createDialogs();
		createActionButtons();
		getContentResolver().registerContentObserver(
				ProjectsProvider.CONTENT_URI, true, mProjectsObserver);
		new LoadProjectsTask().execute();
	}

	final Handler mProjectsHandler = new Handler() {

	};
	final ContentObserver mProjectsObserver = new ContentObserver(
			mProjectsHandler) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			new LoadProjectsTask().execute();
		}

	};

	public void onNewProject(View v) {
		mSaveProjectDialog.show();
	}

	private void createDialogs() {
		final View enterNameView = LayoutInflater.from(this).inflate(
				R.layout.edit_project, null);
		mName = (EditText) enterNameView.findViewById(R.id.name);

		final View editNameView = LayoutInflater.from(this).inflate(
				R.layout.edit_project, null);
		mEditName = (EditText) editNameView.findViewById(R.id.name);
		mSaveProjectDialog = new AlertDialog.Builder(this).setTitle(
				R.string.new_project).setView(enterNameView).setPositiveButton(
				R.string.save, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						new SaveProjectTask().execute();
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();

		mEditProjectDialog = new AlertDialog.Builder(this).setTitle(
				R.string.edit_project).setIcon(R.drawable.edit).setView(
				editNameView).setPositiveButton(R.string.update,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						new UpdateProjectTask().execute(mSelectedProjectId);
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();

		mDeleteProjectDialog = new AlertDialog.Builder(this).setTitle(
				R.string.delete_dialog_title).setIcon(
				android.R.drawable.ic_dialog_alert).setPositiveButton(
				R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						new DeleteProjectTask().execute(mSelectedProjectId);
					}
				}).setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
	}

	private void createActionButtons() {
		mActionEdit = new ActionItem();

		mActionEdit.setTitle(getString(R.string.edit));
		mActionEdit.setIcon(getResources().getDrawable(R.drawable.edit));
		mActionEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mQuickAction.dismiss();
				mEditName.setText(mSelectedProject.name);
				mEditProjectDialog.show();
			}
		});

		mActionDelete = new ActionItem();
		mActionDelete.setTitle(getString(R.string.delete));
		mActionDelete.setIcon(getResources().getDrawable(R.drawable.delete));
		mActionDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mQuickAction.dismiss();
				mDeleteProjectDialog.show();
			}
		});

	}

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
			if (mProjects.length == 0) {
				final TextView tv = (TextView) findViewById(R.id.no_projects);
				tv.setText(R.string.no_projects);
			}
			mPbTitle.setVisibility(View.GONE);
		}

	}

	private class SaveProjectTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			mPbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			String name = mName.getText().toString();
			if (name.length() <= 0) {
				name = getString(R.string.unnamed);
			}
			if (name.length() > 50) {
				name = name.substring(0, 50);
			}
			mProjectsProcessor.saveProject(name);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPbTitle.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}

	private class UpdateProjectTask extends AsyncTask<Long, Void, Void> {
		@Override
		protected void onPreExecute() {
			mPbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Long... params) {
			String name = mEditName.getText().toString();
			if (name.length() <= 0) {
				name = getString(R.string.unnamed);
			}
			if (name.length() > 50) {
				name = name.substring(0, 50);
			}
			mProjectsProcessor.updateProject(params[0], name);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPbTitle.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}

	private class DeleteProjectTask extends AsyncTask<Long, Void, Void> {
		@Override
		protected void onPreExecute() {
			mPbTitle.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Long... params) {
			mProjectsProcessor.deleteProject(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPbTitle.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}

	final OnItemClickListener mOnProjectListClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View v,
				int position, long id) {
			Intent intent = new Intent(ProjectsList.this, SessionsList.class);
			intent.putExtra("project_id", id);
			startActivity(intent);
		}

	};

	final OnItemLongClickListener mOnProjectLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos,
				long id) {
			mSelectedProject = mProjectsListAdapter.getProject(pos);
			mSelectedProjectId = id;
			mQuickAction = new QuickAction(v);
			mQuickAction.addActionItem(mActionEdit);
			mQuickAction.addActionItem(mActionDelete);
			mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
			mQuickAction.show();
			return false;
		}
	};

}
