package com.puerlink.imagepreview.utils;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.puerlink.imagepreview.R;
import com.puerlink.widgets.CircleProgressBar;

public class ImageUtils {

	static DisplayImageOptions S_DISPLAY_OPTIONS;

	static
	{
		S_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.NONE)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.delayBeforeLoading(0)
				.build();
	}

	public static void panuse()
	{
		ImageLoader.getInstance().pause();
	}

	public static void resume()
	{
		ImageLoader.getInstance().resume();
	}

	public static void stop()
	{
		ImageLoader.getInstance().stop();
	}

	public static void clearMemoryCache()
	{
		ImageLoader.getInstance().clearMemoryCache();
	}

	public static boolean isNeedReload(String url, View view)
	{
		String currUrl = "";
		Object tagUrl = view.getTag(R.string.imagepreview_tag_image_url);
		if (tagUrl != null)
		{
			currUrl = tagUrl.toString();
		}

		String currState = "-1";	//0：开始，1：完成，-1：失败
		Object tagState = view.getTag(R.string.imagepreview_tag_view_state);
		if (tagState != null)
		{
			currState = tagState.toString();
		}

		if (!currUrl.equalsIgnoreCase(url) || "-1".equals(currState))
		{
			return true;
		}

		return false;
	}

	public static void displayImageWithProgressBar(String url,
												   final View contentArea,
												   ImageView view,
												   final CircleProgressBar progressBar,
												   final View failedHint)
	{
		if (isNeedReload(url, view))
		{
			view.setTag(R.string.imagepreview_tag_view_state, "0");
			view.setTag(R.string.imagepreview_tag_image_url, url);

			ImageLoader.getInstance().displayImage(url, view, S_DISPLAY_OPTIONS,
					new ImageLoadingListener() {

						@Override
						public void onLoadingCancelled(String url, View view) {
							view.setTag(R.string.imagepreview_tag_view_state, "-1");
						}

						@Override
						public void onLoadingComplete(String url, View view, Bitmap bmp) {
							view.setTag(R.string.imagepreview_tag_view_state, "0");

							if (contentArea != null) {
								contentArea.setVisibility(View.VISIBLE);
							}
							if (progressBar != null) {
								progressBar.setVisibility(View.GONE);
							}
						}

						@Override
						public void onLoadingFailed(String url, View view, FailReason reason) {
							view.setTag(R.string.imagepreview_tag_view_state, "-1");

							if (progressBar != null) {
								progressBar.setVisibility(View.GONE);
							}
							if (failedHint != null) {
								failedHint.setVisibility(View.VISIBLE);
							}
						}

						@Override
						public void onLoadingStarted(String url, View view) {
							if (progressBar != null) {
								progressBar.setVisibility(View.VISIBLE);
							}
							if (failedHint != null) {
								failedHint.setVisibility(View.GONE);
							}
						}

					},
					new ImageLoadingProgressListener() {

						@Override
						public void onProgressUpdate(String url, View view, int current, int total) {
							if (progressBar != null) {
								progressBar.setMax(total);
								int currProgress = Math.max(current, total / 99);
								progressBar.setProgress(currProgress);
							}
						}

					});
		}
	}

}
