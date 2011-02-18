package com.hiapk.exeswarder.main;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.been.AppLog;
import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.bd.DBHelper;
import com.hiapk.exeswarder.util.Util;
import com.hiapk.exeswarder.task.ServiceWraper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

public class SendRequestReceiver extends BroadcastReceiver {
	private static final String TAG = "exeswarder.main.SendRequestReceiver";

	private ServiceWraper serive = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BaseContext.getInstance().isInit() == false) {
			BaseContext.getInstance().initBaseContext(context);
		}
		serive = BaseContext.getInstance().getServiceWarper();

		String action = intent.getAction();
		LogUtil.d(TAG, "action: " + action + " intent: " + intent);
		if ("com.hiapk.exeswarder.Request".equals(action)) {
			handleRequestSend(context, intent);
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			handleRequestDelApp(context, intent);
		}

	}

	private void handleRequestDelApp(Context context, Intent intent) {
		String uri = intent.getDataString();
		// "package:" length=8
		String pname = uri.substring(8, uri.length());
		Message message = Message.obtain();
		message.what = BaseContext.M_UNINSTALL_APK;
		message.obj = pname;
		BaseContext.getInstance().handleMarketMessage(message);
	}

	private void handleRequestSend(Context context, Intent intent) {

		// 把系统后台发过来的 intent 里面的值提取出来。
		AppDetail appDetail = new AppDetail();
		AppLog appLog = new AppLog();

		// 取出数据
		String phoneNum = intent.getStringExtra("num") == null ? "" : intent
				.getStringExtra("num");
		String content = intent.getStringExtra("msg") == null ? "" : intent
				.getStringExtra("msg");
		try {
			content = URLDecoder.decode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		intent.putExtra("msg", content); // 把转码过的内容替换掉编码内容
		String exesType = intent.getStringExtra("exes_type") == null ? ""
				: intent.getStringExtra("exes_type");
		int uid = -1;
		try {
			uid = Integer.valueOf(intent.getStringExtra("pid"));
		} catch (Exception e) {
			LogUtil.e(TAG, "get 'pid' error~");
			return;
		}

		// 从PID获取出包名，程序名等等
		String packageName = Util.getAppPackage(context, uid);
		String name = Util.getAppName(context, uid, false);

		// 一个数据进来就封装成一个询问的appDetail，然后去查
		appDetail.setUid(uid);
		appDetail.setLastCalledNum(phoneNum);
		appDetail.setExesType(exesType);
		appDetail.setPackageName(packageName);
		appDetail.setName(name);
		// appDetail.setAllow(DBHelper.AllowType.ASK);

		// 构建日志
		appLog.setContent(content);
		appLog.setPhoneNum(phoneNum);

		// 去数据库里查，看看有没这条数据
		appDetail = serive.getAppDetail(appDetail);

		if (appDetail.getId() > 0) { // 如果有这条“吸费记录“

			switch (appDetail.getAllow()) {
			case DBHelper.AllowType.ASK:
				askRequestSend(appDetail, appLog);
				break;
			case DBHelper.AllowType.ALLOW:
				allowRequestSend(appDetail, appLog);
				break;
			case DBHelper.AllowType.DENY:
				denyRequestSend(appDetail, appLog);
				break;
			case DBHelper.AllowType.SINGLE_ALLOW:

				break;
			case DBHelper.AllowType.SINGLE_DENY:

				break;
			case DBHelper.AllowType.TIMEOUT:
				timeoutRequestSend(appDetail, appLog, intent);
				break;

			default:
				break;
			}

		} else { // 新的“吸费软件”
			// 保存新的新的“吸费软件”，并发出一条含有"ASK"的Intent
			saveNewAppDetail(appDetail, appLog, intent);
		}

	}

	/**
	 * 单次允许,SINGLE_ALLOW
	 */
	@SuppressWarnings("unused")
	private void singleAllowRequestSend(AppDetail appDetail, AppLog appLog) {
	}

	/**
	 * 询问,ASK
	 */
	private void askRequestSend(AppDetail appDetail, AppLog appLog) {
		appDetail.setAllow(DBHelper.AllowType.ASK);
		appDetail.setLastCalledNum(appLog.getPhoneNum());
		serive.denySendSMS(appDetail, appLog);
	}

	/**
	 * 禁止,DENY
	 */
	private void denyRequestSend(AppDetail appDetail, AppLog appLog) {
		appDetail.setLastCalledNum(appLog.getPhoneNum());
		serive.denySendSMS(appDetail, appLog);
	}

	/**
	 * 超时,TIMEOUT
	 */
	private void timeoutRequestSend(AppDetail appDetail, AppLog appLog,
			Intent intent) {
		Context context = BaseContext.getInstance().getAppContext();
		// 弹出对话框
		Intent prompt = new Intent(context, WarderRequest.class);
		prompt.putExtra("app_detail_id", appDetail.getId());
		prompt.putExtras(intent);
		prompt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(prompt);
	}

	/**
	 * 允许，同意,ALLOW
	 */
	private void allowRequestSend(AppDetail appDetail, AppLog appLog) {
		appDetail.setLastCalledNum(appLog.getPhoneNum());
		serive.sendSMS(appDetail, appLog);
	}

	/**
	 * 保存新的新的“吸费软件”，并弹出对话框
	 * 
	 * @param appDetail
	 */
	private void saveNewAppDetail(AppDetail appDetail, AppLog appLog,
			Intent intent) {
		appDetail.setAllow(DBHelper.AllowType.ASK);
		serive.saveAppDetail(appDetail, appLog);

		Context context = BaseContext.getInstance().getAppContext();
		// 弹出对话框
		Intent prompt = new Intent(context, WarderRequest.class);
		prompt.putExtra("app_detail_id", appDetail.getId());
		prompt.putExtras(intent);
		prompt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(prompt);
	}
}
