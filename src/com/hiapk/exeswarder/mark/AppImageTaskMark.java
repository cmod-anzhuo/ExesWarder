package com.hiapk.exeswarder.mark;

/**
 * 2010-7-25
 * 
 * @author ckcs
 * 
 */
public class AppImageTaskMark extends ATaskMark {

	public static final int APP_SCREENSHOT = 0;
	public static final int APP_ICON = 1;
	public static final int APP_ADVERTISE_ICON = 2;
	// 是截图还是图标
	private int type;
	private int appId;
	private String url;

	/**
	 * @param appId
	 * @param url
	 */
	public AppImageTaskMark(int appId, String url, int type) {
		super();
		this.appId = appId;
		this.url = url;
		this.type = type;
	}

	/**
	 * @return the appId
	 */
	public int getAppId() {
		return appId;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + appId;
		result = prime * result + type;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppImageTaskMark other = (AppImageTaskMark) obj;
		if (appId != other.appId)
			return false;
		if (type != other.type)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AppImageTaskMark [appId=" + appId + ", type=" + type + ", url=" + url
				+ " super.toString()" + super.toString() + "]";
	}

}
