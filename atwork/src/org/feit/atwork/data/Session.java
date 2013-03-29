package org.feit.atwork.data;
import java.util.Date;

public class Session {
	public long id;
	public long project_id;
	public Date start_time;
	public Date end_time;
	public String comment;
	
	public boolean weekStart;
	public boolean monthStart;
	
	public boolean inProgress() {
		return start_time.getTime() == end_time.getTime();
	}
	
	public long getSessionTime() {
		return end_time.getTime() - start_time.getTime();
	}
	
	public String getTimeString() {
		return getTimeString(end_time);
	}
	
	public String getTimeString(Date date) {
		long d = date.getTime() - start_time.getTime();
		long seconds = d / 1000;
		long minutes = d / (60 * 1000);
		long hours = d / (60 * 60 * 1000);
		long days = d / (24 * 60 * 60 * 1000);
		StringBuilder sb = new StringBuilder();
		if(days > 0) {
			sb.append(days);
			sb.append(" days");
		} else {
			if(hours > 0) {
				sb.append(hours);
				sb.append(" hours");
			}else {
				if(minutes > 0){
					if(sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(minutes);
					sb.append(" minutes");
				}else {
					if(seconds > 0) {
						sb.append(seconds);
						sb.append(" seconds");
					}
				}
			}
		}
		return sb.toString();
	}
	
	public static int getHours(long span) {
		return (int)(span / (60 * 60 * 1000));
	}

}
