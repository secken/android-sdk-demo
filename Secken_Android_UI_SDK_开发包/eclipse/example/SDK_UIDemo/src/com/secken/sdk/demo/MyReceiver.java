package com.secken.sdk.demo;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.secken.sdk.SeckenSDK;
import com.secken.sdk.entity.AppBaseInfo;
import com.secken.sdk.entity.ErrorInfo;
import com.secken.sdk.net.api.SeckenInterface;
import com.secken.sdk.open.FaceVertifyActivity;
import com.secken.sdk.open.VoiceVertifyActivity;
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.ui.VertifyActivity;
import com.secken.sdk.util.AndroidUtil;
/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @deprecated 用于接收验证推送信息
 */
public class MyReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		//收到验证推送
		if (intent != null && SeckenInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction()))
		{
			pull(context, Constants.TOKEN, Constants.USERNAME);
		}
	}
	
	/**
	 * 下拉push信息
	 * @param context
	 * @param token
	 * @param username
	 */
	public void pull(final Context context,final String token,final String username)
	{
		AppBaseInfo info = new AppBaseInfo(token, username);
		SeckenSDK.pull(context, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				if (bundle != null)
				{
					/** 验证类型 "1"-一键验证 "3"-"人脸验证" "4"-"声纹验证" */
					String auth_type = bundle.getString("auth_type");
					
					/**
					 *  运行在后台则发送通知
					 *  开发者可以自己定义通知栏显示样式，关于展开式通知栏和浮动通知栏请参考
					 *  Google 官方文档链接：http://developer.android.com/design/patterns/notifications.html
					 */
					if (!AndroidUtil.isRunningForeground(context))
					{
						NotificationManager nManager = (NotificationManager) context
								.getSystemService(Context.NOTIFICATION_SERVICE);
						Notification notification = new Notification();
						long when = System.currentTimeMillis();
						notification.flags |= Notification.FLAG_AUTO_CANCEL;
						notification.icon = R.drawable.default_logo;
						notification.tickerText = "新消息通知";
						notification.when = when; 
						
						Bundle data = bundle;
						data.putString("token", token);
						data.putString("username", username);
						data.putString("longitude", "");//经度（可选）
						data.putString("latitude", "");//纬度（可选）
						Intent intent2 = new Intent(context, VertifyActivity.class);
						intent2.putExtras(data);
						intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent2, 0);
						notification.setLatestEventInfo(context, "SDK Demo", "notify", pIntent);
						nManager.notify(1, notification);
					}else {
						/** app运行在前台则跳转到验证页面 */
						if ("1".equals(auth_type) || "3".equals(auth_type) || "4".equals(auth_type))
						{
							Bundle data = bundle;
							data.putString("token", token);
							data.putString("username", username);
							data.putString("longitude", "");//经度（可选）
							data.putString("latitude", "");//纬度（可选）
							Intent intent2 = new Intent(context, VertifyActivity.class);
							intent2.putExtras(data);
							if ("3".equals(auth_type))
							{
								intent2.putExtra("className", FaceVertifyActivity.class.getName());
							}else if("4".equals(auth_type)){
								intent2.putExtra("className", VoiceVertifyActivity.class.getName());
							}
							intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							context.startActivity(intent2);
						}else{
							//暂不支持的验证类型
						}
					}
				}
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				
			}
		});
	}

}
