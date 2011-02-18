package com.hiapk.exeswarder.task;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.service.ActionException;
import com.hiapk.exeswarder.task.RunAsyncTask.Status;
import com.hiapk.exeswarder.task.tracher.AInvokeTracker;

import android.util.Log;

/**
 * 一个异步的操作 2010-6-16 可以调用asyncTask的get方法外部自己决定同步的操作。
 * 
 * @author ckcs
 * 
 */
@SuppressWarnings("unchecked")
public final class AsyncOperation {

	public static final String TAG = "AsyncOperation";

	// 记录当前的任务列表，以便统一处理
	// 当执行注销的时候有必要停止和取消之当前提交的任务。
	private static Hashtable<ATaskMark, AsyncOperation> taskRecordMap = new Hashtable<ATaskMark, AsyncOperation>();

	// 具体任务执行方法
	// 安全考虑：弱应用
	private Method method;
	// 调用跟踪，用于处理成功或失败的结果。
	private AInvokeTracker invokeTracker;
	// 是否已经发生错误了
	private boolean isError;
	// 异步任务
	private RunAsyncTask asyncTask;
	// 当前任务的标示
	private ATaskMark taskMark;
	// 可能服务异常
	private ActionException actionException;
	// 附件,用于辅助数据处理
	private Object attach;

	public AsyncOperation(ATaskMark taskMark, Method method) {
		this.method = method;
		this.taskMark = taskMark;
		// 以流程开始立即标记为开始加载。
		taskMark.setTaskStatus(ATaskMark.HANDLE_DOING);
	}

	public AInvokeTracker getInvokeTracker() {
		return invokeTracker;
	}

	public void setInvokeTracker(AInvokeTracker invokeTracker) {
		this.invokeTracker = invokeTracker;
	}

	/**
	 * @return the attach
	 */
	public Object getAttach() {
		return attach;
	}

	/**
	 * @param attach
	 *            the attach to set
	 */
	public void setAttach(Object attach) {
		this.attach = attach;
	}

