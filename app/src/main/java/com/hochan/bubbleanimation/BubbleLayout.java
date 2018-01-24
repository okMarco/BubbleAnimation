package com.hochan.bubbleanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import static android.graphics.PixelFormat.TRANSLUCENT;

public class BubbleLayout extends FrameLayout {

	private int[] COLOR_ARRAY = new int[]{R.color.colorOrange, R.color.colorBlue, R.color.colorGreen, R.color.colorRed, R.color.colorPuple};
	private static final int MAX_VIEW_SIZE = 40;
	private static final int MIN_VIEW_SIZE = 15;

	private Handler mHandler;
	private Runnable mRunnable;
	private boolean mTouching;
	private SpringSystem mSpringSystem;
	private SpringConfig mCoasting;
	private SpringConfig mGravity;
	private SpringConfig mScaleConfig;

	float mLeftMargin;
	float mTopMargin;
	private double mViewSize;


	public BubbleLayout(@NonNull Context context) {
		this(context, null);
	}

	public BubbleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BubbleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initSpring();
	}

	private void initSpring() {
		mSpringSystem = SpringSystem.create();
		mHandler = new Handler();
		mRunnable = new CircleSpawn();
		mCoasting = SpringConfig.fromOrigamiTensionAndFriction(0, 0);
		mCoasting.tension = 0;
		mGravity = SpringConfig.fromOrigamiTensionAndFriction(0, 0);
		mGravity.tension = 1;
		mScaleConfig = SpringConfig.fromOrigamiTensionAndFriction(0, 0);
		mScaleConfig.tension = 1;
	}


	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mTouching = true;
				mLeftMargin = motionEvent.getX();
				mTopMargin = motionEvent.getY();
				mHandler.post(mRunnable);
				break;
			case MotionEvent.ACTION_UP:
				mTouching = false;
				break;
			case MotionEvent.ACTION_MOVE:
				mLeftMargin = motionEvent.getX();
				mTopMargin = motionEvent.getY();
				break;
		}
		return true;
	}

	private class CircleSpawn implements Runnable {

		@Override
		public void run() {
			if (mTouching) {
				createView();
				mHandler.postDelayed(this, 0);
			}
		}
	}

	private void createView() {
		final ImageView view = new ImageView(getContext());

		int sizeInDip = (int) (Math.random() * (MAX_VIEW_SIZE - MIN_VIEW_SIZE) + MIN_VIEW_SIZE);
		mViewSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDip, getResources().getDisplayMetrics());

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) mViewSize, (int) mViewSize);
		params.topMargin = (int) (mTopMargin - mViewSize / 2);
		params.leftMargin = (int) (mLeftMargin - mViewSize / 2);
		view.setLayoutParams(params);
		int color = ContextCompat.getColor(getContext(), COLOR_ARRAY[Math.round((float) Math.random() * (COLOR_ARRAY.length - 1))]);
		view.setImageDrawable(new BubbleDrawable(color));
		addView(view);
		setViewAnimation(view);
	}

	private void setViewAnimation(final View view) {
		view.setPivotX((float) (mViewSize / 2));
		view.setPivotY((float) (mViewSize / 2));

		final Spring ySpring = mSpringSystem.createSpring().setSpringConfig(mGravity);
		final Spring xSpring = mSpringSystem.createSpring().setSpringConfig(mCoasting);
		final Spring scaleSpring = mSpringSystem.createSpring().setSpringConfig(mScaleConfig);

		double magnitude = 2000;
		double angle = Math.random() * Math.PI;
		final double y = -magnitude * Math.sin(angle);
		final double x = magnitude / 10 * Math.cos(angle);
		xSpring.setVelocity(x);
		ySpring.setVelocity(y);
		scaleSpring.setVelocity(10);

		xSpring.addListener(new SimpleSpringListener() {
			@Override
			public void onSpringUpdate(Spring spring) {
				view.setTranslationX((float) spring.getCurrentValue());
			}
		});
		ySpring.addListener(new SimpleSpringListener() {
			@Override
			public void onSpringUpdate(Spring spring) {
				view.setTranslationY((float) spring.getCurrentValue());
				if (spring.getVelocity() > 0) {
					view.setAlpha(1 - (float) (spring.getVelocity() / Math.abs(y)));
				}
				if (spring.getVelocity() > Math.abs(y)) {
					removeView(view);
					ySpring.destroy();
					xSpring.destroy();
					scaleSpring.destroy();
				}
			}
		});

		scaleSpring.addListener(new SimpleSpringListener() {
			@Override
			public void onSpringUpdate(Spring spring) {
				view.setScaleX((float) spring.getCurrentValue());
				view.setScaleY((float) spring.getCurrentValue());
			}
		});

		xSpring.setEndValue(getMeasuredWidth() - view.getRight());
		ySpring.setEndValue(getMeasuredHeight());
		scaleSpring.setCurrentValue(1);
		scaleSpring.setEndValue(Math.random() * 2 + 1);
	}

	public static class BubbleDrawable extends Drawable {

		private Paint mPaint;

		BubbleDrawable(int color) {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(color);
			mPaint.setStyle(Paint.Style.FILL);
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			super.setBounds(left, top, right, bottom);
		}

		@Override
		public void draw(@NonNull Canvas canvas) {
			mPaint.setStrokeWidth(canvas.getWidth() / 16);
			float cx = canvas.getWidth() / 2;
			float cy = canvas.getHeight() / 2;
			float radius = canvas.getWidth() / 2;
			canvas.drawCircle(cx, cy, radius - mPaint.getStrokeWidth() / 2, mPaint);
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(@Nullable ColorFilter colorFilter) {

		}

		@Override
		public int getOpacity() {
			return TRANSLUCENT;
		}
	}
}
