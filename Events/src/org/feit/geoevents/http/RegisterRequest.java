package org.feit.geoevents.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RegisterRequest {
	static final String TAG = "RegisterRequest";
	static final String URL = "http://192.168.15.101/api/register/";
	
	public static int register(String username, String password, String email) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URL);
		final HttpParams params = createParamsForPosting();
		httpClient.setParams(params);
		
		NameValuePair usernameValue = new BasicNameValuePair("username", username);
		NameValuePair passwordValue = new BasicNameValuePair("password", password);
		NameValuePair emailValue = new BasicNameValuePair("email", email);
		List<NameValuePair> listParams = new ArrayList<NameValuePair>();
		listParams.add(usernameValue);
		listParams.add(passwordValue);
		listParams.add(emailValue);
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

	private static HttpParams createParamsForPosting() {
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		HttpProtocolParams.setUseExpectContinue(params, false); // solves the
		// '417' issue
		return params;

	}
}
