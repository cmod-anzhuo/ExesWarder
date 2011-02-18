package com.hiapk.exeswarder.task;

import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.service.ActionException;


/**
 * 2010-6-16 操作结果的封装
 * 
 * @author ckcs
 * 
 */
public class OperateResult {

	// 标示执行的一个结果数据。
	private Object resultData;
	// 这个任务是哪个
	private ATaskMark taskMark;
	// 如果执行过程用抛出服务异常那边将被保留
	// 并将任务标记为error
	private ActionException actionException;
	// 附件
	private Object attach;

	/**
	 * @param opMark
	 * @param resultCode
	 * @param resultData
	 */
	public OperateResult(ATaskMark taskMark, Object resultData) {
		this.taskMark = taskMark;
		this.resultData = resultData;
	}

	/**
	 * @param opMark
	 * @param resultCode
	 */
	public OperateResult(ATaskMark taskMark) {
		super();
		this.taskMark = taskMark;
	}

	/**
	 * @return the resultData
	 */
	public Object getResultData() {
		return resultData;
	}

	/**
	 * @param resultData
	 *            the resultData to set
	 */
	public void setResultData(Object resultData) {
		this.resultData = resultData;
	}

	/**
	 * @return the taskMark
	 */
	public ATaskMark getTaskMark() {
		return taskMark;
	}

	/**
	 * @param taskMark
	 *            the taskMark to set
	 */
	public void setTaskMark(ATaskMark taskMark) {
		this.taskMark = taskMark;
	}

	/**
	 * @return the serviceException
	 */
	public ActionException getActionException() {
		return actionException;
	}

	/**
	 * @param serviceException
	 *            the serviceException to set
	 */
	public void setActionException(ActionException actionException) {
		this.actionException = actionException;
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
}
