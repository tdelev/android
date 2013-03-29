package org.feit.jokesmk;

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

public class CategoriesListAdapter extends BaseAdapter{

	static class ViewHolder {
		CheckedTextView name;
	}

	private LayoutInflater mInflater;
	private HashMap<Long, Category> mCategories;
	private Category[] mCategoriesList;
	
	public CategoriesListAdapter(Context context, HashMap<Long, Category> categories) {
		mInflater = LayoutInflater.from(context);
		mCategories = categories;
		mCategoriesList = new Category[mCategories.values().size()];
		mCategories.values().toArray(mCategoriesList);
	}

	@Override
	public int getCount() {
		return mCategories.size();
	}

	@Override
	public Object getItem(int pos) {
		return mCategoriesList[pos];
	}

	@Override
	public long getItemId(int position) {
		return mCategoriesList[position].id;
	}
	
	public Category getCategory(int position) {
		return mCategoriesList[position];
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
			holder.name = (CheckedTextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		
		Category category = mCategoriesList[position];
		holder.name.setText(String.format("%s (%d)",category.name, category.total));
		holder.name.setChecked(category.isSelected);
		return convertView;
	}
	
	public void checkCategory(int position) {
		mCategoriesList[position].isSelected = !mCategoriesList[position].isSelected;
		notifyDataSetChanged();
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	public long[] getCheckedIds() {
		int total = 0;
		for(Category c : mCategoriesList) {
			if(c.isSelected) {
				total++;
			}
		}
		long[] res = new long[total];
		int i = 0;
		for(Category c : mCategoriesList) {
			if(c.isSelected) {
				res[i++] = c.id;
			}
		}
		return res;
	}
	
	

}
