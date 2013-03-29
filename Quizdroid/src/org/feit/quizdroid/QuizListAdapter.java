package org.feit.quizdroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class QuizListAdapter extends BaseAdapter {

	static class ViewHolder {
		TextView name;
		TextView date;
		TextView score;
	}

	private LayoutInflater mInflater;
	private int mSize;
	private List<Quiz> mQuizes;
	private DateFormat mDateFormat;
	private Context mContext;

	public QuizListAdapter(Context context, List<Quiz> quizes) {
		mInflater = LayoutInflater.from(context);
		mQuizes = quizes;
		mSize = mQuizes.size();
		mDateFormat = new SimpleDateFormat("E, d MMM HH:mm");
		mContext = context;
	}

	@Override
	public int getCount() {
		return mSize;
	}

	@Override
	public Object getItem(int pos) {
		return mQuizes.get(pos);
	}

	public Quiz getQuiz(int pos) {
		return mQuizes.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return mQuizes.get(position).id;
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
			convertView = mInflater.inflate(R.layout.quiz_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.score = (TextView) convertView.findViewById(R.id.score);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		Quiz quiz = mQuizes.get(position);
		holder.name.setText(quiz.name);
		if(quiz.played && quiz.score != 0) {
			holder.score.setText(String.format("%s %d",mContext.getString(R.string.result), quiz.score));
			holder.score.setVisibility(View.VISIBLE);
			holder.date.setVisibility(View.GONE);
		}else {
			holder.score.setVisibility(View.GONE);
			holder.date.setVisibility(View.VISIBLE);			
			holder.date.setText(timeFromNow(quiz.date_finish));
		}
		return convertView;
	}

	private String timeFromNow(Date time) {
		Date now = new Date();
		long minutes = (time.getTime() - now.getTime()) / (60 * 1000);
		long hours = (time.getTime() - now.getTime()) / (60 * 60 * 1000);
		long days = (time.getTime() - now.getTime()) / (24 * 60 * 60 * 1000);
		StringBuilder sb = new StringBuilder();
		sb.append(mContext.getString(R.string.active));
		sb.append(" ");
		if (days > 0) {
			if (days > 1) {
				sb.append(String.format("%d %s", days, mContext
						.getString(R.string.days)));
			} else {
				sb.append(String.format("%d %s", days, mContext
						.getString(R.string.day)));
			}
		} else {
			if (hours > 0) {
				if (hours > 1) {
					sb.append(String.format("%d %s", days, mContext
							.getString(R.string.hh)));
				} else {
					sb.append(String.format("%d %s", days, mContext
							.getString(R.string.h)));
				}
			} else {
				if (minutes > 1) {
					sb.append(String.format("%d %s", days, mContext
							.getString(R.string.mm)));
				} else {
					sb.append(String.format("%d %s", days, mContext
							.getString(R.string.m)));
				}
			}
		}
		return sb.toString();
	}

}
