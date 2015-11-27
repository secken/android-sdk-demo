package app.secken.com.lib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.secken.sdk.OnHandlerListener;
import com.secken.sdk.SeckenCode;
import com.secken.sdk.SeckenSDK;
import com.secken.sdk.entity.ErrorInfo;
import com.secken.sdk.entity.FaceInfo;
import com.secken.sdk.entity.QRAuthInfo;
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.toolbox.SeckenHandler;
import com.secken.sdk.ui.BaseActivity;
import com.secken.sdk.ui.FaceManager;
import com.secken.sdk.ui.OnFaceCallBack;
import com.secken.sdk.ui.SeckenUISDK;
import com.secken.sdk.ui.weight.maskview.MaskView;
import com.secken.sdk.util.AppUtil;
import com.secken.sdk.util.StringUtils;
import com.secken.sdk.util.ToastUtils;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author lizhangfeng
 * @version V1.0.0
 * @Description: face vertify
 * @date 2015-9-10
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("InflateParams")
public class FaceVertifyActivity extends BaseActivity implements OnHandlerListener, OnFaceCallBack
{

	/** 人脸不存在 */
	private final static int CODE_FACE_NOTEXIT = 0x0065;
	/** 正在识别人脸 */
	private final static int CODE_FACE_VERTIFYING = 0x0066;
	/** 人脸验证失败 */
	private final static int CODE_VERTIFY_FAILED = 0x0067;
	/** 停止预览 */
	private final static int CODE_STOP_PREVIEW = 0x0068;
	/** 开启预览 */
	private final static int CODE_START_PREVIEW = 0x0069;

	private TextView tv_tip;
	private TextView tv_title;
	/** 人脸识别部分 */
	private SurfaceView preview_face;
	private MaskView maskView;
	private SeckenHandler handler;

	/** 提示弹出框部分 */
	private String title;// 弹出框标题
	private String content;// 弹出框内容
	private Dialog builder;

	/** 必要参数部分 */
	private String token;
	private String username;
	private String event_id;
	private String longitude;
	private String latitude;
	private String action_type;

	private String from;

	/** 用于验证成功或者失败的回调 */
	private Handler resultHandler;

	/** 人脸识别管理类 */
	private FaceManager faceManager;

