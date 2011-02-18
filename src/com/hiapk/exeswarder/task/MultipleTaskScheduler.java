package com.hiapk.exeswarder.task;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.mark.MultipleTaskMark;
import com.hiapk.exeswarder.service.ActionException;
import com.hiapk.exeswarder.task.tracher.AInvokeTracker;

/**
 * 2010-7-24 <br>
 * 多任务调度者，用于辅助ServiceWraper<br>
 * 目前暂定任务同时执行三个
 * 
 * @author ckcs
 * 
 */
public abstract class MultipleTaskScheduler implements IResultReceiver {

	// 多任务封装
	protected MultipleTaskMark multipleTaskMark;
	protected ServiceWraper serviceWraper;
	// 每次同时执行数
	public static final int TASK_WROKER_COUNT = 2;
	// 结果跟踪，用于解释成功或失败的结果。
	// 安全考虑：弱应用
	private WeakReference<IResultReceiver> weakReceiver;

	// 当前执行的任务
	protected HashMap<ATaskMark, AInvokeTracker> schedulingMap = new HashMap<ATaskMark, AInvokeTracker>();

	/**
	 * @param multipleTaskMark
	 */
	public MultipleTaskScheduler(ServiceWraper service, MultipleTaskMark multipleTaskMark) {
		this.serviceWraper = service;
		this.multipleTaskMark = multipleTaskMark;
	}

	/**
	 * 合并两个任务，因为同个任务可能只有部分被执行了，所有如果后续<br>
	 * 要求执行相同的任务是应该合并。
	 * 
	 * @param mTaskMark
	 */
	public void mergeTaskSchedul(MultipleTaskMark mTaskMark) {
		LogUtil.e("MultipleTaskScheduler", "mergeTaskSchedul");
		List<ATaskMark> taskMarkList = mTaskMark.getTaskMarkList();
		// LogUtil.v(TAG(), "merge task: before -->/nadd task size: "
		// + taskMarkList.size());
		// 移除所有已经在执行的任务并丢弃旧的后续任务
		taskMarkList.removeAll(schedulingMap.keySet());
		multipleTaskMark = mTaskMark;
		// LogUtil.v(TAG(), "merge task: scheduling size: "
		// + schedulingMap.keySet().size() + " new add size: "
		// + taskMarkList.size());
	}

	/**
	 * 触发执行任务，受限制于TASK_WROKER_COUNT。
	 */
	public void triggerSchedulTask() {
		if (schedulingMap.size() < TASK_WROKER_COUNT) {
			int canCount = TASK_WROKER_COUNT - schedulingMap.size();
			for (int index = 0; index < canCount; index++) {
				ATaskMark nextTaskMark = multipleTaskMark.pickNextTaskMark();
				if (nextTaskMark != null) {
					AInvokeTracker tracker = handleExecuteNextTask(nextTaskMark, this);
					schedulingMap.put(nextTaskMark, tracker);
					// LogUtil.v(TAG(), "schedul next task mark: " +
					// nextTaskMark);
				}
			}
		}
	}

	/**
	 * @return the multipleTaskMark
	 */
	public MultipleTaskMark getMultipleTaskMark() {
		return multipleTaskMark;
	}

	/**
	 * @param multipleTaskMark
	 *            the multipleTaskMark to set
	 */
	public void setMultipleTaskMark(MultipleTaskMark multipleTaskMark) {
		this.multipleTaskMark = multipleTaskMark;
	}

	/**
	 * 添加图片接收者, 已经存在则不同添加
	 * 
	 * @param receiver
	 *            the receiver to add
	 */
	public void setReceiver(IResultReceiver receiver) {
		weakReceiver = new WeakReference<IResultReceiver>(receiver);
	}

	/**
	 * 子类完成实际的任务
	 * 
	 * @param taskMark
	 *            任务标记
	 * @param tracker
	 *            任务跟踪者
	 */
	protected abstract AInvokeTracker handleExecuteNextTask(ATaskMark taskMark,
			IResultReceiver receiver);

	public abstract String TAG();

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		// LogUtil.v(TAG(), "before --> receiver size" + receiveList.size()
		// + "\nscheduling size: " + schedulingMap.size()
		// + "\ntask schedul over: " + taskMark + "\nrest task count:"
		// + multipleTaskMark.getTaskMarkList().size());

		IResultReceiver receiver = null;
		if (weakReceiver != null) {
			receiver = weakReceiver.get();
			if (receiver != null) {
				try {
					receiver.receiveResult(taskMark, exception, trackerResult);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// 移除记录
		schedulingMap.remove(taskMark);
		// 没有接收者则不再调度剩余的任务
		if (receiver != null) {
			triggerSchedulTask();

		} else if (multipleTaskMark != null) {
			multipleTaskMark.invalidTaskMark();
			schedulingMap.clear();
		}

		// 检查是否已经没有任务
		if (schedulingMap.size() == 0 && multipleTaskMark != null
				&& multipleTaskMark.getTaskMarkList().size() == 0) {
			weakReceiver = null;
		}

		// LogUtil.v(TAG(), "after --> scheduling weakReceiver: " + weakReceiver
		// + "size: " + schedulingMap.size() + "\ntask schedul over: "
		// + taskMark + "\nrest task count:"
		// + multipleTaskMark.getTaskMarkList().size());
	}

	/**
	 * 重置调度者,以便它像新的一样。
	 */
	public void resetScheduler() {
		if (multipleTaskMark != null) {
			multipleTaskMark.invalidTaskMark();
			multipleTaskMark = null;
		}

		weakReceiver = null;
		schedulingMap.clear();
	}
}
