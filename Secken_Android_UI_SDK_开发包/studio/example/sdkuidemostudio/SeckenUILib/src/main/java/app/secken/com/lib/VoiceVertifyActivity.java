package app.secken.com.lib;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.CycleInterpolator;
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
import com.secken.sdk.entity.QRAuthInfo;
import com.secken.sdk.toolbox.RequestListener;
import com.secken.sdk.ui.BaseActivity;
import com.secken.sdk.ui.SeckenUISDK;
import com.secken.sdk.ui.util.VibratorUtil;
import com.secken.sdk.util.ToastUtils;

/**
 * 
 * Copyright 2014-2015 Secken, Inc. All Rights Reserved.
 * 
 * @author Secken
 * @version V1.0
 * @Description: voice vertify
 */
@SuppressLint(
{ "NewApi", "HandlerLeak" })
public class VoiceVertifyActivity extends BaseActivity implements OnTouchListener
{
	/** 用于播放声音的MediaPlayer */
	private MediaPlayer music = null;

	/** 声音说话时的动画 */
	private AnimatorSet animatorSet_image;
	private ObjectAnimator animX;
	private ObjectAnimator animY;

	/** 有效声纹数字 */
	private String verifyPwd;

	private ImageView img_voice;
	private ImageView iv_image;
	private TextView tv_title;
	private TextView tv_speak;
	private TextView voice_number;

	/** 参数部分 */
	private String token;//授权返回的token
	private String username;//用户唯一标示
	private String event_id;//event_id 用于授权验证的验证id
	private String longitude;//经度（可选参数）
	private String latitude;//纬度（可选参数）

	private String from;

	/** 用于训练成功或者失败的回调 */
	private Handler resultHandler;

