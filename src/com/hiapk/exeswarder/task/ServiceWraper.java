package com.hiapk.exeswarder.task;

import java.lang.reflect.Method;

import android.telephony.SmsManager;

import com.hiapk.exeswarder.bd.DBHelper;
import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.been.AppLog;

import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.main.BaseContext;
import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.mark.MultipleTaskMark;
import com.hiapk.exeswarder.service.IBaseService;
import com.hiapk.exeswarder.task.tracher.AInvokeTracker;
import com.hiapk.exeswarder.task.tracher.AppDetailListTracker;
import com.hiapk.exeswarder.task.tracher.AppLogListTracker;
import com.hiapk.exeswarder.task.tracher.AppImageTaskTracker;

/**
 * 每个服务业务的包装者类，通过他来调用服务业务类，返回的数据会被封装好
 * 
 * @author LinLin
 * 
 */
public class ServiceWraper {
	public static final String TAG = "MarketServiceWraper";

	// 用于控制图片资源型请求的阀值,减小流量和系统开销。
	private ImageTaskScheduler imageTaskScheduler;

	private IBaseService service;
	private BaseContext baseContext = BaseContext.getInstance();

	public ServiceWraper(IBaseService service) {
		this.service = service;
	}

	public IBaseService getService() {
		return service;
	}

