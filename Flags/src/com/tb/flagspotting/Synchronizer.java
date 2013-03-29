package com.tb.flagspotting;

import java.util.LinkedList;
import java.util.ListIterator;

import android.os.Handler;

public class Synchronizer implements Runnable {
	private static String TAG = "Synchronizer";

	public static final int TICK_FREQ = 100;
	private int mTickFreq;
	private boolean mRunning;
	public interface Counter {
		public int tick();
	}

	public interface Event {
		public void tick(int time);
	}

	public interface Finalizer {
		public void doFinalEvent();
	}

	private Counter mMainCounter;
	private Finalizer mMainFinalizer;
	private LinkedList<Event> mEvents;
	private boolean mDone;
	private Handler mHandler;
	public Synchronizer(int tickFreq) {
		mTickFreq = tickFreq;
		mMainCounter = null;
		mMainFinalizer = null;
		mEvents = new LinkedList<Event>();
		mDone = false;
		mHandler = new Handler();
		mRunning = false;
	}

	public Synchronizer() {
		mTickFreq = TICK_FREQ;
		mMainCounter = null;
		mMainFinalizer = null;
		mEvents = new LinkedList<Event>();
		mDone = false;
		mHandler = new Handler();
		mRunning = false;
	}

	public void setCounter(Counter c) {
		mMainCounter = c;
	}

	public void setFinalizer(Finalizer f) {
		mMainFinalizer = f;
	}

	public void addEvent(Event e) {
		mEvents.add(e);
	}

	public void start() {
		// Log.d(TAG,"calling start()");
		mDone = false;
		mHandler.postDelayed(this, mTickFreq);
		mRunning = true;
	}

	public void run() {
		if (mDone)
			return;
		int time = mMainCounter.tick();

		ListIterator<Event> iter = mEvents.listIterator();

		while (iter.hasNext()) {
			Event e = iter.next();
			e.tick(time);
		}

		if (time <= 0) {
			if (mMainFinalizer != null) {
				mMainFinalizer.doFinalEvent();
			}
		}

		mHandler.postDelayed(this, mTickFreq);
	}

	public void abort() {
		mRunning = false;
		// Log.d(TAG,"abort() has been called");
		if (mDone)
			return; // bail if abort has already been called
		mDone = true;
	}
	
	public boolean isRunning() {
		return mRunning;
	}

}
