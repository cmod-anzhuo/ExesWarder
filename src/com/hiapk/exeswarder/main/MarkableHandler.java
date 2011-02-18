package com.hiapk.exeswarder.main;

import android.os.Handler;

/**
 * 2010-7-23<br>
 * 可以对消息处理类型进行标记的处理器
 * 
 * @author LinLin
 */
abstract class MarkableHandler extends Handler {

	// 处理器标识
	private int messageMark;
	// 是否处理全部
	private boolean handleAll;

	/**
	 * @param handlerMark
	 */
	public MarkableHandler(int messageMark) {
		super();
		this.messageMark = messageMark;
	}

	/**
	 * @param handlerMark
	 */
	public MarkableHandler(int messageMark, boolean handleAll) {
		super();
		this.messageMark = messageMark;
		this.handleAll = handleAll;
	}

	/**
	 * @return the handlerMark
	 */
	public int getMssageMark() {
		return messageMark;
	}

	/**
	 * @return the handleAll
	 */
	public boolean isHandleAll() {
		return handleAll;
	}

	/**
	 * 如果这个一个不可直接标记的消息，<br>
	 * 那么MarketContent在吧消息转发前会尝试设置 一个默认的标记。
	 * 
	 * @return
	 */
	public int getDefaultMessageMark() {
		return messageMark;
	}

}
