package com.hiapk.exeswarder.bd;

import java.util.ArrayList;

import com.hiapk.exeswarder.been.AppDetail;
import com.hiapk.exeswarder.been.AppLog;
import com.hiapk.exeswarder.log.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库DB操作类
 * 
 * @author LinLin
 */
public class DBHelper {
	public static final String TAG = "exeswarder.db.DBHelper";

	// 本类的单态模式
	private static DBHelper dbHelper = null;

	// 数据库名称
	private static final String DATABASE_NAME = "exeswarder.sqlite";
	// 数据库版本号
	private static final int DATABASE_VERSION = 1;
	// 防火应用程序列表，表名
	private static final String APPS_TABLE = "apps";
	// 防火应用程序日志，表名
	private static final String LOGS_TABLE = "logs";

	/**
	 * 防火应用程序，表里的“字段”
	 * 
	 * @author LinLin
	 */
	public class Apps {
		public static final String ID = "_id"; // id
		public static final String UID = "uid"; // uid应用程序在android系统里的唯一标识
		public static final String PACKAGE = "package"; // 软件包名
		public static final String NAME = "name"; // 软件名称
		public static final String LAST_CALLED_NUM = "last_called_num"; // 最后一次吸费的电话号码
		public static final String ALLOW = "allow"; // 是否允许(1单次允话，2总是允许，3单次禁止，4总是禁止，5超时)
		// 用逗号区分开来,比如:1,2,3代表三种吸费类型
		public static final String EXES_TYPE = "exes_type"; // 是什么类型的吸费，比如：SMS，电话,net等等
	}

	/**
	 * 防火应用程序日志，表里的“字段”
	 * 
	 * @author LinLin
	 */
	public class Logs {
		public static final String ID = "_id";
		public static final String APP_ID = "app_id";
		public static final String DATE = "date";
		public static final String ALLOW = "allow";
		public static final String CONTENT = "content";
		public static final String PHONE_NUM = "phone_num";
	}

	/**
	 * 防火权限的类型（6种）
	 * 
	 * @author LinLin
	 */
	public class AllowType {
		public static final int ASK = 0; // 1询问
		public static final int TIMEOUT = ASK + 1; // 2超时
		public static final int SINGLE_ALLOW = TIMEOUT + 1; // 3单次允许
		public static final int SINGLE_DENY = SINGLE_ALLOW + 1; // 4单次拒绝
		public static final int ALLOW = SINGLE_DENY + 1; // 5总是允许
		public static final int DENY = ALLOW + 1; // 6总是禁止

	}

	@SuppressWarnings("unused")
	private Context mContext;
	private SQLiteDatabase mDB;

	public static DBHelper getInstance(Context context) {
		if (dbHelper == null) {
			dbHelper = new DBHelper(context);
		}
		return dbHelper;
	}

	private DBHelper(Context context) {
		this.mContext = context;
		DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
		this.mDB = dbOpenHelper.getWritableDatabase();
	}

	/**
	 * 清除日志
	 */
	public void clearLog() {
		this.mDB.delete(LOGS_TABLE, null, null);
	}

	/**
	 * 添加一条日志记录
	 * 
	 * @param appLog
	 * @return AppLog
	 */
	public AppLog insertAppLog(AppLog appLog) {
		ContentValues values = new ContentValues();
		values.put(Logs.APP_ID, appLog.getAppId());
		values.put(Logs.DATE, (appLog.getDate() == 0) ? System
				.currentTimeMillis() : appLog.getDate());
		values.put(Logs.ALLOW, appLog.getAllow());
		values.put(Logs.CONTENT, appLog.getContent());
		values.put(Logs.PHONE_NUM, appLog.getPhoneNum());
		long _id = this.mDB.insert(LOGS_TABLE, null, values);
		appLog.setId(_id);
		return appLog;
	}

	/**
	 * 添加一条被拦截的记录
	 * 
	 * @param appDetail
	 * @return AppDetail
	 */
	public AppDetail insertAppDetail(AppDetail appDetail) {
		ContentValues values = new ContentValues();
		values.put(Apps.UID, appDetail.getUid());
		values.put(Apps.PACKAGE, appDetail.getPackageName());
		values.put(Apps.NAME, appDetail.getName());
		values.put(Apps.LAST_CALLED_NUM, appDetail.getLastCalledNum());
		values.put(Apps.ALLOW, appDetail.getAllow());
		values.put(Apps.EXES_TYPE, appDetail.getExesType());

		long id = 0;
		try {
			id = this.mDB.insert(APPS_TABLE, null, values);
			appDetail.setId(id);
		} catch (SQLException e) {
			LogUtil.e(TAG, " insertAppDetail() error ");
		} finally {
			values.clear();
		}
		return appDetail;
	}

