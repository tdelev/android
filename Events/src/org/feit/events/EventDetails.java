package org.feit.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.feit.geoevents.providers.Event;
import org.feit.geoevents.providers.EventsProcessor;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EventDetails extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_details);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/Red October-Regular.otf");
		TextView tv = (TextView) findViewById(R.id.title_info);
		tv.setTypeface(tf);
		long id = getIntent().getLongExtra("event_id", -1);
		EventsProcessor eventProcessor = EventsProcessor.getInstance(this);
		Event event = eventProcessor.getEvent(id);
		tv = (TextView) findViewById(R.id.tvName);
		tv.setText(event.name);
		tv = (TextView) findViewById(R.id.tvTimeFrom);
		DateFormat df = new SimpleDateFormat("dd MMMM, yyyy HH:ss");
		tv.setText(df.format(event.start_time));
		tv = (TextView) findViewById(R.id.tvTimeTo);
		if(event.end_time != null) {
			tv.setText(df.format(event.end_time));
		} else {
			tv.setText("/");
		}
		tv = (TextView) findViewById(R.id.tvVenueName);
		tv.setText(event.venue.name);
		tv = (TextView) findViewById(R.id.tvVenueAddress);
		tv.setText(event.venue.address);
		tv = (TextView) findViewById(R.id.tvVenueCity);
		tv.setText(event.venue.city);
		tv = (TextView) findViewById(R.id.tvDescription);
		tv.setText(event.description);
		tv = (TextView) findViewById(R.id.tvURL);
		tv.setText(event.url);
	}
}
