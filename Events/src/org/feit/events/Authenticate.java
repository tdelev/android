package org.feit.events;

import org.feit.geoevents.http.RestRequest;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class Authenticate extends Activity {

	static final String TAG = "Login";
	private String mFrob;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onResume() {
		super.onResume();
		Uri uri = this.getIntent().getData();
		if (uri != null) {
			mFrob = uri.getQueryParameter("frob");
			Log.d(TAG, "frob: "+ mFrob);			
		}
		new GetTokenTask().execute();
	}
	
	private class GetTokenTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			RestRequest.getToken(mFrob, Authenticate.this);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			finish();
		}
		
	}
}
