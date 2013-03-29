package org.feit.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.feit.geoevents.providers.Category;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoriesListAdapter extends BaseAdapter {

	static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView description;
	}

	private LayoutInflater mInflater;
	private int mSize;
	private List<Category> mCategories;
	private Context mContext;
	private Map<Long, Drawable> mCache;

	public CategoriesListAdapter(Context context, List<Category> categories) {
		mContext = context;
		mCache = new HashMap<Long, Drawable>();
		mInflater = LayoutInflater.from(context);
		mCategories = categories;
		mSize = mCategories.size();
	}

	@Override
	public int getCount() {
		return mSize;
	}

	@Override
	public Object getItem(int pos) {
		return mCategories.get(pos);
	}

	public Category getCategory(int position) {
		return mCategories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mCategories.get(position).id;
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
			convertView = mInflater.inflate(R.layout.category_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.icon = (ImageView)convertView.findViewById(R.id.icon);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		Category category = mCategories.get(position);
		
		holder.name.setText(category.name);
		holder.icon.setImageDrawable(getDrawable(category.id));

		return convertView;
	}

	private Drawable getDrawable(long categoryId) {
		int id = mContext.getResources()
				.getIdentifier(String.format("c%d", categoryId), "drawable",
						"org.feit.events");
		if (mCache.containsKey(categoryId)) {
			return mCache.get(categoryId);
		} else {
			Drawable d = mContext.getResources().getDrawable(id);
			mCache.put(categoryId, d);
			return d;
		}
	}

}
