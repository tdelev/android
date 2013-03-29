package com.at.math;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.at.math.data.Score;

public class ScoresListAdapter extends BaseAdapter {

	static class ViewHolder {
		TextView number;
		TextView name;
		TextView score;
		TextView date;
	}

	private LayoutInflater mInflater;
	private int mSize;
	private DateFormat mDateFormat;
	private List<Score> mScores;
	private Typeface mTf;
	
	public ScoresListAdapter(Context context, List<Score> scores) {
		mInflater = LayoutInflater.from(context);
		mScores = scores;
		mSize = mScores.size();
		mDateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
		mTf = Typeface
		.createFromAsset(context.getAssets(), "fonts/EraserRegular.ttf");
	}
	
	@Override
	public int getCount() {
		return mSize; 
	}

	@Override
	public Object getItem(int pos) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return mScores.get(position).id;
	}
	
	public int getHighScore() {
		if(mScores.size() > 0) {
			return mScores.get(0).points;			
		}else {
			return -1;
		}
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
			convertView = mInflater.inflate(R.layout.score_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.number = (TextView)convertView.findViewById(R.id.no);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.score = (TextView) convertView.findViewById(R.id.score);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.number.setTypeface(mTf);
			holder.name.setTypeface(mTf);
			holder.score.setTypeface(mTf);
			//holder.date.setTypeface(mTf);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		Score score = mScores.get(position);
		
		holder.number.setText(String.format("%d.", position + 1));
		holder.name.setText(String.format("%s", score.name));
		holder.score.setText(String.format("%d", score.points));
		holder.date.setText(mDateFormat.format(new Date(score.date)));
		return convertView;
	}

	public void refreshDataSet(Context context, List<Score> scores) {
		mScores = scores;
		mSize = mScores.size();
		notifyDataSetChanged();
	}

}
