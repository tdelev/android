package org.feit.geoevents.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.util.Log;

public class CreateEventRequest {
	static final String TAG = "CreateEventRequest";
	static final String URL = "http://192.168.15.101/api/event/new/";

	public static int create(ContentValues values) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URL);
		final HttpParams params = createParamsForPosting();
		httpClient.setParams(params);
		
		NameValuePair userIdValue = new BasicNameValuePair("user_id", values.getAsString("user_id"));
		NameValuePair nameValue = new BasicNameValuePair("name", values.getAsString("name"));
		NameValuePair descriptionValue = new BasicNameValuePair("description", values.getAsString("description"));
		NameValuePair latValue = new BasicNameValuePair("lat", values.getAsString("lat"));
		NameValuePair lngValue = new BasicNameValuePair("lng", values.getAsString("lng"));
		NameValuePair addressValue = new BasicNameValuePair("address", values.getAsString("address"));
		NameValuePair locTypeValue = new BasicNameValuePair("loc_type", values.getAsString("loc_type"));
		NameValuePair startDateValue = new BasicNameValuePair("start_date", values.getAsString("start_time"));
		NameValuePair endDateValue = new BasicNameValuePair("end_date", values.getAsString("end_time"));
		NameValuePair isPublicValue = new BasicNameValuePair("is_public", values.getAsString("public"));
		List<NameValuePair> listParams = new ArrayList<NameValuePair>();
		listParams.add(userIdValue);
		listParams.add(nameValue);
		listParams.add(descriptionValue);
		listParams.add(latValue);
		listParams.add(lngValue);
		listParams.add(locTypeValue);
		listParams.add(startDateValue);
		listParams.add(endDateValue);
		listParams.add(addressValue);
		listParams.add(isPublicValue);
		UrlEncodedFormEntity requestEntity = null;
		try {
			requestEntity = new UrlEncodedFormEntity(listParams, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		httpPost.setEntity(requestEntity);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			Log.d(TAG, responseString);
			JSONObject jsonResponse = null;
			jsonResponse = new JSONObject(responseString);

			if (jsonResponse.has("error")) {
				return -1;
			} else {				
				return jsonResponse.getInt("event_id");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	private static HttpParams createParamsForPosting() {
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		HttpProtocolParams.setUseExpectContinue(params, false); // solves the
		// '417' issue
		return params;

	}
}
