package com.hiapk.exeswarder.task;

import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.service.ActionException;



/**
 * 它的作用范围在视图层，更具执行结果进行合理的现实。<br>
 * 它假设tracker已经合理处理数据了， 视图层的数据全部由缓存模块进行统一处理。
 * 
 * @author ckcs
 * 
 */
public interface IResultReceiver {

	/**
	 * 当任务执行完毕的时候的回调接口 注意，这个方法将在事件线程中调用。
	 * 
	 * @param taskMark
	 *            任务标记
	 * @param exception
	 *            这个只在处理中发生错误，包括实际方法抛出异常的时候才有值，否则为null,<br>
	 *            所以先下判断ATaskMark.HANDLE_ERROR
	 * @param trackerResult
	 *            可以有的一个跟踪结果
	 */
	public void receiveResult(ATaskMark taskMark, ActionException exception,
			Object trackerResult);

}