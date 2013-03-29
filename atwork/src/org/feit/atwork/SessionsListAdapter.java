package org.feit.atwork;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.feit.atwork.data.Session;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SessionsListAdapter extends BaseAdapter {

	static class ViewHolder {
		TextView monthLabel;
		TextView weekLabel;
		TextView month;
		TextView week;
		TextView startTime;
		TextView endTime;
		TextView startDate;
		TextView endDate;
		TextView total;
	}

	private LayoutInflater mInflater;
	private Session[] mSessions;
	private Context mContext;
	private Map<Integer, Long> mWeekHours;
	private Map<Integer, Long> mMonthHours;
	private Calendar mCalendar;
	private DateFormat mMonthFormat;
	private DateFormat mDateFormat;
	private DateFormat mTimeFormat;

	public SessionsListAdapter(Context context, Session[] projects) {
		mInflater = LayoutInflater.from(context);
		mSessions = projects;
		mContext = context;
		mCalendar = Calendar.getInstance();
		mMonthFormat = new SimpleDateFormat("MMMM");
		mDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		mTimeFormat = new SimpleDateFormat("HH:mm");
	}

	@Override
	public int getCount() {
		return mSessions.length;
	}

	@Override
	public Object getItem(int pos) {
		return mSessions[pos];
	}

	@Override
	public long getItemId(int position) {
		return mSessions[position].id;
	}

	public Session getSession(int position) {
		return mSessions[position];
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		// When convertView is not null, we can reuse it directly, there is no
		// need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.session_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.month = (TextView) convertView
					.findViewById(R.id.month_total);
			holder.monthLabel = (TextView) convertView
					.findViewById(R.id.month_total_lbl);
			holder.week = (TextView) convertView.findViewById(R.id.week_total);
			holder.weekLabel = (TextView) convertView
					.findViewById(R.id.week_total_lbl);
			holder.startTime = (TextView) convertView
					.findViewById(R.id.start_time);
			holder.endTime = (TextView) convertView.findViewById(R.id.end_time);
			holder.startDate = (TextView) convertView
					.findViewById(R.id.start_date);
			holder.endDate = (TextView) convertView.findViewById(R.id.end_date);
			holder.total = (TextView) convertView.findViewById(R.id.total);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		Session session = mSessions[position];
		mCalendar.setTime(session.start_time);
		int m = mCalendar.get(Calendar.MONTH);
		int w = mCalendar.get(Calendar.WEEK_OF_YEAR);
		if (session.monthStart) {
			holder.monthLabel.setVisibility(View.VISIBLE);
			holder.monthLabel.setText(String.format("%s %s:", mMonthFormat
					.format(session.start_time), mContext
					.getString(R.string.month_total)));
			holder.month.setVisibility(View.VISIBLE);
			holder.month.setText(String.format("%d %s", mMonthHours.get(m)
					/ (60 * 60 * 1000), mContext.getString(R.string.hours)));
		} else {
			holder.month.setVisibility(View.GONE);
			holder.monthLabel.setVisibility(View.GONE);
		}
		if (session.weekStart) {
			holder.week.setVisibility(View.VISIBLE);
			holder.weekLabel.setVisibility(View.VISIBLE);
			holder.weekLabel.setText(String.format("%d %s:", w, mContext
					.getString(R.string.week_total)));
			holder.week.setText(String.format("%d %s", mWeekHours.get(w)
					/ (60 * 60 * 1000), mContext.getString(R.string.hours)));
		} else {
			holder.week.setVisibility(View.GONE);
			holder.weekLabel.setVisibility(View.GONE);
		}
		holder.startTime.setText(mTimeFormat.format(session.start_time));
		if (session.inProgress()) {
			holder.endTime.setText(mContext.getString(R.string.in_progress));
		} else {
			holder.endTime.setText(mTimeFormat.format(session.end_time));
		}
		holder.startDate.setText(mDateFormat.format(session.start_time));
		holder.endDate.setText(mDateFormat.format(session.end_time));
		holder.total.setText(session.getTimeString());
		return convertView;
	}

	public void refreshData(Session[] sessions) {
		mSessions = sessions;
		calculateTotals();
		notifyDataSetChanged();
	}

	private void calculateTotals() {
		mWeekHours = new HashMap<Integer, Long>();
		mMonthHours = new HashMap<Integer, Long>();
		Calendar cal = Calendar.getInstance();
		int prevWeek = -1;
		int prevMonth = -1;
		for (Session s : mSessions) {
			cal.setTime(s.start_time);
			int week = cal.get(Calendar.WEEK_OF_YEAR);
			int month = cal.get(Calendar.MONTH);
			if (week != prevWeek) {
				s.weekStart = true;
			}
			if (month != prevMonth) {
				s.monthStart = true;
			}
			if (mWeekHours.containsKey(week)) {
				mWeekHours.put(week, mWeekHours.get(week) + s.getSessionTime());
			} else {
				mWeekHours.put(week, s.getSessionTime());
			}
			if (mMonthHours.containsKey(month)) {
				mMonthHours.put(month, mMonthHours.get(month)
						+ s.getSessionTime());
			} else {
				mMonthHours.put(month, s.getSessionTime());
			}
			prevWeek = week;
			prevMonth = month;
		}
	}
}
