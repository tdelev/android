package org.feit.sharelater;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TabHost;

public class ShareLaterTabs extends TabActivity {
	static final String PENDING = "Pending";
	static final String SHARED = "Shared";
	public static final String CONNECTED = "connected";
	static final int DISCONNECTED_DIALOG = 0;
	private boolean mWiFiConnected;
	private AlertDialog mDisconnectedDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkWirelessConnection();
		createDialogs();
		if(!mWiFiConnected) {
			showDialog(DISCONNECTED_DIALOG);
		}
		final TabHost tabHost = getTabHost();
		Intent intent = new Intent(this, PendingShareList.class);
		intent.putExtra(CONNECTED, mWiFiConnected);
		tabHost.addTab(tabHost.newTabSpec(PENDING).setIndicator(
				getString(R.string.pending),
				getResources().getDrawable(R.drawable.pending)).setContent(
				intent));
		intent = new Intent(this, SharedList.class);
		intent.putExtra(CONNECTED, mWiFiConnected);
		tabHost.addTab(tabHost.newTabSpec(SHARED).setIndicator(
				getString(R.string.shared),
				getResources().getDrawable(R.drawable.shared)).setContent(
				intent));

	}

	private void checkWirelessConnection() {
		mWiFiConnected = false;
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			SupplicantState state = wifiInfo.getSupplicantState();
			if (state == SupplicantState.COMPLETED) {
				mWiFiConnected = true;
			}
		}
	}

	private void createDialogs() {
		mDisconnectedDialog = new AlertDialog.Builder(this).setTitle(
				R.string.disconnected_dialog_title).setMessage(
				getString(R.string.disconnected_dialog_msg)).setIcon(R.drawable.wifi_off).setPositiveButton(
				R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == DISCONNECTED_DIALOG) {
			return mDisconnectedDialog;
		}
		return super.onCreateDialog(id);
	}
	

}
