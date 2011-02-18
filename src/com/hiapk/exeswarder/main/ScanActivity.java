package com.hiapk.exeswarder.main;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.hiapk.exeswarder.R;
import com.hiapk.exeswarder.util.Util;
import com.hiapk.exeswarder.been.MyPackageInfo;
import com.hiapk.exeswarder.log.LogUtil;
import com.hiapk.exeswarder.mark.ATaskMark;
import com.hiapk.exeswarder.mark.CheckBadAppMark;
import com.hiapk.exeswarder.service.ActionException;
import com.hiapk.exeswarder.task.IResultReceiver;
import com.hiapk.exeswarder.task.ServiceWraper;

public class ScanActivity extends Activity implements IResultReceiver {
	private static final String TAG = "exeswarder.main.ScanActivity";

	private boolean isScan = false;

	private Button scanBtn = null;
	private ProgressBar progress = null;
	private ListView list = null;

	private BaseContext baseContext = null;
	private ServiceWraper service = null;
	private BadAppListAdapter badAppListAdapter = null;
	private ScanHandler scanHandler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_scan);

		baseContext = BaseContext.getInstance();
		service = baseContext.getServiceWarper();

		// 注册控制器
		scanHandler = new ScanHandler(hashCode());
		baseContext.registerSubHandler(scanHandler);

		setupScanTitle();
		if (baseContext.getBadAppList() != null) {
			installedApkList(baseContext.getBadAppList());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// 取消处理器
		if (scanHandler != null) {
			baseContext.unregisterSubHandler(scanHandler);
		}
		super.onDestroy();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception,
			Object trackerResult) {
		baseContext.setBadAppList((ArrayList<MyPackageInfo>) trackerResult);
		installedApkList(baseContext.getBadAppList());
		isScan = !isScan;
		checkScanFlashView();
	}

	private void installedApkList(ArrayList<MyPackageInfo> packageInfoList) {
		// 绑定Layout里面的ListView
		list = (ListView) findViewById(R.id.scan_app_list);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MyPackageInfo packageInfo = baseContext.getBadAppList().get(
						position);
				if (packageInfo.getPackageInfo().packageName != null
						&& !"".equals(packageInfo.getPackageInfo().packageName)) {
					Util
							.showInstalledAppDetail(packageInfo
									.getPackageInfo().packageName);
				}
			}
		});

		badAppListAdapter = new BadAppListAdapter(this);

		// 添加并且显示
		list.setAdapter(badAppListAdapter);
	}

	private void setupScanTitle() {
		scanBtn = (Button) findViewById(R.id.scan_start_btn);
		progress = (ProgressBar) findViewById(R.id.scan_progress_bar);
		checkScanFlashView();

		scanBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				isScan = !isScan;
				checkScanFlashView();
				if (baseContext.getBadAppList() == null) {
					LogUtil.e(TAG, "baseContext.getBadAppList() is null");
				}
				baseContext.getBadAppList().clear();
				badAppListAdapter.notifyDataSetChanged();

				// 开启多线程
				ATaskMark checkBadAppMark = new CheckBadAppMark();
				service.checkBadAppList(ScanActivity.this, checkBadAppMark,
						null);
			}
		});
	}

	private void checkScanFlashView() {
		if (isScan == false) {
			scanBtn.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
		} else {
			scanBtn.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
		}
	}

	// /////// 处理器 /////////
	private class ScanHandler extends MarkableHandler {
		/**
		 * 一般
		 * 
		 * @param messageMark
		 */
		public ScanHandler(int messageMark) {
			super(messageMark);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			LogUtil.d(TAG, "handleMessage: " + msg);
			// 如果已经调用了finish了。则不处理
			if (isFinishing()) {
				return;
			}

			switch (msg.what) {
			case BaseContext.M_UNINSTALL_APK:
				doUninstallApkMessage(msg);
				break;

			default:
				break;
			}
		}

		// 卸载成功后给消息，然后刷新界面。
		private void doUninstallApkMessage(Message msg) {
			if (msg == null) {
				return;
			}
			String packageName = (String) msg.obj;
			ArrayList<MyPackageInfo> packageList = baseContext.getBadAppList();
			for (int i = 0, num = packageList.size(); i < num; i++) {
				PackageInfo packageInfo = packageList.get(i).getPackageInfo();
				if (packageName.equals(packageInfo.packageName)) {
					packageList.remove(i);
					break;
				}
			}

			badAppListAdapter.notifyDataSetChanged();
		}

	}

	// 适配器
	private final class BadAppListAdapter extends BaseAdapter implements
			ListAdapter {
		@SuppressWarnings("unused")
		private Context context;

		BadAppListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return baseContext.getBadAppList().size();
		}

		@Override
		public Object getItem(int position) {
			return baseContext.getBadAppList().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater mInflater = getLayoutInflater(); // 得到一个布局控制器
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.scan_list_item, null);
			}

			MyPackageInfo pi = baseContext.getBadAppList().get(position);
			String name = Util.getAppNameFromPackage(baseContext
					.getAppContext(), pi.getPackageInfo().packageName);
			String packageName = pi.getPackageInfo().packageName;

			ImageView appIconView = (ImageView) convertView
					.findViewById(R.id.ItemImage);
			try {
				Drawable icon = Util.getAppIcon(baseContext.getAppContext(),
						packageName);
				appIconView.setImageDrawable(icon);
			} catch (Exception e) {
				appIconView.setImageResource(R.drawable.sym_def_app_icon);
			}

			TextView itemAlarm = (TextView) convertView
					.findViewById(R.id.ItemAlarm);
			if (pi.getBadCode() != null) {
				itemAlarm.setTextColor(Color.RED);
				itemAlarm.setText(R.string.scan_danger_alarm);
			} else if (pi.getFilterPermission() != null) {
				itemAlarm.setTextColor(Color.YELLOW);
				itemAlarm.setText(R.string.scan_shadiness_alarm);

			}

			TextView appNameView = (TextView) convertView
					.findViewById(R.id.ItemTitle);
			appNameView.setText(name);

			TextView packageNameView = (TextView) convertView
					.findViewById(R.id.ItemText);
			packageNameView.setText(packageName);

			return convertView;
		}
	}

}
