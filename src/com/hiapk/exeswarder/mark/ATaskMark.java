package com.hiapk.exeswarder.mark;

/**
 * 2010-12-23 <br>
 * 在系统中同种类型的任务在执行时必须是唯一的。<br>
 * 规则：所有的taskmark使用equal 和 hashcode 进行唯一性比较<br>
 * 所以子类必须根据合理的表示自定义实现上述两个方法。equal必须比较class类型<br>
 * 如果class不等则任务不同。
 * 
 */
public abstract class ATaskMark {

	// 没有加载或者上此已经成功加载结束
	// 对应http ok: 200
	public static final int HANDLE_OVER = 0;
	// 加载中
	public static final int HANDLE_DOING = 1;
	// 处理错误
	// 当发生错误的时候具体的错误信息封装在对应的ServiceException中。
	public static final int HANDLE_ERROR = 2;
	// 等待加载
	public static final int HANDLE_WAIT = 3;
	// 初始为没价值或加载结束
	protected int taskStatus = HANDLE_OVER;

	// 用于特殊的唯一型任务
	public static final int UNIQUE = -494949;

	/**
	 * @return the loadStatus
	 */
	public int getTaskStatus() {
		return taskStatus;
	}

	/**
	 * @param taskStatus
	 *            the loadStatus to set
	 */
	public void setTaskStatus(int taskStatus) {
		this.taskStatus = taskStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ATaskMark [taskStatus=" + taskStatus + "]";
	}

}