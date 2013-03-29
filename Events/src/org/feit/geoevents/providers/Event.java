package org.feit.geoevents.providers;

import java.util.Date;

public class Event {
	public long id;
	public String name;
	public long category_id;
	public Date start_date;
	public Date end_date;
	public Date start_time;
	public Date end_time;
	public String description;
	public String url;
	public double latitude;
	public double longitude;
	public boolean personal;
	public boolean selfpromotion;
	public String ticket_url;
	public String ticket_price;
	public boolean ticket_free;
	
	public Venue venue;
}
