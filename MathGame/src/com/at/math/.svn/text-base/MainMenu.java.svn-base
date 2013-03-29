package com.at.math;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.scoreloop.client.android.ui.LeaderboardsScreenActivity;

public class MainMenu extends Activity {

	static final int RATE_DIALOG = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/tiza.ttf");
		TextView tv = (TextView) findViewById(R.id.tvAppName);
		tv.setTypeface(tf);
		tf = Typeface.createFromAsset(getAssets(), "fonts/EraserRegular.ttf");
		Button btn = (Button) findViewById(R.id.btnTimeMode);
		btn.setTypeface(tf);
		btn = (Button) findViewById(R.id.btnHighScores);
		btn.setTypeface(tf);
		btn = (Button) findViewById(R.id.btnScoreloop);
		btn.setTypeface(tf);

	}

	public void onBtnClick(View v) {
		if (v.getId() == R.id.btnTimeMode) {
			Intent intent = new Intent(this, MathGame.class);
			startActivity(intent);
		}
		if (v.getId() == R.id.btnHighScores) {
			Intent intent = new Intent(this, HighScores.class);
			startActivity(intent);
		}
		if (v.getId() == R.id.btnScoreloop) {
			Intent intent = new Intent(this, LeaderboardsScreenActivity.class);
			startActivity(intent);
		}
	}

	/*
	 * Override back key press for rating
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			boolean rated = GamePrefs.getInstance(this).isRated();
			if (!rated) {
				int useCount = GamePrefs.getInstance(this).checkUsege();

				if (useCount == 3) {
					GamePrefs.getInstance(this).resetUsage();
					showDialog(RATE_DIALOG);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == RATE_DIALOG) {
			AlertDialog.Builder alert = new AlertDialog.Builder(MainMenu.this);
			alert.setTitle(R.string.Rate_App);
			alert.setMessage(R.string.Rate_App_Msg);
			alert.setPositiveButton(R.string.Rate_App,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							GamePrefs.getInstance(MainMenu.this).setRated();
							Intent marketIntent = new Intent(
									Intent.ACTION_VIEW, Uri
											.parse(GamePrefs.MARKET_LINK));
							startActivity(marketIntent);
						}
					});
			alert.setNegativeButton(R.string.Rate_App_Later,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							finish();
						}
					});
			return alert.create();
		}
		return super.onCreateDialog(id);
	}

}
