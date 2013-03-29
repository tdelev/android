package org.feit.quizdroid;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;

public class Player {
	private static final String QUIZ = "quiz_settings";
	private static Player mInstance;
	private Context mContext;
	public String deviceId;
	public String imsi;
	public String model;
	public String release;
	
	private Player(Context context) {
		mContext = context;
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = telephonyManager.getDeviceId();
		imsi = telephonyManager.getSubscriberId();
		model = android.os.Build.MODEL;
		release = android.os.Build.VERSION.RELEASE;
	}
	public static Player getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new Player(context);
		}
		return mInstance;
	}
	
	public String getName() {
		return mContext.getSharedPreferences(QUIZ, Context.MODE_PRIVATE)
		.getString("name", "");
	}
	
	public void setName(String name) {
		Editor e = mContext.getSharedPreferences(QUIZ, Context.MODE_PRIVATE).edit();
		e.putString("name", name);
		e.commit();
	}
}
