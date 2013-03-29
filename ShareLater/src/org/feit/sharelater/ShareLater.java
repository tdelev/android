package org.feit.sharelater;

import java.util.List;

import org.feit.sharelater.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ShareLater extends Activity {
	static final String TAG = "ShareLater";
	static final int SAVE_INTENT_DIALOG = 0;

	private AlertDialog mSaveIntentDialog;
	private EditText mName;
	private IntentsProcessor mIntentsProcessor;
	private boolean mWiFiConnected;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		createDialogs();
		mIntentsProcessor = new IntentsProcessor(this);
		checkWirelessConnection();
		if (mWiFiConnected) {
			Log.d(TAG, "WiFi connected");
			setContentView(R.layout.share_now);
			Button btn = (Button) findViewById(R.id.btnShareLater);
			btn.setOnClickListener(onBtnListener);
			btn = (Button) findViewById(R.id.btnShareNow);
			btn.setOnClickListener(onBtnListener);

		} else {
			showDialog(SAVE_INTENT_DIALOG);
		}
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

	final View.OnClickListener onBtnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btnShareLater) {
				setContentView(R.layout.loading_view);
				List<ResolveInfo> ri = getPackageManager().queryIntentActivities(getIntent(), PackageManager.MATCH_DEFAULT_ONLY);
				for(ResolveInfo r : ri) {
					Log.d(TAG, "RI: " + r.nonLocalizedLabel);
				}
				showDialog(SAVE_INTENT_DIALOG);
			}
			if (v.getId() == R.id.btnShareNow) {
				Intent i = Intent.createChooser(getIntent(),
						getString(R.string.share_via));
				startActivity(i);
				finish();
			}

			if (v.getId() == R.id.btnPending) {
				Intent intent = new Intent(ShareLater.this,
						ShareLaterTabs.class);
				startActivity(intent);
			}

			if (v.getId() == R.id.btnClose) {
				finish();
			}

		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == SAVE_INTENT_DIALOG) {
			return mSaveIntentDialog;
		}
		return super.onCreateDialog(id);
	}

	private void createDialogs() {
		final View enterNameView = LayoutInflater.from(this).inflate(
				R.layout.intent_name, null);
		mName = (EditText) enterNameView.findViewById(R.id.name);

		mSaveIntentDialog = new AlertDialog.Builder(this).setTitle(
				R.string.save_intent_dialog_title).setView(enterNameView)
				.setPositiveButton(R.string.save,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								saveIntent();
							}
						}).setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).create();
	}

	private void saveIntent() {
		setContentView(R.layout.loading_view);
		setProgressBarIndeterminateVisibility(true);
		Thread t = new Thread(mSaveIntentRunnable);
		t.start();
	}

	final Runnable mSaveIntentRunnable = new Runnable() {

		@Override
		public void run() {
			String name = mName.getText().toString();
			if (name.length() <= 0) {
				name = getString(R.string.unknown);
			}
			if (name.length() > 20) {
				name = name.substring(0, 20);
			}
			mIntentsProcessor.saveIntent(name, getIntent());
			mSaveIntentHandler.sendEmptyMessage(0);
		}
	};

	final Handler mSaveIntentHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			setContentView(R.layout.saved_item);
			Button btn = (Button) findViewById(R.id.btnPending);
			btn.setOnClickListener(onBtnListener);
			btn = (Button) findViewById(R.id.btnClose);
			btn.setOnClickListener(onBtnListener);
		}

	};

}