	/**
	 * 执行一个异步任务
	 * 
	 * @param service
	 *            方法所在对象
	 * @param args
	 *            方法参数
	 */
	public void excuteOperate(final Object service, final Object... args) {
		asyncTask = new RunAsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				LogUtil.iop(TAG, "task begin execute................. taskMark " + taskMark);
				Object result = null;
				try {
					if (method != null) {
						result = method.invoke(service, args);
					}

				} catch (Exception e) {
					e.printStackTrace();
					// 标记错误
					isError = true;
					// 返回服务异常，以便tracker对异常类型进行特定处理。
					if (e.getCause() instanceof ActionException) {
						actionException = (ActionException) e.getCause();
						Log.w(TAG, "actionException " + actionException);

					} else if (e instanceof ActionException) {
						actionException = (ActionException) e;
					}
				}

				// 退出前先调用预处理
				if (invokeTracker != null && !isCancelled()) {
					LogUtil.iop(TAG, "task Prepare................. taskMark " + taskMark);
					try {
						invokeTracker.handleInvoikePrepare(taskMark);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return result;
			}

			@Override
			protected void onPostExecute(Object result) {
				LogUtil.iop(TAG, "task do over................. taskMark " + taskMark);
				// 取消的任务或者invokeTracker==null不需要在处理
				if (invokeTracker != null && !isCancelled()) {
					LogUtil.iop(TAG, "callback task................. taskMark: " + taskMark);
					// 以流程结束
					if (isError) {
						taskMark.setTaskStatus(ATaskMark.HANDLE_ERROR);
					} else {
						taskMark.setTaskStatus(ATaskMark.HANDLE_OVER);
					}
					OperateResult operateResult = new OperateResult(taskMark);
					operateResult.setResultData(result);
					operateResult.setActionException(actionException);
					operateResult.setAttach(attach);
					try {
						invokeTracker.handleInvokeOver(operateResult);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					try {
						invokeTracker.handleInvokeFinalize(taskMark);
					} catch (Exception e) {
						e.printStackTrace();
					}

					// 对应已经没有人认领的任务将直接终止
				} else {
					LogUtil.iop(TAG,
							"ingore task (invokeTracker == null)................. taskMark: "
									+ taskMark);
					if (taskMark != null) {
						taskMark.setTaskStatus(ATaskMark.HANDLE_OVER);
					}
				}

				// 移除任务
				if (taskMark != null) {
					taskRecordMap.remove(taskMark);
				}

				LogUtil.iop(TAG, "remove add now task count: " + taskRecordMap.size()
						+ " isError: " + isError);
				LogUtil.iop(TAG, "excuteOperate method: "
						+ (method == null ? "" : method.getName()) + " isError: " + isError);

				// 清除引用
				taskMark = null;
				invokeTracker = null;
				actionException = null;
				method = null;
			}
		};
		asyncTask.execute();
		// 记录任务
		taskRecordMap.put(taskMark, this);
		// LogUtil.i(TAG, "add now task count: " + taskRecordMap.size());
	}

	/**
	 * @param mayInterruptIfRunning
	 * @return
	 * @see RunAsyncTask#cancel(boolean)
	 */
	public final boolean clearAsysnTask(boolean mayInterruptIfRunning) {
		// 先移除任务
		taskRecordMap.remove(taskMark);
		// 取消任务
		boolean ok = doCancle(mayInterruptIfRunning);
		return ok;
	}

	private final boolean doCancle(boolean mayInterruptIfRunning) {
		// 取消任务
		boolean ok = asyncTask.cancel(mayInterruptIfRunning);

		// 具体任务执行方法
		method = null;
		// 调用跟踪，用于解释成功或失败的结果。
		invokeTracker = null;
		// 额外数
		taskMark.setTaskStatus(ATaskMark.HANDLE_OVER);
		taskMark = null;
		// 异常
		actionException = null;
		// 附件
		attach = null;

		return ok;
	}

	/**
	 * 停止所有的异步操作
	 */
	static void stopAllAsyncOperate() {
		for (AsyncOperation asyncOp : taskRecordMap.values()) {
			// 对多任务的，必须重置，以便能够处理后续的任务
			if (asyncOp.invokeTracker != null
					&& asyncOp.invokeTracker.getResultReceiver() instanceof MultipleTaskScheduler) {
				MultipleTaskScheduler scheduler = (MultipleTaskScheduler) asyncOp.invokeTracker
						.getResultReceiver();
				scheduler.resetScheduler();
			}

			asyncOp.doCancle(true);
		}
		taskRecordMap.clear();
	}

	/**
	 * 代理方法的调用必须咋执行execute之后
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @see RunAsyncTask#get()
	 */
	public final Object get() throws InterruptedException, ExecutionException {
		return asyncTask.get();
	}

	/**
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @see RunAsyncTask#get(long, java.util.concurrent.TimeUnit)
	 */
	public final Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return asyncTask.get(timeout, unit);
	}

	/**
	 * @return
	 * @see RunAsyncTask#getStatus()
	 */
	public final Status getStatus() {
		return asyncTask.getStatus();
	}

	/**
	 * @return
	 * @see RunAsyncTask#isCancelled()
	 */
	public final boolean isCancelled() {
		return asyncTask.isCancelled();
	}

	// 是否已经存在同类的任务了
	static boolean isTaskExist(ATaskMark taskMark) {
		AsyncOperation asyncOperation = taskRecordMap.get(taskMark);
		if (asyncOperation != null) {
			LogUtil.iop(TAG, "check +++++++++task exist: " + taskMark);
			return true;
		} else {
			LogUtil.iop(TAG, "check +++++++++task not exist: " + taskMark);
			return false;
		}
	}

	/**
	 * 获得指定任务
	 * 
	 * @param taskMark
	 *            任务标记
	 */
	static AsyncOperation getTaskByMark(ATaskMark taskMark) {
		return taskRecordMap.get(taskMark);
	}

	/**
	 * 异步的任务
	 * 
	 * @return
	 */
	static Collection<AsyncOperation> asyncOperations() {
		return taskRecordMap.values();
	}

}
