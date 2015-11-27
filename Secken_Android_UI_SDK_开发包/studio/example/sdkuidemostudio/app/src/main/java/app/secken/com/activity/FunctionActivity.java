package app.secken.com.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.secken.sdk.SeckenCode;
import com.secken.sdk.SeckenSDK;
import com.secken.sdk.entity.AppBaseInfo;
import com.secken.sdk.entity.ErrorInfo;
import com.secken.sdk.entity.FaceInfo;
import com.secken.sdk.entity.VoiceInfo;
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.ui.ScanningActivity;
import com.secken.sdk.ui.SeckenUISDK;
import com.secken.sdk.util.ToastUtils;

import app.secken.com.config.Constants;
import app.secken.com.lib.FaceTrainActivity;
import app.secken.com.lib.FaceVertifyActivity;
import app.secken.com.lib.VoiceTrainActivity;
import app.secken.com.lib.VoiceVertifyActivity;
import app.secken.com.sdkdemo.R;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @Description: sdk 主要功能模块
 */
@SuppressLint("HandlerLeak") public class FunctionActivity extends Activity implements OnClickListener
{

	/** 用于接收数据的bundle */
	private Bundle bundle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function);
		initView();
	}

	/**
	 * 初始化view
	 */
	protected void initView()
	{
		findViewById(R.id.tv_authcode).setOnClickListener(this);
		findViewById(R.id.tv_face_train).setOnClickListener(this);
		findViewById(R.id.tv_voice_train).setOnClickListener(this);
		findViewById(R.id.tv_face_delete).setOnClickListener(this);
		findViewById(R.id.tv_voice_delete).setOnClickListener(this);
		findViewById(R.id.tv_face_compare).setOnClickListener(this);
		findViewById(R.id.tv_voice_compare).setOnClickListener(this);
		findViewById(R.id.tv_unbind).setOnClickListener(this);
		
		//用于训练声音人脸和验证结果的回调
		SeckenUISDK.setHandler(handler);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.tv_authcode://扫描验证
			bundle = new Bundle();
			bundle.putString("token", Constants.TOKEN);
			bundle.putString("username", Constants.USERNAME);
			openActivity(ScanningActivity.class, bundle);
			break;
		case R.id.tv_face_train://人脸训练
			bundle = new Bundle();
			bundle.putString("token", Constants.TOKEN);
			bundle.putString("username", Constants.USERNAME);
			openActivity(FaceTrainActivity.class, bundle);
			break;
		case R.id.tv_voice_train:// 声音训练
			bundle = new Bundle();
			bundle.putString("token", Constants.TOKEN);
			bundle.putString("username", Constants.USERNAME);
			openActivity(VoiceTrainActivity.class, bundle);
			break;
		case R.id.tv_face_compare:// 人脸验证
			if (SeckenSDK.hasFace(this))
			{
				bundle = new Bundle();
				bundle.putString("token", Constants.TOKEN);
				bundle.putString("username", Constants.USERNAME);
				openActivity(FaceVertifyActivity.class, bundle);
			}else {
				Toast.makeText(this, "no face", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.tv_voice_compare:// 声音验证
			if (SeckenSDK.hasVoice(this))
			{
				bundle = new Bundle();
				bundle.putString("token", Constants.TOKEN);
				bundle.putString("username", Constants.USERNAME);
				openActivity(VoiceVertifyActivity.class, bundle);
			}else {
				Toast.makeText(this, "no voice", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.tv_face_delete:// 删除人脸
			faceDelete();
			break;
		case R.id.tv_voice_delete:// 删除声音
			voiceDelete();
			break;
		case R.id.tv_unbind:// 解除绑定
			unBind();
			break;

		default:
			break;
		}
	}

	/**
	 * 删除人脸信息，删除之后将无法开启人脸验证。
	 * 
	 * @param token 绑定之后返回的有效token
	 * @param username app的用户名
	 */
	public void faceDelete()
	{
		FaceInfo info = new FaceInfo(Constants.TOKEN, Constants.USERNAME);
		SeckenSDK.faceDelete(this, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				ToastUtils.showToast(getApplicationContext(), R.string.face_delete_success);
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				if (errorInfo != null)
				{
					ToastUtils.showToast(getApplicationContext(), errorInfo.errorMsg);
				}
			}
		});
	}

	/**
	 * 删除声音验证，删除之后将无法开启声音验证。
	 * 
	 * @param token  绑定之后返回的有效token
	 * @param username  app的用户名
	 */
	public void voiceDelete()
	{
		VoiceInfo info = new VoiceInfo(Constants.TOKEN, Constants.USERNAME);
		SeckenSDK.voiceDelete(this, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				ToastUtils.showToast(getApplicationContext(), R.string.voice_delete_success);
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				if (errorInfo != null)
				{
					ToastUtils.showToast(getApplicationContext(), errorInfo.errorMsg);
				}
			}
		});
	}

	/**
	 *  取消授权(注：取消授权之后再次绑定需要重新初始化sdk)
	 * @param token 绑定之后返回的有效token
	 * @param username app的用户名
	 */
	public void unBind()
	{
		AppBaseInfo appBaseInfo = new AppBaseInfo(Constants.TOKEN, Constants.USERNAME);
		SeckenSDK.logout(this, appBaseInfo, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				ToastUtils.showToast(getApplicationContext(), R.string.unbind_success);
				Constants.TOKEN = "";// token置空
				finish();
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				if (errorInfo != null)
				{
					ToastUtils.showToast(getApplicationContext(), errorInfo.errorMsg);
				}
			}
		});
	}

	/**
	 * Secken训练声音,人脸和验证结果的回调
	 */
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what)
			{
			case SeckenCode.FACE_TRAIN_SUCCESS://训练人脸成功
				ToastUtils.showToast(FunctionActivity.this, R.string.face_train_success);
				break;
			case SeckenCode.FACE_TRAIN_FAIL://训练人脸失败
				ToastUtils.showToast(FunctionActivity.this, R.string.face_train_fail);
				break;
			case SeckenCode.FACE_TRAIN_CANCEL://取消训练人脸
				ToastUtils.showToast(FunctionActivity.this, R.string.face_train_cancel);
				break;
			case SeckenCode.VERTIFY_SUCCESS://验证成功（包括所有验证方式验证的结果）
				Bundle bundle = msg.getData();
				/**
				 * auth_token: 集成SeckenSDK的app可将该字段提交至自己服务器，由自己服务器和洋葱服务器进行二次确认，
				 * 以确保洋葱SDK返回的验证结果未被篡改。
				 * (跟洋葱服务器进行二次确认接口：https://api.sdk.yangcong.com/query_auth_token) 
				 */
				String auth_token = bundle.getString("auth_token");
				ToastUtils.showToast(FunctionActivity.this, R.string.vertify_success);
				break;
			case SeckenCode.VERTIFY_FAIL://验证失败（包括所有验证方式验证的结果）
				ToastUtils.showToast(FunctionActivity.this, R.string.vertify_fail);
				break;
			case SeckenCode.VERTIFY_CANCEL://取消验证（包括所有验证方式验证的结果）
				ToastUtils.showToast(FunctionActivity.this, R.string.vertify_cancel);
				break;
			case SeckenCode.VOICE_TRAIN_SUCCESS://训练声音成功
				ToastUtils.showToast(FunctionActivity.this, R.string.voice_train_success);
				break;
			case SeckenCode.VOICE_TRAIN_FAIL://训练声音失败
				ToastUtils.showToast(FunctionActivity.this, R.string.voice_train_fail);
				break;
			case SeckenCode.VOICE_TRAIN_CANCEL://取消训练声音
				ToastUtils.showToast(FunctionActivity.this, R.string.voice_train_cancel);
				break;

			default:
				break;
			}
		};
	};
	
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
