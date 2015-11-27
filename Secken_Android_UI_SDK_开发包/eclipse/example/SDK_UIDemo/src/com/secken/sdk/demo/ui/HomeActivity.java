package com.secken.sdk.demo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import com.secken.sdk.SeckenCode;
import com.secken.sdk.demo.R;
import com.secken.sdk.open.FaceTrainActivity;
import com.secken.sdk.open.VoiceTrainActivity;
import com.secken.sdk.ui.SeckenUISDK;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @Description: 用户功能界面
 */
public class HomeActivity extends Activity
{
	private String token;
	private String username;
	private Handler handler;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		token = getIntent().getStringExtra("token");
		username = getIntent().getStringExtra("username");
		// 声音训练
		findViewById(R.id.voice_train).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(HomeActivity.this, VoiceTrainActivity.class);
				intent.putExtra("token", token);
				intent.putExtra("username", username);
				startActivity(intent);
			}
		});
		// 人脸训练
		findViewById(R.id.face_train).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(HomeActivity.this, FaceTrainActivity.class);
				intent.putExtra("token", token);
				intent.putExtra("username", username);
				startActivity(intent);
			}
		});
		// 退出
		findViewById(R.id.logout).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		handler = new Handler()
		{
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
				case SeckenCode.FACE_TRAIN_SUCCESS:// 训练人脸成功
					finish();
					break;
				case SeckenCode.VOICE_TRAIN_SUCCESS:// 训练声音成功
					finish();
					break;
				default:
					break;
				}
			};
		};
		SeckenUISDK.setHandler(handler);
	};

}