	@Override
	protected int getContentViewID()
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		return R.layout.secken_activity_face;
	}

	@Override
	protected void initView(Bundle savedInstanceState)
	{
		resultHandler = SeckenUISDK.getHandler();
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_tip = (TextView) findViewById(R.id.tv_tip);
		handler = new SeckenHandler(this);
		handler.setOnHandlerListener(this);
		maskView = (MaskView) findViewById(R.id.view_mask);
		maskView.setFaceImage(R.drawable.face);
		Intent bundle = getIntent();
		token = bundle.getStringExtra("token");
		username = bundle.getStringExtra("username");
		longitude = bundle.getStringExtra("longitude");
		latitude = bundle.getStringExtra("latitude");
		action_type = bundle.getStringExtra("action_type");
		event_id = bundle.getStringExtra("event_id");
		from = bundle.getStringExtra("from");
		tv_title.setText(action_type);
		if (!"vertify".equals(from))
		{
			String title = getString(R.string.face_title);
			tv_title.setText(title);
		}

		faceManager = new FaceManager(this);
		faceManager.setMaskView(maskView);
		faceManager.setOnFaceCallBack(this);

	}

	/**
	 * 人脸匹配，上传人脸图片进行人脸匹配。
	 * 
	 * @param token 绑定之后返回的有效token
	 * @param username app的用户名的唯一标示
	 * @param face 人脸图片
	 */
	private void vertifyFace(byte[] face)
	{
		FaceInfo info = new FaceInfo(token, username, face);
		SeckenSDK.faceCompare(this, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				if ("vertify".equals(from))
				{
					String auth_token = bundle.getString("auth_token");
					qrConfirm(longitude, latitude, "1", event_id, auth_token);
				} else
				{
					if (resultHandler != null)
					{
						Message message = Message.obtain();
						message.what = SeckenCode.VERTIFY_SUCCESS;
						message.setData(bundle);
						resultHandler.sendMessage(message);
					}
					finish();
				}
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				if (errorInfo != null)
				{
					// 人脸信息不存在
					if ("40010".equals(errorInfo.status) || "60006".equals(errorInfo.status))
					{
						content = errorInfo.errorMsg;
						handler.sendEmptyMessage(CODE_FACE_NOTEXIT);
					}
				}
				handler.sendEmptyMessage(CODE_VERTIFY_FAILED);// 验证失败
			}
		});
	}

	@Override
	public void onHandlerMessage(Activity activity, Message msg)
	{
		switch (msg.what)
		{
		case CODE_FACE_NOTEXIT:// 人脸不存在
			handler.sendEmptyMessage(CODE_STOP_PREVIEW);
			title = getString(R.string.identify_fail_title);
			showAlterDialog(title, content, true);
			break;
		case CODE_FACE_VERTIFYING:// 正在识别人脸
			tv_tip.setText(getString(R.string.face_vertifying));
			break;
		case CODE_VERTIFY_FAILED:// 人脸验证失败
			handler.sendEmptyMessage(CODE_STOP_PREVIEW);
			title = getString(R.string.identify_fail_title);
			content = getString(R.string.identify_fail_content);
			showAlterDialog(title, content, false);
			break;
		case CODE_STOP_PREVIEW:// 停止预览
			tv_tip.setText(getString(R.string.face_tip));
			faceManager.stopPreview();
			break;
		case CODE_START_PREVIEW:// 开启预览
			tv_tip.setText(getString(R.string.face_tip));
			faceManager.startPreview();
			break;
		default:
			break;
		}

	}

	@Override
	public void onBackPressed()
	{
		faceManager.setFaceLock(true);
		if (resultHandler != null)
		{
			resultHandler.sendEmptyMessage(SeckenCode.VERTIFY_CANCEL);
		}
		super.onBackPressed();
	}

	@Override
	protected void onPause()
	{
		faceManager.quitSynchronously();
		super.onPause();
	}

	private boolean isHasFace = false;// 判断surfaceView是否create

	@Override
	protected void onResume()
	{
		super.onResume();
		if (!isHasFace)
		{
			initCamera();
		}
	}

	/**
	 * 初始化Camera
	 */
	public void initCamera()
	{
		preview_face = (SurfaceView) findViewById(R.id.preview_face);
		faceManager.setSurfaceView(preview_face);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}

	/**
	 * 识别提示框
	 * 
	 * @param title
	 * @param content
	 */
	private void showAlterDialog(String title, String content, boolean hideOne)
	{
		if (isFinishing())
		{
			return;
		}
		if (builder != null && builder.isShowing())
		{
			builder.dismiss();	
		}
		LayoutInflater inflater = LayoutInflater.from(FaceVertifyActivity.this);
		View view = inflater.inflate(R.layout.secken_face_alter_dialog, null);
		builder = new Dialog(FaceVertifyActivity.this, R.style.Secken_Dialog);
		TextView tv_alter_content = (TextView) view.findViewById(R.id.tv_alter_content);
		TextView tv_alter_title = (TextView) view.findViewById(R.id.tv_alter_title);
		if (StringUtils.isNullOrEmpty(title))
		{
			tv_alter_title.setVisibility(View.GONE);
		} else
		{
			tv_alter_title.setVisibility(View.VISIBLE);
			tv_alter_title.setText(title);
		}
		tv_alter_content.setText(content);
		builder.setCancelable(false);
		builder.setContentView(view);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		if (hideOne)
		{
			btn_cancel.setVisibility(View.GONE);
			if (AppUtil.isZh(getApplicationContext()))
			{
				btn_ok.setText("确定");
			} else
			{
				btn_ok.setText("OK");
			}
			btn_ok.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					builder.dismiss();
					if (resultHandler != null)
					{// 验证失败
						resultHandler.sendEmptyMessage(SeckenCode.VERTIFY_FAIL);
					}
					finish();
				}
			});
		} else
		{
			btn_cancel.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View arg0)
				{
					builder.dismiss();
					if (resultHandler != null)
					{// 验证失败
						resultHandler.sendEmptyMessage(SeckenCode.VERTIFY_FAIL);
					}
					finish();
				}
			});
			btn_ok.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					builder.dismiss();
					handler.sendEmptyMessage(CODE_START_PREVIEW);
					faceManager.setFaceLock(false);
				}
			});
		}

		builder.show();

	}

	@Override
	protected void onDestroy()
	{
		faceManager.setFaceLock(true);
		if (builder != null && builder.isShowing())
		{
			builder.dismiss();
		}
		super.onDestroy();
	}

	/**
	 *  授权信息的验证。
	 *  用于验证信息的确认或者拒绝。
	 *  @param token 绑定之后返回的有效token
	 *  @param username app的用户的唯一标示
	 *  @param longitude 经度
	 *  @param latitude    纬度
	 *  @param agree 必选参数 "0"为同意验证 "1"为拒绝验证 
	 *  @param event_id 扫描确认之后返回的参数。
	 */
	private void qrConfirm(String longitude, String latitude, final String agree, String event_id,
			final String auth_token)
	{
		QRAuthInfo info = new QRAuthInfo(token, username, longitude, latitude, agree, event_id);
		SeckenSDK.qrConfirm(this, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				if (resultHandler != null)
				{
					Message message = Message.obtain();
					Bundle bundle2 = new Bundle();
					bundle2.putString("auth_token", auth_token);
					message.setData(bundle2);
					message.what = SeckenCode.VERTIFY_SUCCESS;
					resultHandler.sendMessage(message);
				} else
				{
					resultHandler.sendEmptyMessage(SeckenCode.VERTIFY_FAIL);
				}
				finish();
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				if (errorInfo != null)
				{
					ToastUtils.showToast(getApplicationContext(), errorInfo.errorMsg);
				}
				if (resultHandler != null)
				{
					resultHandler.sendEmptyMessage(SeckenCode.VERTIFY_FAIL);
				}
				finish();
			}
		});
	}

	@Override
	protected void onViewClick(View v)
	{

	}

	@Override
	public void onFaceResult(byte[] bytes)
	{
		tv_tip.setText(getString(R.string.face_vertifying));
		vertifyFace(bytes);
	}

}
