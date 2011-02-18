package com.hiapk.exeswarder.main;

import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.been.AppLog;
import com.hiapk.exeswarder.bd.DBHelper;
import com.hiapk.exeswarder.R;
import com.hiapk.exeswarder.task.ServiceWraper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WarderRequest extends Activity implements OnClickListener {

	private CountDownTimer mCountDown;
	private ServiceWraper service = null;

	SharedPreferences prefs;

	private AppDetail appDetail;
	private AppLog appLog;

	@Override
	protected void onResume() {
		super.onResume();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request);
		BaseContext.getInstance().initBaseContext(this);
		service = BaseContext.getInstance().getServiceWarper();

		Intent intent = getIntent();

		// 取出数据
		String content = intent.getStringExtra("msg") == null ? "" : intent
				.getStringExtra("msg");
		String phoneNum = intent.getStringExtra("num") == null ? "" : intent
				.getStringExtra("num");

		appDetail = new AppDetail();
		appDetail.setId(intent.getLongExtra(DBHelper.Logs.ID, 0));
		// 跟据intent传过来的id到数据里面查出来这个appDetail
		appDetail = service.getAppDetail(appDetail);

		// 构建日志
		appLog = new AppLog();
		appLog.setContent(content);
		appLog.setPhoneNum(phoneNum);

		TextView appNameView = (TextView) findViewById(R.id.appName);
		appNameView.setText(appDetail.getName());

		TextView packageNameView = (TextView) findViewById(R.id.packageName);
		packageNameView.setText(appDetail.getPackageName());

		TextView requestDetailView = (TextView) findViewById(R.id.requestDetail);
		requestDetailView.setText(appDetail.getExesType());

		TextView commandView = (TextView) findViewById(R.id.command);
		commandView.setText(appDetail.getLastCalledNum());

		Button allow = (Button) findViewById(R.id.allow);
		allow.setOnClickListener(this);

		Button deny = (Button) findViewById(R.id.deny);
		deny.setOnClickListener(this);

		final TextView timer = (TextView) findViewById(R.id.timer);
		mCountDown = new CountDownTimer(11000, 1000) {
			public void onTick(long millisUntilFinished) {
				timer.setText(Long.toString(millisUntilFinished / 1000));
				if (millisUntilFinished < 5001) {
					timer.setTextColor(Color.RED);
				}
			}

			public void onFinish() {
				sendTimeout();
				finish();
			}
		}.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			// sendDeny();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		mCountDown.cancel();
		switch (v.getId()) {
		case R.id.allow:
			sendAllow();
			break;

		case R.id.deny:
			sendDeny();
			break;

		}
		finish();
	}

	/**
	 * 允
	 */
	private void sendAllow() {
		appDetail.setAllow(DBHelper.AllowType.ALLOW);
		appDetail.setLastCalledNum(appLog.getPhoneNum());
		service.sendSMS(appDetail, appLog);

		// Intent intent = new Intent();
		// intent.putExtras(getIntent());
		// intent.setAction("com.hiapk.exeswarder.Request");
		// //intent.putExtra(SuRequestReceiver.EXTRA_ALLOW, AppDetail.ALLOW);
		// this.sendBroadcast(intent);
	}

	private void sendTimeout() {
		appDetail.setAllow(DBHelper.AllowType.TIMEOUT);
		appDetail.setLastCalledNum(appLog.getPhoneNum());
		service.timeoutSendSMS(appDetail, appLog);
	}

	private void sendDeny() {
		appDetail.setAllow(DBHelper.AllowType.DENY);
		appDetail.setLastCalledNum(appLog.getPhoneNum());
		service.denySendSMS(appDetail, appLog);
	}

}
