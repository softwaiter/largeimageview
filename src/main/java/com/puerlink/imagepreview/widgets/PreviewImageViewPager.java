package com.puerlink.imagepreview.widgets;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.puerlink.imagepreview.LargeImageView;
import com.puerlink.imagepreview.R;
import com.puerlink.imagepreview.entity.ImageItem;
import com.puerlink.imagepreview.utils.ImageUtils;
import com.puerlink.widgets.CircleProgressBar;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PreviewImageViewPager extends ViewPager {

	public interface OnHorzViewPagerListener
	{
		void onPageMoveStart();
		void onPageMoving(float fraction);
		void onPageMoveCanceled();
		void onPageChangedListener(int pageIndex);
	}

	public interface OnContentClickListener
	{
		void onClick();
	}

	static class ViewPagerAdapter extends PagerAdapter
	{
		private WeakReference<PreviewImageViewPager> mViewPager;

		private List<ImageItem> mViews = new ArrayList<ImageItem>();

		private Hashtable<Integer, View> mPages = new Hashtable<Integer, View>();

		public ViewPagerAdapter(PreviewImageViewPager viewPager, List<ImageItem> views)
		{
			mViewPager = new WeakReference<PreviewImageViewPager>(viewPager);
			mViews = views;
		}

		@Override
		public int getCount() {
			return mViews.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeViewAt(position);
		}

		private Context getViewPagerContext()
		{
			if (mViewPager != null)
			{
				PreviewImageViewPager viewPager = mViewPager.get();
				if (viewPager != null)
				{
					return viewPager.getContext();
				}
			}
			return null;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			if (mPages.containsKey(position)) {
				view = mPages.get(position);
			}
			else
			{
				Context context = getViewPagerContext();
				if (context != null) {
					view = LayoutInflater.from(context).inflate(R.layout.view_page_item, null, false);

					LargeImageView scaleImage = view.findViewById(R.id.image_scale_view);
					scaleImage.setLargeImageViewListener(previewImageListener);
					scaleImage.setVisibility(View.GONE);

					CircleProgressBar progressBar = view.findViewById(R.id.progressbar_load);
					progressBar.setCricleColor(Color.rgb(51, 51, 51));
					progressBar.setProgressColor(Color.rgb(128, 128, 128));
					progressBar.setTextColor(Color.rgb(128, 128, 128));
					progressBar.setMax(100);
					progressBar.setProgress(1);
					progressBar.setVisibility(View.VISIBLE);
					ImageView imageFailed = view.findViewById(R.id.image_load_failed);
					imageFailed.setVisibility(View.GONE);

					container.addView(view);

					ImageItem item = mViews.get(position);
					ImageUtils.displayImageWithProgressBar(item.getImageUri(),
							scaleImage, scaleImage, progressBar, imageFailed);

					mPages.put(position, view);
				}
			}
			return view;
		}

		LargeImageView.ILargeImageViewListener previewImageListener = new LargeImageView.ILargeImageViewListener() {
			@Override
			public void onSingleClick(LargeImageView img) {
				PreviewImageViewPager viewPager = mViewPager.get();
				if (viewPager != null)
				{
					viewPager.performContentClick();
				}
			}
		};

		public void resetImageScale(int position)
		{
			if (position >= 0 && position < mViews.size())
			{
				View view = mPages.get(position);
				if (view != null)
				{
					LargeImageView scaleImage = view.findViewById(R.id.image_scale_view);
					if (scaleImage != null) {
						scaleImage.reset();
					}
				}
			}
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

	};

	private ViewPagerAdapter mAdapter;
	private boolean mInited = false;

	private List<ImageItem> mViews = new ArrayList<ImageItem>();
	private int mCurrentIndex = -1;

	private OnHorzViewPagerListener mPageChangedListener;
	private OnContentClickListener mContentClickListener;

	private boolean mIsStart, mIsScrolling;

	public PreviewImageViewPager(Context context) {
		super(context, null);
	}

	public PreviewImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);

		setFocusable(false);
		setFocusableInTouchMode(false);
		setOnPageChangeListener(ViewPagerScrollListener);
	}

	private void init()
	{
		if (mViews.size() > 0)
		{
			initSelectState();

			mAdapter = new ViewPagerAdapter(this, mViews);

			setOffscreenPageLimit(mViews.size());
			setAdapter(mAdapter);
			setCurrentItem(mCurrentIndex, false);

			mInited = true;
		}
		else
		{
			Handler h = new Handler();
			h.postDelayed(new Runnable() {

				@Override
				public void run() {
					init();
				}

			}, 300);
		}
	}

	private void initSelectState()
	{
		if (mCurrentIndex == -1 && mViews.size() > 0)
		{
			mCurrentIndex = 0;
		}
	}

	private void performContentClick()
	{
		if (mContentClickListener != null)
		{
			mContentClickListener.onClick();
		}
	}

	public void setOnHorzViewPagerListener(OnHorzViewPagerListener listener)
	{
		mPageChangedListener = listener;
	}

	public void setOnContentClickListener(OnContentClickListener listener)
	{
		mContentClickListener = listener;
	}

	public int getPageCount()
	{
		return mViews.size();
	}

	public void addView(ImageItem item)
	{
		mViews.add(item);
	}

	public int getSelectedIndex()
	{
		return mCurrentIndex;
	}

	public void setSelectedIndex(int index)
	{
		if (mCurrentIndex != index)
		{
			setCurrentItem(index, false);
		}
	}

	OnPageChangeListener ViewPagerScrollListener = new OnPageChangeListener ()
	{
		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == 1)
			{
				if (!mIsStart)
				{
					mIsStart = true;
					mIsScrolling = false;

					if (mPageChangedListener != null)
					{
						mPageChangedListener.onPageMoveStart();
					}
				}
			}
			else if (state == 2)
			{
				mIsStart = false;
			}
		}

		@Override
		public void onPageScrolled(int pageIndex, float fraction, int position) {
			if (mIsStart)
			{
				if (mIsScrolling)
				{
					if (mPageChangedListener != null)
					{
						float f = fraction;
						if (pageIndex < mCurrentIndex)
						{
							f = f - 1f;
						}

						mPageChangedListener.onPageMoving(f);
					}
				}
				else
				{
					mIsScrolling = true;
				}
			}
			else
			{
				if (mIsScrolling)
				{
					mIsScrolling = false;

					if (mPageChangedListener != null)
					{
						mPageChangedListener.onPageMoveCanceled();
					}
				}
			}
		}

		@Override
		public void onPageSelected(int index) {
			final int oldIndex = mCurrentIndex;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						mAdapter.resetImageScale(oldIndex);
					}
					catch (Exception exp)
					{
					}
				}
			}, 100);

			mIsStart = false;
			mIsScrolling = false;
			mCurrentIndex = index;

			if (mPageChangedListener != null)
			{
				mPageChangedListener.onPageChangedListener(index);
			}
		}
	};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (!mInited)
		{
			init();
		}
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof LargeImageView) {
			if (dx > 0) {     // 从左侧往右侧拖动
				return ((LargeImageView)v).canScrollToRight();
			} else if (dx < 0) {	//  从右侧往左侧拖动
				return ((LargeImageView)v).canScrollToLeft();
			}
		}
		return super.canScroll(v, checkV, dx, x, y);
	}

}
