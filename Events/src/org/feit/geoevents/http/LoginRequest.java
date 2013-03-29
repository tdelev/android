package org.feit.geoevents.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LoginRequest {
	static final String TAG = "RegisterRequest";
	static final String URL = "http://192.168.15.101/api/login/?username=%s&password=%s";
	
	public static int login(String username, String password) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = String.format(URL, username, password);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			Log.d(TAG, responseString);
			JSONObject jsonResponse = null;
			jsonResponse = new JSONObject(responseString);
			if (jsonResponse.has("error")) {
				return -1;
			} else {				
				return jsonResponse.getInt("user_id");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
