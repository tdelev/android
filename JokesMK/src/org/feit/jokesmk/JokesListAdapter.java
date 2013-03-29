package org.feit.jokesmk;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;

public class JokesListAdapter extends BaseAdapter implements Filterable {

	static class ViewHolder {
		TextView name;
		TextView ratingText;
		TextView views;
		//RatingBar ratingBar;
	}

	private LayoutInflater mInflater;
	private List<Joke> mJokes;
	private List<Joke> mStartingJokes;
	private Context mContext;
	private String mViewsString;
	private JokesFilter mFilter;
	
	public JokesListAdapter(Context context, List<Joke> jokes) {
		mInflater = LayoutInflater.from(context);
		mJokes = jokes;
		mContext = context;
		mViewsString = mContext.getString(R.string.views);
	}

	@Override
	public int getCount() {
		return mJokes.size();
	}

	@Override
	public Object getItem(int pos) {
		return mJokes.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return mJokes.get(position).id;
	}

	public Joke getJoke(int position) {
		return mJokes.get(position);
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
			convertView = mInflater.inflate(R.layout.joke_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.ratingText = (TextView) convertView.findViewById(R.id.rating_text);
			holder.views = (TextView) convertView.findViewById(R.id.views);
			//holder.ratingBar = (RatingBar) convertView.findViewById(R.id.ratingbar);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		Joke joke = mJokes.get(position);
		if(joke.views > 0) {
			holder.name.setTypeface(null, Typeface.NORMAL);
		}else {
			holder.name.setTypeface(null, Typeface.BOLD);
		}
		holder.name.setText(joke.name);
		if(joke.rating > 0) {
			holder.ratingText.setText(String.format("%d / 5", joke.rating));			
		} else {
			holder.ratingText.setText("");//R.string.no_rating);
		}
		//holder.ratingBar.setRating(joke.rating);
		holder.views.setText(String.format("%d %s", joke.views, mViewsString));
		return convertView;
	}
	
	public void reloadData(List<Joke> jokes) {
		mJokes = jokes;
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		if(mFilter == null) {
			mFilter = new JokesFilter();
		}
		return mFilter;
	}
	
	private class JokesFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if(mStartingJokes == null) {
				mStartingJokes = new ArrayList<Joke>(mJokes);
			}
			List<Joke> jokes = null;
			if(constraint == null || constraint.length() <= 0) {
                jokes = new ArrayList<Joke>(mStartingJokes);
			} else {
				final String prefix = constraint.toString();
				jokes = new ArrayList<Joke>();
				for(Joke joke : mJokes) {
					if(joke.name.startsWith(prefix)) {
						jokes.add(joke);
					}
				}
				
			}
			results.count = jokes.size();
			results.values = jokes;
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			mJokes = (ArrayList<Joke>)results.values;
			if(results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
			
		}
		
	}

}
