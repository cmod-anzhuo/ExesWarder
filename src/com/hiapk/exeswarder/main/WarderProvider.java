package com.hiapk.exeswarder.main;

import com.hiapk.exeswarder.bd.DBHelper;
import com.hiapk.exeswarder.been.AppDetail;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class WarderProvider extends ContentProvider {
	// 通过UriMatcher匹配外部请求
	private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	// 通过openHelper进行数据库读写
	private DBHelper dbHelper = null;

	// 匹配状态常量
	private static final int CONTACT_LIST = 1;
	private static final int CONTACT = 2;

	// 添加Uri
	static {
		uriMatcher.addURI("com.hiapk.exeswarder.sqlite.provider", "contact",
				CONTACT_LIST/* 匹配码 */);// 注册URI
		uriMatcher.addURI("com.hiapk.exeswarder.sqlite.provider", "contact/#",
				CONTACT/* 匹配码 */);// 注册URI
	}

	@Override
	public boolean onCreate() {
		dbHelper = DBHelper.getInstance(getContext());
		return true;
	}

	/**
	 * 只提供查询的接口,只能查列表String[] projection, String selection, String[]
	 * selectionArgs, String sortOrder参数都无效 单个只能用"pid"即“uid”来查
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (uriMatcher.match(uri)) {
		case CONTACT_LIST:
			return dbHelper.getAllAppsForCursor();
		case CONTACT:
			long id = ContentUris.parseId(uri);

			AppDetail appDetail = new AppDetail();
			appDetail.setUid((int) id);

			return dbHelper.getAppDetailForCursor(appDetail);

		default:
			throw new IllegalArgumentException("Uri IllegalArgument:" + uri);

		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CONTACT_LIST:// 集合类型必须在前面加上vnd.android.cursor.dir/
			return "vnd.android.cursor.dir/contactlist";
		case CONTACT:// 非集合类型必须在前面加上vnd.android.cursor.item/
			return "vnd.android.cursor.item/contact";
		default:
			throw new IllegalArgumentException("Uri IllegalArgument:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
