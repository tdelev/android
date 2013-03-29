package org.feit.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.feit.geoevents.providers.Event;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EventsListAdapter extends BaseAdapter {

	static class ViewHolder {
		TextView header;
		TextView name;
		TextView timeToGo;
		TextView venue_name;
		ImageView icon;
		TextView distance;
	}

	private LayoutInflater mInflater;
	private int mSize;
	private List<Event> mEvents;
	private DateFormat mDateFormat;
	private Location mCurrentLocation;
	private Context mContext;
	private Map<Long, Drawable> mCache;

	public EventsListAdapter(Context context, List<Event> events,
			Location currentLocation) {
		mContext = context;
		mCache = new HashMap<Long, Drawable>();
		mInflater = LayoutInflater.from(context);
		mEvents = events;
		mSize = mEvents.size();
		mDateFormat = new SimpleDateFormat("E, d MMM");
		mCurrentLocation = currentLocation;
	}

	@Override
	public int getCount() {
		return mSize;
	}

	@Override
	public Object getItem(int pos) {
		return mEvents.get(pos);
	}

	public Event getEvent(int position) {
		return mEvents.get(position);
	}
	@Override
	public long getItemId(int position) {
		return mEvents.get(position).id;
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
			convertView = mInflater.inflate(R.layout.event_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.header = (TextView) convertView.findViewById(R.id.header);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.timeToGo = (TextView) convertView
					.findViewById(R.id.time_to_go);
			holder.venue_name = (TextView) convertView
					.findViewById(R.id.venue_name);
			holder.icon = (ImageView)convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		Event event = mEvents.get(position);
		/*
		 * if(event.state == (EventsProcessor.TRANSACTION |
		 * EventsProcessor.POST)) { convertView.setBackgroundColor(Color.RED);
		 * }else { convertView.setBackgroundColor(Color.GRAY); }
		 */
		if (event.start_time == null) {
			event.start_time = event.start_date;
		}
		final String day = mDateFormat.format(event.start_time);
		holder.header.setVisibility(View.VISIBLE);
		holder.header.setText(day);
		if (position != 0) {
			final Event previous = mEvents.get(position - 1);
			if (previous.start_date.equals(event.start_date)) {
				holder.header.setVisibility(View.GONE);
			}
		}
		holder.name.setText(event.name);
		holder.timeToGo.setText(timeFromNow(event.start_time));
		holder.venue_name.setText(event.venue.name);
		// holder.distance.setText("here distance");
		holder.icon.setImageDrawable(getDrawable(event.category_id));

		return convertView;
	}

	public void refreshDataSet(List<Event> events, Location currentLocation) {
		mCurrentLocation = currentLocation;
		mEvents = events;
		mSize = mEvents.size();
		notifyDataSetChanged();
	}

	private Drawable getDrawable(long categoryId) {
		int id = mContext.getResources().getIdentifier(
				String.format("c%d", categoryId), "drawable", "org.feit.events");
		if (mCache.containsKey(categoryId)) {
			return mCache.get(categoryId);
		} else {
			Drawable d = mContext.getResources().getDrawable(id);
			mCache.put(categoryId, d);
			return d;
		}
	}

	private String timeFromNow(Date startTime) {
		Date now = new Date();
		long minutes = (startTime.getTime() - now.getTime()) / (60 * 1000);
		long hours = (startTime.getTime() - now.getTime()) / (60 * 60 * 1000);
		long days = (startTime.getTime() - now.getTime())
				/ (24 * 60 * 60 * 1000);
		StringBuilder sb = new StringBuilder();
		if (days > 0) {
			sb.append(days);
			sb.append(" days");
		} else {
			if (hours > 0) {
				sb.append(hours);
				sb.append(" hours");
			} else {
				if (minutes > 0) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(minutes);
					sb.append(" minutes");
				}
			}
		}
		sb.append(" to go...");
		return sb.toString();
	}

}
