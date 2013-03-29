package org.feit.sharelater;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IntentsListAdapter extends BaseAdapter {

	static final int ICONS = 20;

	public enum ICON_TYPES {
		DEFAULT, AUDIO, VIDEO, IMAGE, TEXT, PDF
	};

	static class ViewHolder {
		TextView header;
		TextView name;
		ImageView icon;
		TextView date;
	}

	private LayoutInflater mInflater;
	private IntentData[] mIntents;
	private SimpleDateFormat mTimeFormat;
	private SimpleDateFormat mDateFormat;
	private HashMap<ICON_TYPES, Bitmap> mIcons;
	private Context mContext;

	public IntentsListAdapter(Context context, IntentData[] intents) {
		mInflater = LayoutInflater.from(context);
		mIntents = intents;
		mTimeFormat = new SimpleDateFormat("HH:mm");
		mDateFormat = new SimpleDateFormat("E, dd M yyyy");
		mIcons = new HashMap<ICON_TYPES, Bitmap>();
		mContext = context;
	}

	@Override
	public int getCount() {
		return mIntents.length;
	}

	@Override
	public Object getItem(int pos) {
		return mIntents[pos];
	}

	@Override
	public long getItemId(int position) {
		return mIntents[position].Id;
	}

	public IntentData getIntentData(int position) {
		return mIntents[position];
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
			convertView = mInflater.inflate(R.layout.intent_item, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.header = (TextView) convertView.findViewById(R.id.header);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.icon = (ImageView) convertView.findViewById(R.id.ivIcon);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		IntentData data = mIntents[position];
		final Date date = data.isSended ? data.dateSended : data.dateAdded;
		final String day = timeFromNow(date);

		holder.header.setVisibility(View.VISIBLE);
		holder.header.setText(day);
		if (position != 0) {
			final Date d = mIntents[position - 1].isSended ? mIntents[position - 1].dateSended
					: mIntents[position - 1].dateAdded;
			if (day.equals(timeFromNow(d))) {
				holder.header.setVisibility(View.GONE);
			}

		}
		holder.name.setText(data.name);
		holder.icon.setImageBitmap(getIcon(data.type));
		holder.date.setText(String.format("%s %s", mContext
				.getString(R.string.at), mTimeFormat.format(date)));
		return convertView;
	}

	public void refreshData(IntentData[] intents) {
		mIntents = intents;
		notifyDataSetChanged();
	}

	private Bitmap getIcon(String text) {
		ICON_TYPES key = getKey(text);
		if (mIcons.containsKey(key)) {
			return mIcons.get(key);
		} else {
			if (key == ICON_TYPES.AUDIO) {
				mIcons.put(key, BitmapFactory.decodeResource(mContext
						.getResources(), R.drawable.audio));
				return mIcons.get(key);
			}
			if (key == ICON_TYPES.VIDEO) {
				mIcons.put(key, BitmapFactory.decodeResource(mContext
						.getResources(), R.drawable.video));
				return mIcons.get(key);
			}
			if (key == ICON_TYPES.IMAGE) {
				mIcons.put(key, BitmapFactory.decodeResource(mContext
						.getResources(), R.drawable.image));
				return mIcons.get(key);
			}
			if (key == ICON_TYPES.PDF) {
				mIcons.put(key, BitmapFactory.decodeResource(mContext
						.getResources(), R.drawable.pdf));
				return mIcons.get(key);
			}
			if (key == ICON_TYPES.TEXT) {
				mIcons.put(key, BitmapFactory.decodeResource(mContext
						.getResources(), R.drawable.text));
				return mIcons.get(key);
			}
			if (key == ICON_TYPES.DEFAULT) {
				mIcons.put(key, BitmapFactory.decodeResource(mContext
						.getResources(), R.drawable.default_icon));
				return mIcons.get(key);
			}
		}
		return null;
	}

	private ICON_TYPES getKey(String text) {
		if (text.contains("text")) {
			return ICON_TYPES.TEXT;
		}
		if (text.contains("audio")) {
			return ICON_TYPES.AUDIO;
		}
		if (text.contains("pdf")) {
			return ICON_TYPES.PDF;
		}
		if (text.contains("video")) {
			return ICON_TYPES.VIDEO;
		}
		if (text.contains("image")) {
			return ICON_TYPES.IMAGE;
		}
		return ICON_TYPES.DEFAULT;
	}

	private String timeFromNow(Date startTime) {
		Calendar c = Calendar.getInstance();
		int cy = c.get(Calendar.YEAR);
		int cdy = c.get(Calendar.DAY_OF_YEAR);
		c.setTime(startTime);
		if (cy == c.get(Calendar.YEAR)) {
			if (cdy == c.get(Calendar.DAY_OF_YEAR)) {
				return mContext.getString(R.string.today);
			}
			if (cdy == c.get(Calendar.DAY_OF_YEAR) + 1) {
				return mContext.getString(R.string.yesterday);
			}
		}
		return mDateFormat.format(startTime);
	}

}
