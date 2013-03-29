package org.feit.atwork;

import java.util.Calendar;

import org.feit.atwork.data.Project;
import org.feit.atwork.data.ProjectsProcessor;
import org.feit.atwork.data.Session;
import org.feit.atwork.data.SessionsProcessor;
import org.feit.atwork.data.SessionsProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;

public class SessionsList extends Activity {

	static final int START_TIME_DIALOG_ID = 0;
	static final int START_DATE_DIALOG_ID = 1;
	static final int END_TIME_DIALOG_ID = 2;
	static final int END_DATE_DIALOG_ID = 3;

	private ListView mSessionsList;
	private SessionsListAdapter mSessionsListAdapter;
	private Session[] mSessions;
	private SessionsProcessor mSessionsProcessor;
	private ProjectsProcessor mProjectsProcessor;
	private Session mRunningSession;
	private Session mSelectedSession;
	private Project mCurrentProject;
	private ProgressBar mPbTitle;

	private Button btnFromDate;
	private Button btnFromTime;
	private Button btnToDate;
	private Button btnToTime;
	private EditText comment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sessions_list);
		mPbTitle = (ProgressBar) findViewById(R.id.pbTitle);
		long projectId = getIntent().getLongExtra("project_id", -1);
		mProjectsProcessor = new ProjectsProcessor(this);
		mCurrentProject = mProjectsProcessor.getProject(projectId);
		final TextView projectName = (TextView) findViewById(R.id.tvProjectName);
		projectName.setText(mCurrentProject.name);
		mSessionsProcessor = new SessionsProcessor(this);
		mSessions = new Session[0];
		mRunningSession = mSessionsProcessor.runningSession(projectId);
		mSessionsListAdapter = new SessionsListAdapter(this, mSessions);
		mSessionsList = (ListView) findViewById(R.id.sessions_list);
		mSessionsList.setEmptyView(findViewById(R.id.no_sessions));
		mSessionsList.setAdapter(mSessionsListAdapter);
		mSessionsList.setOnItemClickListener(mOnSessionsListClick);
		setStartStopButton();
		getContentResolver().registerContentObserver(
				SessionsProvider.CONTENT_URI, true, mSessionsObserver);
		createDialogs();
		new LoadSessionsTask().execute();
	}

	final Handler mSessionsHandler = new Handler() {

	};
	final ContentObserver mSessionsObserver = new ContentObserver(
			mSessionsHandler) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			new LoadSessionsTask().execute();
		}

	};
	private AlertDialog mEditSessionDetails;

	private void createDialogs() {
		final View editSessionView = LayoutInflater.from(this).inflate(
				R.layout.session_details, null);
		btnFromDate = (Button) editSessionView.findViewById(R.id.btnFromDate);
		btnFromTime = (Button) editSessionView.findViewById(R.id.btnFromTime);
		btnToDate = (Button) editSessionView.findViewById(R.id.btnToDate);
		btnToTime = (Button) editSessionView.findViewById(R.id.btnToTime);
		comment = (EditText) editSessionView.findViewById(R.id.comment);

		mEditSessionDetails = new AlertDialog.Builder(this).setTitle(
				R.string.edit_session).setView(editSessionView).setIcon(
				R.drawable.edit).setPositiveButton(R.string.save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						new SaveSessionTask().execute();
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
	}

	private void showEditDialog() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(mSelectedSession.start_time);

		btnFromDate.setText(String.format("%02d.%02d.%d", cal
				.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal
				.get(Calendar.YEAR)));
		btnFromTime.setText(String.format("%02d:%02d", cal
				.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
		cal.setTime(mSelectedSession.end_time);

		btnToDate.setText(String.format("%02d.%02d.%d", cal
				.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal
				.get(Calendar.YEAR)));

		btnToTime.setText(String.format("%02d:%02d", cal
				.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));

		comment.setText(mSelectedSession.comment);
		mEditSessionDetails.show();
	}

	private void setStartStopButton() {
		final Button startStopBtn = (Button) findViewById(R.id.startSession);
		if (mCurrentProject.progress) {
			startStopBtn.setBackgroundResource(R.drawable.pause_button);
		} else {
			startStopBtn.setBackgroundResource(R.drawable.start_button);
		}
	}

	public void onButtonClick(View button) {
		if (button.getId() == R.id.startSession) {
			if (mCurrentProject.progress) {
				new StartEndSessionTask().execute(false);
			} else {
				new StartEndSessionTask().execute(true);
			}
		}
		if (button.getId() == R.id.btnFromTime) {
			showDialog(START_TIME_DIALOG_ID);
		}
		if (button.getId() == R.id.btnFromDate) {
			showDialog(START_DATE_DIALOG_ID);
		}
		if (button.getId() == R.id.btnToTime) {
			showDialog(END_TIME_DIALOG_ID);
		}
		if (button.getId() == R.id.btnToDate) {
			showDialog(END_DATE_DIALOG_ID);
		}
	}

	private class LoadSessionsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mPbTitle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			mSessions = mSessionsProcessor.getSessions(mCurrentProject.Id);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mSessionsListAdapter.refreshData(mSessions);
			if (mSessions.length == 0) {
				final TextView tv = (TextView) findViewById(R.id.no_sessions);
				tv.setText(R.string.no_projects);
			}
			mPbTitle.setVisibility(View.GONE);
		}

	}

	private class StartEndSessionTask extends AsyncTask<Boolean, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mPbTitle.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if (params[0]) {
				mRunningSession = mSessionsProcessor
						.startSession(mCurrentProject.Id);
				mCurrentProject.progress = true;
			} else {
				mSessionsProcessor.endSession(mRunningSession);
				mCurrentProject.progress = false;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setStartStopButton();
			mPbTitle.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}

	final OnItemClickListener mOnSessionsListClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View v,
				int position, long id) {
			mSelectedSession = mSessionsListAdapter.getSession(position);
			showEditDialog();
			// Intent intent = new Intent(SessionsList.this,
			// SessionDetails.class);
			// intent.putExtra("project_name", mCurrentProject.name);
			// intent.putExtra("session_id", id);
			// startActivity(intent);
		}

	};

	@Override
	protected Dialog onCreateDialog(int id) {
		Calendar cal = Calendar.getInstance();
		if (id == START_TIME_DIALOG_ID) {
			cal.setTime(mSelectedSession.start_time);
			return new TimePickerDialog(this, onStartTimeSet, cal
					.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
		}
		if (id == START_DATE_DIALOG_ID) {
			cal.setTime(mSelectedSession.start_time);
			return new DatePickerDialog(this, onStartDateSet, cal
					.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					.get(Calendar.DAY_OF_MONTH));
		}
		if (id == END_TIME_DIALOG_ID) {
			cal.setTime(mSelectedSession.end_time);
			return new TimePickerDialog(this, onEndTimeSet, cal
					.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
		}
		if (id == END_DATE_DIALOG_ID) {
			cal.setTime(mSelectedSession.end_time);
			return new DatePickerDialog(this, onEndDateSet, cal
					.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					.get(Calendar.DAY_OF_MONTH));
		}
		return super.onCreateDialog(id);
	}

	final OnTimeSetListener onStartTimeSet = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			btnFromTime.setText(String.format("%02d:%02d", hourOfDay, minute));
			mSelectedSession.start_time.setHours(hourOfDay);
			mSelectedSession.start_time.setMinutes(minute);
		}
	};

	final OnDateSetListener onStartDateSet = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			btnFromDate.setText(String.format("%02d.%02d.%d", dayOfMonth,
					monthOfYear, year));
			mSelectedSession.start_time.setYear(year - 1900);
			mSelectedSession.start_time.setMonth(monthOfYear);
			mSelectedSession.start_time.setDate(dayOfMonth);
		}
	};

	final OnTimeSetListener onEndTimeSet = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			btnToTime.setText(String.format("%02d:%02d", hourOfDay, minute));
			mSelectedSession.end_time.setHours(hourOfDay);
			mSelectedSession.end_time.setMinutes(minute);
		}
	};

	final OnDateSetListener onEndDateSet = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			btnToDate.setText(String.format("%02d.%02d.%d", dayOfMonth,
					monthOfYear, year));
			mSelectedSession.end_time.setYear(year - 1900);
			mSelectedSession.end_time.setMonth(monthOfYear);
			mSelectedSession.end_time.setDate(dayOfMonth);
		}
	};

	private class SaveSessionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mPbTitle.setVisibility(View.VISIBLE);
		}


		@Override
		protected Void doInBackground(Void... params) {
			mSelectedSession.comment = comment.getText().toString();
			mSessionsProcessor.updateSession(mSelectedSession);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mPbTitle.setVisibility(View.GONE);
		}

	}

}
