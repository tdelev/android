package org.feit.quizdroid;

import android.content.Context;

public class Score {
	public int score;
	public int time;

	public String toReadableTime(Context c) {
		time /= 1000;
		int d = time / (3600 * 24);
		int h = (time % (3600 * 24)) / 3600;
		int m = (time % 3600) / 60;
		int s = time % 60;
		StringBuffer result = new StringBuffer();
		if (d > 0) {
			if (d == 1) {
				result.append(String.format("%d %s", d, c
						.getString(R.string.day)));
			} else {
				result.append(String.format("%d %s", d, c
						.getString(R.string.days)));
			}
		}
		if (h > 0 || d > 0) {
			if (d > 0)
				result.append(" ");
			result.append(String
					.format("%d %s", h, c.getString(R.string.hours)));

		}
		if (m > 0 || h > 0 || d > 0) {
			if (h > 0 || d > 0)
				result.append(", ");
			result.append(String.format("%d %s", m, c
					.getString(R.string.minutes)));

		}
		if ((s > 0 || m > 0 || h > 0) && d == 0) {
			if (m > 0 || h > 0)
				result.append(", ");
			result.append(String.format("%d %s", s, c
					.getString(R.string.seconds)));

		}
		return result.toString();
	}
}
