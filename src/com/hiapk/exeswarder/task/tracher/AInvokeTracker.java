package com.hiapk.exeswarder.task.tracher;

import java.lang.ref.WeakReference;

import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.mark.DependTaskMark;
import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.task.OperateResult;
import com.hiapk.exeswarder.util.Reflection;

import android.app.Activity;
import android.os.SystemClock;

/**
 * 2010-6-26 调用的回调器，用于接收成功或失败的信息。 2010-6-16<br>
 * 它的作用范围是数据层, 只对存在的数据进行 保持或者处理。<br>
 * 作用域内存并于视图层建立连接，与服务链顶层MemoryService互补。
 * 
 */
public abstract class AInvokeTracker {

	// 结果跟踪，用于解释成功或失败的结果。
	// 安全考虑：弱应用
	private WeakReference<IResultReceiver> iReference;

	// 跟踪后的一个结果，用于处理某些临时的，瞬态的数据，以便将他们传递个视图成。
	protected Object trackerResult;

	/**
	 * @param iReference
	 */
	public AInvokeTracker(IResultReceiver resultReceiver) {
		super();
		this.iReference = new WeakReference<IResultReceiver>(resultReceiver);
	}

	/**
	 * @return the iReference
	 */
	public IResultReceiver getResultReceiver() {
		return iReference.get();
	}

	/**
	 * @param iReference
	 *            the iReference to set
	 */
	public void setResultReceiver(IResultReceiver iReference) {
		this.iReference = new WeakReference<IResultReceiver>(iReference);
	}

	/**
	 * 预处理,在任务对应的方法执行完毕从后台执行方法退出前调用。 <br>
	 * 默认执行等待依赖的任务。<br>
	 * 注意：它不是在ui线程执行的。
	 */
	public void handleInvoikePrepare(ATaskMark taskMark) {
		if (taskMark instanceof DependTaskMark) {
			DependTaskMark dependTaskMark = (DependTaskMark) taskMark;
			ATaskMark dependTask = dependTaskMark.getDependTask();
			// 是否需要等待, 如果需要最多等待15s。注意只等待处理中的任务。
			if (dependTask != null
					&& dependTask.getTaskStatus() == ATaskMark.HANDLE_DOING) {
				LogUtil
						.d(TAG(), "handleInvoikePrepare wait for: "
								+ dependTask);
				int tryCount = 0;
				while (true) {
					if (dependTask.getTaskStatus() != ATaskMark.HANDLE_DOING
							|| tryCount >= 50) {
						break;
					}
					SystemClock.sleep(150);
					tryCount++;
				}
			} else {
				LogUtil.d(TAG(), "handleInvoikePrepare not need wait for: "
						+ dependTask);
			}
		}
	}

	/**
	 * 任务的最最终调用。注意:在ui线程调用。<br>
	 * 默认执行取消依赖的任务。
	 * 
	 * @param taskMark
	 */
	public void handleInvokeFinalize(ATaskMark taskMark) {
		if (taskMark instanceof DependTaskMark) {
			DependTaskMark dependTaskMark = (DependTaskMark) taskMark;
			dependTaskMark.setDependTask(null);
		}
	}

	/**
	 * 当任务执行完毕的时候的回调接口 注意，这个方法将在事件线程中调用。<br>
	 * 注意:在ui线程调用。
	 * 
	 * @param result
	 */
	public void handleInvokeOver(OperateResult result) {
		// 传给接受者
		ATaskMark taskWraper = result.getTaskMark();

		// 如果内部抛出未捕获的异常则提示错误
		try {
			if (taskWraper.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				// 实际的数据处理
				// LogUtil.i(TAG(), "handleInvokeOver handle ok");
				handleResult(result);
			} else {
				// LogUtil.i(TAG(), "handleInvokeOver handle fault");
				handleFault(result);
			}

		} catch (Exception e) {
			taskWraper.setTaskStatus(ATaskMark.HANDLE_ERROR);
			e.printStackTrace();
		}

		// 传给接受者
		IResultReceiver receiver = getResultReceiver();
		if (receiver == null
				|| (receiver instanceof Activity && ((Activity) receiver)
						.isFinishing())) {
			// LogUtil.i(TAG(),
			// "handleInvokeOver receive ingore................. taskMark: "
			// + taskWraper);
		} else {
			// LogUtil.i(TAG(),
			// "handleInvokeOver to receive................. taskMark: "
			// + taskWraper);
			receiver.receiveResult(taskWraper, result.getActionException(),
					trackerResult);
		}

	}

	/**
	 * 日志标记
	 * 
	 * @return
	 */
	public abstract String TAG();

	/**
	 * 当任务执行完毕的时候的回调接口 注意，这个方法将在事件线程中调用。<br>
	 * 实际的数据处理延时达到子类完成。<br>
	 * 这个方法只有在ATaskMark.HANDLE_OVER的时候才会调用，子类可以更具实际的<br>
	 * 返回值合理的设置ATaskMark的状态。<br>
	 * 注意:在ui线程调用。
	 * 
	 * @param result
	 */
	public abstract void handleResult(OperateResult result);

	/**
	 * 如果是失败了，那么可能需要对数据进行一定的处理。<br>
	 * 注意:在ui线程调用。
	 */
	public void handleFault(OperateResult result) {

	}

	public Object getTrackerResult() {
		return trackerResult;
	}

	protected Object invokeMethod(Object owner, String methodName, Object[] args)
			throws Exception {
		return Reflection.invokeMethod(owner, methodName, args);
	}
}