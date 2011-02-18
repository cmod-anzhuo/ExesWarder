package com.hiapk.exeswarder.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.hiapk.exeswarder.bd.DBHelper;
import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.been.AppLog;
import com.hiapk.exeswarder.been.MyPackageInfo;
import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.main.BaseContext;
import com.hiapk.exeswarder.service.IBaseService;

/**
 * 程序所有服务业务
 * 
 * @author LinLin
 * 
 */
public class ExesWarderService implements IBaseService {
	public static final String TAG = "exeswarder.service.impl.ExesWarderService";

	private DBHelper dbHelper = null;
	private Context context = null;

	public ExesWarderService(Context context) {
		this.context = context;
		dbHelper = DBHelper.getInstance(context);
	}

	@Override
	public AppDetail saveAppDetail(AppDetail appDetail) {
		return dbHelper.insertAppDetail(appDetail);
	}

	@Override
	public AppLog saveAppLog(AppLog appLog) {
		return dbHelper.insertAppLog(appLog);
	}

	@Override
	public ArrayList<AppDetail> getAllAppDetailList() {
		return dbHelper.getAllAppsForList();
	}

	@Override
	public AppDetail getAppDetailForDB(AppDetail appDetail) {
		return dbHelper.getAppDetail(appDetail);
	}

	@Override
	public AppDetail getAppDetailForCache(AppDetail appDetail) {
		// 这个不用去数据库找，直接在内存的list里找就行了。
		// return dbHelper.getAppDetail(appDetail);

		if (appDetail == null) {
			return null;
		}
		ArrayList<AppDetail> appList = BaseContext.getInstance()
				.getAppDetailList();
		for (AppDetail appTmp : appList) {
			if (appDetail.getId() > 0 && (appDetail.getId() == appTmp.getId())) {
				return appTmp;
			}
			if (appDetail.getUid() > 0
					&& (appDetail.getUid() == appTmp.getUid())) {
				return appTmp;
			}

		}

		return appDetail;
	}

	@Override
	public AppLog getAppLogForDB(AppLog appLog) {
		return dbHelper.getAppLog(appLog);
	}

	@Override
	public AppLog getAppLogForCache(AppLog appLog) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<AppLog> getTopAppLogList(int topNum) {
		return dbHelper.getAllLogsForList(topNum);
	}

	@Override
	public int updateAppDetail(AppDetail appDetail) {
		return dbHelper.updateAppDetail(appDetail);
	}

	@Override
	public int updateAppLog(AppLog appLog) {
		return dbHelper.updateAppLog(appLog);
	}

	@Override
	public void delAppDetail(AppDetail appDetail) {
		dbHelper.deleteById(appDetail.getId());
	}

	@Override
	public void clearLog() {
		dbHelper.clearLog();
	}

	@Override
	public ArrayList<MyPackageInfo> checkBadAppList() {
		// 获得系统apk（开始）
		ArrayList<PackageInfo> packageInfoList = new ArrayList<PackageInfo>();
		List<PackageInfo> installPackageInfo = context.getPackageManager()
				.getInstalledPackages(PackageManager.GET_PERMISSIONS);

		for (PackageInfo pkgInfo : installPackageInfo) {
			if ((pkgInfo.applicationInfo.flags & 0x1) != 0) {
				continue;
			}
			packageInfoList.add(pkgInfo);
		}
		// 获得系统apk（结束）

		// 权限验证(第一步),权限扫描
		ArrayList<MyPackageInfo> badAppList = new ArrayList<MyPackageInfo>();
		for (PackageInfo pkgInfo : packageInfoList) {
			// 扫权限
			MyPackageInfo myPckInfo = checkSendSMSPermission(pkgInfo);

			myPckInfo = checkInstalledBadCodeForAPK(myPckInfo);

			if (myPckInfo.getBadCode() != null
					|| myPckInfo.getFilterPermission() != null) {
				badAppList.add(myPckInfo);
			}

			// if(checkSendSMSPermission(pkgInfo) > 0){
			// MyPackageInfo myPckInfo= new MyPackageInfo(pkgInfo);
			// badAppList.add(myPckInfo);
			// }

			// 扫权限 + 扫文件
			// if(checkSendSMSPermission(packageInfoList.get(i)) > 0 ||
			// checkInstalledBadCodeForAPK(packageInfoList.get(i)) > 0){
			// badAppList.add(packageInfoList.get(i));
			// }
		}

		return badAppList;
	}

