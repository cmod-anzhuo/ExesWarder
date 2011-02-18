package com.hiapk.exeswarder.mark;

import java.util.ArrayList;
import java.util.List;

/**
 * 2010-7-24<br>
 * 这是一个可以包含多个子任务的的任务标记
 * 
 * @author ckcs
 * 
 */
public class MultipleTaskMark extends ATaskMark {

	// 子任务列表
	private List<ATaskMark> taskMarkList = new ArrayList<ATaskMark>();

	/**
	 * @param appId
	 * @param type
	 */
	public MultipleTaskMark() {
		super();
	}

	/**
	 * 添加子任务
	 * 
	 * @param taskMark
	 */
	public void addSubTaskMark(ATaskMark taskMark) {
		taskMarkList.add(taskMark);
	}

	/**
	 * @return the taskMarkList
	 */
	public List<ATaskMark> getTaskMarkList() {
		return taskMarkList;
	}

	/**
	 * 将任务处理为无效
	 */
	public void invalidTaskMark() {
		taskMarkList.clear();
	}

	/**
	 * 获得下一个任务
	 * 
	 * @param 返回下一个将要执行的任务
	 *            ，如果返回null表示任务结束
	 */
	public ATaskMark pickNextTaskMark() {
		if (taskMarkList.size() > 0) {
			return taskMarkList.remove(0);
		} else {
			return null;
		}
	}

}