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
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.toolbox.SeckenHandler;
import com.secken.sdk.ui.BaseActivity;
import com.secken.sdk.ui.FaceManager;
import com.secken.sdk.ui.OnFaceCallBack;
import com.secken.sdk.ui.SeckenUISDK;
import com.secken.sdk.ui.weight.maskview.MaskView;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @Description: face train
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class FaceTrainActivity extends BaseActivity implements OnHandlerListener, OnFaceCallBack
{

	/** 人脸识别成功 */
	private final static int CODE_VERTIFY_SUCESS = 0x03E9;
	/** 人脸识别失败 */
	private final static int CODE_VERTIFY_FAIL = 0x03EA;
	/** 开启识别 */
	private final static int CODE_START_PREVIEW = 0x03EB;
	/** 停止识别 */
	private final static int CODE_STOP_PREVIEW = 0x03EC;
	
	private TextView tv_title;
	private TextView tv_tip;
	/** 人脸识别部分 */
	private SurfaceView preview_face;
	private MaskView maskView;
	private SeckenHandler handler;

	/** 提示弹出框部分 */
	private String title;// 弹出框标题
	private String content;// 弹出框内容
	private Dialog builder;

	/** 用于训练成功或者失败的回调 */
	private Handler resultHandler;

	/** 人脸识别管理类 */
	private FaceManager faceManager;
	
	/** 必要参数部分 */
	private String token;
	private String username;
	private int step;// 训练人脸的步数

	@Override
	protected int getContentViewID()
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		return R.layout.secken_activity_face;
	}

	@Override
	protected void initView(Bundle savedInstanceState)
	{
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getString(R.string.face_train_title));
		tv_tip = (TextView) findViewById(R.id.tv_tip);
		handler = new SeckenHandler(this);
		handler.setOnHandlerListener(this);
		maskView = (MaskView) findViewById(R.id.view_mask);
		//设置人脸框
		maskView.setFaceImage(R.drawable.face);
		/** 获取传入的token和username */
		Intent bundle = getIntent();
		token = bundle.getStringExtra("token");
		username = bundle.getStringExtra("username");
		
		resultHandler = SeckenUISDK.getHandler();
		faceManager = new FaceManager(this);
		faceManager.setMaskView(maskView);
		faceManager.setOnFaceCallBack(this);
	}

	@Override
	public void onHandlerMessage(Activity activity, Message msg)
	{
		switch (msg.what)
		{
		case CODE_VERTIFY_SUCESS:// 验证成功
			handler.sendEmptyMessage(CODE_STOP_PREVIEW);
			showSuccessDialog(title, content);
			break;
		case CODE_VERTIFY_FAIL:// 验证失败
			handler.sendEmptyMessage(CODE_STOP_PREVIEW);
			title = getString(R.string.identify_fail_title);
			content = getString(R.string.identify_fail_content);
			showAlterDialog(title, content);
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
	protected void onResume()
	{
		super.onResume();
		initCamera();
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
	protected void onPause()
	{
		faceManager.quitSynchronously();
		super.onPause();
	}

	@Override
	public void onBackPressed()
	{
		faceManager.setFaceLock(true);
		if (resultHandler != null)
		{
			resultHandler.sendEmptyMessage(SeckenCode.FACE_TRAIN_CANCEL);
		}
		super.onBackPressed();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}

	@Override
	protected void onViewClick(View v)
	{

	}

	@Override
	public void onFaceResult(byte[] bytes)
	{
		tv_tip.setText(getString(R.string.face_vertifying));
		faceTrain(bytes);
	}
	
	/**
	 *  人脸训练。
	 *  提供带有人脸的图片即可进行人脸训练。
	 *  人脸训练分为三步上传， 当第三次上传(即step为3的时候)人脸成功之后 即为训练人脸成功。
	 *  @param byte[] face即为人脸的图片字节数组
	 *  @param step 为训练的步数
	 */
	public void faceTrain(byte[] face)
	{
		FaceInfo info = new FaceInfo(token, username, "" + (step + 1), face);
		SeckenSDK.faceTrain(this, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				step++;
				switch (step)
				{
				case 1:
					title = getString(R.string.face_train_one_title);
					content = getString(R.string.face_train_one_content);
					break;
				case 2:
					title = getString(R.string.face_train_two_title);
					content = getString(R.string.face_train_two_content);
					break;
				case 3:
					title = getString(R.string.dialog_alert);
					content = getString(R.string.face_train_final_content);
					break;

				default:
					break;
				}
				handler.sendEmptyMessage(CODE_VERTIFY_SUCESS);// 验证成功
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				handler.sendEmptyMessage(CODE_VERTIFY_FAIL);
			}
		});
	}
	
	/**
	 * 识别成功提示框
	 * 
	 * @param title
	 * @param content
	 */
	@SuppressLint("InflateParams")
	private void showSuccessDialog(String title, String content)
	{
		if (builder != null && builder.isShowing())
		{
			builder.dismiss();
		}
		
		if (isFinishing())
		{
			return;
		}
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.secken_face_alter_dialog, null);
		builder = new Dialog(FaceTrainActivity.this, R.style.Secken_Dialog);
		TextView tv_alter_content = (TextView) view.findViewById(R.id.tv_alter_content);
		TextView tv_alter_title = (TextView) view.findViewById(R.id.tv_alter_title);
		tv_alter_title.setText(title);
		tv_alter_content.setText(content);
		builder.setCancelable(false);
		builder.setContentView(view);
		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		view.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
		if (step < 3)
		{
			btn_ok.setText(getString(R.string.face_train_continue));
		} else
		{
			btn_ok.setText(getString(R.string.success));
		}
		btn_ok.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				builder.dismiss();
				if (step < 3)// 训练人脸1，2步
				{
					handler.sendEmptyMessage(CODE_START_PREVIEW);
					faceManager.setFaceLock(false);
				} else
				{// 训练成功
					if (resultHandler != null)
					{
						resultHandler.sendEmptyMessage(SeckenCode.FACE_TRAIN_SUCCESS);
					}
					finish();
				}
			}
		});
		builder.show();
	}

	/**
	 * 识别失败提示框
	 * 
	 * @param title
	 * @param content
	 */
	@SuppressLint("InflateParams")
	private void showAlterDialog(String title, String content)
	{
		if (builder != null && builder.isShowing())
		{
			builder.dismiss();
		}
		
		if (isFinishing())
		{
			return;
		}
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.secken_face_alter_dialog, null);
		builder = new Dialog(FaceTrainActivity.this, R.style.Secken_Dialog);
		TextView tv_alter_content = (TextView) view.findViewById(R.id.tv_alter_content);
		TextView tv_alter_title = (TextView) view.findViewById(R.id.tv_alter_title);
		tv_alter_title.setText(title);
		tv_alter_content.setText(content);
		builder.setCancelable(false);
		builder.setContentView(view);
		view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				builder.dismiss();
				// 训练失败
				if (resultHandler != null)
				{
					resultHandler.sendEmptyMessage(SeckenCode.FACE_TRAIN_FAIL);
				}
				finish();
			}
		});
		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				builder.dismiss();
				handler.sendEmptyMessage(CODE_START_PREVIEW);
				faceManager.setFaceLock(false);
			}
		});
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

}
