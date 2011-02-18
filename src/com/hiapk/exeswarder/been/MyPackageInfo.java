package com.hiapk.exeswarder.been;

import android.content.pm.PackageInfo;

public class MyPackageInfo {
	//被命中的不合格的访问权限
	private String[] filterPermission = null;
	//有被命中的不合格的badCode
	private String[] badCode = null;
	//一个包的info
	private PackageInfo packageInfo= null;
	
	public MyPackageInfo(){}
	
	public MyPackageInfo(PackageInfo packageInfo){
		this.packageInfo = packageInfo;
	}
	
	public String[] getFilterPermission() {
		return filterPermission;
	}
	public void setFilterPermission(String[] filterPermission) {
		this.filterPermission = filterPermission;
	}
	public String[] getBadCode() {
		return badCode;
	}
	public void setBadCode(String[] badCode) {
		this.badCode = badCode;
	}
	public PackageInfo getPackageInfo() {
		return packageInfo;
	}
	public void setPackageInfo(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}
	
	
	
}
