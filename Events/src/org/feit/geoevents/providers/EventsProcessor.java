package org.feit.geoevents.providers;

import java.util.List;

import org.feit.geoevents.http.RestRequest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

public class EventsProcessor {
	private static final String TAG = "EventsProcessor";
	
	private Context mContext;
	private ContentResolver mContent;
	private ContentValues mContentValues;
	public static final int POST = 0x1;
	public static final int GET = 0x2;
	public static final int DELETE = 0x3;
	public static final int UPDATE = 0x4;
	public static final int TRANSACTION = 0x10;
	public static final int REQUEST_OK = 0x0;
	public static final int REQUEST_FAILED = 0x20;
	private static EventsProcessor mInstance;
	
	private int mLastEventId;
	private int mUserId;
	private List<Event> mLocalEvents;
	private List<Event> mSearchedEvents;
	private List<Category> mCategories;
	private double mLat;
	private double mLng;
	private double mDistance;

	private EventsProcessor(Context context) {
		mContext = context;
		mContent = mContext.getContentResolver();
	}
	
	public static EventsProcessor getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new EventsProcessor(context);
		}
		return mInstance;
	}

	public ContentResolver getContentResolver() {
		return mContent;
	}	
	
	public List<Event> getNearByEvents(double latitude, double longitude, float radius, long categoryId) {
		mLocalEvents = RestRequest.getEventsNearBy(latitude, longitude, radius, categoryId);
		return mLocalEvents;
	}
	
	public Event getEvent(long id) {
		if(mLocalEvents != null) {
			for(Event event : mLocalEvents) {
				if(event.id == id) {
					return event;
				}
			}			
		}
		if(mSearchedEvents != null) {
			for(Event event : mSearchedEvents) {
				if(event.id == id) {
					return event;
				}
			}			
		}
		return null;
	}
		
	public List<Event> searchEvents(String keywords) {
		mSearchedEvents = RestRequest.searchEvents(keywords);
		return mSearchedEvents;
	}
	
	public List<Event> friendsEvents() {
		String token = UserSettings.getInstance(mContext).getToken();
		return RestRequest.friendsEvents(token);
	}
	
	public List<Category> getCategories() {
		if(mCategories == null) {
			mCategories = RestRequest.getCategories();
		}
		return mCategories;
	}

}
