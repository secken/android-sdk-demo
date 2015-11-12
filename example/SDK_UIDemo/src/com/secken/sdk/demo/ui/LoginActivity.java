package com.secken.sdk.demo.ui;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.secken.sdk.SeckenCode;
import com.secken.sdk.SeckenSDK;
import com.secken.sdk.demo.R;
import com.secken.sdk.demo.bean.ResultBean;
import com.secken.sdk.demo.http.HttpUtil;
import com.secken.sdk.entity.AuthInfo;
import com.secken.sdk.entity.ErrorInfo;
import com.secken.sdk.open.FaceVertifyActivity;
import com.secken.sdk.open.VoiceVertifyActivity;
import com.secken.sdk.toolbox.GsonTools;
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.ui.SeckenUISDK;
import com.secken.sdk.util.ToastUtils;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @Description: 登陆界面
 */
public class LoginActivity extends Activity
{

	private EditText et_username;
	private EditText et_password;
	
	/** 表示用户身份的Token，用于SeckenSDK其他接口的调用 */
	private String token;
	
	/** 第三方APP用户的唯一标示，用于确定用户的唯一性*/
	private String username;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		/** 使用前要记得初始化SDK并传入正确的APPKEY和APPID*/
		//查看本地是否有token和username存储
		token = getSharedPreferences("sdk_test", Context.MODE_PRIVATE).getString("token", "");
		username = getSharedPreferences("sdk_test", Context.MODE_PRIVATE).getString("username", "");
		
		//用于查看人脸、声音训练和验证的结果返回。
		SeckenUISDK.setHandler(handler);
		initView();
	}
	
	/**
	 * 初始化view
	 */
	private void initView()
	{
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		
		//账号密码登录
		findViewById(R.id.tv_login).setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				login();				
			}
		});
		//人脸登录
		findViewById(R.id.tv_face_login).setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Bundle bundle = new Bundle();
				bundle.putString("token", token);
				bundle.putString("username", username);
				openActivity(FaceVertifyActivity.class, bundle);
			}
		});
		//声音登录
		findViewById(R.id.tv_voice_login).setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Bundle bundle = new Bundle();
				bundle.putString("token", token);
				bundle.putString("username", username);
				openActivity(VoiceVertifyActivity.class, bundle);
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if (SeckenSDK.hasFace(this))
		{
			findViewById(R.id.tv_face_login).setVisibility(View.VISIBLE);
			findViewById(R.id.ll_username_and_pwd).setVisibility(View.GONE);
		}
		if (SeckenSDK.hasVoice(this))
		{
			findViewById(R.id.tv_voice_login).setVisibility(View.VISIBLE);
			findViewById(R.id.ll_username_and_pwd).setVisibility(View.GONE);
		}
		SeckenUISDK.setHandler(handler);
	}
	
	private ProgressDialog progressDialog;

	/**
	 * 传统的账号密码登陆
	 */
	private void login()
	{
		if (progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
		}
		progressDialog.show();
		String username = et_username.getText().toString();
		String password = et_password.getText().toString();
		final Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("password", password);
		new Thread(){
			public void run() {
				String result = HttpUtil.post("http://tpdemo.sdk.yangcong.com/user/login", map);
				if (!TextUtils.isEmpty(result))
				{
					Message msg = Message.obtain();
					msg.what = CODE_LOGIN_RESULT;
					msg.obj = result;
					handler.sendMessage(msg);
				}else {
					progressDialog.dismiss();
				}
			};
		}.start();
	}
	
	/** 登陆有结果返回*/
	private final static int CODE_LOGIN_RESULT = 100;
	
	/** 验证核对有结果返回*/
	private final static int CODE_YCLOGIN_RESULT = 101;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what)
			{
			case CODE_LOGIN_RESULT://登录返回
				String result = (String) msg.obj;
				ResultBean resultBean = (ResultBean) GsonTools.getPerson(result, ResultBean.class);
				if (resultBean != null)
				{
					if ("200".equals(resultBean.code))//登陆成功
					{
						auth();
					}
				}
				break;
			case CODE_YCLOGIN_RESULT://核对验证返回
				String result2 = (String) msg.obj;
				ResultBean resultBean2 = (ResultBean) GsonTools.getPerson(result2, ResultBean.class);
				if (resultBean2 != null)
				{
					if ("200".equals(resultBean2.code))//人脸或声音核对成功（即账号＋洋葱登录成功）
					{
						Bundle bundle = new Bundle();
						bundle.putString("token", token);
						bundle.putString("username", username);
						openActivity(HomeActivity.class, bundle);
						finish();
					}
				}
				break;
			case SeckenCode.VERTIFY_SUCCESS://验证成功（人脸、声音）
				ToastUtils.showToast(getApplicationContext(), "验证成功");
				Bundle data = msg.getData();
				/** auth_token用于开发者服务端向洋葱服务端核对验证结果的凭据 */
				String auth_token = data.getString("auth_token");
				yclogin(username, auth_token);
				break;
			default:
				break;
			}
		};
	};
	
	
	/**
	 * 用验证令牌auth_token请求服务器核对验证结果
	 */
	private void yclogin(String username,String auth_token)
	{
		if (progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
		}
		progressDialog.show();
		final Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("auth_token", auth_token);
		
		new Thread(){
			public void run() {
				String result = HttpUtil.post("http://tpdemo.sdk.yangcong.com/user/yclogin", map);
				if (!TextUtils.isEmpty(result))
				{
					Message msg = Message.obtain();
					msg.what = CODE_YCLOGIN_RESULT;
					msg.obj = result;
					handler.sendMessage(msg);
				}else {
					//核对验证失败
				}
			};
		}.start();
	}

	/**
	 * SDK授权
	 */
	private void auth()
	{
		username = et_username.getText().toString().trim();
		getSharedPreferences("sdk_test", Context.MODE_PRIVATE).edit().putString("username", username).commit();
		AuthInfo info = new AuthInfo(this, username);
		SeckenSDK.authorize(this, info, new RequestListener()
		{
			@Override
			public void onSuccess(Bundle data)
			{
				progressDialog.dismiss();
				if (data != null)
				{
					// 从 Bundle 中解析授权返回的 Token，并将解析的token存储到本地
					token = data.getString("token");
					getSharedPreferences("sdk_test", Context.MODE_PRIVATE).edit().putString("token", token).commit();
					Bundle bundle = new Bundle();
					bundle.putString("token", token);
					bundle.putString("username", username);
					openActivity(HomeActivity.class, bundle);
				}
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				progressDialog.dismiss();
				if (errorInfo != null)
				{
					ToastUtils.showToast(LoginActivity.this, errorInfo.errorMsg);
				}
			}
		});
	}
	
	protected void openActivity(Class<?> pClass, Bundle pBundle)
	{
		Intent intent = new Intent(this, pClass);
		if (pBundle != null)
		{
			intent.putExtras(pBundle);
		}
		startActivity(intent);
	}
	
}