	/**
	 * 删除 AppDetail by _id，并且也删除相对应的日志
	 * 
	 * @param _id
	 */
	public void deleteById(long id) {
		Log.d(TAG, "Deleting from logs table where app_id=" + id);
		this.mDB.delete(LOGS_TABLE, "app_id=?", new String[] { Long
				.toString(id) });
		Log.d(TAG, "Deleting from apps table where _id=" + id);
		this.mDB
				.delete(APPS_TABLE, "_id=?", new String[] { Long.toString(id) });
	}

	/**
	 * 删除 AppDetail by UID
	 * 
	 * @param uid
	 */
	public void deleteByUid(int uid) {
		Cursor cursor = this.mDB.query(APPS_TABLE, new String[] { Apps.ID },
				"uid=?", new String[] { Integer.toString(uid) }, null, null,
				null);
		if (cursor.moveToFirst()) {
			Log.d(TAG, "_id found, deleting logs");
			long id = cursor.getLong(cursor.getColumnIndex(Apps.ID));
			this.mDB.delete(LOGS_TABLE, "_id=?", new String[] { Long
					.toString(id) });
		}
		this.mDB.delete(APPS_TABLE, "uid=?", new String[] { Integer
				.toString(uid) });
		cursor.close();
	}

	/**
	 * 返回全部的被过滤的项
	 * 
	 * @return Cursor
	 */
	public Cursor getAllAppsForCursor() {
		Cursor cursor = this.mDB.query(APPS_TABLE, new String[] { Apps.ID,
				Apps.UID, Apps.PACKAGE, Apps.NAME, Apps.ALLOW, Apps.EXES_TYPE,
				Apps.LAST_CALLED_NUM }, null, null, null, null,
				"allow DESC, name ASC");

		if (cursor == null) {
			return null;
		}
		return cursor;
	}

	/**
	 * 返回全部的被过滤的项
	 * 
	 * @return
	 */
	public ArrayList<AppDetail> getAllAppsForList() {
		Cursor cursor = this.mDB.query(APPS_TABLE, new String[] { Apps.ID,
				Apps.UID, Apps.PACKAGE, Apps.NAME, Apps.ALLOW, Apps.EXES_TYPE,
				Apps.LAST_CALLED_NUM }, null, null, null, null,
				"allow DESC, name ASC");

		if (cursor == null) {
			return null;
		}
		ArrayList<AppDetail> appList = new ArrayList<AppDetail>();
		AppDetail app = null;
		if (cursor.isBeforeFirst()) {
			while (cursor.moveToNext()) {
				app = new AppDetail();
				app.setId(cursor.getLong(cursor.getColumnIndex(Apps.ID)));
				app.setUid(cursor.getInt(cursor.getColumnIndex(Apps.UID)));
				app.setAllow(cursor.getInt(cursor.getColumnIndex(Apps.ALLOW)));
				app.setExesType(cursor.getString(cursor
						.getColumnIndex(Apps.EXES_TYPE)));
				app.setName(cursor.getString(cursor.getColumnIndex(Apps.NAME)));
				app.setPackageName(cursor.getString(cursor
						.getColumnIndex(Apps.PACKAGE)));
				app.setLastCalledNum(cursor.getString(cursor
						.getColumnIndex(Apps.LAST_CALLED_NUM)));
				appList.add(app);
			}
		}
		cursor.close();
		return appList;
	}

	/**
	 * 返回全部的日志
	 * 
	 * @return
	 */
	public ArrayList<AppLog> getAllLogsForList(int topNum) {
		Cursor cursor = this.mDB.query(LOGS_TABLE, new String[] { Logs.ID,
				Logs.APP_ID, Logs.CONTENT, Logs.DATE, Logs.PHONE_NUM,
				Logs.ALLOW }, null, null, null, null, "date DESC limit "
				+ topNum);

		if (cursor == null) {
			return null;
		}
		ArrayList<AppLog> logList = new ArrayList<AppLog>();
		AppLog log = null;
		if (cursor.isBeforeFirst()) {
			while (cursor.moveToNext()) {
				log = new AppLog();
				log.setId(cursor.getLong(cursor.getColumnIndex(Logs.ID)));
				log
						.setAppId(cursor.getLong(cursor
								.getColumnIndex(Logs.APP_ID)));
				log.setContent(cursor.getString(cursor
						.getColumnIndex(Logs.CONTENT)));
				log.setDate(cursor.getLong(cursor.getColumnIndex(Logs.DATE)));
				log.setPhoneNum(cursor.getString(cursor
						.getColumnIndex(Logs.PHONE_NUM)));
				log.setAllow(cursor.getInt(cursor.getColumnIndex(Logs.ALLOW)));
				logList.add(log);
			}
		}
		cursor.close();
		return logList;
	}

