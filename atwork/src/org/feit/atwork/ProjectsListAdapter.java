package org.feit.atwork;

import org.feit.atwork.data.Project;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProjectsListAdapter extends BaseAdapter {

	static class ViewHolder {
		TextView name;
		ImageView progress;
	}

	private LayoutInflater mInflater;
	private Project[] mProjects;
	private Context mContext;

	public ProjectsListAdapter(Context context, Project[] projects) {
		mInflater = LayoutInflater.from(context);
		mProjects = projects;
		mContext = context;
	}

	@Override
	public int getCount() {
		return mProjects.length;
	}

	@Override
	public Object getItem(int pos) {
		return mProjects[pos];
	}

	@Override
	public long getItemId(int position) {
		return mProjects[position].Id;
	}

	public Project getProject(int position) {
		return mProjects[position];
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
			convertView = mInflater.inflate(R.layout.project_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.progress = (ImageView) convertView
					.findViewById(R.id.ivProgress);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		Project project = mProjects[position];
		holder.name.setText(project.name);
		if (project.progress) {
			holder.progress.setVisibility(View.VISIBLE);
		} else {
			holder.progress.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	public void refreshData(Project[] projects) {
		mProjects = projects;
		notifyDataSetChanged();
	}
}
