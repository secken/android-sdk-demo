package com.secken.sdk.demo;

import android.app.Application;

import com.secken.sdk.ui.SeckenUISDK;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 */
public class MyApplication extends Application
{

	/** 鉴于app安全考虑开发者的appkey和app_id尽量不要暴露出去 */

	/** 开发者申请应用的appkey */
	private String appkey = "";

	/** 开发者申请应用的app_id */
	private String app_id = "";

	@Override
	public void onCreate()
	{
		super.onCreate();
		// 初始化SDK
		SeckenUISDK.init(this, appkey, app_id);

	}
}
