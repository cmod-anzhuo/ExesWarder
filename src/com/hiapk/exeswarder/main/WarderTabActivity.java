package com.hiapk.exeswarder.main;

import com.hiapk.exeswarder.main.AppListActivity;
import com.hiapk.exeswarder.main.LogActivity;
import com.hiapk.exeswarder.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class WarderTabActivity extends TabActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "exeswarder.main.WarderTabActivity";

	private BaseContext baseContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// 初始化全局上下文环境
		baseContext = BaseContext.getInstance();
		baseContext.initBaseContext(this);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, AppListActivity.class);
		spec = tabHost.newTabSpec("apps").setIndicator(
				getString(R.string.tab_apps),
				res.getDrawable(R.drawable.ic_tab_permissions)).setContent(
				intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, LogActivity.class);
		spec = tabHost.newTabSpec("log").setIndicator(
				getString(R.string.tab_log),
				res.getDrawable(R.drawable.ic_tab_log)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ScanActivity.class);
		spec = tabHost.newTabSpec("scan").setIndicator(
				getString(R.string.tab_scan),
				res.getDrawable(R.drawable.ic_tab_permissions)).setContent(
				intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}

}
