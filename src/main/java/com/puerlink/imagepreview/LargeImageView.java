package com.puerlink.imagepreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class LargeImageView extends ImageView {

	public interface ILargeImageViewListener {
		void onSingleClick(LargeImageView img);
	}

	private static final int COLORDRAWABLE_DIMENSION = 2;
	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

	private Bitmap mBitmap;

	private int mOffsetX = 0;
	private int mOffsetY = 0;

	private boolean mIsScaling = false;
	private float mScaleFactor = 1.0f;
	private float mScale = 1.0f;
	private float mDrawScale = 1.0f;

	private GestureDetector mTapDetector;
	private ScaleGestureDetector mScaleDetector;

	private ILargeImageViewListener mListener;

	public LargeImageView(Context context)
	{
		this(context, null);
	}

	public LargeImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.setScaleType(ScaleType.FIT_XY);

		mTapDetector = new GestureDetector(context, simpleOnGestureListener);
		mScaleDetector = new ScaleGestureDetector(context, simpleOnScaleGestureListener);
	}

	GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (!mIsScaling) {
				int width = getWidth();
				int height = getHeight();

				if (mBitmap != null && width > 0 && height > 0) {
					boolean needInvalidate = false;

					int srcWidth = getRealSrcWidth();	//(int) ((double) mBitmap.getWidth() / (double) mDrawScale);
					int srcHeight = getRealSrcHeight();	//(int) ((double) desHeight / (double) desWidth * srcWidth);

					if (srcWidth < mBitmap.getWidth()) {
						int oldOffsetX = mOffsetX;
						oldOffsetX += distanceX;
						if (oldOffsetX < 0) {
							oldOffsetX = 0;
						}
						if (oldOffsetX + srcWidth > mBitmap.getWidth()) {
							oldOffsetX = mBitmap.getWidth() - srcWidth;
						}
						if (mOffsetX != oldOffsetX) {
							mOffsetX = oldOffsetX;
							needInvalidate = true;
						}
					}

					if (srcHeight < mBitmap.getHeight()) {
						int oldOffsetY = mOffsetY;
						oldOffsetY += distanceY;
						if (oldOffsetY < 0) {
							oldOffsetY = 0;
						}
						if (oldOffsetY + srcHeight > mBitmap.getHeight()) {
							oldOffsetY = mBitmap.getHeight() - srcHeight;
						}
						if (mOffsetY != oldOffsetY) {
							mOffsetY = oldOffsetY;
							needInvalidate = true;
						}
					}

					if (needInvalidate) {
						invalidate();
					}
				}
			}

			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mListener != null) {
				mListener.onSingleClick(LargeImageView.this);
			}
			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mDrawScale != 1) {
				scale(1);
				mScale = 1;
			} else {
				scale(2);
				mScale = 2;
			}

			return super.onDoubleTap(e);
		}
	};

	ScaleGestureDetector.SimpleOnScaleGestureListener simpleOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (mIsScaling) {
				float currSpan = detector.getCurrentSpan();
				float prevSpan = detector.getPreviousSpan();
				mScaleFactor = currSpan / prevSpan;

				float currScale = mScale * mScaleFactor;
				scale(currScale);
			}

			return super.onScale(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			super.onScaleEnd(detector);
			mScale = mScale * mScaleFactor;
			if (mScale < 1) {
				mScale = 1;
			}
			scale(mScale);

			mIsScaling = false;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mIsScaling = true;
			return super.onScaleBegin(detector);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		mTapDetector.onTouchEvent(event);
		mScaleDetector.onTouchEvent(event);

		return true;
	}

	private synchronized void scale(float scale) {
		float temp = (float)((double)Math.round(scale * 100) / (double)100);
		if (mDrawScale != temp) {
			int desWidth = getWidth();
			int desHeight = getHeight();
			int srcWidth = mBitmap.getWidth();
			int srcHeight = (int)((double)srcWidth / (double)desWidth * (double)desHeight);

			if (temp > 1) {
				mOffsetX += (int)(((double)srcWidth / (double)mDrawScale - (double)srcWidth / (double)temp) / 2);
			} else {
				mOffsetX = 0;
			}

			mOffsetY += (int)(((double)srcHeight / (double)mDrawScale - (double)srcHeight / (double)temp) / 2);
			if (mOffsetY < 0) {
				mOffsetY = 0;
			}

			mDrawScale = temp;
			invalidate();
		}
	}

	private Bitmap getBitmapFromDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}

		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		try {
			Bitmap bitmap;

			if (drawable instanceof ColorDrawable) {
				bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
			} else {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
			}

			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}

	@Override
	public void setImageBitmap(Bitmap bmp) {
		mBitmap = bmp;
		super.setImageBitmap(bmp);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		mBitmap = getBitmapFromDrawable(drawable);
		super.setImageDrawable(drawable);
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		mBitmap = getBitmapFromDrawable(getDrawable());
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		mBitmap = getBitmapFromDrawable(getDrawable());
	}

	private boolean mIsDrawing = false;

	@Override
	protected void onDraw(Canvas canvas) {
		if (!mIsDrawing) {
			mIsDrawing = true;

			int width = getWidth();
			int height = getHeight();

			if (mBitmap == null || width <= 0 || height <= 0) {
				return;
			}

			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas bmpCanvas = new Canvas(bmp);

			try {
				int srcWidth = getRealSrcWidth();
				int srcHeight = getRealSrcHeight();

				int srcOffsetX = mOffsetX;
				int srcOffsetY = mOffsetY;
				if (srcOffsetY + srcHeight > mBitmap.getHeight()) {
					srcOffsetY = mBitmap.getHeight() - srcHeight;
				}

				int desWidth = getRealDesWidth();
				int desHeight = getRealDesHeight();

				int desOffsetX = Math.max(0, (width - desWidth) / 2);
				int desOffsetY = 0;
				if (desHeight < height) {
					desOffsetY = Math.max(0, (height - desHeight) / 2);
				}

				if (mDrawScale < 1 || desHeight < height) {
					Paint fillPaint = new Paint();
					fillPaint.setColor(Color.BLACK);
					bmpCanvas.drawRect(0, 0, width, height, fillPaint);
				}

				bmpCanvas.drawBitmap(mBitmap,
						new Rect(srcOffsetX, srcOffsetY, srcOffsetX + srcWidth, srcOffsetY + srcHeight),
						new Rect(desOffsetX, desOffsetY, desOffsetX + desWidth, desOffsetY + desHeight),
						null);

				canvas.drawBitmap(bmp,
						new Rect(0, 0, bmp.getWidth(), bmp.getHeight()),
						new Rect(0, 0, width, height),
						null);

				bmp.recycle();
			}
			catch (Exception exp) {
			}
			finally {
				mIsDrawing = false;
			}
		}
	}

	public void setLargeImageViewListener(ILargeImageViewListener listener) {
		mListener = listener;
	}

	public int getRealSrcWidth() {
		if (mBitmap != null) {
			int srcWidth = mBitmap.getWidth();
			if (mDrawScale > 1) {
				return (int)((double)srcWidth / (double)mDrawScale);
			}
			return srcWidth;
		}
		return 0;
	}

	public int getRealSrcHeight() {
		int srcWidth = getRealSrcWidth();
		if (srcWidth > 0) {
			int desWidth = getWidth();
			if (mDrawScale < 1) {
				desWidth = (int)((double)desWidth * (double)mDrawScale);
			}
			int desHeight = getHeight();

			int srcHeight = (int)((double)srcWidth / (double)desWidth * (double)desHeight);
			if (srcHeight > mBitmap.getHeight()) {
				srcHeight = mBitmap.getHeight();
			}
			return srcHeight;
		}
		return 0;
	}

	public int getRealDesWidth() {
		int desWidth = getWidth();

		if (mDrawScale < 1) {
			return (int)((double)desWidth * (double)mDrawScale);
		}

		return desWidth;
	}

	public int getRealDesHeight() {
		int srcWidth = getRealSrcWidth();
		int srcHeight = getRealSrcHeight();
		int desWidth = getRealDesWidth();

		int desHeight = (int)((double)srcHeight / (double)srcWidth * (double)desWidth);
		if (desHeight > getHeight()) {
			desHeight = getHeight();
		}

		return desHeight;
	}

	public boolean canScrollToLeft()
	{
		if (mBitmap != null) {
			return mOffsetX + getRealSrcWidth() < mBitmap.getWidth();
		}
		return false;
	}

	public boolean canScrollToRight()
	{
		return mOffsetX > 0;
	}

	public void reset() {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				scale(1);
				mScale = 1;
			}
		});
	}

}