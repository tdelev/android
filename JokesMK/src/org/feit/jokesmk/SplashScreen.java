package org.feit.jokesmk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

public class SplashScreen extends Activity {
	static final int TIMEOUT = 3000;
	private boolean mActive = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/idolwild.ttf");
		TextView tv = (TextView) findViewById(R.id.tvName);
		tv.setTypeface(font);
		// thread for displaying the SplashScreen
	    Thread splashTread = new Thread() {
	        @Override
	        public void run() {
	            try {
	                int waited = 0;
	                while(mActive && (waited < TIMEOUT)) {
	                    sleep(100);
	                    if(mActive) {
	                        waited += 100;
	                    }
	                }
	            } catch(InterruptedException e) {
	                // do nothing
	            } finally {
	                finish();
	                Intent intent = new Intent(SplashScreen.this, JokesList.class);
	                startActivity(intent);
	            }
	        }
	    };
	    splashTread.start();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        mActive = false;
	    }
	    return true;
	}
}
