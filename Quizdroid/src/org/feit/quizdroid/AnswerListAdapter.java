package org.feit.quizdroid;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

public class AnswerListAdapter extends BaseAdapter {

	static class ViewHolder {
		CheckedTextView text;
	}

	private LayoutInflater mInflater;
	private	List<Answer> mAnswers;

	public AnswerListAdapter(Context context, List<Answer> answers) {
		mInflater = LayoutInflater.from(context);
		mAnswers = answers;
	}

	@Override
	public int getCount() {
		return mAnswers.size();
	}

	@Override
	public Object getItem(int pos) {
		return mAnswers.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return mAnswers.get(position).id;
	}

	public Answer getAnswer(int position) {
		return mAnswers.get(position);
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
			convertView = mInflater.inflate(R.layout.answer_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.text = (CheckedTextView) convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		Answer answer = getAnswer(position);
		holder.text.setText(answer.text);
		holder.text.setChecked(answer.selected);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void reloadData(List<Answer> answers) {
		mAnswers = answers;
		notifyDataSetChanged();
	}

}
