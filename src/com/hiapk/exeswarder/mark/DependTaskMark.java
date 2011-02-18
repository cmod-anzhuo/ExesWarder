package com.hiapk.exeswarder.mark;

/**
 * 2010-8-25<br>
 * 这个一个可能需要依赖别人的认为<br>
 * 如果查找依赖的任务，那个AInvokeTracker的handleInvoikePrepare<br>
 * 需要做相应的处理比如等待。
 * 
 * @author ckcs
 * 
 */
public class DependTaskMark extends APageTaskMark {

	// 所依赖的任务
	private ATaskMark dependTask;

	/**
	 * @return the dependTask
	 */
	public ATaskMark getDependTask() {
		return dependTask;
	}

	/**
	 * @param dependTask
	 *            the dependTask to set
	 */
	public void setDependTask(ATaskMark dependTask) {
		this.dependTask = dependTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DependTaskMark [dependTask=" + dependTask + ", toString()=" + super.toString()
				+ "]";
	}

}