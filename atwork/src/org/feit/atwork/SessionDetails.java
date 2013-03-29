package org.feit.atwork;

import java.util.Calendar;

import org.feit.atwork.data.Session;
import org.feit.atwork.data.SessionsProcessor;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class SessionDetails extends Activity {
	static final int START_TIME_DIALOG_ID = 0;
	static final int START_DATE_DIALOG_ID = 1;
	static final int END_TIME_DIALOG_ID = 2;
	static final int END_DATE_DIALOG_ID = 3;
	
	private Session mSession;
	private SessionsProcessor mSessionsProcessor;
	
	private Button btnFromDate;
	private Button btnFromTime;
	private Button btnToDate;
	private Button btnToTime;
	private EditText comment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.session_details);
		long sessionId = getIntent().getLongExtra("session_id", -1);
		String projectName = getIntent().getStringExtra("project_name");
		mSessionsProcessor = new SessionsProcessor(this);
		mSession = mSessionsProcessor.getSession(sessionId);
		
		final TextView name = (TextView)findViewById(R.id.tvProjectName);
		name.setText(projectName);
		Calendar cal = Calendar.getInstance();
		cal.setTime(mSession.start_time);
		btnFromDate = (Button) findViewById(R.id.btnFromDate);
		btnFromDate.setText(String.format("%02d.%02d.%d", cal
				.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal
				.get(Calendar.YEAR)));
		btnFromTime = (Button) findViewById(R.id.btnFromTime);
		btnFromTime.setText(String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE)));
		cal.setTime(mSession.end_time);
		btnToDate = (Button) findViewById(R.id.btnToDate);
		btnToDate.setText(String.format("%02d.%02d.%d", cal
				.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal
				.get(Calendar.YEAR)));
		btnToTime = (Button) findViewById(R.id.btnToTime);
		btnToTime.setText(String.format("%02d:%02d",
				cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
		
		comment = (EditText)findViewById(R.id.comment);
		comment.setText(mSession.comment);
	}
	
	public void onButtonClick(View button) {
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
		/*
		if (button.getId() == R.id.btnSave) {
			new SaveSessionTask().execute();
		}
		if (button.getId() == R.id.btnCancel) {
			finish();
		}
		*/
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Calendar cal = Calendar.getInstance();
		if (id == START_TIME_DIALOG_ID) {
			cal.setTime(mSession.start_time);
			return new TimePickerDialog(this, onStartTimeSet, cal
					.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
		}
		if (id == START_DATE_DIALOG_ID) {
			cal.setTime(mSession.start_time);
			return new DatePickerDialog(this, onStartDateSet, cal
					.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					.get(Calendar.DAY_OF_MONTH));
		}
		if (id == END_TIME_DIALOG_ID) {
			cal.setTime(mSession.end_time);
			return new TimePickerDialog(this, onEndTimeSet, cal
					.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
		}
		if (id == END_DATE_DIALOG_ID) {
			cal.setTime(mSession.end_time);
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
			mSession.start_time.setHours(hourOfDay);
			mSession.start_time.setMinutes(minute);
		}
	};

	final OnDateSetListener onStartDateSet = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			
			btnFromDate.setText(String.format("%02d.%02d.%d", dayOfMonth, monthOfYear,
					year));
			mSession.start_time.setYear(year - 1900);
			mSession.start_time.setMonth(monthOfYear);
			mSession.start_time.setDate(dayOfMonth);
		}
	};

	final OnTimeSetListener onEndTimeSet = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			btnToTime.setText(String.format("%02d:%02d", hourOfDay, minute));
			mSession.end_time.setHours(hourOfDay);
			mSession.end_time.setMinutes(minute);
		}
	};

	final OnDateSetListener onEndDateSet = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			btnToDate.setText(String.format("%02d.%02d.%d", dayOfMonth, monthOfYear,
					year));
			mSession.end_time.setYear(year - 1900);
			mSession.end_time.setMonth(monthOfYear);
			mSession.end_time.setDate(dayOfMonth);
		}
	};
	
	private class SaveSessionTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mSession.comment = comment.getText().toString();
			mSessionsProcessor.updateSession(mSession);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			finish();
		}

	}

}
