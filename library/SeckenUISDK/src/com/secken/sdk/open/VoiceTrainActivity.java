package com.secken.sdk.open;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.VerifierResult;
import com.secken.sdk.OnVerifierListener;
import com.secken.sdk.OnVoiceListener;
import com.secken.sdk.SeckenCode;
import com.secken.sdk.SeckenSDK;
import com.secken.sdk.entity.ErrorInfo;
import com.secken.sdk.entity.VoiceInfo;
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.ui.BaseActivity;
import com.secken.sdk.ui.SeckenUISDK;
import com.secken.sdk.ui.util.VibratorUtil;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @Description: voice train
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VoiceTrainActivity extends BaseActivity implements OnTouchListener
{
	/** 用于播放声音的MediaPlayer */
	private MediaPlayer music = null;

	/** 有效声纹数字 */
	private String mNumPwd = "";

	/** 数字声纹密码段，默认有5段 */
	private String[] mNumPwdSegs;

	/** 声音说话时的动画 */
	AnimatorSet animatorSet_image;
	ObjectAnimator animX;
	ObjectAnimator animY;
	ObjectAnimator rotation;

	/** voice训练成功之后返回的语音vid */
	private String vid;

	private ImageView img_voice;
	private ImageView iv_image;
	private TextView voice_number;
	private TextView tv_speak;
	private TextView tv_title;

	/** 参数部分 */
	private String token;//授权返回的token
	private String username;//用户唯一标示

	/** 用于训练成功或者失败的回调 */
	private Handler resultHandler;

	/** 音量大小 */
	private int index = 0;

	@Override
	protected int getContentViewID()
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		return R.layout.secken_activity_voice;
	}

	@Override
	protected void initView(Bundle savedInstanceState)
	{
		Intent bundle = getIntent();
		token = bundle.getStringExtra("token");
		username = bundle.getStringExtra("username");
		resultHandler = SeckenUISDK.getHandler();
		iv_image = (ImageView) findViewById(R.id.iv_image);
		voice_number = (TextView) findViewById(R.id.checkvoice_number);
		img_voice = (ImageView) findViewById(R.id.tv_voice);
		tv_speak = (TextView) findViewById(R.id.tv_speak);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getString(R.string.voice_train_title));
		// 初始化声音
		SeckenSDK.initRegistVoice(this, new OnVoiceListener()
		{
			@Override
			public void onResult(String numbers)
			{
				// 获取数字声纹密码段
				mNumPwd = SeckenSDK.getNumberSegs();
				mNumPwdSegs = mNumPwd.split("-");
				voice_number.setText(mNumPwd.substring(0, 8));
				img_voice.setOnTouchListener(VoiceTrainActivity.this);
			}

			@Override
			public void onFail(ErrorInfo errorInfo)
			{
				img_voice.setEnabled(false);
			}
		});

	}

	@Override
	protected void onViewClick(View v)
	{

	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		if (resultHandler != null)
		{
			resultHandler.sendEmptyMessage(SeckenCode.VOICE_TRAIN_CANCEL);
		}
	}

	/**
	 * 旋转animation
	 * 
	 * @param view
	 * @param size
	 */
	public void rotateAnimation(View view, float size)
	{
		animatorSet_image = new AnimatorSet();
		rotation = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
		animX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, size);
		animY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, size);
		rotation.setDuration(2000);
		rotation.setRepeatCount(-1);
		rotation.setInterpolator(new LinearInterpolator());
		animX.setDuration(2000);
		animX.setRepeatCount(-1);
		animX.setInterpolator(new CycleInterpolator(1));
		animY.setDuration(2000);
		animY.setRepeatCount(-1);
		animY.setInterpolator(new CycleInterpolator(1));
		// 两个动画同时执行
		animatorSet_image.playTogether(animX, animY, rotation);
		animatorSet_image.start();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN://按下操作
			PlayMusic(R.raw.secken_beep);
			rotateAnimation(iv_image, 1.1f);
			SeckenSDK.registSpeak(this, mNumPwd, mRegisterListener);
			tv_speak.setText(getString(R.string.voice_speeching));
			img_voice.setEnabled(false);
			break;
		case MotionEvent.ACTION_MOVE:

			break;
		case MotionEvent.ACTION_UP:
			break;

		default:
			break;
		}
		return false;
	}

	/**
	 * 播放音乐，参数是资源id
	 * 
	 * @param MusicId
	 */
	private void PlayMusic(int MusicId)
	{
		music = MediaPlayer.create(this, MusicId);
		music.start();
	}

	OnVerifierListener mRegisterListener = new OnVerifierListener()
	{
		private long lastCunrrentTime;

		@Override
		public void onVolumeChanged(int volume)
		{
			long currentTimeMillis = System.currentTimeMillis();

			/** 根据声音大小播放animation*/
			if (currentTimeMillis - lastCunrrentTime > 1000)
			{
				index = (volume);

				if (index <= 1)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.1f);
				} else if (index < 6 && index >= 3)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.2f);
				} else if (index >= 6 && index < 9)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.3f);
				} else if (index >= 9 && index < 12)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.4f);
				} else if (index >= 12 && index < 15)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.50f);
				} else if (index >= 15 && index < 18)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.60f);
				} else if (index >= 18 && index < 21)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.70f);
				} else if (index >= 21 && index < 24)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.80f);
				} else if (index >= 24 && index < 27)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 1.90f);
				} else if (index >= 27 && index < 30)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 2.0f);
				} else if (index >= 30 && index < 33)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 2.10f);
				} else if (index >= 33 && index < 36)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 2.20f);
				} else if (index >= 36 && index < 39)
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 2.30f);
				} else
				{
					animatorSet_image.end();
					rotateAnimation(iv_image, 2.40f);
				}
				lastCunrrentTime = currentTimeMillis;
			}

		}

		@Override
		public void onResult(VerifierResult result)
		{
			if (result.ret == ErrorCode.SUCCESS)
			{
				tv_speak.setVisibility(View.VISIBLE);
				switch (result.err)
				{
				case VerifierResult.MSS_ERROR_IVP_GENERAL://正常,请继续传音频
					tv_speak.setText(getString(R.string.voice_general));
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED://音频波形幅度太大,超出系统范围,发生截幅(建议说话离设备拉开适当的距离)
					tv_speak.setText(getString(R.string.voice_truncated));
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE://噪音太多
					tv_speak.setText(getString(R.string.voice_noise));
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT://录音太短
					tv_speak.setText(getString(R.string.voice_store));
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH://所读的的数字跟有效数字不一致
					tv_speak.setText(getString(R.string.context_error));
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW://声音太小
					tv_speak.setText(getString(R.string.voice_low));
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO://音频长达不到自由说的要求
					tv_speak.setText(getString(R.string.voice_no_enough_audio));
					break;
				default:// 其他错误
					tv_speak.setText(getString(R.string.check_fail1));
					break;
				}
				if (result.suc == result.rgn)//三次训练成功，则上传语音相关信息到服务器
				{
					VibratorUtil.Vibrate(VoiceTrainActivity.this, 400); // 震动400ms
					animatorSet_image.end();
					img_voice.setEnabled(true);
					PlayMusic(R.raw.secken_beep);
					vid = result.vid;
					updateVoice();
				} else
				{
					if (result.suc == 1)//第一次训练成功
					{
						tv_speak.setVisibility(View.VISIBLE);
						tv_speak.setText(getString(R.string.voice_again));
					} else if ((result.suc == 2))//第二次训练成功
					{
						tv_speak.setVisibility(View.VISIBLE);
						tv_speak.setText(getString(R.string.voice_last));
					}
					//更换有效数字
					voice_number.setText(mNumPwdSegs[result.suc]);
				}
			} else
			{
				showToast(R.string.voice_regist_fail);
				if (resultHandler != null)
				{
					resultHandler.sendEmptyMessage(SeckenCode.VOICE_TRAIN_FAIL);
				}
				finish();
			}

		}

		@Override
		public void onSpeechError(SpeechError error)
		{
			if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST)
			{//声纹信息已存在
				tv_speak.setText(getString(R.string.voice_isexit));
				img_voice.setEnabled(false);
			} else
			{//返回训练失败code
				showToast(R.string.voice_regist_fail);
				if (resultHandler != null)
				{
					resultHandler.sendEmptyMessage(SeckenCode.VOICE_TRAIN_FAIL);
				}
				finish();
			}

		}

		@Override
		public void onEndOfSpeech()
		{
			tv_speak.setText(getString(R.string.check_laoding));
			tv_speak.setVisibility(View.VISIBLE);
		}

		@Override
		public void onBeginOfSpeech()
		{
		}

		@Override
		public void onError(ErrorInfo arg0)
		{

		}

	};

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (animatorSet_image != null)
		{
			animatorSet_image.end();
		}
		if (music != null)
		{
			music.release();
			music = null;
		}
		//删除声音临时存储文件
		SeckenSDK.cleanVoice();
	}

	/**
	 * 更新声纹信息
	 * 
	 * @param voiceid
	 * @param regid
	 */
	public void updateVoice()
	{
		tv_speak.setText(getString(R.string.voice_uploading));
		VoiceInfo info = new VoiceInfo(token, username, vid);
		SeckenSDK.voiceTrain(this, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				tv_speak.setText("");
				if (resultHandler != null)
				{
					resultHandler.sendEmptyMessage(SeckenCode.VOICE_TRAIN_SUCCESS);
				}
				finish();
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				showToast(R.string.voice_regist_fail);
				if (resultHandler != null)
				{
					resultHandler.sendEmptyMessage(SeckenCode.VOICE_TRAIN_FAIL);
				}
				finish();
			}
		});
	}

}
