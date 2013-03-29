package org.feit.sharelater;

import org.feit.sharelater.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WifiChangedReceiver extends BroadcastReceiver {

	static final String TAG = "WifiChangedRecevier";

	@Override
	public void onReceive(Context context, Intent intent) {
		NetworkInfo info = (NetworkInfo) intent
				.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		if (info != null) {
			Log.d(TAG, "onReceive: start");
			if (info.getState() == State.CONNECTED) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				if (prefs.getBoolean("notifications", true)) {
					showNotification(context);
					Log.d(TAG, "onReceive: notification shown");
				}
			}
		}
	}

	private void showNotification(Context context) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		IntentsProcessor intentsProcessor = new IntentsProcessor(context);
		int pending = intentsProcessor.getNewIntents();
		if(pending > 0) {
			String n = String.format("(%d) %s", pending, context
					.getString(R.string.notification_title));
			Notification notification = new Notification(
					R.drawable.notification_icon, n, System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults = Notification.DEFAULT_ALL;
			Intent i = new Intent(context, ShareLaterTabs.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i,
					Intent.FLAG_ACTIVITY_NEW_TASK);
			notification.setLatestEventInfo(context, n, context
					.getString(R.string.notification_text), contentIntent);
			nm.notify(R.string.app_name, notification);			
		}
	}

}
