package com.puerlink.imagepreview.entity;

import java.io.Serializable;

public class ImageItem implements Serializable {

	private String mImageUri;
	private String mDesc;


	public ImageItem(String uri, String desc)
	{
		mImageUri = uri;
		mDesc = desc;
	}

	public String getImageUri()
	{
		return mImageUri;
	}

	public void setImageUri(String uri)
	{
		mImageUri = uri;
	}

	public String getDescription()
	{
		return mDesc;
	}

	public void setDescription(String desc)
	{
		mDesc = desc;
	}

}
