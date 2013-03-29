package org.feit.geoevents.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.feit.geoevents.providers.Category;
import org.feit.geoevents.providers.Event;
import org.feit.geoevents.providers.UserSettings;
import org.feit.geoevents.providers.Venue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class RestRequest {
	static final String TAG = "GetEventsRequest";

	public static List<Event> getEventsNearBy(double latitude,
			double longitude, float radius, long categoryId) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = String
				.format(
						"%s?api_key=%s&method=event.search&location=%f,%f&radius=%f&format=json",
						Constants.URL, Constants.API_KEY, latitude, longitude,
						radius);
		if(categoryId != -1) {
			url = String
			.format(
					"%s?api_key=%s&method=event.search&location=%f,%f&radius=%f&category_id=%d&format=json",
					Constants.URL, Constants.API_KEY, latitude, longitude,
					radius, categoryId);
		}
		Log.d(TAG, url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			List<Event> result = parseEventsResponse(json);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<Event> searchEvents(String keywords) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		keywords = URLEncoder.encode(keywords);
		String url = String.format(
				"%s?api_key=%s&method=event.search&search_text=%s&format=json",
				Constants.URL, Constants.API_KEY, keywords);
		Log.d(TAG, url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			List<Event> result = parseEventsResponse(json);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Event> getByCategory(long categoryId) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = String.format(
				"%s?api_key=%s&method=event.search&category_id=%s&format=json",
				Constants.URL, Constants.API_KEY, categoryId);
		Log.d(TAG, url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			List<Event> result = parseEventsResponse(json);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Category> getCategories() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = String.format(
				"%s?api_key=%s&method=category.getList&format=json",
				Constants.URL, Constants.API_KEY);
		Log.d(TAG, url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			List<Category> result = parseCategoriesResponse(json);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Event> friendsEvents(String token) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = String.format(
				"%s?api_key=%s&token=%s&method=user.getMyFriendsEvents&format=json",
				Constants.URL, Constants.API_KEY, token);
		Log.d(TAG, url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			List<Event> result = parseEventsResponse(json);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void getToken(String frob, Context context) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = String.format(
				"%s?api_key=%s&method=auth.getToken&frob=%s&format=json",
				Constants.URL, Constants.API_KEY, frob);
		Log.d(TAG, url);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			JSONObject jo = new JSONObject(json);
			JSONObject rsp = jo.getJSONObject("rsp");
			JSONArray ta = rsp.getJSONArray("token");
			JSONObject t = ta.getJSONObject(0);
			UserSettings user = UserSettings.getInstance(context);
			user.setAuthenticated();
			user.setToken(t.getString("token"));
			user.setUserId(t.getInt("user_id"));
			user.setName(t.getString("user_username"));
			user.setUsername(t.getString("user_name"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static List<Event> parseEventsResponse(String json) {
		try {
			JSONObject jo = new JSONObject(json);
			JSONObject rsp = jo.getJSONObject("rsp");
			JSONArray events = rsp.getJSONArray("event");
			int size = events.length();
			Log.d(TAG, "size: " + size);
			List<Event> result = new ArrayList<Event>(size);
			for (int i = 0; i < size; i++) {
				Event event = new Event();
				JSONObject ej = events.getJSONObject(i);
				event.id = ej.getInt("id");
				event.name = ej.getString("name");
				event.description = ej.getString("description");
				event.latitude = Double.parseDouble(ej.getString("latitude"));
				event.longitude = Double.parseDouble(ej.getString("longitude"));
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				try {
					String sd = ej.getString("start_date");
					event.start_date = df.parse(sd);
					String d = ej.getString("end_date");
					if (d != null && !d.equals("")) {
						event.end_date = df.parse(d);
					}
					d = ej.getString("start_time");
					df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					if (d != null && !d.equals("")) {
						event.start_time = df.parse(String.format("%s %s", sd,
								d));
					}
					d = ej.getString("end_time");
					if (d != null && !d.equals("")) {
						event.end_time = df
								.parse(String.format("%s %s", sd, d));
					}
				} catch (ParseException pe) {
					// should never happen
				}
				event.personal = ej.getInt("personal") == 1;
				event.selfpromotion = ej.getInt("selfpromotion") == 1;
				event.venue = new Venue();
				event.venue.name = ej.getString("venue_name");
				event.venue.address = ej.getString("venue_address");
				event.venue.city = ej.getString("venue_city");
				String categories = ej.getString("category_id");
				String[] clist = categories.split(";");
				if(clist != null && clist.length > 0) {
					event.category_id = Long.parseLong(clist[0]);					
				}else {
					event.category_id = Long.parseLong(categories);	
				}
				event.url = ej.getString("url");
				result.add(event);
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static List<Category> parseCategoriesResponse(String json) {
		try {
			JSONObject jo = new JSONObject(json);
			JSONObject rsp = jo.getJSONObject("rsp");
			JSONArray events = rsp.getJSONArray("category");
			int size = events.length();
			Log.d(TAG, "size: " + size);
			List<Category> result = new ArrayList<Category>(size);
			for (int i = 0; i < size; i++) {
				Category category = new Category();
				JSONObject ej = events.getJSONObject(i);
				category.id = ej.getInt("id");
				category.name = ej.getString("name");
				category.description = ej.getString("description");
				result.add(category);
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

}
