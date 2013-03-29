package com.tb.flagspotting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TimerBarView extends View implements Synchronizer.Event {
	private static final String TAG = "TimerBarView";
	static int PADDING;
	public static final int REDRAW_FREQ = 10;
	private int mTimeRemaining;
	private int mWidth;
	private int mHeight;

	private int mRedrawCount;
	private Paint mPaint;
	private FlagsGame mGame;

	public TimerBarView(Context context, FlagsGame game) {
		super(context);
		mGame = game;
		initView();
	}

	public TimerBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public TimerBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();

	}
	
	public void setGame(FlagsGame game) {
		mGame = game;
	}

	private void initView() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(0);
		mPaint.setARGB(0xff, 0xff, 0xcc, 0xaa);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mWidth = getWidth();
		mHeight = getHeight();
		drawTimer(canvas);
	}
	
	private void drawTimer(Canvas canvas) {
		if (mTimeRemaining < 30) {
			mPaint.setARGB(0xff, 0xfd, 0x13, 0x12);
		} else if (mTimeRemaining < 100) {
			mPaint.setARGB(0xff, 0xf8, 0xc7, 0x12);
		} else {
			mPaint.setARGB(0xff, 0x00, 0x80, 0x00);
		}

		mPaint.setStrokeWidth(40);
		canvas.drawLine(mWidth - 40, mHeight, mWidth - 40, mHeight - (mHeight * mTimeRemaining
				/ mGame.getMaxTimeRemaining()), mPaint);

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
