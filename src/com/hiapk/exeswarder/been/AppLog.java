package com.hiapk.exeswarder.been;

public class AppLog {
	@SuppressWarnings("unused")
	private static final String TAG = "exeswarder.been.AppLog";

	private long _id;
	private long appId;
	private long date;
	private int allow;
	private String content;
	private String phoneNum;

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		_id = id;
	}

	public long getAppId() {
		return appId;
	}

	public void setAppId(long appId) {
		this.appId = appId;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getAllow() {
		return allow;
	}

	public void setAllow(int allow) {
		this.allow = allow;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

}
