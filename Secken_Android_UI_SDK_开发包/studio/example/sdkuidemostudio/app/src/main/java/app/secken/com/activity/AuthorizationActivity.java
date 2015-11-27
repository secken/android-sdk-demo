package app.secken.com.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.secken.sdk.SeckenSDK;
import com.secken.sdk.entity.AuthInfo;
import com.secken.sdk.entity.ErrorInfo;
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.ui.SeckenUISDK;
import com.secken.sdk.util.ToastUtils;

import app.secken.com.config.Constants;
import app.secken.com.sdkdemo.R;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @deprecated 授权页面
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AuthorizationActivity extends Activity implements TextWatcher, OnClickListener
{

	/** appkey,app_id, usernames信息 */
	private EditText et_appkey;
	private EditText et_appid;
	private EditText et_username;

	private TextView tv_auth;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		initView();
	}

	/**
	 * 初始化view
	 */
	public void initView()
	{
		tv_auth = (TextView) findViewById(R.id.tv_auth);
		tv_auth.setOnClickListener(this);
		tv_auth.setEnabled(false);

		et_appkey = (EditText) findViewById(R.id.et_appkey);
		et_appid = (EditText) findViewById(R.id.et_appid);
		et_username = (EditText) findViewById(R.id.et_username);
		et_appkey.addTextChangedListener(this);
		et_appid.addTextChangedListener(this);
		et_username.addTextChangedListener(this);
		
		// 此处可以设置开发者自己申请的域名
//		SeckenInterface.setBaseUrl("http://172.0.0.1");
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.tv_auth:// 授权sdk
			auth();
			break;

		default:
			break;
		}
	}

	/**
	 * 授权sdk 1. 开发者调用前需要传入在洋葱开发者平台上申请的appkey，app_id,以及开发者app当前用户的username 2.
	 * 授权成功后的结果将在RequestListener 的onSuccess返回想要的信息，失败则返回到onFailed中；其中ErrorInfo
	 * 中的字段status即为错误码，errorMsg为返回的错误信息，开发者也可以通过api文档中的错误码对应的错误信息 查看错误原因。 3.
	 * 授权成功返回的token需要用户存储。
	 * 
	 * @param appkey
	 *            开发者申请应用的appkey
	 * @param app_id
	 *            开发者申请应用的app_id
	 * @param username
	 *            开发者app的用户名
	 */
	private void auth()
	{
		if (progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
		}
		progressDialog.show();

		final String appKey = et_appkey.getText().toString().trim();
		final String app_id = et_appid.getText().toString().trim();
		// 重新设置appKey和app_id
		SeckenUISDK.setAppInfo(this, appKey, app_id);

		Constants.USERNAME = et_username.getText().toString().trim();
		AuthInfo info = new AuthInfo(this, Constants.USERNAME);
		SeckenSDK.authorize(this, info, new RequestListener()
		{
			@Override
			public void onSuccess(Bundle data)
			{
				progressDialog.dismiss();
				if (data != null)
				{
					ToastUtils.showToast(AuthorizationActivity.this, R.string.auth_success);

					/**
					 * Token:仅作为本次授权身份验证token，用于SeckenSDK其他接口的调用。
					 * 不作为用户唯一id使用。（有效期为一周，建议在token到期之前重新授权获取新的token）
					 * 建议本地存储token以方便其它接口的调用。
					 */
					String token = data.getString("token");
					
					Constants.TOKEN = token;
					Bundle bundle = new Bundle();
					openActivity(FunctionActivity.class, bundle);
					finish();
				}
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				progressDialog.dismiss();
				if (errorInfo != null)
				{
					ToastUtils.showToast(AuthorizationActivity.this, errorInfo.errorMsg);
				}
			}
		});
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		String appkey = et_appkey.getText().toString().trim();
		String appid = et_appid.getText().toString().trim();
		String username = et_username.getText().toString().trim();
		if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(appid) || TextUtils.isEmpty(username))
		{
			tv_auth.setEnabled(false);
			tv_auth.setAlpha(0.6f);
		} else
		{
			tv_auth.setEnabled(true);
			tv_auth.setAlpha(1.0f);
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (progressDialog != null && progressDialog.isShowing())
		{
			progressDialog.dismiss();
		}
	}

	/**
	 * 通过类名启动Activity，并且含有Bundle数据
	 * 
	 * @param pClass
	 * @param pBundle
	 */
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
