package com.at.math;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class TimerPieView extends View implements Synchronizer.Event {
	private static final String TAG = "TimerBarView";
	static int PADDING;
	public static final int REDRAW_FREQ = 10;
	private int mTimeRemaining;
	private int mWidth;
	private int mHeight;

	private int mRedrawCount;
	private Paint mPaint;
	private RectF mTimerRect;
	private TimeMode mGame;

	public TimerPieView(Context context, TimeMode game) {
		super(context);
		mGame = game;
		initView();
	}

	public TimerPieView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public TimerPieView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();

	}
	
	public void setGame(TimeMode game) {
		mGame = game;
	}

	private void initView() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(5);
		mPaint.setStyle(Style.FILL);
		mPaint.setARGB(0xff, 0xff, 0xcc, 0xaa);
		mTimerRect = new RectF();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mWidth = getWidth();
		mHeight = getHeight();
		drawTimer(canvas);
		mTimerRect.set(0, 0, mWidth, mHeight);
	}
	
	private void drawTimer(Canvas canvas) {
		if (mTimeRemaining < 30) {
			mPaint.setARGB(0xff, 0xfd, 0x13, 0x12);
		} else if (mTimeRemaining < 100) {
			mPaint.setARGB(0xff, 0xf8, 0xc7, 0x12);
		} else {
			mPaint.setARGB(0xff, 0x00, 0x80, 0x00);
		}
		float sweep = 360 - mGame.getTimeRatio() * 360;
		canvas.drawArc(mTimerRect, 0, 360, true, mPaint);
		mPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		canvas.drawArc(mTimerRect, 270, sweep, true, mPaint);
		//canvas.drawArc(mTimerRect, start, 360 * mGame.getTimeRatio(), true, mPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(width, height);
	}
	
	@Override
	public void tick(int time) {
		boolean doRedraw = false;

		mTimeRemaining = time;
		if (--mRedrawCount <= 0) {
			doRedraw = true;
		}
		if (doRedraw) {
			redraw();
		}
	}
	public void redraw() {
		mRedrawCount = REDRAW_FREQ;
		invalidate();
	}

}
