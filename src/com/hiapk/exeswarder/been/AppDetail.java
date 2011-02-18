package com.hiapk.exeswarder.been;

public class AppDetail {
	@SuppressWarnings("unused")
	private static final String TAG = "exeswarder.been.AppDetail";

	private long id;
	private int uid;
	private int allow;
	private String packageName;
	private String name;
	private String lastCalledNum;
	private String exesType;

	public AppDetail() {
	}

	public AppDetail(int _id, int uid, int allow, String packageName,
			String name, String lastCalledNum, String exesType) {
		this.id = _id;
		this.uid = uid;
		this.allow = allow;
		this.packageName = packageName;
		this.name = name;
		this.lastCalledNum = lastCalledNum;
		this.exesType = exesType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getAllow() {
		return allow;
	}

	public void setAllow(int allow) {
		this.allow = allow;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastCalledNum() {
		return lastCalledNum;
	}

	public void setLastCalledNum(String lastCalledNum) {
		this.lastCalledNum = lastCalledNum;
	}

	public String getExesType() {
		return exesType;
	}

	public void setExesType(String exesType) {
		this.exesType = exesType;
	}

}