	@Override
	protected int getContentViewID()
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		return R.layout.secken_activity_voice;
	}

	@Override
	protected void initView(Bundle savedInstanceState)
	{
		resultHandler = SeckenUISDK.getHandler();
		Intent bundle = getIntent();
		token = bundle.getStringExtra("token");
		username = bundle.getStringExtra("username");
		longitude = bundle.getStringExtra("longitude");
		latitude = bundle.getStringExtra("latitude");
		event_id = bundle.getStringExtra("event_id");
		
		iv_image = (ImageView) findViewById(R.id.iv_image);
		img_voice = (ImageView) findViewById(R.id.tv_voice);
		img_voice.setOnTouchListener(this);
		tv_speak = (TextView) findViewById(R.id.tv_speak);
		tv_title = (TextView) findViewById(R.id.tv_title);
		voice_number = (TextView) findViewById(R.id.checkvoice_number);
		// 初始化语音
		SeckenSDK.initCheckVoice(this, new OnVoiceListener()
		{

			@Override
			public void onResult(String number)
			{
				// 获取8位有效数字
				verifyPwd = number;
				voice_number.setText(verifyPwd);
				img_voice.setOnTouchListener(VoiceVertifyActivity.this);
			}

			@Override
			public void onFail(ErrorInfo errorInfo)
			{
				if (errorInfo != null)
				{
					ToastUtils.showToast(getApplicationContext(), errorInfo.errorMsg);
				}
				img_voice.setEnabled(false);
			}
		});
		from = getIntent().getStringExtra("from");
		tv_title.setText(bundle.getStringExtra("action_type"));
		//是尝试验证还是推送的验证
		if (!"vertify".equals(from))
		{
			String title = getString(R.string.voice_title);
			tv_title.setText(title);
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
		animX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, size);
		animY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, size);
		animX.setDuration(2000);
		animX.setRepeatCount(-1);
		animX.setInterpolator(new CycleInterpolator(1));
		animY.setDuration(2000);
		animY.setRepeatCount(-1);
		animY.setInterpolator(new CycleInterpolator(1));
		// 两个动画同时执行
		animatorSet_image.playTogether(animX, animY);
		animatorSet_image.start();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN://按下事件
			PlayMusic(R.raw.secken_beep);
			tv_speak.setText(getString(R.string.voice_speeching));
			img_voice.setEnabled(false);
			rotateAnimation(iv_image, 1.1f);
			SeckenSDK.checkSpeak(this, verifyPwd, mVerifyListener);
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

	private int index = 0;
	OnVerifierListener mVerifyListener = new OnVerifierListener()
	{
		private long lastCunrrentTime;

		@Override
		public void onVolumeChanged(int volume)
		{
			/** 根据声音大小播放animation*/
			long currentTimeMillis = System.currentTimeMillis();
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
			animatorSet_image.end();
			img_voice.setEnabled(true);
			VibratorUtil.Vibrate(VoiceVertifyActivity.this, 400); // 震动400ms
			if (result.ret == 0)//声音验证通过
			{
				tv_speak.setVisibility(View.VISIBLE);
				tv_speak.setText(getString(R.string.check_ok));
				if ("vertify".equals(from))// 声音通过验证并授权
				{
					qrConfirm(longitude, latitude, "1", event_id);
				} else
				{// 只是验证声音通过
					if (resultHandler != null)
					{
						Message message = Message.obtain();
						message.what = SeckenCode.VERTIFY_SUCCESS;
						Bundle bundle2 = new Bundle();
						bundle2.putString("auth_token", SeckenSDK.getAuthToken());
						message.setData(bundle2);
						resultHandler.sendMessage(message);
					}
					finish();
				}
			} else// 声音验证不通过
			{
				tv_speak.setVisibility(View.VISIBLE);
				//重新获取有效数字
				verifyPwd = SeckenSDK.getNumber();
				voice_number.setText(verifyPwd);
				rotateAnimation(iv_image, 1f);
				switch (result.err)
				{
				case VerifierResult.MSS_ERROR_IVP_GENERAL:// 正常,请继续传音频
					tv_speak.setText(getString(R.string.voice_general));
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:// 音频波形幅度太大,超出系统范围,发生截幅(建议说话离设备拉开适当的距离)
					tv_speak.setText(getString(R.string.voice_truncated));
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:// 噪音太多
					tv_speak.setText(getString(R.string.voice_noise));
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:// 录音太短
					tv_speak.setText(getString(R.string.voice_store));
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:// 所读的的数字跟有效数字不一致
					tv_speak.setText(getString(R.string.context_error));
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:// 声音太小
					tv_speak.setText(getString(R.string.voice_low));
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:// 音频长达不到自由说的要求
					tv_speak.setText(getString(R.string.voice_no_enough_audio));
					break;
				default:// 其他错误
					tv_speak.setText(getString(R.string.check_fail1));
					break;
				}
			}

		}

		@Override
		public void onSpeechError(SpeechError error)
		{
			VibratorUtil.Vibrate(VoiceVertifyActivity.this, 400); // 震动1000ms
			verifyPwd = SeckenSDK.getNumber();
			voice_number.setText(verifyPwd);
			switch (error.getErrorCode())
			{
			case ErrorCode.MSP_ERROR_NOT_FOUND://声纹无效
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.check_fail));
				ToastUtils.showToast(VoiceVertifyActivity.this, "Invalid voice information");//即没有训练声音
				break;
			case VerifierResult.MSS_ERROR_IVP_GENERAL://// 正常,请继续传音频
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.voice_general));
				break;
			case VerifierResult.MSS_ERROR_IVP_TRUNCATED:// 音频波形幅度太大,超出系统范围,发生截幅(建议说话离设备拉开适当的距离)
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.voice_truncated));
				break;
			case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:// 噪音太多
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.voice_noise));
				break;
			case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:// 录音太短
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.voice_store));
				break;
			case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:// 所读的的数字跟有效数字不一致
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.context_error));
				break;
			case VerifierResult.MSS_ERROR_IVP_TOO_LOW:// 声音太小
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.voice_low));
				break;
			case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:// 音频长达不到自由说的要求
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.voice_no_enough_audio));
				break;
			default:
				animatorSet_image.end();
				img_voice.setEnabled(true);
				tv_speak.setText(getString(R.string.check_fail));
				tv_speak.setVisibility(View.VISIBLE);
				break;
			}

		}

		@Override
		public void onEndOfSpeech()
		{
			findViewById(R.id.speak_layout).setVisibility(View.VISIBLE);
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
			if (resultHandler != null)
			{
				resultHandler.sendEmptyMessage(SeckenCode.VERTIFY_FAIL);
			}
			finish();
		}
	};

	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}

	@Override
	protected void onDestroy()
	{
		if (animatorSet_image != null)
		{
			animatorSet_image.end();
		}
		if (music != null)
		{
			music.release();
			music = null;
		}
		SeckenSDK.cleanVoice();
		super.onDestroy();
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
			resultHandler.sendEmptyMessage(SeckenCode.VERTIFY_CANCEL);
		}
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
	private void qrConfirm(String longitude, String latitude, final String agree, String event_id)
	{
		QRAuthInfo info = new QRAuthInfo(token, username, longitude, latitude, agree, event_id);
		SeckenSDK.qrConfirm(this, info, new RequestListener()
		{

			@Override
			public void onSuccess(Bundle bundle)
			{
				//授权成功
				if ("1".equals(agree))
				{
					if (resultHandler != null)
					{
						Message message = Message.obtain();
						message.what = SeckenCode.VERTIFY_SUCCESS;
						Bundle bundle2 = new Bundle();
						bundle2.putString("auth_token", SeckenSDK.getAuthToken());
						message.setData(bundle2);
						resultHandler.sendMessage(message);
					} 
				}
				finish();
			}

			@Override
			public void onFailed(ErrorInfo errorInfo)
			{
				tv_speak.setText(getString(R.string.check_fail));
				if (errorInfo != null)
				{
					ToastUtils.showToast(getApplicationContext(), errorInfo.errorMsg);
				}
			}
		});
	}

}
