package com.secken.sdk.demo.ui;

import com.secken.sdk.demo.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
public class MainActivity extends Activity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.tv_sdk_demo).setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(MainActivity.this, AuthorizationActivity.class));
			}
		});
		
		findViewById(R.id.tv_sdk_use).setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
			}
		});
		
	}

}
