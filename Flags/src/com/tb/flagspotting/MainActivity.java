package com.tb.flagspotting;

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
import android.widget.ToggleButton;

import com.scoreloop.client.android.ui.LeaderboardsScreenActivity;

public class MainActivity extends Activity {
	static final int RATE_DIALOG = 0;

	GamePrefs mGamePrefs;
	ToggleButton mTbSoundOnOff;
	boolean mSavedGame;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Typeface tf = Typeface
				.createFromAsset(getAssets(), "fonts/GoodDog.otf");
		Button btn = (Button) findViewById(R.id.btnNewGame);
		btn.setTypeface(tf);
		btn = (Button) findViewById(R.id.btnContinue);
		btn.setTypeface(tf);
		btn = (Button) findViewById(R.id.btnHighScores);
		btn.setTypeface(tf);
		btn = (Button) findViewById(R.id.btnHighScoresScoreloop);
		btn.setTypeface(tf);

		mTbSoundOnOff = (ToggleButton) findViewById(R.id.tbSoundOnOff);
		mGamePrefs = GamePrefs.getInstance(this);
		
		//AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });

		//AdView view = (AdView) findViewById(R.id.ad);
		//view.requestFreshAd();
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadGamePrefs();
	}

	private void loadGamePrefs() {
		Button btn = (Button) findViewById(R.id.btnContinue);
		if (mGamePrefs.getSaved()) {
			btn.setVisibility(View.VISIBLE);
		} else {
			btn.setVisibility(View.INVISIBLE);
		}
		mTbSoundOnOff.setChecked(mGamePrefs.getSoundOn());
	}

	public void onBtnClick(View v) {
		if (v.getId() == R.id.btnNewGame) {
			mGamePrefs.clearSavedGame();
			Intent intent = new Intent("com.tb.flags.action.NEW_GAME");
			startActivity(intent);
		}
		if (v.getId() == R.id.btnContinue) {
			Intent intent = new Intent("com.tb.flags.action.CONTINUE");
			startActivity(intent);
		}
		if (v.getId() == R.id.btnHighScores) {
			Intent intent = new Intent(MainActivity.this, HighScores.class);
			startActivity(intent);
		}
		if (v.getId() == R.id.btnHighScoresScoreloop) {
			startActivity(new Intent(MainActivity.this,
					LeaderboardsScreenActivity.class));
		}
		if (v.getId() == mTbSoundOnOff.getId()) {
			mGamePrefs.setSound(mTbSoundOnOff.isChecked());
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

				if (useCount == 5) {
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
			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
			alert.setTitle(R.string.Rate_App);
			alert.setMessage(R.string.Rate_App_Msg);
			alert.setPositiveButton(R.string.Rate_App,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							GamePrefs.getInstance(MainActivity.this).setRated();
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