package org.feit.geoevents.providers;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.Contacts;
import android.provider.Contacts.People;

public class FriendsProcessor {
	private Context mContext;
	public FriendsProcessor(Context context) {
		mContext = context;
	}
	
	public List<Friend> getFriends() {
		List<Friend> result = new ArrayList<Friend>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor people = cr.query(People.CONTENT_URI, null, null, null, null);
		while(people.moveToNext()) {
			Friend friend = new Friend();
			friend.id = people.getString(people.getColumnIndex(People._ID));
			friend.name = people.getString(people.getColumnIndex(People.DISPLAY_NAME));
			result.add(friend);
		}
		people.close();
		return result;
	}
}