	/**
	 * 获取“吸费的been”日志一条数据库对象对应项 (按程序类型来分)
	 * 
	 * @param packageName
	 * @param phoneNum
	 * @return AppDetail
	 */
	public AppLog getAppLog(AppLog appLog) {
		String queryStr = "SELECT logs._id AS _id,logs.app_id AS app_id,logs.date AS date,"
				+ "logs.allow AS allow,logs.content AS content,logs.phone_num AS phone_num "
				+ "FROM logs WHERE 1=1 ";

		ArrayList<String> selectionArgs = new ArrayList<String>();
		if (appLog.getId() > 0) {
			queryStr = queryStr + " and logs._id=? ";
			selectionArgs.add(String.valueOf(appLog.getId()));
		}
		queryStr = queryStr + "ORDER BY logs._id DESC ";

		Cursor cursor = this.mDB.rawQuery(queryStr, (String[]) selectionArgs
				.toArray());

		if (cursor.moveToFirst()) {
			appLog.setId(cursor.getInt(cursor.getColumnIndex(Logs.ID)));
			appLog.setAllow(cursor.getInt(cursor.getColumnIndex(Logs.ALLOW)));
			appLog.setAppId(cursor.getInt(cursor.getColumnIndex(Logs.APP_ID)));
			appLog.setContent(cursor.getString(cursor
					.getColumnIndex(Logs.CONTENT)));
			appLog.setDate(cursor.getInt(cursor.getColumnIndex(Logs.DATE)));
			appLog.setPhoneNum(cursor.getString(cursor
					.getColumnIndex(Logs.PHONE_NUM)));
		}
		cursor.close();
		return appLog;
	}

	/**
	 * 获取“吸费的been”一条数据库对象对应项 (按程序类型来分)
	 * 
	 * @param packageName
	 * @param phoneNum
	 * @return AppDetail
	 */
	public Cursor getAppDetailForCursor(AppDetail appDetail) {
		String queryStr = "SELECT apps._id AS _id,apps.uid AS uid,apps.package AS package,"
				+ "apps.name AS name,apps.last_called_num AS last_called_num,apps.allow AS allow,apps.exes_type AS exes_type "
				+ "FROM apps WHERE 1=1 ";

		ArrayList<String> selectionArgs = new ArrayList<String>();
		if (appDetail.getId() > 0) {
			queryStr = queryStr + " and apps._id=? ";
			selectionArgs.add(String.valueOf(appDetail.getId()));
		}
		if (appDetail.getUid() > 0) {
			queryStr = queryStr + " and apps.uid=? ";
			selectionArgs.add(String.valueOf(appDetail.getUid()));
		}
		if (appDetail.getPackageName() != null
				&& "".equals(appDetail.getPackageName())) {
			queryStr = queryStr + " and apps.package=? ";
			selectionArgs.add(String.valueOf(appDetail.getPackageName()));
		}
		queryStr = queryStr + "ORDER BY apps._id DESC ";

		Cursor cursor = this.mDB.rawQuery(queryStr, selectionArgs
				.toArray(new String[0]));

		if (cursor == null) {
			return null;
		}

		return cursor;
	}

