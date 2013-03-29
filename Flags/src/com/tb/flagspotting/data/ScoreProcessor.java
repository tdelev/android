package com.tb.flagspotting.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ScoreProcessor {
	static final int TOTAL_SCORES = 10;
	private Context mContext;
	public ScoreProcessor(Context context) {
		mContext = context;
	}
	
	public boolean isHighScore(int points) {
		ContentResolver cr = mContext.getContentResolver();
		String sortOrder = "score ASC";
		Cursor cursor = cr.query(ScoresProvider.CONTENT_URI, ScoresProvider.ALL, null, null, sortOrder);
		if(cursor == null || cursor.getCount() < TOTAL_SCORES) {
			return true;
		}
		while(cursor.moveToNext()) {
			int score = cursor.getInt(cursor.getColumnIndex(ScoresProvider.SCORE));
			if(points > score) {
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}
	
	public void addHighScore(String name, int points) {
		ContentResolver cr = mContext.getContentResolver();
		// Insert new high score
		ContentValues values = new ContentValues();
		values.put(ScoresProvider.NAME, name);
		values.put(ScoresProvider.SCORE, points);
		values.put(ScoresProvider.DATE, new Date().getTime());
		cr.insert(ScoresProvider.CONTENT_URI, values);
		// Delete last high score
		String sortOrder = "score ASC";
		Cursor cursor = cr.query(ScoresProvider.CONTENT_URI, ScoresProvider.ALL, null, null, sortOrder);
		if(cursor.getCount() > TOTAL_SCORES) {
			cursor.moveToFirst();
			long id = cursor.getLong(0);
			String where = "_id = " + id;
			cr.delete(ScoresProvider.CONTENT_URI, where, null);
			
		}
		cursor.close();
	}
	
	public List<Score> getHighScores() {
		ContentResolver cr = mContext.getContentResolver();
		String sortOrder = "score DESC";
		Cursor cursor = cr.query(ScoresProvider.CONTENT_URI, ScoresProvider.ALL, null, null, sortOrder);
		List<Score> scores = new ArrayList<Score>();
		if(cursor != null) {
			while(cursor.moveToNext()) {
				Score s = new Score();
				s.id = cursor.getLong(0);
				s.name = cursor.getString(1);
				s.points = cursor.getInt(2);
				s.date = cursor.getLong(3);
				scores.add(s);
			}
		}
		cursor.close();
		return scores;
	}
	
	public void clearHighScores() {
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(ScoresProvider.CONTENT_URI, null, null);
	}
}
