package com.hiapk.exeswarder.task;

import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.mark.AppImageTaskMark;
import com.hiapk.exeswarder.mark.MultipleTaskMark;
import com.hiapk.exeswarder.task.tracher.AInvokeTracker;

/**
 * 2010-7-24<br>
 * 图片资源获取任务调度
 * 
 * @author ckcs
 * 
 */
public class ImageTaskScheduler extends MultipleTaskScheduler {

	public static final String TAG = "ImageTaskScheduler";

	/**
	 * @param service
	 * @param receiver
	 * @param multipleTaskMark
	 */
	public ImageTaskScheduler(ServiceWraper service,
			MultipleTaskMark multipleTaskMark) {
		super(service, multipleTaskMark);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hiapk.market.task.MultipleTaskScheduler#TAG()
	 */
	@Override
	public String TAG() {
		// TODO Auto-generated method stub
		return TAG;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hiapk.market.task.MultipleTaskScheduler#handleExecuteNextTask(com
	 * .hiapk.market.task.mark.ATaskMark, com.hiapk.market.task.IResultReceiver)
	 */
	@Override
	protected AInvokeTracker handleExecuteNextTask(ATaskMark taskMark,
			IResultReceiver receiver) {
		AppImageTaskMark appImageTaskMark = (AppImageTaskMark) taskMark;
		AsyncOperation operation = serviceWraper.getAppImageResource(receiver,
				taskMark, null, appImageTaskMark.getAppId(), appImageTaskMark
						.getUrl(), appImageTaskMark.getType());

		return operation.getInvokeTracker();
	}

}
