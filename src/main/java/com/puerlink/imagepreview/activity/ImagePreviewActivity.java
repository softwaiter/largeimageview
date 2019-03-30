package com.puerlink.imagepreview.activity;

import com.puerlink.common.DeviceUtils;
import com.puerlink.common.DisplayUtils;
import com.puerlink.common.NetUtils;
import com.puerlink.common.StringUtils;
import com.puerlink.common.http.HttpUtils;
import com.puerlink.imagepreview.entity.ImageItem;
import com.puerlink.imagepreview.widgets.ContextMenu;
import com.puerlink.imagepreview.widgets.PreviewImageViewPager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.puerlink.imagepreview.R;
import com.puerlink.widgets.MessageDialog;
import com.puerlink.widgets.ToastShow;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends Activity {

	public interface OnCommentClickListener
	{
		boolean commentClick(long commentOwnerId, int commentOwnerType);
	}

	private static List<OnCommentClickListener> sCommentClickListeners = new ArrayList<OnCommentClickListener>();

	public static void addCommentClickListener(OnCommentClickListener listener)
	{
		if (listener != null)
		{
			sCommentClickListeners.add(listener);
		}
	}

	public static void removeCommentClickListener(OnCommentClickListener listener)
	{
		if (listener != null) {
			for (int i = 0; i < sCommentClickListeners.size(); i++) {
				OnCommentClickListener item = sCommentClickListeners.get(i);
				if (item.equals(listener)) {
					sCommentClickListeners.remove(i);
					break;
				}
			}
		}
	}

	public static void clearCommentClickListeners()
	{
		sCommentClickListeners.clear();
	}

	public static void commentClick(long commentOwnerId, int commentOwnerType)
	{
		for (int i = 0; i < sCommentClickListeners.size(); i++) {
			OnCommentClickListener item = sCommentClickListeners.get(i);
			if (item.commentClick(commentOwnerId, commentOwnerType))
			{
				break;
			}
		}
	}

	static class AutoHideTitleAndFooterTimer extends CountDownTimer
	{
		private WeakReference<ImagePreviewActivity> mActivity;

		public AutoHideTitleAndFooterTimer(ImagePreviewActivity activity) {
			super(1000 * 2, 500);
			mActivity = new WeakReference<ImagePreviewActivity>(activity);
		}

		private ImagePreviewActivity getActivity()
		{
			if (mActivity != null)
			{
				return mActivity.get();
			}
			return null;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			ImagePreviewActivity activity = getActivity();
			if (activity != null && !activity.isFinishing())
			{
				if (activity.mTitleAndFooterChangedAlready) {
					super.cancel();
				}
			}
			else
			{
				super.cancel();
			}
		}

		@Override
		public void onFinish() {
			ImagePreviewActivity activity = getActivity();
			if (activity != null && !activity.isFinishing() &&
					!activity.mTitleAndFooterChangedAlready)
			{
				activity.hideTitleAndFooter();
			}
		}
	}

	private RelativeLayout mTitleBar;
	private TextView mPageTitle;
	private PreviewImageViewPager mViewPager;
	private LinearLayout mFooterBar;
	private TextView mImageDesc;
	private LinearLayout mIndicatorContainer;
	private RelativeLayout mCommentButton;
	private TextView mCommentText;

	private List<ImageItem> mItems = new ArrayList<ImageItem>();

	private int mCurrIndex = 0;
	//小圆点图片数组
	private ImageView[] mIndicatorViews;

	private String mTitle = "";
	private boolean mShowDesc = true;
	private boolean mShowPageDot = true;
	private boolean mShowComment = false;
	private int mCommentNum = 0;
	private long mCommentOwnerId = 0;
	private int mCommentOwnerType = 0;

	private boolean mTitleAndFooterChangedAlready = false;

	private ContextMenu mMenu;

	private Context mContext;
	private boolean mIsNight = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_imagepreview);

		mContext = this;

		mTitleBar = findViewById(R.id.relative_title_bar);
		mPageTitle = findViewById(R.id.text_title_page);

		mViewPager = findViewById(R.id.pager_images);
		mViewPager.setOnHorzViewPagerListener(onPageChangeListener);
		mViewPager.setOnContentClickListener(onContentSingleClickListener);

		mFooterBar = findViewById(R.id.linear_footer);
		mImageDesc = findViewById(R.id.text_desc);
		mIndicatorContainer = findViewById(R.id.linear_indicator);

		mCommentButton = findViewById(R.id.relative_comment);
		mCommentButton.setOnClickListener(onCommentButtonClickListener);
		mCommentText = findViewById(R.id.text_comment_num);

		LinearLayout linearBack = findViewById(R.id.linear_back_button);
		linearBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		LinearLayout mMenuButton = findViewById(R.id.linear_menu_button);
		mMenuButton.setOnClickListener(onMenuButtonClickListener);

		Intent intent = getIntent();
		if (intent != null)
		{
			if (intent.hasExtra("images")) {
				mItems = (List<ImageItem>) intent.getSerializableExtra("images");
				if (mItems != null && mItems.size() > 0) {
					for (int i = 0; i < mItems.size(); i++) {
						mViewPager.addView(mItems.get(i));
					}
				}
			}

			if (intent.hasExtra("night"))
			{
				mIsNight = intent.getBooleanExtra("night", false);
			}
		}

		if (intent != null && intent.hasExtra("curr_index"))
		{
			mCurrIndex = intent.getIntExtra("curr_index", 0);
		}
		if (mCurrIndex >= 0 && mCurrIndex < mItems.size())
		{
			ImageItem ii = mItems.get(mCurrIndex);
			if (ii != null)
			{
				mImageDesc.setText(ii.getDescription());
			}
		}

		if (intent != null && intent.hasExtra("show_desc"))
		{
			mShowDesc = intent.getBooleanExtra("show_desc", true);
		}
		if (!mShowDesc)
		{
			findViewById(R.id.linear_desc).setVisibility(View.GONE);
		}

		if (intent != null && intent.hasExtra("show_dot"))
		{
			mShowPageDot = intent.getBooleanExtra("show_dot", true);
		}
		if (!mShowPageDot)
		{
			mIndicatorContainer.setVisibility(View.GONE);
		}
		else
		{
			initIndicatorViews(mItems.size(), mCurrIndex);
		}

		if (intent != null && intent.hasExtra("title"))
		{
			mTitle = intent.getStringExtra("title");
		}
		if (TextUtils.isEmpty(mTitle)) {
			mPageTitle.setText((mCurrIndex + 1) +
					getString(R.string.imagepreview_split_symbol) +
					mItems.size());
		}
		else
		{
			mPageTitle.setText(mTitle);
		}

		if (intent != null && intent.hasExtra("showComment"))
		{
			mShowComment = intent.getBooleanExtra("showComment", false);
		}

		if (intent != null && intent.hasExtra("commentNum"))
		{
			mCommentNum = intent.getIntExtra("commentNum", 0);
		}

		if (intent != null && intent.hasExtra("commentOwnerId"))
		{
			mCommentOwnerId = intent.getLongExtra("commentOwnerId", 0);
		}

		if (intent != null && intent.hasExtra("commentOwnerType"))
		{
			mCommentOwnerType = intent.getIntExtra("commentOwnerType", 0);
		}

		if (mShowComment)
		{
			if (mCommentNum > 0) {
				mCommentText.setText(mCommentNum + "");
				mCommentText.setVisibility(View.VISIBLE);
			} else {
				mCommentText.setVisibility(View.INVISIBLE);
			}

			mCommentButton.setVisibility(View.VISIBLE);
		}
		else
		{
			mCommentButton.setVisibility(View.GONE);
		}

		boolean autoHideBar = false;
		if (intent != null && intent.hasExtra("auto_hide"))
		{
			autoHideBar = intent.getBooleanExtra("auto_hide", false);
		}
		if (autoHideBar) {
			new AutoHideTitleAndFooterTimer(this).start();
		}
	}

	private void hideTitleBar()
	{
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -1f);
		anim.setDuration(200);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setFillAfter(true);

		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mTitleBar.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		mTitleBar.startAnimation(anim);
	}

	private void showTitleBar()
	{
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -1f,
				Animation.RELATIVE_TO_SELF, 0f);
		anim.setDuration(200);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setFillAfter(true);

		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mTitleBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		mTitleBar.startAnimation(anim);
	}

	private void hideFooter()
	{
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 1f);
		anim.setDuration(200);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setFillAfter(true);

		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mShowComment) {
					int size = DisplayUtils.dp2px(mContext, 50);
					int margin = DisplayUtils.dp2px(mContext, 8);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
					lp.bottomMargin = margin;
					lp.rightMargin = margin;
					lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					mCommentButton.setLayoutParams(lp);
				}

				mFooterBar.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		mFooterBar.startAnimation(anim);
	}

	private void showFooter()
	{
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 1f,
				Animation.RELATIVE_TO_SELF, 0f);
		anim.setDuration(200);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setFillAfter(true);

		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mFooterBar.setVisibility(View.VISIBLE);

				if (mShowComment) {
					int size = DisplayUtils.dp2px(mContext, 50);
					int margin = DisplayUtils.dp2px(mContext, 8);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
					lp.bottomMargin = margin;
					lp.rightMargin = margin;
					lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					lp.addRule(RelativeLayout.ABOVE, R.id.linear_footer);
					mCommentButton.setLayoutParams(lp);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		mFooterBar.startAnimation(anim);
	}

	private void hideTitleAndFooter()
	{
		hideTitleBar();
		if (mShowDesc || mShowPageDot)
		{
			hideFooter();
		}
	}

	PreviewImageViewPager.OnContentClickListener onContentSingleClickListener = new PreviewImageViewPager.OnContentClickListener() {
		@Override
		public void onClick() {
			mTitleAndFooterChangedAlready = true;

			if (mTitleBar.getVisibility() == View.GONE)
			{
				showTitleBar();
				if (mShowDesc || mShowPageDot)
				{
					showFooter();
				}
			}
			else
			{
				hideTitleAndFooter();
			}
		}
	};

	View.OnClickListener onMenuButtonClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view) {
			mTitleAndFooterChangedAlready = true;

			if (mMenu == null)
			{
				mMenu = new ContextMenu(view.getContext());
				mMenu.setOnMenuItemClickListener(onMenuItemClickListener);
				mMenu.addItem(R.string.imagepreview_save_menu_item);
			}

			mMenu.show(view, 0, 0);
		}
	};

	ContextMenu.OnMenuItemClickListener onMenuItemClickListener = new ContextMenu.OnMenuItemClickListener() {
		@Override
		public void onMenuItemClickListener(int position, String label) {
			mMenu.close();

			String saveMenu = getString(R.string.imagepreview_save_menu_item);
			if (TextUtils.equals(saveMenu, label))	//保存到手机
			{
				if (mItems != null && mItems.size() > 0)
				{
					ImageItem item = mItems.get(mCurrIndex);
					final String filePath = DeviceUtils.getWritePath("/pictures_download/") +
							StringUtils.md5(item.getImageUri()) + ".jpeg";
					File f = new File(filePath);
					if (f.exists())
					{
						String msg = getString(R.string.hint_image_saved);

						MessageDialog.Builder builder = new MessageDialog.Builder(mContext);
						builder.setCaption(R.string.app_name)
								.setNightMode(mIsNight)
								.setMessage(String.format(msg, filePath))
								.hideCancelButton()
								.build().show();
					}
					else
					{
						HttpUtils.download(getApplicationContext(), item.getImageUri(), filePath,
								new HttpUtils.HttpDownloadCallback() {
									@Override
									public void onStart() {

									}

									@Override
									public void onProgress(long total, long current) {

									}

									@Override
									public void onSucceeded(File file) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												ToastShow.centerShort(mContext, R.string.hint_save_succ);
											}
										});

										Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
										Uri uri = Uri.fromFile(new File(filePath));
										intent.setData(uri);
										sendBroadcast(intent);
									}

									@Override
									public void onFailed(String code, String message) {

									}

									@Override
									public void onNetworkDisconnected(NetUtils.NetState state) {

									}
								});
					}
				}
			}
		}
	};

	private void initIndicatorViews(int count, int currIndex)
	{
		mIndicatorViews = new ImageView[count];
		for (int i = 0; i < count; i++)
		{
			ImageView image = new ImageView(this);
			image.setScaleType(ImageView.ScaleType.FIT_CENTER);
			image.setLayoutParams(new ViewGroup.LayoutParams(28, 14));

			if (i == currIndex)
			{
				image.setImageResource(R.drawable.imagepreview_icon_dot_shining);
			}
			else
			{
				image.setImageResource(R.drawable.imagepreview_icon_dot_normal);
			}

			mIndicatorViews[i] = image;
			mIndicatorContainer.addView(image);
		}
	}

	PreviewImageViewPager.OnHorzViewPagerListener onPageChangeListener = new PreviewImageViewPager.OnHorzViewPagerListener() {
		@Override
		public void onPageMoveStart() {
		}

		@Override
		public void onPageMoving(float fraction) {
		}

		@Override
		public void onPageMoveCanceled() {
		}

		@Override
		public void onPageChangedListener(int pageIndex) {
			if (mCurrIndex != pageIndex)
			{
				if (mCurrIndex >= 0 && mCurrIndex < mIndicatorViews.length)
				{
					mIndicatorViews[mCurrIndex].setImageResource(R.drawable.imagepreview_icon_dot_normal);
				}
				if (pageIndex >= 0 && pageIndex < mIndicatorViews.length)
				{
					mIndicatorViews[pageIndex].setImageResource(R.drawable.imagepreview_icon_dot_shining);
				}

				if (pageIndex >= 0 && pageIndex < mItems.size())
				{
					ImageItem item = mItems.get(pageIndex);
					if (item != null)
					{
						mImageDesc.setText(item.getDescription());
					}
					else
					{
						mImageDesc.setText("");
					}
				}
				else
				{
					mImageDesc.setText("");
				}

				mCurrIndex = pageIndex;

				if (TextUtils.isEmpty(mTitle)) {
					mPageTitle.setText((mCurrIndex + 1) +
							getString(R.string.imagepreview_split_symbol) +
							mItems.size());
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	View.OnClickListener onCommentButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			commentClick(mCommentOwnerId, mCommentOwnerType);
		}
	};

}
