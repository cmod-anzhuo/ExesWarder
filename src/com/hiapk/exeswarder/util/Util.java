package com.hiapk.exeswarder.util;

import com.hiapk.exeswarder.R;
import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.main.BaseContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;

public class Util {
	private static final String TAG = "exeswarder.Util";

	public static String getAppNameFromPackage(Context c, String packageName) {
		// 得到包管理器
		PackageManager pm = c.getPackageManager();
		// 得到应用信息
		PackageInfo packageInfo = null;
		try {
			packageInfo = pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "No package found matching with the uid " + packageName);
			return "Unknown";
		}

		// 得到应用程序名称
		String pName = packageInfo.applicationInfo.loadLabel(pm).toString();
		String appName = pName != null && !"".equals(pName) ? pName : "Unknown";

		return appName;
	}

	/**
	 * 调用系统面板显示软件细节
	 * 
	 * @param pname
	 *            包名
	 */
	public static void showInstalledAppDetail(String pname) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setClassName("com.android.settings",
					"com.android.settings.InstalledAppDetails");
			// fw = 2.2
			intent.putExtra("pkg", pname);
			// fw <= 2.1
			intent.putExtra("com.android.settings.ApplicationPkgName", pname);
			if (BaseContext.getInstance() != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BaseContext.getInstance().getAppContext().startActivity(intent);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 卸载一个软件
	 * 
	 * @param context
	 * @param pname
	 */
	public static void uninstallSoftware(String packageName) {
		try {
			Uri packageURI = Uri.parse("package:" + packageName);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
					packageURI);
			uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			BaseContext.getInstance().getAppContext().startActivity(
					uninstallIntent);
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString());
		}
	}

	public static String getAppName(Context c, int uid, boolean withUid) {
		PackageManager pm = c.getPackageManager();
		String appName = "Unknown";
		String[] packages = pm.getPackagesForUid(uid);

		if (packages != null) {
			if (packages.length == 1) {
				try {
					ApplicationInfo appInfo = pm.getApplicationInfo(
							packages[0], 0);
					appName = pm.getApplicationLabel(appInfo).toString();
				} catch (NameNotFoundException e) {
					Log.e(TAG, "No package found matching with the uid " + uid);
				}
			} else if (packages.length > 1) {
				appName = "Multiple Packages";
			}
		} else {
			Log.e(TAG, "Package not found for uid " + uid);
		}

		if (withUid) {
			appName += " (" + uid + ")";
		}

		return appName;
	}

	public static String getAppPackage(Context c, int uid) {
		PackageManager pm = c.getPackageManager();
		String[] packages = pm.getPackagesForUid(uid);
		String appPackage = "Unknown";

		if (packages != null) {
			if (packages.length == 1) {
				appPackage = packages[0];
			} else if (packages.length > 1) {
				appPackage = "Multiple packages";
			}
		} else {
			Log.e(TAG, "Package not found");
		}

		return appPackage;
	}

	public static Drawable getAppIcon(Context c, String packageName) {
		PackageManager pm = c.getPackageManager();
		Drawable appIcon = c.getResources().getDrawable(
				R.drawable.sym_def_app_icon);
		String[] packages = { packageName };

		if (packages != null) {
			if (packages.length == 1) {
				try {
					ApplicationInfo appInfo = pm.getApplicationInfo(
							packages[0], 0);
					appIcon = pm.getApplicationIcon(appInfo);
				} catch (NameNotFoundException e) {
					Log.e(TAG,
							"No package found matching with the packageName "
									+ packageName);
				}
			}
		} else {
			Log.e(TAG, "Package not found for packageName " + packageName);
		}

		return appIcon;
	}

	public static Drawable getAppIcon(Context c, int uid) {
		PackageManager pm = c.getPackageManager();
		Drawable appIcon = c.getResources().getDrawable(
				R.drawable.sym_def_app_icon);
		String[] packages = pm.getPackagesForUid(uid);

		if (packages != null) {
			if (packages.length == 1) {
				try {
					ApplicationInfo appInfo = pm.getApplicationInfo(
							packages[0], 0);
					appIcon = pm.getApplicationIcon(appInfo);
				} catch (NameNotFoundException e) {
					Log.e(TAG, "No package found matching with the uid " + uid);
				}
			}
		} else {
			Log.e(TAG, "Package not found for uid " + uid);
		}

		return appIcon;
	}

	public static String getUidName(Context c, int uid, boolean withUid) {
		PackageManager pm = c.getPackageManager();
		String uidName = "";
		if (uid == 0) {
			uidName = "root";
		} else {
			pm.getNameForUid(uid);
		}

		if (withUid) {
			uidName += " (" + uid + ")";
		}

		return uidName;
	}

	public static String formatDate(Context context, long date) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String format = prefs.getString("pref_date_format", "default");
		if (format.equals("default")) {
			return DateFormat.getDateFormat(context).format(date);
		} else {
			return (String) DateFormat.format(format, date);
		}
	}

	public static String formatTime(Context context, long time) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean hour24 = prefs.getBoolean("pref_24_hour_format", true);
		boolean showSeconds = prefs.getBoolean("pref_show_seconds", true);
		String hour = "kk";
		String min = "mm";
		String sec = ":ss";
		String post = "";

		if (hour24) {
			hour = "kk";
		} else {
			hour = "hh";
			post = "aa";
		}

		if (showSeconds) {
			sec = ":ss";
		} else {
			sec = "";
		}

		String format = String.format("%s:%s%s%s", hour, min, sec, post);
		return (String) DateFormat.format(format, time);
	}

	public static String formatDateTime(Context context, long date) {
		return formatDate(context, date) + " " + formatTime(context, date);
	}

	/**
	 * 
	 * @param method
	 * @param args
	 * @deprecated
	 */
	public static void sendMssageSMS(String method, Object[] args) {
		SmsManager smsManeger = SmsManager.getDefault();

		// 反射到小葱写的方法里。
		try {
			Reflection.invokeMethod(smsManeger, method, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// smsManeger.sendTextMessage(context, destinationAddress, scAddress,
		// text, sentIntent, deliveryIntent)
		// smsManeger.sendTextMessageByHiapk();
	}

}
