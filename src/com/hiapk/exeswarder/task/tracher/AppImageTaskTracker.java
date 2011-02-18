package com.hiapk.exeswarder.task.tracher;

import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.task.OperateResult;

/**
 * 2010-7-25 <br>
 * 应用的图片跟踪
 * 
 * @author ckcs
 * 
 */
public class AppImageTaskTracker extends AInvokeTracker {

	public static final String TAG = "AppImageTaskTracker";

	/**
	 * @param resultReceiver
	 */
	public AppImageTaskTracker(IResultReceiver resultReceiver) {
		super(resultReceiver);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hiapk.market.task.tracker.AInvokeTracker#TAG()
	 */
	@Override
	public String TAG() {
		return TAG;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hiapk.market.task.tracker.AInvokeTracker#handleResult(com.hiapk.market
	 * .task.OperateResult)
	 */
	@Override
	public void handleResult(OperateResult result) {
		// AppImageTaskMark taskMark = (AppImageTaskMark) result.getTaskMark();
		trackerResult = result.getResultData();
		// byte[] bytes = (byte[]) result.getResultData();
		// if (taskMark.getType() == AppImageTaskMark.APP_SCREENSHOT) {
		// assertCacheManager.addScreenshotsByteToCache(taskMark.getUrl(),
		// bytes);
		//			
		// } else if (taskMark.getType() == AppImageTaskMark.APP_ICON) {
		// assertCacheManager.addAppIconByteToCache(taskMark.getAppId(), bytes);
		//
		// } else if (taskMark.getType() == AppImageTaskMark.APP_ADVERTISE_ICON)
		// {
		// assertCacheManager.addAdvertiseIconByteToCache(taskMark.getAppId(),
		// bytes);
		// }
	}

}