	/**
	 * 验证apk是不是 吸费软件（开始）,APK源文件扫描
	 * 
	 * @param packageInfo
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "finally" })
	private MyPackageInfo checkInstalledBadCodeForAPK(MyPackageInfo myPckInfo) {
		File file = new File(
				myPckInfo.getPackageInfo().applicationInfo.sourceDir);
		BufferedInputStream is = null;
		ZipFile zipFile = null;
		ZipEntry entry = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration e = zipFile.entries();
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				if (!"classes.dex".equals(entry.getName())) {
					continue;
				}
				is = new BufferedInputStream(zipFile.getInputStream(entry));

				byte data[] = new byte[2048];
				String bufferStr = "";
				while ((is.read(data, 0, data.length)) != -1) {
					String strTmp = new String(data);
					String totalStr = bufferStr + strTmp; // 这样防止漏掉

					String[] badCodes = { "kl4ofgsmgeje5gko99s1fc2ofm",
							"TANCActivity", "3lgoagdmfejekgfos9t15chojm" };
					ArrayList<String> badCodeList = new ArrayList<String>();
					for (String badCode : badCodes) {
						int flag = totalStr.indexOf(badCode);
						if (flag > 0) {
							badCodeList.add(badCode);
						}
					}

					if (badCodeList.size() > 0) {
						myPckInfo
								.setBadCode(badCodeList.toArray(new String[0]));
					}

					bufferStr = strTmp;
				}
			}

		} catch (Exception e) {
			LogUtil.e(TAG, myPckInfo.getPackageInfo().packageName
					+ "checkInstalledBadCodeForAPK() error~");
		} finally {
			return myPckInfo;
		}

	}

	// 判断发短信（权限）
	@SuppressWarnings({ "unused", "unchecked", "finally" })
	private int checkSendSMSPermissionForFile(PackageInfo packageInfo) {
		int num = 0;
		File file = new File(packageInfo.applicationInfo.sourceDir);
		BufferedInputStream is = null;
		ZipFile zipFile = null;
		ZipEntry entry = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration e = zipFile.entries();
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				if (!"AndroidManifest.xml".equalsIgnoreCase(entry.getName())) {
					continue;
				}
				LogUtil.e(TAG, entry.getName());
				is = new BufferedInputStream(zipFile.getInputStream(entry));
				int count;
				byte data[] = new byte[2048];
				String bufferStr = "";
				while ((count = is.read(data, 0, data.length)) != -1) {
					String strTmp = new String(data);
					String totalStr = bufferStr + strTmp; // 这样防止漏掉
					int flag = -1;
					// flag = totalStr.indexOf(permission.SEND_SMS);
					LogUtil.e(TAG, totalStr);
					flag = totalStr
							.indexOf("a.n.d.r.o.i.d..p.e.r.m.i.s.s.i.o.n..S.E.N.D._.S.M.S");

					if (flag > 0) {
						num++;
						break;
					}

					bufferStr = strTmp;
				}

			}

		} catch (Exception e) {
			LogUtil.e(TAG, packageInfo.packageName
					+ "checkInstalledBadCodeForAPK() error~");
		} finally {
			return num;
		}
	}

	// 判断发短信（权限）
	private MyPackageInfo checkSendSMSPermission(PackageInfo pckInfo) {
		MyPackageInfo myPckInfo = new MyPackageInfo(pckInfo);
		String[] permissionArray = pckInfo.requestedPermissions;
		if (permissionArray == null) {
			return myPckInfo;
		}

		ArrayList<String> permissionList = new ArrayList<String>();
		for (String permissionString : permissionArray) {
			// 判断有没有发短信的权限，有就++
			if (permission.SEND_SMS.equalsIgnoreCase(permissionString)) {
				permissionList.add(permissionString);
			}

		}

		if (permissionList.size() != 0) {
			myPckInfo
					.setFilterPermission(permissionList.toArray(new String[0]));
		}

		return myPckInfo;
	}

}
