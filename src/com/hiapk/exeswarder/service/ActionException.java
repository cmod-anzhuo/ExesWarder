package com.hiapk.exeswarder.service;

/**
 * 2010-6-23<br>
 * 服务过程中的行为异常
 * 
 * @author ckcs
 * 
 */
@SuppressWarnings("serial")
public class ActionException extends Exception {

	// / <summary>
	// / 参数传递异常
	// / </summary>
	public static final int PARAM_ERROR = 1;

	// / <summary>
	// / 数据库操作异常
	// / </summary>
	public static final int SQL_ERROR = 2;

	// / <summary>
	// / 参数传递格式不对，如：搜索内容有特殊符号
	// / </summary>
	public static final int PARAM_FORMAT_ERROR = 3;

	// / <summary>
	// / 用户会话异常
	// / </summary>
	public static final int SESSION_ERROR = 4;

	// / <summary>
	// / 操作异常
	// / </summary>
	public static final int OPERATE_ERROR = 5;

	// / <summary>
	// / 注册异常
	// / </summary>
	public static final int REGISTE_ERROR = 6;

	// / <summary>
	// / 登入异常
	// / </summary>
	public static final int LOGIN_ERROR = 7;

	// / <summary>
	// / 外围环境因素造成的异常（供监控系统监控使用）
	// / </summary>
	public static final int ENVIROMENT_ERROR = 8;

	// / <summary>
	// / 系统提醒
	// / </summary>
	public static final int SYSTEM_WARN = 9;

	// 网络异常
	public static final int NETWORK_ERROR = 20;

	// 异常代号
	private int exCode;

	// 异常信息
	private String exMessage;

	/**
	 * @param exCode
	 */
	public ActionException() {
		super();
	}

	/**
	 * @param exCode
	 */
	public ActionException(int exCode) {
		super();
		this.exCode = exCode;
	}

	/**
	 * @param exCode
	 */
	public ActionException(int exCode, String message) {
		super();
		this.exCode = exCode;
		this.exMessage = message;
	}

	/**
	 * @return the exMessage
	 */
	public String getExMessage() {
		return exMessage;
	}

	/**
	 * @param exMessage
	 *            the exMessage to set
	 */
	public void setExMessage(String exMessage) {
		this.exMessage = exMessage;
	}

	/**
	 * @return the exCode
	 */
	public int getExCode() {
		return exCode;
	}

	/**
	 * @param exCode
	 *            the exCode to set
	 */
	public void setExCode(int exCode) {
		this.exCode = exCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServiceException [exCode=" + exCode + ", exMessage="
				+ exMessage + "]";
	}

}