	/**
	 * 获取第三方安装软件列表,多线程来做
	 * 
	 * @return AsyncOperation
	 */
	public AsyncOperation checkBadAppList(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppLogListTracker appLogListTracker = new AppLogListTracker(resultReceiver);
				operation = wraperOperation(service, appLogListTracker, taskMark, "checkBadAppList", attach);
				operation.excuteOperate(service);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 清除日志
	 */
	public void clearLog() {
		service.clearLog();
	}

	/**
	 * 更新AppDetail List
	 */
	public void refreshAppDetailList() {
		baseContext.setAppDetailList(service.getAllAppDetailList());
	}

	/**
	 * 更新AppLog List
	 */
	public void refreshAppLogList() {
		baseContext.setAppLogList(service.getTopAppLogList(50));
	}

	/**
	 * 更新AppDetail
	 * 
	 * @param appDetail
	 * @return
	 */
	public void delAppDetail(AppDetail appDetail) {
		service.delAppDetail(appDetail);
		refreshAppDetailList();
		refreshAppLogList();
	}

	/**
	 * 更新AppDetail
	 * 
	 * @param appDetail
	 * @return
	 */
	public void updateAppDetail(AppDetail appDetail) {
		service.updateAppDetail(appDetail);

		// 补构日志
		// appLog.setAllow(appDetail.getAllow());
		// appLog.setAppId(appDetail.getId());
		// appLog.setDate(System.currentTimeMillis());
		//		
		// service.saveAppLog(appLog);
	}

	/**
	 * 保存新的新的“吸费软件”，并弹出对话框
	 * 
	 * @param appDetail
	 * @return
	 */
	public void saveAppDetail(AppDetail appDetail, AppLog appLog) {
		appDetail = service.saveAppDetail(appDetail);

		appLog.setAllow(appDetail.getAllow());
		appLog.setAppId(appDetail.getId());
		appLog.setDate(System.currentTimeMillis());

		service.saveAppLog(appLog);
	}

	/**
	 * 超时 不让发送短信要做的事情
	 * 
	 * @param appDetail
	 * @param appLog
	 */
	public void timeoutSendSMS(AppDetail appDetail, AppLog appLog) {
		// 补全日志
		appLog.setAllow(appDetail.getAllow());
		appLog.setAppId(appDetail.getId());
		appLog.setDate(System.currentTimeMillis());

		// 补全AppDetail
		appDetail.setLastCalledNum(appLog.getPhoneNum());

		service.saveAppLog(appLog); // 添加日志
		service.updateAppDetail(appDetail); // 更新防火应用程序
	}

	/**
	 * 不让发送短信要做的事情
	 * 
	 * @param appDetail
	 * @param appLog
	 */
	public void denySendSMS(AppDetail appDetail, AppLog appLog) {
		// 补全日志
		appLog.setAllow(appDetail.getAllow());
		appLog.setAppId(appDetail.getId());
		appLog.setDate(System.currentTimeMillis());

		// 补全AppDetail
		appDetail.setLastCalledNum(appLog.getPhoneNum());

		service.saveAppLog(appLog); // 添加日志
		service.updateAppDetail(appDetail); // 更新防火应用程序
	}

	/**
	 * 发送短信要做的事情
	 * 
	 * @param appDetail
	 * @param appLog
	 */
	public void sendSMS(AppDetail appDetail, AppLog appLog) {
		// 发送短信
		// Object[] args = new Object[6];
		// args[0] = BaseContext.getInstance().getAppContext();
		// args[1] = appLog.getPhoneNum();
		// args[2] = null;
		// args[3] = appLog.getContent();
		// args[4] = null;
		// args[5] = null;
		// Util.sendMssageSMS("sendTextMassageByHiapk", args);

		// 用系统发送短信(新接口)
		SmsManager smsManager = SmsManager.getDefault();
		
		LogUtil.e("-->", appLog.getPhoneNum() + " " + appLog.getContent());
		
		smsManager.sendTextMessage(appLog.getPhoneNum(), null, appLog.getContent(), null, null);

		// 补全日志
		appLog.setAllow(DBHelper.AllowType.ALLOW);
		appLog.setAppId(appDetail.getId());
		appLog.setDate(System.currentTimeMillis());

		// 补全AppDetail
		appDetail.setLastCalledNum(appLog.getPhoneNum());

		service.saveAppLog(appLog); // 添加日志
		service.updateAppDetail(appDetail); // 更新防火应用程序
	}

	/**
	 * 获取全部的“被防火软件”列表(单线程版)
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param attach
	 * @return AsyncOperation
	 */
	public AppDetail getAppDetail(AppDetail appDetail) {
		return service.getAppDetailForDB(appDetail);
	}

	/**
	 * 获取相对应id的日志
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param attach
	 * @return AsyncOperation
	 */
	public AppLog getAppLog(AppLog appLog) {
		return service.getAppLogForDB(appLog);
	}

	/**
	 * 获取top的“被防火软件”日志列表
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param attach
	 * @return AsyncOperation
	 */
	public AsyncOperation getTopAppLogList(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppLogListTracker appLogListTracker = new AppLogListTracker(resultReceiver);
				operation = wraperOperation(service, appLogListTracker, taskMark, "getTopAppLogList", attach);
				operation.excuteOperate(service, 50);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取全部的“被防火软件”列表
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param attach
	 * @return AsyncOperation
	 */
	public AsyncOperation getAllAppDetailList(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppDetailListTracker appDetailListTracker = new AppDetailListTracker(resultReceiver);
				operation = wraperOperation(service, appDetailListTracker, taskMark, "getAllAppDetailList", attach);
				operation.excuteOperate(service);
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 同步软件信息列表
	 * 
	 * @param resultReceiver
	 * @param taskMark
	 * @param packageList
	 *            需要同步的软件包列表
	 * @param sign
	 * @return
	 */
	public AsyncOperation getAppImageResource(IResultReceiver resultReceiver, ATaskMark taskMark, Object attach,
			int appId, String url, int type) {
		try {
			AsyncOperation operation = null;
			if (AsyncOperation.isTaskExist(taskMark)) {
				operation = takeoverExistTask(resultReceiver, taskMark);

			} else {
				AppImageTaskTracker downloadUrlTracker = new AppImageTaskTracker(resultReceiver);
				operation = wraperOperation(service, downloadUrlTracker, taskMark, "getAppImageResource", attach);
				// String sign = SecurityUtil.md5Encode("time " + "hiziyuan");
				operation.excuteOperate(service, String.valueOf(appId), String.valueOf(type));
			}
			return operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获得图片资源的任务
	 * 
	 * @param receiver
	 * @param mTaskMark
	 * @param attach
	 * @return
	 */
	public MultipleTaskScheduler scheduleAppImageResourceTask(IResultReceiver receiver, MultipleTaskMark mTaskMark,
			Object attach) {
		if (imageTaskScheduler != null) {
			// 之前的任务wuxiao
			MultipleTaskMark oldTaskMark = imageTaskScheduler.getMultipleTaskMark();
			if (oldTaskMark != null) {
				// 合并任务，重新设置接受者。
				imageTaskScheduler.mergeTaskSchedul(mTaskMark);
			} else {
				imageTaskScheduler.setMultipleTaskMark(mTaskMark);
			}
		} else {
			imageTaskScheduler = new ImageTaskScheduler(this, mTaskMark);
		}
		imageTaskScheduler.setReceiver(receiver);
		LogUtil.iop(TAG, "schedulAppImageResourceTask: task size: " + mTaskMark.getTaskMarkList().size() + "\n task: "
				+ mTaskMark);
		imageTaskScheduler.triggerSchedulTask();

		return imageTaskScheduler;
	}

	// 封装一个操作
	private AsyncOperation wraperOperation(Object service, AInvokeTracker invokeTracker, ATaskMark taskMark,
			String methodName, Object attach) {
		Method method = getMethod(service, methodName);
		AsyncOperation operation = new AsyncOperation(taskMark, method);
		operation.setInvokeTracker(invokeTracker);
		operation.setAttach(attach);
		return operation;
	}

	/**
	 * 如果一个任务之前已经执行，当还没有返回的时候，如果这个时候 有新的任务请求进来，<br>
	 * 那么请求者将接管此任务, 但不能更改旧任务的状态，以便保证任务的完整性。
	 */
	private AsyncOperation takeoverExistTask(IResultReceiver resultReceiver, ATaskMark taskMark) {
		AsyncOperation asyncOperation = AsyncOperation.getTaskByMark(taskMark);
		if (asyncOperation != null) {
			AInvokeTracker aInvokeTracker = asyncOperation.getInvokeTracker();
			if (aInvokeTracker != null && aInvokeTracker.getResultReceiver() != resultReceiver) {
				aInvokeTracker.setResultReceiver(resultReceiver);
			} else {
				LogUtil.e(TAG, "!!!! the same resultReceiver : " + taskMark);
			}
			LogUtil.iop(TAG, "!!!! taskover : " + taskMark);
		} else {
			LogUtil.iop(TAG, "!!!! not need taskover : " + taskMark);
		}

		return asyncOperation;
	}

	// 获得方法，如果缓存中没有这查找。
	private Method getMethod(Object service, String name) {
		Method method = null;
		Method[] methods = service.getClass().getMethods();
		for (Method aMethod : methods) {
			if (aMethod.getName().equals(name)) {
				method = aMethod;
				break;
			}
		}

		if (method == null) {
			throw new NoSuchMethodError("unknow method : " + name);
		} else {
			return method;
		}
	}
}