	/**
	 * 获取“吸费的been”一条数据库对象对应项 (按程序类型来分)
	 * 
	 * @param packageName
	 * @param phoneNum
	 * @return AppDetail
	 */
	public AppDetail getAppDetail(AppDetail appDetail) {
		String queryStr = "SELECT apps._id AS _id,apps.uid AS uid,apps.package AS package,"
				+ "apps.name AS name,apps.last_called_num AS last_called_num,apps.allow AS allow,apps.exes_type AS exes_type "
				+ "FROM apps WHERE 1=1 ";

		ArrayList<String> selectionArgs = new ArrayList<String>();
		if (appDetail.getId() > 0) {
			queryStr = queryStr + " and apps._id=? ";
			selectionArgs.add(String.valueOf(appDetail.getId()));
		}
		if (appDetail.getUid() > 0) {
			queryStr = queryStr + " and apps.uid=? ";
			selectionArgs.add(String.valueOf(appDetail.getUid()));
		}
		if (appDetail.getPackageName() != null
				&& "".equals(appDetail.getPackageName())) {
			queryStr = queryStr + " and apps.package=? ";
			selectionArgs.add(String.valueOf(appDetail.getPackageName()));
		}
		queryStr = queryStr + "ORDER BY apps._id DESC ";

		Cursor cursor = this.mDB.rawQuery(queryStr, selectionArgs
				.toArray(new String[0]));

		if (cursor.moveToFirst()) {
			appDetail.setId(cursor.getInt(cursor.getColumnIndex(Apps.ID)));
			appDetail.setUid(cursor.getInt(cursor.getColumnIndex(Apps.UID)));
			appDetail
					.setAllow(cursor.getInt(cursor.getColumnIndex(Apps.ALLOW)));
			appDetail.setPackageName(cursor.getString(cursor
					.getColumnIndex(Apps.PACKAGE)));
			appDetail.setName(cursor
					.getString(cursor.getColumnIndex(Apps.NAME)));
			appDetail.setLastCalledNum(cursor.getString(cursor
					.getColumnIndex(Apps.LAST_CALLED_NUM)));
			appDetail.setExesType(cursor.getString(cursor
					.getColumnIndex(Apps.EXES_TYPE)));
		}
		cursor.close();
		return appDetail;
	}

	public int updateAppDetail(AppDetail appDetail) {
		ContentValues values = new ContentValues();
		values.put(Apps.ALLOW, appDetail.getAllow());
		values.put(Apps.EXES_TYPE, appDetail.getExesType());
		values.put(Apps.LAST_CALLED_NUM, appDetail.getLastCalledNum());
		values.put(Apps.NAME, appDetail.getName());
		values.put(Apps.PACKAGE, appDetail.getPackageName());
		values.put(Apps.UID, appDetail.getUid());

		int updateNum = this.mDB.update(APPS_TABLE, values, "_id=?",
				new String[] { Long.toString(appDetail.getId()) });
		values.clear();
		return updateNum;
	}

	public int updateAppLog(AppLog appLog) {
		ContentValues values = new ContentValues();
		values.put(Logs.ALLOW, appLog.getAllow());
		values.put(Logs.APP_ID, appLog.getAppId());
		values.put(Logs.CONTENT, appLog.getContent());
		values.put(Logs.DATE, appLog.getDate());
		values.put(Logs.PHONE_NUM, appLog.getPhoneNum());

		int updateNum = this.mDB.update(LOGS_TABLE, values, "_id=?",
				new String[] { Long.toString(appLog.getId()) });
		values.clear();
		return updateNum;
	}

	/**
	 * 关闭数据库
	 */
	@SuppressWarnings("unused")
	private void close() {
		if (this.mDB.isOpen()) {
			this.mDB.close();
		}
	}

	// ////////////////////////////// 内部类 ///////////////////////////////
	/**
	 * SQLite数据库的操作帮作类DB
	 * 
	 * @author LinLin
	 */
	private static class DBOpenHelper extends SQLiteOpenHelper {
		// 创建防火应用程序列表，SQL语句
		private static final String CREATE_APPS = "CREATE TABLE IF NOT EXISTS "
				+ APPS_TABLE
				+ " (_id INTEGER, uid INTEGER, package TEXT, name TEXT, last_called_num TEXT, "
				+ "allow INTEGER, exes_type TEXT," + " PRIMARY KEY (_id) );";

		// 创建防火应用程序日志，SQL语句
		private static final String CREATE_LOGS = "CREATE TABLE IF NOT EXISTS "
				+ LOGS_TABLE
				+ " (_id INTEGER, app_id INTEGER, date INTEGER, allow INTEGER, content TEXT, phone_num TEXT, "
				+ "PRIMARY KEY (_id));";

		@SuppressWarnings("unused")
		private Context mContext;

		DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_APPS); // 创建防火应用程序列表，SQL语句
			db.execSQL(CREATE_LOGS); // 创建防火应用程序日志，SQL语句
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 数据数升级
		}
	}

}
