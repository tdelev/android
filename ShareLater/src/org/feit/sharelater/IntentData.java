package org.feit.sharelater;

import java.util.Date;
import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class IntentData {
	public long Id;
	public String name;
	public String action;
	public String data;
	public Set<String> categories;
	public String type;
	public String component;
	public Date dateAdded;
	public Date dateSended;
	public boolean isSended;
	
	public Bundle extras;
	
	public Intent getIntent() {
		Intent intent = new Intent(action);
		intent.setType(type);
		intent.putExtras(extras);
		return intent;
	}
	
	public String extrasToString() {
		StringBuilder sb = new StringBuilder();
		for(String key : extras.keySet()) {
			sb.append(String.format("%s : %s\n", key, extras.get(key)));
		}
		return sb.toString();
	}
	
	public Uri getUri() {
		if(extras.containsKey(Intent.EXTRA_STREAM)) {
			return Uri.parse(String.valueOf(extras.get(Intent.EXTRA_STREAM)));
		}
		if(extras.containsKey(Intent.EXTRA_TEXT)) {
			return Uri.parse(String.valueOf(extras.get(Intent.EXTRA_TEXT)));
		}
		return null;
	}
}
